package fp.serrano.karat.ast

infix fun KSet<Int>.gt(other: KSet<Int>): KFormula =
  KFormula(this.expr.gt(other.expr))

infix fun KSet<Int>.gte(other: KSet<Int>): KFormula =
  KFormula(this.expr.gte(other.expr))

infix fun KSet<Int>.lt(other: KSet<Int>): KFormula =
  KFormula(this.expr.lt(other.expr))

infix fun KSet<Int>.lte(other: KSet<Int>): KFormula =
  KFormula(this.expr.lte(other.expr))