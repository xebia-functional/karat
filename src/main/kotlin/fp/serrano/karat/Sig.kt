package fp.serrano.karat

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.ExprConstant
import edu.mit.csail.sdg.ast.Sig
import edu.mit.csail.sdg.ast.Sig.Field
import edu.mit.csail.sdg.ast.Sig.PrimSig

class Sigs {
  companion object {
    fun <A> UNIV(): KPrimSig<TSet<A>> = KPrimSig(PrimSig.UNIV)
    fun <A> NONE(): KPrimSig<TSet<A>> = KPrimSig(PrimSig.NONE)
    fun <A> IDEN(): Relation<TSet<A>, TSet<A>> = Relation(ExprConstant.IDEN)
    val SIGINT: KPrimSig<Int> = KPrimSig(PrimSig.SIGINT)
    val SEQIDX: KPrimSig<Int> = KPrimSig(PrimSig.SEQIDX)
    val STRING: KPrimSig<Int> = KPrimSig(PrimSig.STRING)
  }
}

open class KPrimSig<A>(val primSig: PrimSig): Set<TSet<A>>(primSig) {
  constructor(name: String, vararg attributes: Attr)
          : this(PrimSig(name, *attributes))
  constructor(name: String, extends: KPrimSig<*>, vararg attributes: Attr)
          : this(PrimSig(name, extends.primSig, *attributes))
}



class KField<A, M: TMultiplicity>(val field: Field): Relation<TSet<A>, M>(field)

fun <A: KPrimSig<A>, M: TMultiplicity> KPrimSig<A>.field(
  name: String, type: KSetOrRelation<M>
): KField<A, M> =
  KField(primSig.addField(name, type.expr))

fun <A: KPrimSig<A>, M: TMultiplicity> KPrimSig<A>.variable(
  name: String, type: KSetOrRelation<M>
): KField<A, M> =
  KField(primSig.addTrickyField(
    null, null, null, null, null,
    Pos.UNKNOWN, arrayOf(name), type.expr
  ).first())

fun <A: KPrimSig<A>> KPrimSig<A>.fact(
  formula: (KExprWithName<TSet<A>>) -> KFormula
): Unit =
  primSig.addFact(formula(KExprWithName(primSig.decl.get())).expr)