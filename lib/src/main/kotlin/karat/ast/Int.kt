package karat.ast

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.ExprConstant

fun literal(n: Int): KSet<Int> =
  KSet(ExprConstant.Op.NUMBER.make(Pos.UNKNOWN, n))

// KSet vs. KSet

infix fun KSet<Int>.gt(other: KSet<Int>): KFormula =
  KFormula(this.expr.gt(other.expr))

infix fun KSet<Int>.gte(other: KSet<Int>): KFormula =
  KFormula(this.expr.gte(other.expr))

infix fun KSet<Int>.lt(other: KSet<Int>): KFormula =
  KFormula(this.expr.lt(other.expr))

infix fun KSet<Int>.lte(other: KSet<Int>): KFormula =
  KFormula(this.expr.lte(other.expr))

// KExpr vs. Int

infix fun KSet<Int>.gt(other: Int): KFormula =
  this gt literal(other)

infix fun KSet<Int>.gte(other: Int): KFormula =
  this gte literal(other)

infix fun KSet<Int>.lt(other: Int): KFormula =
  this lt literal(other)

infix fun KSet<Int>.lte(other: Int): KFormula =
  this lte literal(other)