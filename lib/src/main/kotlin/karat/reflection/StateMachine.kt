package karat.reflection

import edu.mit.csail.sdg.ast.Attr
import karat.KModuleBuilder
import karat.ast.*
import karat.initial
import karat.stutter
import kotlin.reflect.*
import kotlin.reflect.full.*

fun interface StateMachine {
  context(ReflectedModule) fun execute(): KFormula
}

inline fun <reified A: StateMachine> KModuleBuilder.reflectMachine(
  transitionSigName: String? = null,
  transitionVarName: String = "Transition",
  initialName: String = "Init",
  stutterName: String = "Stutter"
) {
  val ty = typeOf<A>()
  reflectMachine(transitionSigName ?: ty.klass?.simpleName!!, ty, transitionVarName = transitionVarName, initialName = initialName, stutterName = stutterName)
}

fun KModuleBuilder.reflectMachine(
  transitionSigName: String,
  oneType: KType,
  vararg moreTypes: KType,
  transitionVarName: String = "Transition",
  initialName: String = "Init",
  stutterName: String = "Stutter",
) = stateMachine {
  val types = listOf(oneType) + moreTypes

  types.forEach { ty ->
    val klass = requireNotNull(ty.klass) {
      "only bare interfaces can be turned into state machines"
    }
    require(klass.java.isInterface && klass.isSealed && klass.declaredMembers.isEmpty()) {
      "only empty sealed interfaces can be turned into state machines"
    }
    require(klass.typeParameters.isEmpty()) {
      "only interfaces without parameters can be turned into state machines"
    }
  }

  // 1. find the different steps
  val klasses = types.map { it.klass!! }
  val initials = klasses.mapNotNull { klass ->
    klass.sealedSubclasses.firstOrNull { it.hasAnnotation<initial>() }?.let { klass to it }
  }.toMap()
  val stutters = klasses.mapNotNull { klass ->
    klass.sealedSubclasses.firstOrNull { it.hasAnnotation<stutter>() }?.let { klass to it }
  }.toMap()
  val actualTransitions = klasses.associateWith { klass ->
    klass.sealedSubclasses.filter {
      !it.hasAnnotation<initial>() && !it.hasAnnotation<stutter>()
    }
  }

  // 2. declare the top of the hierarchy
  val newSig = KPrimSig<StateMachine>(transitionSigName, Attr.ABSTRACT)
  recordSig(newSig)

  // 3. declare a single element to hold the current transition
  val stateSig = KSubsetSig<StateMachine>(transitionVarName, newSig, Attr.ONE, Attr.VARIABLE)
  recordSig(stateSig)

  // 4. declare the initial transition
  val initialSig = KPrimSig<Nothing>(initialName, extends = newSig, Attr.ONE)
  recordSig(initialSig)
  initial {
    val t = and {
      + (current(stateSig) `==` initialSig)
      for ((_, v) in initials) {
        + formulaFromObject("initial", v)
      }
    }
    t
  }

  // 5. declare the stutter transition
  val stutterSig = KPrimSig<Nothing>(stutterName, extends = newSig, Attr.ONE)
  recordSig(stutterSig)
  transition {
    val t = and {
      + (next(stateSig) `==` stutterSig)
      for ((_, v) in stutters) {
        + formulaFromObject("stutter", v)
      }
    }
    t
  }

  // 6. declare each of the others
  actualTransitions.forEach { (klass, subclasses) ->
    subclasses.forEach { transitionKlass ->
      val transitionSig: KPrimSig<StateMachine> =
        if (transitionKlass.objectInstance == null)
          KPrimSig(transitionKlass.simpleName!!, extends = newSig)
        else
          KPrimSig(transitionKlass.simpleName!!, extends = newSig, Attr.ONE)
      recordSig(transitionKlass.starProjectedType, transitionSig)

      val properties = when {
        transitionKlass.primaryConstructor != null -> transitionKlass.primaryConstructor!!.parameters.map { property ->
          val ret = property.type
          require(ret.klass?.isSubclassOf(KArg::class) == true) { "all arguments must be KArg" }
          val sig = requireNotNull(findSet(ret.arguments.firstOrNull()?.type)) {
            "cannot reflect type $ret"
          }
          val field = transitionSig.field(property.name!!, sig)
          PropInfo(property.name!!, field, sig)
        }
        transitionKlass.objectInstance != null -> emptyList()
        else -> throw IllegalArgumentException("${transitionKlass.simpleName} is not a valid transition class")
      }
      // and then generate the transition
      transition {
        val t = and {
          // execute this step
          + (next(stateSig) `in` set(transitionKlass.starProjectedType))
          + innerForSome(properties, transitionKlass, next(stateSig))
          // stutter on the rest
          for ((_, v) in stutters.filterKeys { it != klass })
            + formulaFromObject("stutter", v)
        }
        t
      }
    }
  }
}

private fun KModuleBuilder.formulaFromObject(element: String, klass: KClass<*>): KFormula {
  requireNotNull(klass.objectInstance) { "$element must be declared as object" }
  return with(klass.objectInstance as StateMachine) {
    execute()
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
