package fp.serrano.karat.ast

import edu.mit.csail.sdg.ast.ExprQt
import edu.mit.csail.sdg.ast.Sig

data class KModule(val sigs: List<KSig<*>>, val facts: List<KFormula>)

// this creates the 'skip' step for temporal formulae
fun KModule.skip(): KFormula =
  and {
    sigs
      .forEach { sig ->
        +`for`(ExprQt.Op.ALL, sig.primSig.label.toLowerCase() to sig) { arg ->
          and {
            sig.primSig.fields.forEach { field: Sig.Field ->
              +stays(arg / KRelation<Any, Any>(field))
            }
          }
        }
      }
  }
