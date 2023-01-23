package karat.reflection

import edu.mit.csail.sdg.ast.Attr
import karat.KModuleBuilder
import karat.ast.*
import karat.initial
import kotlin.reflect.*
import kotlin.reflect.full.*

fun interface StateMachine {
  context(ReflectedModule) fun execute(): KFormula
}

inline fun <reified A: StateMachine> KModuleBuilder.reflectMachine(
  skip: Boolean = true,
  transitionSigName: String = "Transition",
  skipName: String = "Skip"
) = reflectMachine(A::class, skip, transitionSigName, skipName)

fun <A: StateMachine> KModuleBuilder.reflectMachine(
  klass: KClass<A>,
  skip: Boolean = true,
  transitionSigName: String = "Transition",
  skipName: String = "Skip"
) = stateMachine(skip = false) {
  require(klass.java.isInterface && klass.isSealed && klass.declaredMembers.isEmpty()) {
    "only empty sealed interfaces may bed turned into state machines"
  }

  val skipFormula = this@reflectMachine.build().skip()

  // 1. declare the top of the hierarchy
  val newSig = KPrimSig<A>(klass.simpleName!!, Attr.ABSTRACT)
  recordSig(klass.starProjectedType, newSig)

  // 2. declare a single element to hold the current transition
  val stateSig = KSubsetSig<A>(transitionSigName, newSig, Attr.ONE, Attr.VARIABLE)
  recordSig(stateSig)

  // 3. declare the skip transition
  if (skip) {
    val skipSig = KPrimSig<Nothing>(skipName, extends = newSig, Attr.ONE)
    recordSig(skipSig)
    transition {
      skipFormula and (next(stateSig) `==` skipSig)
    }
  }

  // 4. declare each of the others
  klass.sealedSubclasses.forEach { transitionKlass ->
    val transitionSig: KPrimSig<Any> =
      if (transitionKlass.objectInstance == null)
        KPrimSig(transitionKlass.simpleName!!, extends = newSig)
      else
        KPrimSig(transitionKlass.simpleName!!, extends = newSig, Attr.ONE)
    recordSig(transitionKlass.starProjectedType, transitionSig)

    if (transitionKlass.hasAnnotation<initial>()) {
      // if it's initial just execute the object
      when (val o = transitionKlass.objectInstance) {
        null -> throw IllegalArgumentException("the initial transition must be an object")
        else -> initial {
          with(o) { execute() }
        }
      }
    } else {
      // if not, we need to declare the properties
      val properties = when {
        transitionKlass.primaryConstructor != null -> transitionKlass.primaryConstructor!!.parameters.map { property ->
          val ret = property.type
          val ty = ret.classifier as? KClass<*>
          require(ty?.isSubclassOf(KArg::class) == true) {
            "all arguments must be KArg"
          }

          val sig =
            findSet(ret.arguments.firstOrNull()?.type)
              ?: throw IllegalArgumentException("cannot reflect type $ret")

          val field = transitionSig.field(property.name!!, sig)
          PropInfo(property.name!!, field, sig)
        }
        transitionKlass.objectInstance != null -> emptyList()
        else -> throw IllegalArgumentException("${transitionKlass.simpleName} is not a valid transition class")
      }
      // and then generate the transition
      val inner = innerForSome(properties, transitionKlass, stateSig)
      transition {
        inner and (stateSig `in` set(transitionKlass.starProjectedType))
      }
    }
  }
}

data class PropInfo(val name: String, val field: KField<*, *>, val sig: KSig<*>)

// generates nested forSome by recursively traversing the elements
fun KModuleBuilder.innerForSome(elements: List<PropInfo>, klass: KClass<*>, currentStateRef: KSet<*>): KFormula {
  fun worker(remainingElements: List<PropInfo>, acc: List<KArg<*>>): KFormula =
    if (remainingElements.isEmpty()) {
      val stateMachine = when {
        klass.primaryConstructor != null ->
          klass.primaryConstructor!!.call(*acc.toTypedArray()) as StateMachine
        klass.objectInstance != null ->
          klass.objectInstance as StateMachine
        else ->
          throw IllegalArgumentException("${klass.simpleName} is not a valid transition class")
      }
      with (stateMachine) { execute() }
    } else {
      val e = remainingElements.first()
      forSome(e.name to e.sig) { arg ->
        worker(remainingElements.drop(1), acc + listOf(arg)) and (currentStateRef / e.field `==` arg)
      }
    }

  return worker(elements, emptyList())
}
