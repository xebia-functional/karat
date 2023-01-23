package karat

import edu.mit.csail.sdg.ast.ExprQt
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

  fun nextUnique(type: KType): String
}

context(ReflectedModule) inline fun <reified A, F> field(property: KProperty1<A, F>): KField<A, F> =
  field(typeOf<A>(), property)
context(ReflectedModule) inline operator fun <reified A, B> KSet<A>.div(other: KProperty1<A, B>): KSet<B> =
  this / field(other)
context(ReflectedModule) inline infix fun <reified A, reified B, C> KProperty1<A, B>.join(other: KProperty1<B, C>): KRelation<A, C> =
  field(this) join field(other)
context(ReflectedModule) inline operator fun <reified A, B> KProperty1<A, B>.rem(other: KSet<B>): KSet<A> =
  field(this) % other
context(ReflectedModule) inline operator fun <reified A: Any, F> KProperty1<A, F>.get(other: KSet<A>): KSet<F> =
  field(this)[other]

context(ReflectedModule) inline val <reified A, B> KProperty1<A, Set<B>>.flattenR: KRelation<A, B>
  get() = field(this).flattenR
context(ReflectedModule) inline val <reified A, B, C> KProperty1<A, Map<B, C>>.asRelationR: KRelation<A, Pair<B, C>>
  get() = field(this).asRelationR
context(ReflectedModule) inline val <reified A, B, C> KProperty1<A, Map<B, Set<C>>>.asRelationSetR: KRelation<A, Pair<B, C>>
  get() = field(this).asRelationSetR
context(ReflectedModule) inline val <reified A, B, C> KProperty1<A, Map<B, C?>>.asRelationNullableR: KRelation<A, Pair<B, C>>
  get() = field(this).asRelationNullableR

// indicates a global fact
interface Fact: ReflectedModule
// indicates a fact which applies to each instance of the class
interface InstanceFact<A>: ReflectedModule {
  val self: KThis<A>
}

context(ReflectedModule) @Suppress("UNCHECKED_CAST") inline fun <reified A> set(): KSig<A> =
  set(typeOf<A>()) as KSig<A>
context(ReflectedModule) @Suppress("UNCHECKED_CAST") inline fun <reified A> element(): KSig<A> =
  set(typeOf<A>()) as KSig<A>
context(ReflectedModule) inline fun <B, reified A: B> limit(x: KSet<B>): KSet<A> =
  KSet((x `&` set<A>()).expr)

context(ReflectedModule) inline fun <reified A> empty(): KFormula = empty(set<A>())
context(ReflectedModule) inline fun <reified A, F> empty(property: KProperty1<A, F>): KFormula = empty(field(property))
context(ReflectedModule) fun <F> empty(property: KProperty0<F>): KFormula = empty(global(property))

context(ReflectedModule) inline fun <reified A> atMostOne(): KFormula = atMostOne(set<A>())
context(ReflectedModule) inline fun <reified A, F> atMostOne(property: KProperty1<A, F>): KFormula = atMostOne(field(property))
context(ReflectedModule) fun <F> atMostOne(property: KProperty0<F>): KFormula = atMostOne(global(property))

context(ReflectedModule) inline fun <reified A, F> next(property: KProperty1<A, F>): KSet<Pair<A, F>> = next(field(property))
context(ReflectedModule) fun <F> next(property: KProperty0<F>): KSet<F> = next(global(property))

context(ReflectedModule) inline fun <reified A, F> current(property: KProperty1<A, F>): KSet<Pair<A, F>> = current(field(property))
context(ReflectedModule) fun <F> current(property: KProperty0<F>): KSet<F> = current(global(property))

context(ReflectedModule) inline fun <reified A, F> stays(property: KProperty1<A, F>): KFormula = stays(field(property))
context(ReflectedModule) fun <F> stays(property: KProperty0<F>): KFormula = stays(global(property))

context(ReflectedModule) inline fun <reified A> closure(p: KProperty1<A, A>): KRelation<A, A> = closure(field(p))
context(ReflectedModule) inline fun <reified A> closureOptional(p: KProperty1<A, A?>): KRelation<A, A> = closureOptional(field(p))
context(ReflectedModule) inline fun <reified A> oneOrMore(p: KProperty1<A, A?>): KRelation<A, A> = closureOptional(field(p))

context(ReflectedModule) inline fun <reified A> reflexiveClosure(p: KProperty1<A, A>): KRelation<A, A> = reflexiveClosure(field(p))
context(ReflectedModule) inline fun <reified A> reflexiveClosureOptional(p: KProperty1<A, A?>): KRelation<A, A> =
  reflexiveClosureOptional(field(p))
context(ReflectedModule) inline fun <reified A> zeroOrMore(p: KProperty1<A, A?>): KRelation<A, A> = reflexiveClosureOptional(field(p))

context(ReflectedModule) inline fun <reified A> `for`(
  op: ExprQt.Op,
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(op, x to set<A>(), block)

context(ReflectedModule) inline fun <reified A, reified B> `for`(
  op: ExprQt.Op,
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(op, x to set<A>(), y to set<B>(), block)

context(ReflectedModule) inline fun <reified A, reified B, reified C> `for`(
  op: ExprQt.Op,
  x: String,
  y: String,
  z: String,
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(op, x to set<A>(), y to set<B>(), z to set<C>(), block)

context(ReflectedModule) inline fun <reified A> forAll(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, block)

context(ReflectedModule) inline fun <reified A> forAll(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()), block)

context(ReflectedModule) inline fun <reified A> forAll(
  s: KSet<A>,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()) to s, block)

context(ReflectedModule) inline fun <reified A, reified B> forAll(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, y, block)

context(ReflectedModule) inline fun <reified A, reified B> forAll(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), block)

context(ReflectedModule) inline fun <reified A, reified B, reified C> forAll(
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), nextUnique(typeOf<C>()), block)

context(ReflectedModule) inline fun <reified A> forSome(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, block)

context(ReflectedModule) inline fun <reified A> forSome(
  s: KSet<A>,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()) to s, block)

context(ReflectedModule) inline fun <reified A> forSome(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()), block)

context(ReflectedModule) inline fun <reified A, reified B> forSome(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, y, block)

context(ReflectedModule) inline fun <reified A, reified B> forSome(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), block)

context(ReflectedModule) inline fun <reified A, reified B, reified C> forSome(
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), nextUnique(typeOf<C>()), block)

context(ReflectedModule) inline fun <reified A> forNo(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, nextUnique(typeOf<A>()), block)

context(ReflectedModule) inline fun <reified A, reified B> forNo(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), block)

context(ReflectedModule) inline fun <reified A, reified B, reified C> forNo(
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), nextUnique(typeOf<C>()), block)

context(ReflectedModule) inline fun <reified A> forOne(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ONE, nextUnique(typeOf<A>()), block)

context(ReflectedModule) inline fun <reified A, reified B> forOne(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ONE, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), block)

context(ReflectedModule) inline fun <reified A, reified B, reified C> forOne(
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(ExprQt.Op.ONE, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), nextUnique(typeOf<C>()), block)