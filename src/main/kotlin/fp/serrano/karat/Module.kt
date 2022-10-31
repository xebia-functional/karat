package fp.serrano.karat

import edu.mit.csail.sdg.ast.ExprQt
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

data class KModule(val sigs: List<KSig<*>>, val facts: List<KFormula>)

// this creates the 'skip' step for temporal formulae
fun KModule.skip(): KFormula =
  and {
    sigs
      .map { it to it::class }
      .forEach { (sig, klass) ->
        +`for`(ExprQt.Op.ALL, sig.primSig.label to sig) { arg ->
          and {
            klass.declaredMemberProperties
              .mapNotNull { property ->
                val getter = property as KProperty1<KSig<*>, *>
                getter(sig) as? KField<*, *>
              }
              .forEach {
                +stays(arg / it)
              }
          }
        }
      }
  }

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

  fun stateMachine(skip: Boolean, block: KTemporalFormulaBuilder.() -> Unit) =
    fact {
      temporal {
        if (skip) skipTransition()
        block()
      }
    }

  fun build(): KModule = KModule(sigs.toList(), facts.toList())
}