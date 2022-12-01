package fp.serrano.karat.ast

import edu.mit.csail.sdg.ast.Expr
import fp.serrano.karat.ReflectedModule
import kotlin.reflect.*

// this represent a set of element, possibly with a multiplicity
open class KSet<out A>(expr: Expr): KExpr<Set<A>>(expr)
// a relation is a set of tuples
// (unfortunately we cannot flatten them as in Alloy)
open class KRelation<out A, out B>(expr: Expr): KSet<Pair<A, B>>(expr)

// functions to create formulae

fun <A> no(sr: KSet<A>): KFormula = KFormula(sr.expr.no())

fun <A> some(sr: KSet<A>): KFormula = KFormula(sr.expr.some())

fun <A> lone(sr: KSet<A>): KFormula = KFormula(sr.expr.lone())

fun <A> one(sr: KSet<A>): KFormula = KFormula(sr.expr.one())

// basic set operations

fun <A> cardinality(sr: KSet<A>): KExpr<Int> =
  KExpr(sr.expr.cardinality())

operator fun <A> KSet<A>.plus(other: KSet<A>): KSet<A> =
  KSet(this.expr.plus(other.expr))

infix fun <A> KSet<A>.union(other: KSet<A>): KSet<A> =
  this + other

operator fun <A> KSet<A>.minus(other: KSet<A>): KSet<A> =
  KSet(this.expr.minus(other.expr))

infix fun <A> KSet<A>.diff(other: KSet<A>): KSet<A> =
  this - other

infix fun <A> KSet<A>.`&`(other: KSet<A>): KSet<A> =
  KSet(this.expr.intersect(other.expr))

infix fun <A> KSet<A>.intersect(other: KSet<A>): KSet<A> =
  this `&` other

operator fun <A, B> KSet<A>.times(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.product(other.expr))

infix fun <A, B> KSet<A>.cartesianProduct(other: KSet<B>): KRelation<A, B> =
  this * other

fun <A, B> transpose(sr: KRelation<A, B>): KRelation<B, A> =
  KRelation(sr.expr.transpose())

fun <A> closure(sr: KRelation<A, A>): KRelation<A, A> =
  KRelation(sr.expr.closure())

fun <A> ReflectedModule.closure(p: KProperty1<A, A>): KRelation<A, A> =
  closure(field(p))

fun <A> reflexiveClosure(sr: KRelation<A, A>): KRelation<A, A> =
  KRelation(sr.expr.reflexiveClosure())

fun <A> reflexiveClosureOptional(sr: KRelation<A, A?>): KRelation<A, A> =
  KRelation(sr.expr.reflexiveClosure())

fun <A> ReflectedModule.reflexiveClosure(p: KProperty1<A, A>): KRelation<A, A> =
  reflexiveClosure(field(p))

fun <A> ReflectedModule.reflexiveClosureOptional(p: KProperty1<A, A?>): KRelation<A, A> =
  reflexiveClosureOptional(field(p))

// joins

operator fun <A, B> KSet<A>.div(other: KRelation<A, B>): KSet<B> =
  KSet(this.expr.join(other.expr))

operator fun <A, B, C> KRelation<A, B>.div(other: KRelation<B, C>): KRelation<A, C> =
  KRelation(this.expr.join(other.expr))

operator fun <A, B> KRelation<A, B>.div(other: KSet<B>): KSet<A> =
  KSet(this.expr.join(other.expr))

operator fun <A, B> KRelation<A, B>.get(other: KSet<A>): KSet<B> =
  other / this

// functions to create relations

infix fun <A, B> KSet<A>.`--#`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.product(other.expr))

infix fun <A, B> KSet<A>.`any --# some`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.any_arrow_some(other.expr))

infix fun <A, B> KSet<A>.`any --# one`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.any_arrow_one(other.expr))

infix fun <A, B> KSet<A>.`any --# lone`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.any_arrow_lone(other.expr))

infix fun <A, B> KSet<A>.`some --# any`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.some_arrow_any(other.expr))

infix fun <A, B> KSet<A>.`some --# some`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.some_arrow_some(other.expr))

infix fun <A, B> KSet<A>.`some --# one`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.some_arrow_one(other.expr))

infix fun <A, B> KSet<A>.`some --# lone`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.some_arrow_lone(other.expr))

infix fun <A, B> KSet<A>.`one --# any`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.one_arrow_any(other.expr))

infix fun <A, B> KSet<A>.`one --# some`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.one_arrow_some(other.expr))

infix fun <A, B> KSet<A>.`one --# one`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.one_arrow_one(other.expr))

infix fun <A, B> KSet<A>.`one --# lone`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.one_arrow_lone(other.expr))

infix fun <A, B> KSet<A>.`lone --# any`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.lone_arrow_any(other.expr))

infix fun <A, B> KSet<A>.`lone --# some`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.lone_arrow_some(other.expr))

infix fun <A, B> KSet<A>.`lone --# one`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.lone_arrow_one(other.expr))

infix fun <A, B> KSet<A>.`lone --# lone`(other: KSet<B>): KRelation<A, B> =
  KRelation(this.expr.lone_arrow_lone(other.expr))

// functions to create multiplicity constraints

fun <A> someOf(sr: KSet<A>): KSet<A> = KSet(sr.expr.someOf())

fun <A> loneOf(sr: KSet<A>): KSet<A> = KSet(sr.expr.loneOf())

fun <A> oneOf(sr: KSet<A>): KSet<A> = KSet(sr.expr.oneOf())

fun <A> setOf(sr: KSet<A>): KSet<A> = KSet(sr.expr.setOf())