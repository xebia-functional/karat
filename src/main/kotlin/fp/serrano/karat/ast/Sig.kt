package fp.serrano.karat.ast

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.ExprConstant
import edu.mit.csail.sdg.ast.ExprHasName
import edu.mit.csail.sdg.ast.Sig.Field
import edu.mit.csail.sdg.ast.Sig.PrimSig
import kotlin.reflect.KClass

class Sigs {
  companion object {
    fun <A> UNIV(): KSig<A> = KSig(PrimSig.UNIV)
    fun <A> NONE(): KSig<A> = KSig(PrimSig.NONE)
    fun <A> IDEN(): KRelation<A, A> = KRelation(ExprConstant.IDEN)
    val SIGINT: KSig<Int> = KSig(PrimSig.SIGINT)
    val SEQIDX: KSig<Int> = KSig(PrimSig.SEQIDX)
    val STRING: KSig<Int> = KSig(PrimSig.STRING)
  }
}

open class KSig<out A>(val primSig: PrimSig): KSet<A>(primSig) {
  constructor(name: String, vararg attributes: Attr)
          : this(PrimSig(name, *attributes))
  constructor(name: String, extends: KSig<*>, vararg attributes: Attr)
          : this(PrimSig(name, extends.primSig, *attributes))

  companion object {
    operator fun <A: Any> invoke(klass: KClass<A>, name: String, vararg attributes: Attr): KSig<A> =
      KSig(name, *attributes)
    operator fun <A: Any> invoke(klass: KClass<A>, name: String, extends: KSig<*>, vararg attributes: Attr): KSig<A> =
      KSig(name, extends, *attributes)
  }
}

class KField<A, F>(field: Field): KRelation<A, F>(field)

fun <A, F> KSig<A>.field(
  name: String, type: KSet<F>
): KField<A, F> = KField(primSig.addField(name, type.expr))

fun <A, F> KSig<A>.variable(
  name: String, type: KSet<F>
): KField<A, F> =
  KField(primSig.addTrickyField(
    null, null, null, null, null,
    Pos.UNKNOWN, arrayOf(name), type.expr
  ).first())

class KThis<A>(expr: ExprHasName): KSet<A>(expr), KHasName {
  override val label: String = expr.label
}

fun <A> KSig<A>.self(): KThis<A> =
  KThis(primSig.decl.get())

fun <A> KSig<A>.fact(
  formula: (KThis<A>) -> KFormula
): Unit = primSig.addFact(formula(self()).expr)