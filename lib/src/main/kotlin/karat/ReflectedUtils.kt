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

interface ReflectedModule {
  fun set(type: KType): KSig<*>
  fun <A, F> field(property: KProperty1<A, F>): KField<A, F>
  fun <F> global(property: KProperty0<F>): KSet<F>

  operator fun <A, B> KSet<A>.div(other: KProperty1<A, B>): KSet<B> =
    this / field(other)
  infix fun <A, B, C> KProperty1<A, B>.join(other: KProperty1<B, C>): KRelation<A, C> =
    field(this) join field(other)
  operator fun <A, B> KProperty1<A, B>.rem(other: KSet<B>): KSet<A> =
    field(this) % other
  operator fun <A: Any, F> KProperty1<A, F>.get(other: KSet<A>): KSet<F> =
    field(this)[other]

  val <A, B> KProperty1<A, Set<B>>.flattenR: KRelation<A, B>
    get() = field(this).flattenR
  val <A, B, C> KProperty1<A, Map<B, C>>.asRelationR: KRelation<A, Pair<B, C>>
    get() = field(this).asRelationR
  val <A, B, C> KProperty1<A, Map<B, Set<C>>>.asRelationSetR: KRelation<A, Pair<B, C>>
    get() = field(this).asRelationSetR
  val <A, B, C> KProperty1<A, Map<B, C?>>.asRelationNullableR: KRelation<A, Pair<B, C>>
    get() = field(this).asRelationNullableR

  infix fun <A: Any, B: A> KSet<A>.`==`(other: KType): KFormula =
    this `==` set(other)

  fun nextUnique(type: KType): String
}

// indicates a global fact
interface Fact: ReflectedModule
// indicates a fact which applies to each instance of the class
interface InstanceFact<A>: ReflectedModule {
  val self: KThis<A>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified A: Any> ReflectedModule.set(): KSig<A> =
  set(typeOf<A>()) as KSig<A>
@Suppress("UNCHECKED_CAST")
inline fun <reified A: Any> ReflectedModule.element(): KSig<A> =
  set(typeOf<A>()) as KSig<A>
inline fun <B: Any, reified A: B> ReflectedModule.limit(x: KSet<B>): KSet<A> =
  KSet((x `&` set<A>()).expr)

inline fun <reified A: Any> ReflectedModule.empty(): KFormula = empty(set<A>())
fun <A, F> ReflectedModule.empty(property: KProperty1<A, F>): KFormula = empty(field(property))
fun <F> ReflectedModule.empty(property: KProperty0<F>): KFormula = empty(global(property))

inline fun <reified A: Any> ReflectedModule.atMostOne(): KFormula = atMostOne(set<A>())
fun <A, F> ReflectedModule.atMostOne(property: KProperty1<A, F>): KFormula = atMostOne(field(property))
fun <F> ReflectedModule.atMostOne(property: KProperty0<F>): KFormula = atMostOne(global(property))

fun <A, F> ReflectedModule.next(property: KProperty1<A, F>): KSet<Pair<A, F>> = next(field(property))
fun <F> ReflectedModule.next(property: KProperty0<F>): KSet<F> = next(global(property))

fun <A, F> ReflectedModule.current(property: KProperty1<A, F>): KSet<Pair<A, F>> = current(field(property))
fun <F> ReflectedModule.current(property: KProperty0<F>): KSet<F> = current(global(property))

fun <A, F> ReflectedModule.stays(property: KProperty1<A, F>): KFormula = stays(field(property))
fun <F> ReflectedModule.stays(property: KProperty0<F>): KFormula = stays(global(property))

fun <A> ReflectedModule.closure(p: KProperty1<A, A>): KRelation<A, A> = closure(field(p))
fun <A> ReflectedModule.closureOptional(p: KProperty1<A, A?>): KRelation<A, A> = closureOptional(field(p))
fun <A> ReflectedModule.oneOrMore(p: KProperty1<A, A?>): KRelation<A, A> = closureOptional(field(p))

fun <A> ReflectedModule.reflexiveClosure(p: KProperty1<A, A>): KRelation<A, A> = reflexiveClosure(field(p))
fun <A> ReflectedModule.reflexiveClosureOptional(p: KProperty1<A, A?>): KRelation<A, A> =
  reflexiveClosureOptional(field(p))
fun <A> ReflectedModule.zeroOrMore(p: KProperty1<A, A?>): KRelation<A, A> = reflexiveClosureOptional(field(p))

inline fun <reified A: Any> ReflectedModule.`for`(
  op: ExprQt.Op,
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(op, x to set<A>(), block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.`for`(
  op: ExprQt.Op,
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(op, x to set<A>(), y to set<B>(), block)

inline fun <reified A: Any, reified B: Any, reified C: Any> ReflectedModule.`for`(
  op: ExprQt.Op,
  x: String,
  y: String,
  z: String,
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(op, x to set<A>(), y to set<B>(), z to set<C>(), block)

inline fun <reified A: Any> ReflectedModule.forAll(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, block)

inline fun <reified A: Any> ReflectedModule.forAll(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()), block)

inline fun <reified A: Any> ReflectedModule.forAll(
  s: KSet<A>,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()) to s, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forAll(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, y, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forAll(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), block)

inline fun <reified A: Any, reified B: Any, reified C: Any> ReflectedModule.forAll(
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), nextUnique(typeOf<C>()), block)

inline fun <reified A: Any> ReflectedModule.forSome(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, block)

inline fun <reified A: Any> ReflectedModule.forSome(
  s: KSet<A>,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()) to s, block)

inline fun <reified A: Any> ReflectedModule.forSome(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()), block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forSome(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, y, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forSome(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), block)

inline fun <reified A: Any, reified B: Any, reified C: Any> ReflectedModule.forSome(
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), nextUnique(typeOf<C>()), block)

inline fun <reified A: Any> ReflectedModule.forNo(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, nextUnique(typeOf<A>()), block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forNo(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), block)

inline fun <reified A: Any, reified B: Any, reified C: Any> ReflectedModule.forNo(
  noinline block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, nextUnique(typeOf<A>()), nextUnique(typeOf<B>()), nextUnique(typeOf<C>()), block)