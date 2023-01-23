package karat.ast

import edu.mit.csail.sdg.ast.ExprQt
import edu.mit.csail.sdg.ast.Sig
import java.util.*

interface ModuleLoader {
  fun module(name: String): KModule?
}

data class KModule(
  val sigs: List<KSig<*>>,
  val facts: List<KFormula>,
  val funcs: List<KFunction<*>> = emptyList(),
  val preds: List<KPredicate> = emptyList()
) {
  private fun KFunctionOrPredicate.named(f: String) =
    func.label == f || func.label == "this/$f"

  fun function(f: String): KFunction<*>? =
    funcs.firstOrNull { it.named(f) }
  fun predicate(f: String): KPredicate? =
    preds.firstOrNull { it.named(f) }
}

// this creates the 'skip' step for temporal formulae
fun KModule.skip(): KFormula =
  and {
    sigs
      .forEach { sig ->
        +`for`(ExprQt.Op.ALL, sig.sig.label.lowercase(Locale.getDefault()) to sig) { arg ->
          and {
            sig.sig.fields.forEach { field: Sig.Field ->
              +stays(arg / KRelation<Any, Any>(field))
            }
          }
        }
        if (sig is KSubsetSig<*>) {
          +stays(sig)
        }
      }
  }