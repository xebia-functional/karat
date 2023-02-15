package karat

/**
 * Generic builder for formulae which talk about [Subject],
 * may perform [Test] over it or the environment, and has
 * a hierarchy of [Atomic] and non-atomic [Formula].
 */
public interface FormulaBuilder<Subject, Test, Formula, Atomic : Formula> {
  public val `true`: Atomic
  public val `false`: Atomic
  public fun predicate(test: Test): Atomic
  public fun not(formula: Atomic): Formula
  public fun and(formulae: List<Formula>): Formula
  public fun or(formulae: List<Formula>): Formula
  public fun implies(condition: Atomic, then: Formula): Formula
  public fun next(formula: Formula): Formula
  public fun always(formula: Formula): Formula
  public fun eventually(formula: Formula): Formula
  public fun remember(block: (Subject) -> Formula): Formula

  public fun and(vararg formula: Formula): Formula =
    and(formula.toList())
  public fun or(vararg formula: Formula): Formula =
    or(formula.toList())

  public fun afterwards(block: Formula): Formula =
    next(always(block))
}

public fun <Subject, Test, Formula, Atomic : Formula, R> formula(
  builder: FormulaBuilder<Subject, Test, Formula, Atomic>,
  block: FormulaBuilder<Subject, Test, Formula, Atomic>.() -> R
): R = block(builder)