package fp.serrano.karat

data class KModule(val sigs: List<KSig<*>>, val facts: List<KFormula>)

fun module(block: KModuleBuilder.() -> Unit): KModule =
  KModuleBuilder().also(block).build()

class KModuleBuilder {
  val sigs: MutableList<KSig<*>> = mutableListOf()
  val facts: MutableList<KFormula> = mutableListOf()

  fun sigs(vararg newSigs: KSig<*>) {
    sigs.addAll(newSigs)
  }

  fun fact(formula: KFormula) {
    facts.add(formula)
  }

  fun fact(formula: () -> KFormula) {
    facts.add(formula())
  }

  fun build(): KModule = KModule(sigs.toList(), facts.toList())
}