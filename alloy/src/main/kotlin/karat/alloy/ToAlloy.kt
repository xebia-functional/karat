package karat.alloy

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.*
import edu.mit.csail.sdg.ast.Expr as AlloyExpr
import karat.symbolic.*
import karat.symbolic.Expr as KaratExpr
import karat.symbolic.Formula as KaratFormula
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

public class AlloyBuilder {

  // this is the actual public API

  public val sigs: MutableList<Sig> = mutableListOf()
  public val facts: MutableList<AlloyExpr> = mutableListOf()

  public fun fact(vararg formula: KaratFormula) {
    facts.addAll(formula.map { it.translate() })
  }

  // rest is implementation details

  private val unique: AtomicLong = AtomicLong(0L)
  private fun nextUnique(prefix: String): String {
    val i = unique.incrementAndGet()
    return "__${prefix}__$i"
  }
  private fun nextUnique(e: AlloyExpr) = when (e) {
    is Sig -> nextUnique(e.label)
    else -> nextUnique("x")
  }

  private val cache: ModuleCache = ModuleCache()

  // translation to Alloy

  internal fun KaratFormula.translate(): AlloyExpr = when (this) {
    is TRUE -> ExprConstant.TRUE
    is FALSE -> ExprConstant.FALSE
    is Not -> formula.translate().not()
    is And -> ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.AND, formulae.map { it.translate() })
    is Or -> ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.OR, formulae.map { it.translate() })
    is Implies -> condition.translate().implies(then.translate())
    is Iff -> condition.translate().iff(then.translate())
    is IfThenElse -> ExprITE.make(Pos.UNKNOWN, condition.translate(), then.translate(), orElse.translate())
    is Quantified<*> -> {
      val set = over.translate()
      val arg = Decl(
        null, null, null, null,
        listOf(ExprVar.make(Pos.UNKNOWN, nextUnique(set), set.type())), set
      )
      quantifier.translate().make(
        null, null, listOf(arg), formula(Argument(arg)).translate()
      )
    }
    is Always -> formula.translate().always()
    is Eventually -> formula.translate().eventually()
    is Historically -> formula.translate().historically()
    is After -> formula.translate().after()
    is Before -> formula.translate().before()
    is Once -> formula.translate().once()
    is Until -> condition.translate().until(then.translate())
    is Releases -> condition.translate().releases(then.translate())
    is Since -> condition.translate().since(then.translate())
    is Triggered -> condition.translate().triggered(then.translate())
    is Equals<*> -> x.translate().equal(y.translate())
    is ListIsEmpty<*> -> cache["util/sequniv"]!!["isEmpty"]!!.call(x.translate())
    is NumberComparison -> when (r) {
      NumberRelation.GT -> x.translate().gt(y.translate())
      NumberRelation.GTE -> x.translate().gte(y.translate())
      NumberRelation.LT -> x.translate().lt(y.translate())
      NumberRelation.LTE -> x.translate().lte(y.translate())
    }
  }

  internal fun Quantifier.translate(): ExprQt.Op = when (this) {
    Quantifier.ALL -> ExprQt.Op.ALL
    Quantifier.NO -> ExprQt.Op.NO
    Quantifier.OPTIONAL -> ExprQt.Op.LONE
    Quantifier.SINGLE -> ExprQt.Op.ONE
    Quantifier.EXISTS -> ExprQt.Op.SOME
  }

  internal fun <A> KaratExpr<A>.translate(): AlloyExpr = when (this) {
    is Flatten<*, A> -> x.translate()
    is TypeSet<A> -> set(type) ?: throw IllegalArgumentException("cannot find $type")
    is FieldRelation<*, *> -> field(type, property)
    is GlobalField<A> -> global(property)
    is Argument<*, *> -> (decl as Decl).get()
    is SuchThat<*> -> {
      val set = over.translate()
      val arg = Decl(
        null, null, null, null,
        listOf(ExprVar.make(Pos.UNKNOWN, nextUnique(set), set.type())), set
      )
      ExprQt.Op.COMPREHENSION.make(
        null, null, listOf(arg), formula(Argument(arg)).translate()
      )
    }
    is Next<A> -> x.translate().prime()
    is Cardinality<*> -> x.translate().cardinality()
    is Union<A> -> x.translate().plus(y.translate())
    is Override<A> -> x.translate().override(y.translate())
    is Minus<A> -> x.translate().minus(y.translate())
    is Intersect<A> -> x.translate().intersect(y.translate())
    is Product<*, *> -> x.translate().product(y.translate())
    is Transpose<*, *> -> r.translate().transpose()
    is Closure<*> -> r.translate().closure()
    is ReflexiveClosure<*> -> r.translate().reflexiveClosure()
    is Domain<A> -> r.translate().join(Sig.PrimSig.UNIV)
    is Range<A> -> Sig.PrimSig.UNIV.join(r.translate())
    is JoinRelRel<*, *, *> -> r1.translate().join(r1.translate())
    is JoinSetRel<*, *> -> s.translate().join(r.translate())
    is JoinRelSet<*, *> -> r.translate().join(s.translate())
    is ListFirst<A> -> cache["util/sequniv"]!!["first"]!!.call(x.translate())
    is ListAdd<*> -> cache["util/sequniv"]!!["add"]!!.call(elt.translate(), lst.translate())
    is ListRest<*> -> cache["util/sequniv"]!!["rest"]!!.call(x.translate())
    is ListElements<*> -> cache["util/sequniv"]!!["elemts"]!!.call(x.translate())
    is NumberLiteral -> ExprConstant.Op.NUMBER.make(Pos.UNKNOWN, n)
  }

  // things that deal with signatures and reflection

  private data class ReflectedSig(
    val sig: Sig, val fields: MutableMap<KProperty1<*, *>, Sig.Field> = mutableMapOf()
  )
  private val reflectedSigs: MutableMap<ReflectedType, ReflectedSig> = mutableMapOf()
  private val reflectedGlobals: MutableMap<Method, Sig> = mutableMapOf()

  private fun set(type: KType?): AlloyExpr? {
    val c = type?.klass
    return when {
      c == null -> null
      c.isSubclassOf(List::class) ->
        Sig.PrimSig.SEQIDX.isSeq_arrow_lone(set(type.arguments.first().type!!))
      c.isSubclassOf(Int::class) -> Sig.PrimSig.SIGINT
      c.isSubclassOf(String::class) -> Sig.PrimSig.SIGINT
      else -> reflectedSigs[type.reflected]?.sig
    }
  }

  private fun <A, F> field(type: KType, property: KProperty1<A, F>): Sig.Field =
    reflectedSigs[type.reflected]!!.fields[property]!!

  private fun <F> global(property: KProperty0<F>): Sig =
    reflectedGlobals[property.javaGetter]!!
}
