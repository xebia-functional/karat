package fp.serrano.karat

import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.Sig
import edu.mit.csail.sdg.ast.Sig.Field
import edu.mit.csail.sdg.ast.Sig.PrimSig

fun sigs(vararg sig: KPrimSig): List<Sig> =
  sig.map { it.primSig }

open class KPrimSig(val primSig: PrimSig): KSetOrRelation(primSig) {
  constructor(name: String, vararg attributes: Attr)
          : this(PrimSig(name, *attributes))
  constructor(name: String, extends: KPrimSig, vararg attributes: Attr)
          : this(PrimSig(name, extends.primSig, *attributes))
}

class KField(val field: Field): KExpr(field)

fun KPrimSig.field(name: String, type: KMultiplicityConstraint): KField =
  KField(primSig.addField(name, type.expr))