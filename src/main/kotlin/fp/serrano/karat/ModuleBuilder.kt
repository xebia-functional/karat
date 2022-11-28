package fp.serrano.karat

import edu.mit.csail.sdg.ast.Attr
import fp.serrano.karat.ast.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.*
import kotlin.reflect.full.*

fun module(block: KModuleBuilder.() -> Unit): KModule =
  KModuleBuilder().also(block).build()

open class KModuleBuilder: ReflectedModule {
  val sigs: MutableList<KSig<*>> = mutableListOf()
  val facts: MutableList<KFormula> = mutableListOf()

  data class ReflectedSig<A>(val sig: KSig<A>, val fields: MutableMap<KProperty1<*, *>, KField<*, *>> = mutableMapOf())
  val reflectedSigs: MutableMap<KClass<*>, ReflectedSig<*>> = mutableMapOf()

  var unique: AtomicLong = AtomicLong(0L)

  fun sig(newSig: KSig<*>) {
    sigs(newSig)
  }
  fun sigs(vararg newSigs: KSig<*>) {
    sigs.addAll(newSigs)
  }

  override fun nextUnique(klass: KClass<*>): String {
    val n = klass.simpleName ?: "var"
    val i = unique.incrementAndGet()
    return "__${n}__$i"
  }

  internal fun recordSig(newSig: KSig<*>) {
    sig(newSig)
  }

  internal fun recordSig(klass: KClass<*>, newSig: KSig<*>) {
    reflectedSigs[klass] = ReflectedSig(newSig)
    recordSig(newSig)
  }

  fun reflect(vararg klasses: KClass<*>) =
    reflect(false, *klasses)

  fun reflect(reflectAll: Boolean, vararg klasses: KClass<*>) {
    // step 1, generate all signatures
    klasses.forEach { klass ->
      // a. find the attributes
      val attribs = listOfNotNull(
        (Attr.ABSTRACT)?.takeIf { klass.hasAnnotation<abstract>() },
        (Attr.ONE)?.takeIf { klass.hasAnnotation<one>() || klass.objectInstance != null }
      )
      // b. find any possible super-class
      //    rule for now: superclasses must be reflected before
      val superSig = klass.supertypes.firstNotNullOfOrNull { ty ->
        (ty.classifier as? KClass<*>)?.let { reflectedSigs[it]?.sig }
      }
      // c. generate the thing
      val newSig = when (superSig) {
        null -> KSig(klass, klass.simpleName!!, *attribs.toTypedArray())
        else -> KSig(klass, klass.simpleName!!, superSig, *attribs.toTypedArray())
      }
      // d. record it
      recordSig(klass, newSig)
    }
    // step 2, generate all fields
    klasses.forEach { klass ->
      val k = reflectedSigs[klass]!!  // should never fail, we've just added it
      klass.declaredMemberProperties
        // a. only the reflected ones
        .filter {
          reflectAll || it.hasAnnotation<reflect>()
        }
        .forEach { property ->
          // b. figure out the type
          val ret = property.returnType
          val ty = ret.classifier as? KClass<*>
          val sigTy: KSet<*>? = when {
            property.returnType.isMarkedNullable ->
              findSet(ty)?.let { loneOf(it) }
            ty?.isSubclassOf(Set::class) == true ->
              findSet(ret.arguments.firstOrNull()?.type?.classifier as? KClass<*>)?.let {
                when {
                  property.hasAnnotation<lone>() -> loneOf(it)
                  property.hasAnnotation<one>() -> oneOf(it)
                  property.hasAnnotation<some>() -> someOf(it)
                  else -> setOf(it)
                }
              }
            else ->
              findSet(ty)
          }
          when (sigTy) {
            null -> throw IllegalArgumentException("cannot reflect type $ret")
            else -> {
              val newProp =
                if (property is KMutableProperty1)
                  k.sig.variable(property.name, sigTy)
                else
                  k.sig.field(property.name, sigTy)
              k.fields[property] = newProp
            }
          }
        }
    }
    // step 3, add facts
    klasses.forEach { klass ->
      val k = reflectedSigs[klass]!!  // should never fail, we've just added it
      val companionKlass = klass.companionObject
      val companionValue = klass.companionObjectInstance
      if (companionKlass != null && companionValue != null) {
        companionKlass.declaredMemberExtensionFunctions
          .forEach {
            // we can have a ReflectedModule as argument
            val ext = it.extensionReceiverParameter
            val newFact =
              if (ext != null && (ext.type.classifier as? KClass<*>)?.isSubclassOf(Fact::class) == true) {
                it.call(companionValue, factBuilder(klass))
              } else null
            when (newFact) {
              null -> { /* do nothing */ }
              is KFormula -> k.sig.fact { newFact }
              else -> throw IllegalArgumentException("annotated @fact returns type ${newFact::class.simpleName}")
            }
          }
      }
    }
  }

  override fun <A : Any> set(klass: KClass<A>): KSig<A> =
    findSet(klass)!!

  @Suppress("UNCHECKED_CAST")
  internal fun <A : Any> findSet(klass: KClass<A>?): KSig<A>? =
    when {
      klass == null -> null
      klass.isSubclassOf(Int::class) -> Sigs.SIGINT
      klass.isSubclassOf(String::class) -> Sigs.STRING
      else -> reflectedSigs[klass]?.sig
    } as KSig<A>?

  @Suppress("UNCHECKED_CAST")
  override fun <A, F> field(property: KProperty1<A, F>): KField<A, F> =
    (reflectedSigs[property.instanceParameter?.type?.classifier as KClass<*>]!!.fields[property]!!) as KField<A, F>

  fun fact(formula: KFormula) {
    facts.add(formula)
  }

  fun fact(formula: ReflectedModule.() -> KFormula) {
    facts.add(formula())
  }

  private fun <A: Any> factBuilder(klass: KClass<A>): Fact<A> = object : Fact<A>, ReflectedModule by this {
    override val self: KThis<A> = set(klass).self()
  }

  fun KTemporalFormulaBuilder.skipTransition(): Unit =
    this@KModuleBuilder.build().skipTransition()

  fun stateMachine(skip: Boolean, block: KTemporalFormulaBuilder.() -> Unit) =
    fact {
      val t = temporal {
        if (skip) skipTransition()
        block()
      }
      t
    }

  fun build(): KModule = KModule(sigs.toList(), facts.toList())
}