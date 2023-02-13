package karat.alloy

import karat.symbolic.reflect
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.withNullability

internal data class ReflectedType(val ty: KClass<*>, val args: List<ReflectedType>)

internal val KType.klass: KClass<*>?
  get() = classifier as? KClass<*>?

internal val KType.reflected: ReflectedType
  get() =
    try {
      ReflectedType(klass!!, arguments.map { it.type?.reflected!! })
    } catch(e: NullPointerException) {
      throw IllegalArgumentException("cannot reflect type $this")
    }

internal fun KType.substitute(subst: Map<KTypeParameter, KTypeProjection>): KType =
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

internal fun KType.substitute(from: KType): KType = when (val c = from.klass) {
  null -> this
  else -> {
    val subst = c.typeParameters.zip(from.arguments).toMap()
    this.substitute(subst)
  }
}

private val primitiveTypes: List<KClass<*>> =
  listOf(Int::class, String::class)

/**
 * Finds all the [KType] that can be reached from [this].
 */
internal fun KType.reflectedClosure(
  doNotVisit: Iterable<ReflectedType> = emptySet()
): Set<KType> {
  val queue = mutableListOf(this)
  val alreadyVisited = mutableSetOf<ReflectedType>().apply { addAll(doNotVisit) }
  val found = mutableSetOf<KType>()
  while (queue.isNotEmpty()) {
    val current = queue.removeFirst().let {
      // see "through" nullable types
      if (it.isMarkedNullable) it.withNullability(false) else it
    }
    // do not visit things twice
    if (current.reflected in alreadyVisited) continue
    // check type arguments
    val currentKlass = current.klass
    // primitive types are not added
    if (currentKlass == null || primitiveTypes.any { currentKlass.isSubclassOf(it)}) continue
    // for collection types we inspect argument
    if (currentKlass.isSubclassOf(Collection::class) ||currentKlass.isSubclassOf(Map::class)) {
      queue.addAll(current.arguments.mapNotNull { it.type })
      continue
    }
    // it seems that it's indeed new!
    found.add(current)
    alreadyVisited.add(current.reflected)
    currentKlass.declaredMemberProperties
      .filter { it.hasAnnotation<reflect>() }
      .map { it.returnType.substitute(current) }
      .let { queue.addAll(it) }
  }
  return found.toSet()
}