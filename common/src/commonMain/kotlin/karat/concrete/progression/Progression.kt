package karat.concrete.progression

import karat.concrete.*

// Based on the Quickstrom paper
// https://arxiv.org/pdf/2203.11532.pdf,
// which is in turned based on formula progression
// https://users.cecs.anu.edu.au/~thiebaux/papers/icaps05.pdf

public interface StepResultManager<A, R, E> {
  public val everythingOk: E
  public val falseFormula: E
  public val unknown: E
  public fun negationWasTrue(formula: Formula<A, R>): E
  public fun shouldHoldEventually(formula: Formula<A, R>): E
  public val E.isOk: Boolean
  public fun andResults(results: List<E>): E
  public fun orResults(results: List<E>): E
  public suspend fun predicate(test: suspend (A) -> R, value: A): E
}

public data class FormulaStep<A, R, E>(val result: E, val next: Formula<A, R>)

public suspend fun <A, R, E> StepResultManager<A, R, E>.checkAtomic(formula: Atomic<A, R>, x: A): E = when (formula) {
  is TRUE -> everythingOk
  is FALSE -> falseFormula
  is Predicate -> predicate(formula.test, x)
}

public suspend fun <A, R, E> StepResultManager<A, R, E>.check(formula: Formula<A, R>, x: A): FormulaStep<A, R, E> = when (formula) {
  is Atomic ->
    FormulaStep(checkAtomic(formula, x), TRUE)
  is Not -> {
    if (checkAtomic(formula.formula, x).isOk) {
      FormulaStep(negationWasTrue(formula), TRUE)
    } else {
      FormulaStep(everythingOk, TRUE)
    }
  }
  is Remember -> check(formula.block(x), x)
  is And -> {
    val steps = formula.formulae.map { check(it, x) }
    val result = andResults(steps.map { it.result })
    FormulaStep(result, and(steps.map { it.next }))
  }
  is Or -> {
    val steps = formula.formulae.map { check(it, x) }
    val result = orResults(steps.map { it.result })
    FormulaStep(result, or(steps.map { it.next }))
  }
  is Implies -> {
    val leftResult = checkAtomic(formula.condition, x)
    if (leftResult.isOk) {
      // if left is true, we check the right
      check(formula.then, x)
    } else {
      // otherwise the formula is true (false => x == true)
      FormulaStep(everythingOk, TRUE)
    }
  }
  is Next -> FormulaStep(everythingOk, formula.formula)
  is Always -> {
    // when we have always it has to be true
    // 1. in this state,
    // 2. in any other next state
    val step = check(formula.formula, x)
    FormulaStep(step.result, step.next and formula)
  }
  is Eventually -> {
    val step = check(formula.formula, x)
    if (step.result.isOk) {
      // this one is true, so we're done
      FormulaStep(everythingOk, TRUE)
    } else {
      // we have to try in the next one
      // so if we are done we haven't proved it yet
      FormulaStep(everythingOk, formula)
    }
  }
}

// is there something missing to prove?
// if we have 'eventually', we cannot conclude
public fun <A, R, E> StepResultManager<A, R, E>.leftToProve(formula: Formula<A, R>): E = when (formula) {
  // atomic predicates are done
  is Atomic -> everythingOk
  is Not -> everythingOk
  // if there's some 'current', we can't really know...
  is Remember -> unknown
  // if we have 'and' and 'or', combine
  is And -> andResults(formula.formulae.map { leftToProve(it) })
  is Or -> orResults(formula.formulae.map { leftToProve(it) })
  is Implies -> andResults(listOf(leftToProve(formula.condition), leftToProve(formula.then)))
  // we have nothing missing here
  is Next -> everythingOk
  is Always -> everythingOk
  // we have an 'eventually' missing
  is Eventually -> shouldHoldEventually(formula.formula)
}
