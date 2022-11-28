package fp.serrano.karat

import fp.serrano.karat.ast.*
import kotlin.reflect.*

interface ReflectedModule {
  fun <A: Any> set(klass: KClass<A>): KSig<A>
  fun <A, F> field(property: KProperty1<A, F>): KField<A, F>

  operator fun <A, B> KSet<A>.div(other: KProperty1<A, B>): KSet<B> =
    this / field(other)
  operator fun <A, B, C> KProperty1<A, B>.div(other: KProperty1<B, C>): KRelation<A, C> =
    field(this) / field(other)
  operator fun <A, B> KProperty1<A, B>.div(other: KSet<B>): KSet<A> =
    field(this) / other
  operator fun <A: Any, F> KProperty1<A, F>.get(other: KSet<A>): KSet<F> =
    field(this)[other]

  infix fun <A: Any, B: A> KExpr<Set<A>>.`==`(other: KClass<B>): KFormula =
    this `==` set(other)

  fun nextUnique(klass: KClass<*>): String
}

fun <A: Any> ReflectedModule.someOf(klass: KClass<A>): KSet<A> = someOf(set(klass))
fun <A: Any> ReflectedModule.loneOf(klass: KClass<A>): KSet<A> = loneOf(set(klass))
fun <A: Any> ReflectedModule.oneOf(klass: KClass<A>): KSet<A> = oneOf(set(klass))
fun <A: Any> ReflectedModule.setOf(klass: KClass<A>): KSet<A> = setOf(set(klass))

inline fun <reified A: Any> ReflectedModule.set(): KSig<A> = set(A::class)
inline fun <reified A: Any> ReflectedModule.element(): KSig<A> = set(A::class)

interface Fact<A>: ReflectedModule {
  val self: KThis<A>
}
