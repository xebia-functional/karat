package karat.reflection

import karat.ast.*
import kotlin.reflect.*

object model {
  operator fun <S, A> getValue(thisRef: S, property: KProperty<*>): A =
    throw IllegalStateException("this should never be de-referenced")
  operator fun <S, A> setValue(thisRef: S, property: KProperty<*>, value: A) { }
}

inline fun <reified A> type(): KType = typeOf<A>()

interface ReflectedModule: ModuleLoader {
  fun set(type: KType): KSig<*>
  fun <A, F> field(type: KType, property: KProperty1<A, F>): KField<A, F>
  fun <F> global(property: KProperty0<F>): KSet<F>

  infix fun <A: Any, B: A> KSet<A>.`==`(other: KType): KFormula =
    this `==` set(other)

  fun nextUnique(prefix: String): String
  fun nextUnique(type: KType): String {
    val n = (type.classifier as? KClass<*>?)?.simpleName ?: "var"
    return nextUnique(n)
  }
}

// indicates a global fact
interface Fact: ReflectedModule
// indicates a fact which applies to each instance of the class
interface InstanceFact<A>: ReflectedModule {
  val self: KThis<A>
}