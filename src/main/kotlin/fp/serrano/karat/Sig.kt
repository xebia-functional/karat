package fp.serrano.karat

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.ExprConstant
import edu.mit.csail.sdg.ast.ExprHasName
import edu.mit.csail.sdg.ast.Sig.Field
import edu.mit.csail.sdg.ast.Sig.PrimSig

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

open class KSig<A>(val primSig: PrimSig): KSet<A>(primSig) {
  constructor(name: String, vararg attributes: Attr)
          : this(PrimSig(name, *attributes))
  constructor(name: String, extends: KSig<*>, vararg attributes: Attr)
          : this(PrimSig(name, extends.primSig, *attributes))
}

class KField<A, F>(val field: Field): KRelation<A, F>(field)

fun <A: KSig<A>, F> KSig<A>.field(
  name: String, type: KSet<F>
): KField<A, F> = KField(primSig.addField(name, type.expr))

fun <A: KSig<A>, F> KSig<A>.variable(
  name: String, type: KSet<F>
): KField<A, F> =
  KField(primSig.addTrickyField(
    null, null, null, null, null,
    Pos.UNKNOWN, arrayOf(name), type.expr
  ).first())

class KThis<A>(expr: ExprHasName): KSet<A>(expr), KHasName {
  override val label: String = expr.label
}

fun <A: KSig<A>> KSig<A>.fact(
  formula: (KThis<A>) -> KFormula
): Unit = primSig.addFact(formula(KThis(primSig.decl.get())).expr)