package fp.serrano.karat

import edu.mit.csail.sdg.ast.Expr

open class KSetOrRelation<M: TMultiplicity>(expr: Expr) : KExpr<M>(expr)

interface TMultiplicity
interface TSet<A> : TMultiplicity
interface TRelation<R : TMultiplicity, S : TMultiplicity> : TMultiplicity

open class Set<R: TMultiplicity>(expr: Expr): KSetOrRelation<R>(expr)
open class Relation<R: TMultiplicity, S: TMultiplicity>(expr: Expr): KSetOrRelation<TRelation<R, S>>(expr)

// functions to create formulae

fun <A: TMultiplicity> no(sr: KSetOrRelation<A>): KFormula =
  KFormula(sr.expr.no())

fun <A: TMultiplicity> some(sr: KSetOrRelation<A>): KFormula =
  KFormula(sr.expr.some())

fun <A: TMultiplicity> lone(sr: KSetOrRelation<A>): KFormula =
  KFormula(sr.expr.lone())

fun <A: TMultiplicity> one(sr: KSetOrRelation<A>): KFormula =
  KFormula(sr.expr.one())

// joins

operator fun <A, B: TMultiplicity> KExpr<TSet<A>>.div(
  other: Relation<TSet<A>, B>
): Set<B> = Set(this.expr.join(other.expr))

operator fun <A: TMultiplicity, B: TMultiplicity, C: TMultiplicity> Relation<A, B>.div(
  other: Relation<B, C>
): Relation<A, C> = Relation(this.expr.join(other.expr))

operator fun <A: TMultiplicity, B> Relation<A, TSet<B>>.div(
  other: KExpr<TSet<B>>
): Set<A> = Set(this.expr.join(other.expr))

// functions to create multiplicity constraints

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`any --# some`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.any_arrow_some(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`any --# one`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.any_arrow_one(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`any --# lone`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.any_arrow_lone(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`some --# any`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.some_arrow_any(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`some --# some`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.some_arrow_some(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`some --# one`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.some_arrow_one(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`some --# lone`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.some_arrow_lone(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`one --# any`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.one_arrow_any(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`one --# some`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.one_arrow_some(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`one --# one`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.one_arrow_one(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`one --# lone`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.one_arrow_lone(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`lone --# any`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.lone_arrow_any(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`lone --# some`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.lone_arrow_some(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`lone --# one`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.lone_arrow_one(other.expr))

infix fun <A: TMultiplicity, B: TMultiplicity> KSetOrRelation<A>.`lone --# lone`(other: KSetOrRelation<B>): Relation<A, B> =
  Relation(this.expr.lone_arrow_lone(other.expr))

fun <A: TMultiplicity> someOf(sr: KSetOrRelation<A>): Set<A> =
  Set(sr.expr.someOf())

fun <A: TMultiplicity> loneOf(sr: KSetOrRelation<A>): Set<A> =
  Set(sr.expr.loneOf())

fun <A: TMultiplicity> oneOf(sr: KSetOrRelation<A>): Set<A> =
  Set(sr.expr.oneOf())

fun <A: TMultiplicity> setOf(sr: KSetOrRelation<A>): Set<A> =
  Set(sr.expr.setOf())