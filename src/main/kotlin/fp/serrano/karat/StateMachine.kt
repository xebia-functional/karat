package fp.serrano.karat

import edu.mit.csail.sdg.ast.Attr
import fp.serrano.karat.ast.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

fun interface StateMachine {
  fun ReflectedModule.execute(): KFormula
}

fun <A: StateMachine> KModuleBuilder.reflectMachine(
  klass: KClass<A>,
  skip: Boolean = true,
  transitionSigName: String = "Transition",
  currentName: String = "current",
  skipName: String = "Skip"
) = stateMachine(skip = false) {
  if (!klass.java.isInterface || !klass.isSealed || klass.declaredMembers.isNotEmpty())
    throw IllegalArgumentException("only empty sealed interfaces may bed turned into state machines")

  val skipFormula = this@reflectMachine.build().skip()

  // 1. declare the top of the hierarchy
  val newSig = KSig(klass, klass.simpleName!!, Attr.ABSTRACT)
  recordSig(klass, newSig)

  // 2. declare a single element to hold the current transition
  val stateSig = KSig<Nothing>(transitionSigName, Attr.ONE)
  recordSig(stateSig)
  val currentState = stateSig.variable(currentName, newSig)
  val currentStateRef = stateSig / currentState

  // 3. declare the skip transition
  if (skip) {
    val skipSig = KSig<Nothing>(skipName, extends = newSig, Attr.ONE)
    recordSig(skipSig)
    transition {
      skipFormula and (next(currentStateRef) `==` skipSig)
    }
  }

  // 4. declare each of the others
  klass.sealedSubclasses.forEach { transitionKlass ->
    val transitionSig =
      if (transitionKlass.objectInstance == null)
        KSig(transitionKlass, transitionKlass.simpleName!!, extends = newSig, Attr.ONE)
      else
        KSig(transitionKlass, transitionKlass.simpleName!!, extends = newSig)
    recordSig(transitionKlass, transitionSig)

    if (transitionKlass.hasAnnotation<initial>()) {
      // if it's initial just execute the object
      when (val o = transitionKlass.objectInstance) {
        null -> throw IllegalArgumentException("the initial transition must be an object")
        else -> initial {
          with(o) { execute() } and (currentStateRef `==` set(transitionKlass))
        }
      }
    } else {
      // if not, we need to declare the properties
      val properties = transitionKlass.declaredMemberProperties.map { property ->
        val ret = property.returnType
        val ty = ret.classifier as? KClass<*>
        if (ty?.isSubclassOf(KArg::class) != true)
          throw IllegalArgumentException("all arguments must be KArg")

        val sig =
          findSet(ret.arguments.firstOrNull()?.type?.classifier as? KClass<*>)
            ?: throw IllegalArgumentException("cannot reflect type $ret")

        val field = transitionSig.field(property.name, sig)
        PropInfo(property.name, field, sig)
      }
      // and then generate the transition
      transition {
        next(currentStateRef) `in` set(transitionKlass) and innerForSome(properties, transitionKlass, currentStateRef)
      }
    }
  }
}

data class PropInfo(val name: String, val field: KField<*, *>, val sig: KSig<*>)

// generates nested forSome by recursively traversing the elements
fun KModuleBuilder.innerForSome(elements: List<PropInfo>, klass: KClass<*>, currentStateRef: KSet<*>): KFormula {
  fun worker(remainingElements: List<PropInfo>, acc: List<KArg<*>>): KFormula =
    if (remainingElements.isEmpty()) {
      with (klass.primaryConstructor!!.call(*acc.toTypedArray()) as StateMachine) { execute() }
    } else {
      val e = remainingElements.first()
      forSome(e.name to e.sig) { arg ->
        worker(remainingElements.drop(1), acc + listOf(arg)) and (next(currentStateRef) / e.field `==` arg)
      }
    }

  return worker(elements, emptyList())
}