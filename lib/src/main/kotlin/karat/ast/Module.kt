package karat.ast

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