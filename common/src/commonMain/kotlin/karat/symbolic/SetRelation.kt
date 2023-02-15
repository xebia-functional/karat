package karat.symbolic

import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

public inline fun <reified T> set(): Expr<T> =
  TypeSet(typeOf<T>())
public inline fun <reified A, B> field(property: KProperty1<A, B>): Relation<A, B> =
  FieldRelation(typeOf<A>(), property)
public fun <A> field(property: KProperty0<A>): Expr<A> =
  GlobalField(property)

public infix fun <A> Expr<A>.`==`(other: Expr<A>): Formula =
  Equals(this, other)
public infix fun <A> Expr<A>.`in`(other: Expr<A>): Formula =
  In(this, other)
public infix fun <A> Expr<A>.`!=`(other: Expr<A>): Formula =
  Not(Equals(this, other))

public operator fun <A, B> Expr<A>.div(r: Relation<A, B>): Expr<B> =
  JoinSetRel(this, r)
public inline operator fun <reified A, B> Expr<A>.div(r: KProperty1<A, B>): Expr<B> =
  JoinSetRel(this, field(r))
public operator fun <A, B> Relation<A, B>.rem(s: Expr<B>): Expr<A> =
  JoinRelSet(this, s)
public inline operator fun <reified A, B> KProperty1<A, B>.rem(s: Expr<B>): Expr<A> =
  JoinRelSet(field(this), s)
public infix fun <A, B, C> Relation<A, B>.join(next: Relation<B, C>): Relation<A, C> =
  JoinRelRel(this, next)
public inline infix fun <A, reified B, C> Relation<A, B>.join(next: KProperty1<B, C>): Relation<A, C> =
  JoinRelRel(this, field(next))

public fun <A> Expr<A>.count(): Expr<Int> = Cardinality(this)

public infix fun <A> Expr<A>.union(x: Expr<A>): Expr<A> = Union(this, x)
public operator fun <A> Expr<A>.plus(x: Expr<A>): Expr<A> = Union(this, x)

public fun <A> Expr<A>.with(x: Expr<A>): Expr<A> = Override(this, x)

public infix fun <A> Expr<A>.diff(x: Expr<A>): Expr<A> = Minus(this, x)
public operator fun <A> Expr<A>.minus(x: Expr<A>): Expr<A> = Minus(this, x)

public infix fun <A> Expr<A>.intersect(x: Expr<A>): Expr<A> = Intersect(this, x)

public infix fun <A, B> Expr<A>.to(x: Expr<B>): Expr<Pair<A, B>> = Product(this, x)
public operator fun <A, B> Expr<A>.times(x: Expr<B>): Expr<Pair<A, B>> = Product(this, x)

public fun <R, A> Expr<R>.flatten(f: Flattener<R, A>): Expr<A> = Flatten(this, f)
public val <A, B> Expr<Map<A, B>>.entries: Relation<A, B>
  get() = Flatten(this, Flattener.Map())
public fun <T, R, A> Relation<T, R>.map(f: Flattener<R, A>): Relation<T, A> =
  Flatten(this, Flattener.Pair(f))

public fun <A, B> Relation<A, B>.domain(): Expr<A> = Domain(this)
public fun <A, B> Relation<A, B>.range(): Expr<B> = Range(this)
public fun <A, B> Relation<A, B>.transpose(): Relation<B, A> = Transpose(this)
public fun <A> Relation<A, A>.oneOrMoreSteps(): Relation<A, A> = Closure(this)
public inline fun <reified A> KProperty1<A, A>.oneOrMoreSteps(): Relation<A, A> = Closure(field(this))
public fun <A> Relation<A, A>.zeroOrMoreSteps(): Relation<A, A> = ReflexiveClosure(this)
public inline fun <reified A> KProperty1<A, A>.zeroOrMoreSteps(): Relation<A, A> = ReflexiveClosure(field(this))