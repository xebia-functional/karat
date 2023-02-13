package karat.alloy

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection

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