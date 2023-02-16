package karat.alloy

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.*
import edu.mit.csail.sdg.ast.Expr as AlloyExpr
import karat.symbolic.*
import karat.symbolic.Expr as KaratExpr
import karat.symbolic.Formula as KaratFormula
import karat.symbolic.TypeSet as KaratSet
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import javax.validation.constraints.NotEmpty
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaGetter

public open class AlloyBuilder {

  // this is the actual public API

  public val sigs: MutableList<Sig> = mutableListOf()
  public val facts: MutableList<AlloyExpr> = mutableListOf()

  public fun fact(vararg formula: KaratFormula) {
    facts.addAll(formula.map { it.translate() })
  }

  public fun stateMachine(block: TemporalFormulaBuilder.() -> Unit) {
    fact(TemporalFormulaBuilder().also(block).build())
  }

  public inline fun <reified T> stateMachine() {
    reflectMachineFromClass(T::class.simpleName!!, typeOf<T>())
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

  public fun KaratFormula.translate(): AlloyExpr = when (this) {
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
      when (val inner = formula(Argument(arg))) {
        TRUE -> quantifier.translateEmpty().invoke(set)
        else -> quantifier.translate().make(
          null, null, listOf(arg), inner.translate()
        )
      }
    }
    is Always -> formula.translate().always()
    is Eventually -> formula.translate().eventually()
    // historically and after were removed in 6.1.0
    is Historically -> ExprUnary.Op.HISTORICALLY.make(Pos.UNKNOWN, formula.translate())
    is After -> ExprUnary.Op.AFTER.make(Pos.UNKNOWN, formula.translate())
    is Before -> formula.translate().before()
    is Once -> formula.translate().once()
    is Until -> condition.translate().until(then.translate())
    is Releases -> condition.translate().releases(then.translate())
    is Since -> condition.translate().since(then.translate())
    is Triggered -> condition.translate().triggered(then.translate())
    is Equals<*> -> x.translate().equal(y.translate())
    is In<*> -> x.translate().`in`(y.translate())
    is ListIsEmpty<*> -> cache["util/sequniv"]!!["isEmpty"]!!.call(x.translate())
    is NumberComparison -> when (r) {
      NumberRelation.GT -> x.translate().gt(y.translate())
      NumberRelation.GTE -> x.translate().gte(y.translate())
      NumberRelation.LT -> x.translate().lt(y.translate())
      NumberRelation.LTE -> x.translate().lte(y.translate())
    }
  }

  private fun Quantifier.translate(): ExprQt.Op = when (this) {
    Quantifier.ALL -> ExprQt.Op.ALL
    Quantifier.NO -> ExprQt.Op.NO
    Quantifier.OPTIONAL -> ExprQt.Op.LONE
    Quantifier.SINGLE -> ExprQt.Op.ONE
    Quantifier.EXISTS -> ExprQt.Op.SOME
  }

  private fun Quantifier.translateEmpty(): (AlloyExpr) -> AlloyExpr = when (this) {
    Quantifier.EXISTS -> { x -> x.some() }
    Quantifier.NO -> { x -> x.no() }
    Quantifier.OPTIONAL -> { x -> x.lone() }
    Quantifier.SINGLE -> { x -> x.one() }
    Quantifier.ALL -> { _ -> ExprConstant.TRUE }
  }

  public fun <A> KaratExpr<A>.translate(): AlloyExpr = when (this) {
    is ImplDefinedFormula<*, A> -> requireNotNull(this.formula as? AlloyExpr)
    is Flatten<*, A> -> x.translate()
    is KaratSet<A> -> set(type) ?: throw IllegalArgumentException("cannot find $type")
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
    is NumberArithmetic -> when (op) {
      NumberOperation.PLUS -> x.translate().iplus(y.translate())
      NumberOperation.MINUS -> x.translate().iminus(y.translate())
    }
  }

  // things that deal with signatures and reflection

  private data class ReflectedSig(
    val sig: Sig, val fields: MutableMap<KProperty1<*, *>, Sig.Field> = mutableMapOf()
  )
  private val reflectedSigs: MutableMap<ReflectedType, ReflectedSig> = mutableMapOf()
  private val reflectedGlobals: MutableMap<Method, Sig> = mutableMapOf()

  internal fun set(type: KType?): Sig? {
    val c = type?.klass
    return when {
      c == null -> null
      c.isSubclassOf(Int::class) -> Sig.PrimSig.SIGINT
      c.isSubclassOf(String::class) -> Sig.PrimSig.SIGINT
      type.reflected in reflectedSigs -> reflectedSigs[type.reflected]!!.sig
      else -> {
        reflect(type.reflectedClosure(doNotVisit = reflectedSigs.keys))
        // at this point the type should have been added
        reflectedSigs[type.reflected]!!.sig
      }
    }
  }

  // call only during creation!!
  private fun setUnsafe(type: KType?): Sig? {
    val c = type?.klass
    return when {
      c == null -> null
      c.isSubclassOf(Int::class) -> Sig.PrimSig.SIGINT
      c.isSubclassOf(String::class) -> Sig.PrimSig.SIGINT
      else -> reflectedSigs[type.reflected]!!.sig
    }
  }

  private fun <A, F> field(type: KType, property: KProperty1<A, F>): Sig.Field {
    if (type.reflected !in reflectedSigs)
      reflect(type.reflectedClosure(doNotVisit = reflectedSigs.keys))
    return reflectedSigs[type.reflected]!!.fields[property]!!
  }

  private fun <F> global(property: KProperty0<F>): Sig {
    if (property.javaGetter !in reflectedGlobals)
      reflectProperty(property)
    return reflectedGlobals[property.javaGetter]!!
  }

  private fun reflect(types: Iterable<KType>) {
    types.forEach { reflectClassAsSig(it) }
    types.forEach { reflectClassFields(it) }
    types.forEach { reflectClassCompanionFields(it) }
    types.forEach { reflectClassCompanionFacts(it) }
  }

  // step 1, generate all signatures
  private fun reflectClassAsSig(type: KType) {
    // 0. checks:
    //    - we have a class
    val klass = checkNotNull(type.klass)
    //    - all the arguments are classes themselves (no weird variances)
    type.arguments.forEach {
      checkNotNull(it.type?.klass)
      check(it.variance == null || it.variance == KVariance.INVARIANT)
    }
    // a. create name by smashing together the names
    val name = when (type.arguments.size) {
      0 -> klass.simpleName!!
      else -> "${klass.simpleName!!}<${type.arguments.joinToString(separator = ",") { it.type?.klass?.simpleName!! }}>"
    }
    // b. find the attributes
    val attribs = listOfNotNull(
      (Attr.ABSTRACT)?.takeIf { klass.hasAnnotation<abstract>() },
      (Attr.ONE)?.takeIf { klass.objectInstance != null || klass.hasAnnotation<one>() },
      (Attr.VARIABLE)?.takeIf { klass.hasAnnotation<variable>() }
    )
    val isSubset = klass.hasAnnotation<subset>()
    // c. find any possible super-class
    //    rule for now: superclasses must be reflected before
    val superSig =
      klass.supertypes
        .map { it.substitute(type) }
        .firstNotNullOfOrNull { reflectedSigs[it.reflected]?.sig }
    // d. generate the thing
    // at this point we don't care about the generic argument, so we use Any?
    val newSig: Sig = when {
      isSubset -> Sig.SubsetSig(Pos.UNKNOWN, name, listOf(Pos.UNKNOWN), listOf(superSig), *attribs.toTypedArray())
      superSig == null -> Sig.PrimSig(name, *attribs.toTypedArray())
      superSig is Sig.PrimSig -> Sig.PrimSig(Pos.UNKNOWN, name, Pos.UNKNOWN, superSig, *attribs.toTypedArray())
      else -> throw IllegalArgumentException("non-subset signatures must extend a non-subset one")
    }
    // e. record it
    reflectedSigs[type.reflected] = ReflectedSig(newSig)
    sigs.add(newSig)
  }

  // step 2, generate all fields
  private fun reflectClassFields(type: KType) {
    val k = reflectedSigs[type.reflected]!!  // should never fail, we've just added it
    val klass = type.klass!!
    klass.declaredMemberProperties
      // a. only the reflected ones
      .filter {
        it.hasAnnotation<reflect>()
      }
      .forEach { property ->
        // b. figure out the type
        val ret = property.returnType.substitute(type)
        val sigTy: AlloyExpr? = when {
          ret.isMarkedNullable ->
            setUnsafe(ret)?.loneOf()
          ret.klass?.isSubclassOf(Set::class) == true ->
            setUnsafe(ret.arguments.firstOrNull()?.type)?.let {
              when {
                property.hasAnnotation<NotEmpty>() -> it.someOf()
                else -> it.setOf()
              }
            }
          ret.klass?.isSubclassOf(List::class) == true ->
            setUnsafe(ret.arguments.firstOrNull()?.type)?.let {
              Sig.PrimSig.SEQIDX.isSeq_arrow_lone(it)
            }
          ret.klass?.isSubclassOf(Map::class) == true -> {
            setUnsafe(ret.arguments[0].type)?.let { k ->
              val v = ret.arguments[1].type
              when {
                v?.isMarkedNullable == true ->
                  setUnsafe(v)?.let { k.any_arrow_lone(it) }
                v?.klass?.isSubclassOf(Set::class) == true ->
                  setUnsafe(v.arguments.firstOrNull()?.type)?.let { k.product(it)}
                else ->
                  setUnsafe(v)?.let { k.any_arrow_one(it) }
              }
            }
          }
          else ->
            setUnsafe(ret)
        }
        when (sigTy) {
          null -> throw IllegalArgumentException("cannot reflect type $ret of $type")
          else -> {
            val newProp =
              if (property is KMutableProperty<*> || property.hasAnnotation<variable>())
                k.sig.addTrickyField(
                  Pos.UNKNOWN, null, null, null, null,
                  Pos.UNKNOWN, listOf(ExprVar.make(null, property.name)), sigTy
                ).first()
              else
                k.sig.addField(property.name, sigTy)
            k.fields[property] = newProp
          }
        }
      }
  }

  // step 3, add global fields
  private fun reflectClassCompanionFields(type: KType) {
    val companionKlass = type.klass!!.companionObject
    val companionValue = type.klass!!.companionObjectInstance
    if (companionKlass != null && companionValue != null) {
      companionKlass.declaredMemberProperties.filter {
        it.hasAnnotation<reflect>()
      }.forEach { reflectProperty(it) }
    }
  }

  private fun reflectProperty(property: KProperty<*>) {
    val ret = property.returnType
    val sigTy: Pair<List<Attr>, AlloyExpr>? = when {
      property.returnType.isMarkedNullable ->
        setUnsafe(ret)?.let { listOf(Attr.LONE) to it }

      ret.klass?.isSubclassOf(Set::class) == true ->
        setUnsafe(ret.arguments.firstOrNull()?.type)?.let {
          when {
            property.hasAnnotation<NotEmpty>() -> listOf(Attr.SOME) to it
            else -> emptyList<Attr>() to it
          }
        }

      ret.klass?.isSubclassOf(List::class) == true ->
        throw IllegalArgumentException("List cannot be used within a property")

      else ->
        setUnsafe(ret)?.let { listOf(Attr.ONE) to it }
    }
    when (sigTy) {
      null ->
        throw IllegalArgumentException("cannot reflect type $ret")
      else -> {
        val parentSigs = listOfNotNull(sigTy.second as? Sig)
        val newProp: Sig.SubsetSig =
          if (property is KMutableProperty<*> || property.hasAnnotation<variable>())
            Sig.SubsetSig(Pos.UNKNOWN, property.name, parentSigs.map { Pos.UNKNOWN }, parentSigs, *(listOf(Attr.VARIABLE) + sigTy.first).toTypedArray())
          else
            Sig.SubsetSig(Pos.UNKNOWN, property.name, parentSigs.map { Pos.UNKNOWN }, parentSigs, *sigTy.first.toTypedArray())

        reflectedGlobals[property.javaGetter!!] = newProp
        sigs.add(newProp)
      }
    }
  }
  
  // step 4, add facts
  private fun reflectClassCompanionFacts(type: KType) {
    val k = reflectedSigs[type.reflected]!!  // should never fail, we've just added it
    val companionKlass = type.klass!!.companionObject
    val companionValue = type.klass!!.companionObjectInstance
    if (companionKlass != null && companionValue != null) {
      companionKlass.declaredMemberExtensionFunctions
        .forEach {
          // we can have a ReflectedModule as argument
          val ext = it.extensionReceiverParameter
          when {
            ext == null -> { }
            ext.type.klass?.isSubclassOf(InstanceFact::class) == true ->
              when (val newFact = it.call(companionValue, instanceFactBuilder(k.sig))) {
                null -> { /* do nothing */ }
                is KaratFormula -> k.sig.addFact(newFact.translate())
                else -> throw IllegalArgumentException("instance fact returns type ${newFact::class.simpleName}")
              }
            ext.type.klass?.isSubclassOf(Fact::class) == true ->
              when (val newFact = it.call(companionValue, object : Fact { })) {
                null -> { /* do nothing */ }
                is KaratFormula -> fact(newFact)
                else -> throw IllegalArgumentException("fact returns type ${newFact::class.simpleName}")
              }
            else -> { }
          }
        }
    }
  }

  private fun instanceFactBuilder(sig: Sig): InstanceFact<Any?> =
    object : InstanceFact<Any?> {
      override val self: KaratExpr<Any?> = Argument(sig.decl)
    }

  @PublishedApi
  internal fun reflectMachineFromClass(
    transitionSigName: String,
    oneType: KType,
    vararg moreTypes: KType,
    transitionVarName: String = "Transition",
    initialName: String = "Init",
    stutterName: String = "Stutter",
  ): Unit = stateMachine {
    val types = listOf(oneType) + moreTypes

    types.forEach { ty ->
      val klass = requireNotNull(ty.klass) {
        "only bare interfaces can be turned into state machines"
      }
      require(klass.java.isInterface && klass.isSealed && klass.declaredMembers.isEmpty()) {
        "only empty sealed interfaces can be turned into state machines"
      }
      require(klass.typeParameters.isEmpty()) {
        "only interfaces without parameters can be turned into state machines"
      }
    }

    // 1. find the different steps
    val klasses = types.map { it.klass!! }
    val initials = klasses.mapNotNull { klass ->
      klass.sealedSubclasses.firstOrNull { it.hasAnnotation<initial>() }?.let { klass to it }
    }.toMap()
    val stutters = klasses.mapNotNull { klass ->
      klass.sealedSubclasses.firstOrNull { it.hasAnnotation<stutter>() }?.let { klass to it }
    }.toMap()
    val stutterFors = klasses.flatMap { klass ->
      klass.sealedSubclasses.mapNotNull { stutterKlass ->
        stutterKlass.findAnnotation<stutterFor>()?.klass?.let { forKlass ->
          (klass to forKlass) to stutterKlass
        }
      }
    }.toMap()
    val actualTransitions = klasses.associateWith { klass ->
      klass.sealedSubclasses.filter {
        !it.hasAnnotation<initial>() && !it.hasAnnotation<stutter>() && !it.hasAnnotation<stutterFor>()
      }
    }

    // 2. declare the top of the hierarchy
    val newSig = Sig.PrimSig(transitionSigName, Attr.ABSTRACT)
    sigs.add(newSig)

    // 3. declare a single element to hold the current transition
    val stateSig = Sig.SubsetSig(Pos.UNKNOWN, transitionVarName, listOf(Pos.UNKNOWN), listOf(newSig), Attr.ONE, Attr.VARIABLE)
    sigs.add(stateSig)

    val currentStateRef = current<Any?>(ImplDefinedFormula(stateSig))
    val nextStateRef = next<Any?>(ImplDefinedFormula(stateSig))

    // 4. declare the initial transition
    val initialSig = Sig.PrimSig(Pos.UNKNOWN, initialName, Pos.UNKNOWN, newSig, Attr.ONE)
    sigs.add(initialSig)
    initial {
      val t = and(
        initials.map { (_, v) ->
          formulaFromObject("initial", v)
        } + (currentStateRef `==` ImplDefinedFormula(initialSig)),
      )
      t
    }

    // 5. declare the stutter transition
    val stutterSig = Sig.PrimSig(Pos.UNKNOWN, stutterName, Pos.UNKNOWN, newSig, Attr.ONE)
    sigs.add(stutterSig)
    transition {
      val t = and(
        stutters.map { (_, v) ->
          formulaFromObject("stutter", v)
        } + (nextStateRef `==` ImplDefinedFormula(stutterSig))
      )
      t
    }

    // 6. declare each of the others
    actualTransitions.forEach { (klass, subclasses) ->
      subclasses.forEach { transitionKlass ->
        val actionSigName = when (val enclosing = transitionKlass.java.enclosingClass) {
          null -> transitionKlass.simpleName!!
          else -> "${enclosing.simpleName}>${transitionKlass.simpleName!!}"
        }
        val companion = requireNotNull(transitionKlass.companionObjectInstance as? Transition) {
          "transition classes must have a companion object implementing Transition"
        }
        val companionKlass = requireNotNull(transitionKlass.companionObject)
        val transitionArguments = companionKlass.supertypes.find {
          it.klass!!.isSubclassOf(Transition::class)
        }!!.arguments.map { it.type!! }
        val transitionSig: Sig.PrimSig =
          if (companion is Transition0)
            Sig.PrimSig(Pos.UNKNOWN, actionSigName, Pos.UNKNOWN, newSig, Attr.ONE)
          else
            Sig.PrimSig(Pos.UNKNOWN, actionSigName, Pos.UNKNOWN, newSig)
        sigs.add(transitionSig)
        reflectedSigs[companionKlass.starProjectedType.reflected] = ReflectedSig(transitionSig)
        val transitionProps = transitionArguments.mapIndexed { i, ty ->
          transitionSig.addField("arg$i", set(ty))
        }

        val thisOne: KaratFormula = when (companion) {
          is Transition0 -> companion.execute()
          is Transition1<*> -> KaratSet<Nothing>(transitionArguments[0]).any {
            and(
              companion.execute(it),
              ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[0])) `==` it
            )
          }
          is Transition2<*, *> -> KaratSet<Nothing>(transitionArguments[0]).any { a ->
            KaratSet<Nothing>(transitionArguments[1]).any { b ->
              and(
                companion.execute(a, b),
                ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[0])) `==` a,
                ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[1])) `==` b
              )
            }
          }
          is Transition3<*, *, *> -> KaratSet<Nothing>(transitionArguments[0]).any { a ->
            KaratSet<Nothing>(transitionArguments[1]).any { b ->
              KaratSet<Nothing>(transitionArguments[2]).any { c ->
                and(
                  companion.execute(a, b, c),
                  ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[0])) `==` a,
                  ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[1])) `==` b,
                  ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[2])) `==` c
                )
              }
            }
          }
          is Transition4<*, *, *, *> -> KaratSet<Nothing>(transitionArguments[0]).any { a ->
            KaratSet<Nothing>(transitionArguments[1]).any { b ->
              KaratSet<Nothing>(transitionArguments[2]).any { c ->
                KaratSet<Nothing>(transitionArguments[3]).any { d ->
                  and(
                    companion.execute(a, b, c, d),
                    ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[0])) `==` a,
                    ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[1])) `==` b,
                    ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[2])) `==` c,
                    ImplDefinedFormula<_, Any?>(stateSig.prime().join(transitionProps[3])) `==` d
                  )
                }
              }
            }
          }
        }

        val stutterFormulae = klasses.filter { it != klass }.mapNotNull { other ->
          when {
            (other to klass) in stutterFors ->
              formulaFromObject("stutter", stutterFors[other to klass]!!)
            other in stutters ->
              formulaFromObject("stutter", stutters[other]!!)
            else ->
              null
          }
        }

        transition {
          val t = and(
            stutterFormulae + thisOne + (nextStateRef `in` KaratSet(companionKlass.starProjectedType))
          )
          t
        }
      }
    }
  }

  private fun formulaFromObject(element: String, klass: KClass<*>): KaratFormula =
    requireNotNull(klass.companionObjectInstance as? Transition0) { "$element must be declared as object" }.execute()

}
