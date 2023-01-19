package karat

import edu.mit.csail.sdg.ast.Attr
import karat.ast.*
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*
import kotlin.reflect.KTypeProjection.*

fun module(block: KModuleBuilder.() -> Unit): KModule =
  KModuleBuilder().also(block).build()

open class KModuleBuilder: ReflectedModule {
  private val sigs: MutableList<KSig<*>> = mutableListOf()
  private val facts: MutableList<KFormula> = mutableListOf()

  data class ReflectedType(val ty: KClass<*>, val args: List<ReflectedType>)
  data class ReflectedSig<A>(val sig: KSig<A>, val fields: MutableMap<KProperty1<*, *>, KField<*, *>> = mutableMapOf())
  private val reflectedSigs: MutableMap<ReflectedType, ReflectedSig<*>> = mutableMapOf()
  private val reflectedGlobals: MutableMap<Method, KSig<*>> = mutableMapOf()

  private var unique: AtomicLong = AtomicLong(0L)

  fun sig(newSig: KSig<*>) {
    sigs(newSig)
  }
  fun sigs(vararg newSigs: KSig<*>) {
    sigs.addAll(newSigs)
  }

  private val KType.reflected: ReflectedType
    get() = ReflectedType(klass!!, arguments.map { it.type?.reflected!! })

  val KType.klass: KClass<*>?
    get() = classifier as? KClass<*>?

  override fun nextUnique(type: KType): String {
    val n = type.klass?.simpleName ?: "var"
    val i = unique.incrementAndGet()
    return "__${n}__$i"
  }

  internal fun recordSig(newSig: KSig<*>) {
    sig(newSig)
  }

  internal fun recordSig(type: KType, newSig: KSig<*>) {
    reflectedSigs[type.reflected] = ReflectedSig(newSig)
    recordSig(newSig)
  }

  internal fun recordGlobal(prop: KProperty<*>, newSig: KSig<*>) {
    reflectedGlobals[prop.javaGetter!!] = newSig
    recordSig(newSig)
  }

  fun reflect(vararg types: KType) =
    reflect(false, *types)

  fun reflect(reflectAll: Boolean, vararg types: KType) {
    types.forEach { reflectClassAsSig(it) }
    types.forEach { reflectClassFields(reflectAll, it) }
    types.forEach { reflectClassCompanionFields(it) }
    types.forEach { reflectClassCompanionFacts(it) }
  }

  fun reflect(vararg properties: KProperty<*>) {
    properties.forEach { reflectProperty(it) }
  }

  private fun KType.substitute(subst: Map<KTypeParameter, KTypeProjection>): KType =
    when (val c = classifier) {
      null -> this
      is KTypeParameter -> subst.entries.firstOrNull { it.key.name == c.name }?.value?.type ?: this
      is KClass<*> -> object : KType by this {
        override val arguments: List<KTypeProjection>
          get() = this@substitute.arguments.map { argument ->
            argument.type?.substitute(subst)?.let { KTypeProjection.invariant(it) } ?: argument
          }
      }
      else -> this
    }

  private fun KType.substitute(from: KType): KType = when (val c = from.klass) {
    null -> this
    else -> {
      val subst = c.typeParameters.zip(from.arguments).toMap()
      this.substitute(subst)
    }
  }

  // step 1, generate all signatures
  private fun reflectClassAsSig(type: KType) {
    // 0. checks:
    //    - we have a class
    val klass = checkNotNull(type.klass)
    //    - all the arguments are classes themselves (no weird variances)
    type.arguments.forEach {
      checkNotNull(it.type?.klass)
      check(it.variance == null || it.variance == KVariance.INVARIANT)
    }
    // a. create name by smashing together the names
    val name = when (type.arguments.size) {
      0 -> klass.simpleName!!
      else -> "${klass.simpleName!!}<${type.arguments.joinToString(separator = ",") { it.type?.klass?.simpleName!! }}>"
    }
    // b. find the attributes
    val attribs = listOfNotNull(
      (Attr.ABSTRACT)?.takeIf { klass.hasAnnotation<abstract>() },
      (Attr.ONE)?.takeIf { klass.hasAnnotation<one>() || klass.hasAnnotation<element>() || klass.objectInstance != null },
      (Attr.VARIABLE)?.takeIf { klass.hasAnnotation<variable>() }
    )
    val isSubset = klass.hasAnnotation<subset>() || klass.hasAnnotation<element>()
    // c. find any possible super-class
    //    rule for now: superclasses must be reflected before
    val superSig =
      klass.supertypes
        .map { it.substitute(type) }
        .firstNotNullOfOrNull { reflectedSigs[it.reflected]?.sig }
    // d. generate the thing
    // at this point we don't care about the generic argument, so we use Any?
    val newSig: KSig<Any?> = when {
      isSubset -> KSubsetSig(name, superSig!!, *attribs.toTypedArray())
      superSig == null -> KPrimSig(name, *attribs.toTypedArray())
      superSig is KPrimSig<*> -> KPrimSig(name, superSig, *attribs.toTypedArray())
      else -> throw IllegalArgumentException("non-subset signatures must extend a non-subset one")
    }
    // e. record it
    recordSig(type, newSig)
  }

  // step 2, generate all fields
  private fun reflectClassFields(reflectAll: Boolean, type: KType) {
    val k = reflectedSigs[type.reflected]!!  // should never fail, we've just added it
    val klass = type.klass!!
    klass.declaredMemberProperties
      // a. only the reflected ones
      .filter {
        reflectAll || it.hasAnnotation<reflect>()
      }
      .forEach { property ->
        // b. figure out the type
        val ret = property.returnType.substitute(type)
        val sigTy: KSet<*>? = when {
          ret.isMarkedNullable ->
            findSet(ret)?.let { loneOf(it) }
          ret.klass?.isSubclassOf(Set::class) == true ->
            findSet(ret.arguments.firstOrNull()?.type)?.let {
              when {
                property.hasAnnotation<lone>() -> loneOf(it)
                property.hasAnnotation<one>() -> oneOf(it)
                property.hasAnnotation<some>() -> someOf(it)
                else -> setOf(it)
              }
            }
          ret.klass?.isSubclassOf(List::class) == true ->
            findSet(ret.arguments.firstOrNull()?.type)?.let {
              seq(it)
            }
          ret.klass?.isSubclassOf(Map::class) == true -> {
            findSet(ret.arguments[0].type)?.let { k ->
              val v = ret.arguments[1].type
              when {
                v?.isMarkedNullable == true ->
                  findSet(v)?.let { k `any --# lone` it }
                v?.klass?.isSubclassOf(Set::class) == true ->
                  findSet(ret.arguments.firstOrNull()?.type)?.let { k `--#` it }
                else ->
                  findSet(v)?.let { k `any --# one` it }
              }
            }
          }
          else ->
            findSet(ret)
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
  private fun reflectClassCompanionFields(type: KType) {
    val companionKlass = type.klass!!.companionObject
    val companionValue = type.klass!!.companionObjectInstance
    if (companionKlass != null && companionValue != null) {
      companionKlass.declaredMemberProperties.forEach { reflectProperty(it) }
    }
  }

  private fun reflectProperty(property: KProperty<*>) {
    val ret = property.returnType
    val sigTy: Pair<List<Attr>, KSig<*>>? = when {
      property.returnType.isMarkedNullable ->
        findSet(ret)?.let { listOf(Attr.LONE) to it }

      ret.klass?.isSubclassOf(Set::class) == true ->
        findSet(ret.arguments.firstOrNull()?.type)?.let {
          when {
            property.hasAnnotation<lone>() -> listOf(Attr.LONE) to it
            property.hasAnnotation<one>() -> listOf(Attr.ONE) to it
            property.hasAnnotation<some>() -> emptyList<Attr>() to it
            else -> emptyList<Attr>() to it
          }
        }

      ret.klass?.isSubclassOf(List::class) == true ->
        throw IllegalArgumentException("List cannot be used within a property")

      else ->
        findSet(ret)?.let { listOf(Attr.ONE) to it }
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
  private fun reflectClassCompanionFacts(type: KType) {
    val k = reflectedSigs[type.reflected]!!  // should never fail, we've just added it
    val companionKlass = type.klass!!.companionObject
    val companionValue = type.klass!!.companionObjectInstance
    if (companionKlass != null && companionValue != null) {
      companionKlass.declaredMemberExtensionFunctions
        .forEach {
          // we can have a ReflectedModule as argument
          val ext = it.extensionReceiverParameter
          when {
            ext == null -> { }
            ext.type.klass?.isSubclassOf(InstanceFact::class) == true ->
              when (val newFact = it.call(companionValue, instanceFactBuilder(type))) {
                null -> { /* do nothing */ }
                is KFormula -> k.sig.fact { newFact }
                else -> throw IllegalArgumentException("instance fact returns type ${newFact::class.simpleName}")
              }
            ext.type.klass?.isSubclassOf(Fact::class) == true ->
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

  override fun set(type: KType): KSig<*> =
    findSet(type)!!

  internal fun findSet(type: KType?): KSig<*>? {
    val c = type?.klass
    return when {
      c == null -> null
      c.isSubclassOf(Int::class) -> Sigs.SIGINT
      c.isSubclassOf(String::class) -> Sigs.STRING
      else -> reflectedSigs[type.reflected]?.sig
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun <A, F> field(property: KProperty1<A, F>): KField<A, F> =
    reflectedSigs[property.instanceParameter?.type?.reflected]!!.fields[property]!! as KField<A, F>

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

  private fun instanceFactBuilder(type: KType): InstanceFact<Any?> =
    object : InstanceFact<Any?>, ReflectedModule by this {
      override val self: KThis<Any?> = set(type).self()
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
