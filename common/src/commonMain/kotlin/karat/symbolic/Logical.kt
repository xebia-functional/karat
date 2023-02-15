package karat.symbolic

import karat.FormulaBuilder
import karat.TraceFormulaBuilder
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

public operator fun Formula.not(): Formula = Not(this)

public fun and(formulae: List<Formula>): Formula =
  if (formulae.isEmpty()) TRUE else And(formulae)
public fun and(vararg formula: Formula): Formula = And(formula.toList())
public infix fun Formula.and(other: Formula): Formula = and(this, other)
public fun or(formulae: List<Formula>): Formula =
  if (formulae.isEmpty()) FALSE else Or(formulae)
public fun or(vararg formula: Formula): Formula = Or(formula.toList())
public infix fun Formula.or(other: Formula): Formula = or(this, other)

public infix fun Formula.implies(then: Formula): Implies = Implies(this, then)
public infix fun Formula.iff(other: Formula): Formula = Iff(this, other)
public infix fun Implies.orElse(orElse: Formula): Formula = IfThenElse(this.condition, this.then, orElse)
public fun Formula.ifThenElse(then: Formula, orElse: Formula): Formula = IfThenElse(this, then, orElse)

public fun <A> current(x: Expr<A>): Expr<A> = x
public fun <A> current(prop: KProperty0<A>): Expr<A> = current(field(prop))
public fun <A> next(x: Expr<A>): Expr<A> = Next(x)
public fun <A> next(prop: KProperty0<A>): Expr<A> = next(field(prop))
public fun <A> stays(e: Expr<A>): Formula = next(e) `==` current(e)
public inline fun <reified A> stays(): Formula = stays(set<A>())
public fun <A> stays(prop: KProperty0<A>): Formula = stays(field(prop))
public inline fun <reified A, F> stays(prop: KProperty1<A, F>): Formula = stays(field(prop))

public fun always(formula: Formula): Formula = Always(formula)
public fun always(formula: () -> Formula): Formula = always(formula())
public fun eventually(formula: Formula): Formula = Eventually(formula)
public fun eventually(formula: () -> Formula): Formula = eventually(formula())
public fun historically(formula: Formula): Formula = Historically(formula)
public fun historically(formula: () -> Formula): Formula = historically(formula())
public fun neverBefore(formula: Formula): Formula = historically(!formula)
public fun neverBefore(formula: () -> Formula): Formula = neverBefore(formula())
public fun after(formula: Formula): Formula = After(formula)
public fun after(formula: () -> Formula): Formula = after(formula())
public fun before(formula: Formula): Formula = Before(formula)
public fun before(formula: () -> Formula): Formula = before(formula())
public fun once(formula: Formula): Formula = Once(formula)
public fun once(formula: () -> Formula): Formula = once(formula())

public infix fun Formula.until(other: Formula): Formula = Until(this, other)
public infix fun Formula.releases(other: Formula): Formula = Releases(this, other)
public infix fun Formula.since(other: Formula): Formula = Since(this, other)
public infix fun Formula.triggered(other: Formula): Formula = Triggered(this, other)

internal object SymbolicFormulaBuilder: FormulaBuilder<Nothing, Formula, Formula, Formula> {
  override val `true`: Formula = TRUE
  override val `false`: Formula = FALSE
  override fun predicate(test: Formula): Formula = test
  override fun not(formula: Formula): Formula = !formula
  override fun and(formulae: List<Formula>): Formula = karat.symbolic.and(formulae)
  override fun or(formulae: List<Formula>): Formula = karat.symbolic.or(formulae)
  override fun implies(condition: Formula, then: Formula): Formula = condition implies then
  override fun next(formula: Formula): Formula = throw IllegalStateException("not implemented yet")
  override fun remember(block: (Nothing) -> Formula): Formula = throw IllegalStateException("nothing to remember")
  override fun always(formula: Formula): Formula = karat.symbolic.always(formula)
  override fun eventually(formula: Formula): Formula = karat.symbolic.eventually(formula)
}

public fun trace(
  block: suspend TraceFormulaBuilder<Nothing, Formula>.() -> Unit
): Formula = karat.trace(SymbolicFormulaBuilder, block)