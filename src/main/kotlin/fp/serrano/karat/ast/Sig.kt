package fp.serrano.karat.ast

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.ExprConstant
import edu.mit.csail.sdg.ast.ExprHasName
import edu.mit.csail.sdg.ast.Sig
import edu.mit.csail.sdg.ast.Sig.Field
import edu.mit.csail.sdg.ast.Sig.PrimSig
import edu.mit.csail.sdg.ast.Sig.SubsetSig
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

open class KSig<out A>(val sig: Sig): KSet<A>(sig)

open class KPrimSig<out A>(val primSig: PrimSig): KSig<A>(primSig) {
  constructor(name: String, vararg attributes: Attr)
          : this(PrimSig(name, *attributes))
  constructor(name: String, extends: KPrimSig<*>, vararg attributes: Attr)
          : this(PrimSig(name, extends.primSig, *attributes))

  companion object {
    operator fun <A: Any> invoke(klass: KClass<A>, name: String, vararg attributes: Attr): KPrimSig<A> =
      KPrimSig(name, *attributes)
    operator fun <A: Any> invoke(klass: KClass<A>, name: String, extends: KPrimSig<*>, vararg attributes: Attr): KPrimSig<A> =
      KPrimSig(name, extends, *attributes)
  }
}

open class KSubsetSig<out A>(subsetSig: SubsetSig): KSig<A>(subsetSig) {
  constructor(name: String, of: KSig<*>, vararg attributes: Attr)
          : this(SubsetSig(name, listOf(of.sig), *attributes))
}

class KField<A, F>(field: Field): KRelation<A, F>(field)

fun <A, F> KSig<A>.field(
  name: String, type: KSet<F>
): KField<A, F> = KField(sig.addField(name, type.expr))

fun <A, F> KSig<A>.variable(
  name: String, type: KSet<F>
): KField<A, F> =
  KField(sig.addTrickyField(
    null, null, null, null, null,
    Pos.UNKNOWN, arrayOf(name), type.expr
  ).first())

class KThis<A>(expr: ExprHasName): KSet<A>(expr), KHasName {
  override val label: String = expr.label
}

fun <A> KSig<A>.self(): KThis<A> =
  KThis(sig.decl.get())

fun <A> KSig<A>.fact(
  formula: (KThis<A>) -> KFormula
): Unit = sig.addFact(formula(self()).expr)