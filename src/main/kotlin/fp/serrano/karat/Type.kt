package fp.serrano.karat

import edu.mit.csail.sdg.ast.Expr

open class KSetOrRelationOrMultiplicityConstraint(expr: Expr): KExpr(expr)
open class KSetOrRelation(expr: Expr): KSetOrRelationOrMultiplicityConstraint(expr)
class KMultiplicityConstraint(expr: Expr): KSetOrRelationOrMultiplicityConstraint(expr)

// functions to create formulae

fun no(sr: KSetOrRelation): KFormula =
  KFormula(sr.expr.no())

fun some(sr: KSetOrRelation): KFormula =
  KFormula(sr.expr.some())

fun lone(sr: KSetOrRelation): KFormula =
  KFormula(sr.expr.lone())

fun one(sr: KSetOrRelation): KFormula =
  KFormula(sr.expr.one())

// functions to create multiplicity constraints

infix fun KSetOrRelation.any_arrow_some(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.any_arrow_some(other.expr))

infix fun KSetOrRelation.any_arrow_one(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.any_arrow_one(other.expr))

infix fun KSetOrRelation.any_arrow_lone(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.any_arrow_lone(other.expr))

infix fun KSetOrRelation.some_arrow_any(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.any_arrow_some(other.expr))

infix fun KSetOrRelation.some_arrow_some(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.any_arrow_some(other.expr))

infix fun KSetOrRelation.some_arrow_one(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.any_arrow_some(other.expr))

infix fun KSetOrRelation.some_arrow_lone(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.some_arrow_lone(other.expr))

infix fun KSetOrRelation.one_arrow_any(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.one_arrow_any(other.expr))

infix fun KSetOrRelation.one_arrow_some(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.one_arrow_some(other.expr))

infix fun KSetOrRelation.one_arrow_one(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.one_arrow_one(other.expr))

infix fun KSetOrRelation.one_arrow_lone(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.one_arrow_lone(other.expr))

infix fun KSetOrRelation.lone_arrow_any(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.lone_arrow_any(other.expr))

infix fun KSetOrRelation.lone_arrow_some(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.lone_arrow_some(other.expr))

infix fun KSetOrRelation.lone_arrow_one(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.lone_arrow_one(other.expr))

infix fun KSetOrRelation.lone_arrow_lone(other: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(this.expr.lone_arrow_lone(other.expr))

fun someOf(sr: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(sr.expr.someOf())

fun loneOf(sr: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(sr.expr.loneOf())

fun oneOf(sr: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(sr.expr.oneOf())

fun setOf(sr: KSetOrRelation): KMultiplicityConstraint =
  KMultiplicityConstraint(sr.expr.setOf())