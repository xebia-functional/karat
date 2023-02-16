package karat.reflection

import edu.mit.csail.sdg.ast.Attr
import karat.KModuleBuilder
import karat.ast.*
import karat.stutterFor
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

interface StateMachineDefinition {
  fun init(): KFormula = Constants.TRUE
  fun stutter(): KFormula = Constants.TRUE
}

fun KModuleBuilder.reflectMachineFromMethods(
  oneMachine: StateMachineDefinition,
  vararg moreMachines: StateMachineDefinition,
  transitionSigName: String = "Action",
  transitionVarName: String = "Transition",
  initialName: String = "Init",
  stutterName: String = "Stutter",
) = stateMachine {
  val machines = listOf(oneMachine) + moreMachines

  // 1. declare the top of the hierarchy
  val newSig = KPrimSig<StateMachineTransition>(transitionSigName, Attr.ABSTRACT)
  recordSig(newSig)

  // 2. declare a single element to hold the current transition
  val stateSig = KSubsetSig<StateMachineTransition>(transitionVarName, newSig, Attr.ONE, Attr.VARIABLE)
  recordSig(stateSig)

  // 3. declare the initial transition
  val initialSig = KPrimSig<Nothing>(initialName, extends = newSig, Attr.ONE)
  recordSig(initialSig)
  initial {
    val t = and {
      + (current(stateSig) `==` initialSig)
      machines.forEach { + it.init() }
    }
    t
  }

  // 4. declare the stutter transition
  val stutterSig = KPrimSig<Nothing>(stutterName, extends = newSig, Attr.ONE)
  recordSig(stutterSig)
  transition {
    val t = and {
      + (next(stateSig) `==` stutterSig)
      machines.forEach { + it.stutter() }
    }
    t
  }

  // 5. declare the other transitions
  machines.forEach { machine ->
    val klass = machine::class
    klass.declaredFunctions
      .filter { it.name !in forbiddenMethodNames && !it.hasAnnotation<stutterFor>() }
      .forEach { method ->

        val actionSigName = "${klass.simpleName}->${method.name}"
        val transitionSig: KPrimSig<StateMachineTransition> =
          if (method.valueParameters.isEmpty())
            KPrimSig(actionSigName, extends = newSig, Attr.ONE)
          else
            KPrimSig(actionSigName, extends = newSig)
        recordSig(transitionSig)

        val properties = method.valueParameters.map { param ->
          val ret = param.type
          require(ret.klass?.isSubclassOf(KArg::class) == true) {
            "all arguments must be KArg"
          }
          val sig = requireNotNull(findSet(ret.arguments.firstOrNull()?.type)) {
            "cannot reflect type $ret"
          }
          val field = transitionSig.field(param.name!!, sig)
          PropInfo(nextUnique(param.name!!), field, sig)
        }

        // and then generate the transition
        transition {
          val t = and {
            // execute this step
            + (next(stateSig) `in` transitionSig)
            + innerForSomeMethod(machine, properties, method, next(stateSig))
            // stutter on the rest
            for (other in machines.filter { it::class != klass }) {
              val specialStutter = other::class.declaredFunctions.firstOrNull {
                it.findAnnotation<stutterFor>()?.klass == klass
              }
              if (specialStutter != null) {
                + (specialStutter.call(other) as KFormula)
              } else {
                + other.stutter()
              }
            }
          }
          t
        }
      }
  }
}

// generates nested forSome by recursively traversing the elements
fun innerForSomeMethod(
  machine: StateMachineDefinition,
  elements: List<PropInfo>,
  fn: KFunction<*>,
  currentStateRef: KSet<*>
): KFormula {
  fun worker(remainingElements: List<PropInfo>, accArgs: List<KArg<*>>, accFormula: List<KFormula>): KFormula =
    if (remainingElements.isEmpty()) {
      and(accFormula + fn.call(machine, *accArgs.toTypedArray()) as KFormula)
    } else {
      val e = remainingElements.first()
      forSome(e.name to e.sig) { arg ->
        worker(remainingElements.drop(1), accArgs + arg, accFormula + (currentStateRef / e.field `==` arg))
      }
    }

  return worker(elements, emptyList(), emptyList())
}
