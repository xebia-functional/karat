package fp.serrano.karat

import edu.mit.csail.sdg.ast.Attr
import fp.serrano.karat.ast.*
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

fun module(block: KModuleBuilder.() -> Unit): KModule =
  KModuleBuilder().also(block).build()

open class KModuleBuilder: ReflectedModule {
  val sigs: MutableList<KSig<*>> = mutableListOf()
  val facts: MutableList<KFormula> = mutableListOf()

  data class ReflectedSig<A>(val sig: KSig<A>, val fields: MutableMap<KProperty1<*, *>, KField<*, *>> = mutableMapOf())
  val reflectedSigs: MutableMap<KClass<*>, ReflectedSig<*>> = mutableMapOf()
  val reflectedGlobals: MutableMap<Method, KSig<*>> = mutableMapOf()

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

  internal fun recordGlobal(prop: KProperty<*>, newSig: KSig<*>) {
    reflectedGlobals[prop.javaGetter!!] = newSig
    recordSig(newSig)
  }

  fun reflect(vararg klasses: KClass<*>) =
    reflect(false, *klasses)

  fun reflect(reflectAll: Boolean, vararg klasses: KClass<*>) {
    klasses.forEach { reflectClassAsSig(it) }
    klasses.forEach { reflectClassFields(reflectAll, it) }
    klasses.forEach { reflectClassCompanionFields(it) }
    klasses.forEach { reflectClassCompanionFacts(it) }
  }

  fun reflect(vararg properties: KProperty<*>) {
    properties.forEach { reflectProperty(it) }
  }

  // step 1, generate all signatures
  private fun reflectClassAsSig(klass: KClass<*>) {
    // a. find the attributes
    val attribs = listOfNotNull(
      (Attr.ABSTRACT)?.takeIf { klass.hasAnnotation<abstract>() },
      (Attr.ONE)?.takeIf { klass.hasAnnotation<one>() || klass.hasAnnotation<element>() || klass.objectInstance != null },
      (Attr.VARIABLE)?.takeIf { klass.hasAnnotation<variable>() }
    )
    val isSubset = klass.hasAnnotation<subset>() || klass.hasAnnotation<element>()
    // b. find any possible super-class
    //    rule for now: superclasses must be reflected before
    val superSig = klass.supertypes.firstNotNullOfOrNull { ty ->
      (ty.classifier as? KClass<*>)?.let { reflectedSigs[it]?.sig }
    }
    // c. generate the thing
    val newSig = when {
      isSubset -> KSubsetSig(klass, klass.simpleName!!, superSig!!, *attribs.toTypedArray())
      superSig == null -> KPrimSig(klass, klass.simpleName!!, *attribs.toTypedArray())
      superSig is KPrimSig<*> -> KPrimSig(klass, klass.simpleName!!, superSig, *attribs.toTypedArray())
      else -> throw IllegalArgumentException("non-subset signatures must extend a non-subset one")
    }
    // d. record it
    recordSig(klass, newSig)
  }

  // step 2, generate all fields
  private fun reflectClassFields(reflectAll: Boolean, klass: KClass<*>) {
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
          ty?.isSubclassOf(Map::class) == true -> {
            findSet(ret.arguments[0].type?.classifier as? KClass<*>)?.let { k ->
              val v = ret.arguments[1].type
              val vTy = v?.classifier as? KClass<*>
              when {
                v?.isMarkedNullable == true ->
                  findSet(vTy)?.let { k `any --# lone` it }
                vTy?.isSubclassOf(Set::class) == true ->
                  findSet(ret.arguments.firstOrNull()?.type?.classifier as? KClass<*>)?.let { k `--#` it }
                else ->
                  findSet(vTy)?.let { k `any --# one` it }
              }
            }
          }
          else ->
            findSet(ty)
        }
        when (sigTy) {
          null -> throw IllegalArgumentException("cannot reflect type $ret")
          else -> {
            val newProp =
              if (property is KMutableProperty<*> || property.hasAnnotation<variable>())
                k.sig.variable(property.name, sigTy)
              else
                k.sig.field(property.name, sigTy)
            k.fields[property] = newProp
          }
        }
      }
  }

  // step 3, add global fields
  private fun reflectClassCompanionFields(klass: KClass<*>) {
    val companionKlass = klass.companionObject
    val companionValue = klass.companionObjectInstance
    if (companionKlass != null && companionValue != null) {
      companionKlass.declaredMemberProperties.forEach { reflectProperty(it)}
    }
  }

  private fun reflectProperty(property: KProperty<*>) {
    val ret = property.returnType
    val ty = ret.classifier as? KClass<*>
    val sigTy: Pair<List<Attr>, KSig<*>>? = when {
      property.returnType.isMarkedNullable ->
        findSet(ty)?.let { listOf(Attr.LONE) to it }

      ty?.isSubclassOf(Set::class) == true ->
        findSet(ret.arguments.firstOrNull()?.type?.classifier as? KClass<*>)?.let {
          when {
            property.hasAnnotation<lone>() -> listOf(Attr.LONE) to it
            property.hasAnnotation<one>() -> listOf(Attr.ONE) to it
            property.hasAnnotation<some>() -> emptyList<Attr>() to it
            else -> emptyList<Attr>() to it
          }
        }

      else ->
        findSet(ty)?.let { listOf(Attr.ONE) to it }
    }
    when (sigTy) {
      null -> throw IllegalArgumentException("cannot reflect type $ret")
      else -> {
        val newProp =
          if (property is KMutableProperty<*> || property.hasAnnotation<variable>())
            KSubsetSig<Nothing>(property.name, sigTy.second, *(listOf(Attr.VARIABLE) + sigTy.first).toTypedArray())
          else
            KSubsetSig<Nothing>(property.name, sigTy.second, *sigTy.first.toTypedArray())
        recordGlobal(property, newProp)
      }
    }
  }


  // step 4, add facts
  private fun reflectClassCompanionFacts(klass: KClass<*>) {
    val k = reflectedSigs[klass]!!  // should never fail, we've just added it
    val companionKlass = klass.companionObject
    val companionValue = klass.companionObjectInstance
    if (companionKlass != null && companionValue != null) {
      companionKlass.declaredMemberExtensionFunctions
        .forEach {
          // we can have a ReflectedModule as argument
          val ext = it.extensionReceiverParameter
          when {
            ext == null -> { }
            (ext.type.classifier as? KClass<*>)?.isSubclassOf(InstanceFact::class) == true ->
              when (val newFact = it.call(companionValue, instanceFactBuilder(klass))) {
                null -> { /* do nothing */ }
                is KFormula -> k.sig.fact { newFact }
                else -> throw IllegalArgumentException("instance fact returns type ${newFact::class.simpleName}")
              }
            (ext.type.classifier as? KClass<*>)?.isSubclassOf(Fact::class) == true ->
              when (val newFact = it.call(companionValue, factBuilder())) {
                null -> { /* do nothing */ }
                is KFormula -> fact { newFact }
                else -> throw IllegalArgumentException("fact returns type ${newFact::class.simpleName}")
              }
            else -> { }
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
    reflectedSigs[property.instanceParameter?.type?.classifier as KClass<*>]!!.fields[property]!! as KField<A, F>

  @Suppress("UNCHECKED_CAST")
  override fun <F> global(property: KProperty0<F>): KSet<F> =
    reflectedGlobals[property.javaGetter]!! as KSet<F>

  fun fact(formula: KFormula) {
    facts.add(formula)
  }

  fun fact(formula: () -> KFormula) {
    facts.add(formula())
  }

  fun facts(formula: KParagraphBuilder.() -> Unit) {
    fact { and(formula) }
  }

  private fun <A: Any> instanceFactBuilder(klass: KClass<A>): InstanceFact<A> =
    object : InstanceFact<A>, ReflectedModule by this {
      override val self: KThis<A> = set(klass).self()
    }

  private fun factBuilder(): Fact =
    object : Fact, ReflectedModule by this { }

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
