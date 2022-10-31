package fp.serrano.karat

import edu.mit.csail.sdg.ast.Expr

// this represent a set of element, possibly with a multiplicity
open class KSet<A>(expr: Expr): KExpr<Set<A>>(expr)
// a relation is a set of tuples
// (unfortunately we cannot flatten them as in Alloy)
open class KRelation<A, B>(expr: Expr): KSet<Pair<A, B>>(expr)

// functions to create formulae

fun <A> no(sr: KSet<A>): KFormula = KFormula(sr.expr.no())

fun <A> some(sr: KSet<A>): KFormula = KFormula(sr.expr.some())

fun <A> lone(sr: KSet<A>): KFormula = KFormula(sr.expr.lone())

fun <A> one(sr: KSet<A>): KFormula = KFormula(sr.expr.one())

// joins

operator fun <A, B> KSet<A>.div(other: KRelation<A, B>): KSet<B> =
  KSet(this.expr.join(other.expr))

operator fun <A, B, C> KRelation<A, B>.div(other: KRelation<B, C>): KRelation<A, C> =
  KRelation(this.expr.join(other.expr))

operator fun <A, B> KRelation<A, B>.div(other: KSet<B>): KSet<A> =
  KSet(this.expr.join(other.expr))

// functions to create relations

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