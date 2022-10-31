package fp.serrano.karat

// temporal formulae

fun <A> next(e: KSet<A>): KSet<A> =
  KSet(e.expr.prime())

fun always(formula: KFormula): KFormula =
  KFormula(formula.expr.always())

fun always(formula: () -> KFormula): KFormula =
  always(formula())

fun eventually(formula: KFormula): KFormula =
  KFormula(formula.expr.eventually())

fun eventually(formula: () -> KFormula): KFormula =
  eventually(formula())

fun before(formula: KFormula): KFormula =
  KFormula(formula.expr.before())

fun before(formula: () -> KFormula): KFormula =
  before(formula())

fun once(formula: KFormula): KFormula =
  KFormula(formula.expr.once())

fun once(formula: () -> KFormula): KFormula =
  once(formula())

infix fun KFormula.until(other: KFormula) =
  KFormula(this.expr.until(other.expr))

infix fun KFormula.releases(other: KFormula) =
  KFormula(this.expr.releases(other.expr))

infix fun KFormula.since(other: KFormula) =
  KFormula(this.expr.since(other.expr))

infix fun KFormula.triggered(other: KFormula) =
  KFormula(this.expr.triggered(other.expr))