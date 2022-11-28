package fp.serrano.karat.ast

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
