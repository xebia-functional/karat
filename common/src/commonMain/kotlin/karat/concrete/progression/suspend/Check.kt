package karat.concrete.progression.suspend

import karat.concrete.*
import karat.concrete.progression.*

public interface SuspendStepResultManager<A, R, E>: StepResultManager<A, R, E> {
  public suspend fun predicate(test: suspend (A) -> R, value: A): E
}

public suspend fun <A, R, E> SuspendStepResultManager<A, R, E>.checkAtomic(formula: Atomic<A, R>, x: A): E = when (formula) {
  is TRUE -> everythingOk
  is FALSE -> falseFormula
  is Predicate -> predicate(formula.test, x)
}

public suspend fun <A, R, E> SuspendStepResultManager<A, R, E>.check(formula: Formula<A, R>, x: A): FormulaStep<A, R, E> = when (formula) {
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
public fun <A, R, E> SuspendStepResultManager<A, R, E>.leftToProve(formula: Formula<A, R>): E = when (formula) {
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

public tailrec suspend fun <Action, State, Response, Test, Error>
  SuspendStepResultManager<Info<Action, State, Response>, Test, Error>.check(
  formula: Formula<Info<Action, State, Response>, Test>,
  actions: List<Action>,
  current: State,
  step: suspend (Action, State) -> Step<State, Response>?,
  previousActions: MutableList<Action> = mutableListOf()
): Problem<Action, State, Error>? = when {
  actions.isEmpty() -> problem(leftToProve(formula), previousActions, current)
  else -> {
    val action = actions.first()
    val oneStepFurther =
      step(action, current)?.let { Info(action, current, it.state, it.response) }
    when (oneStepFurther) {
      null -> problem(leftToProve(formula), previousActions, current)
      else -> {
        val progress = check(formula, oneStepFurther)
        when {
          !progress.result.isOk -> Problem(previousActions, current, progress.result)
          else -> check(progress.next, actions.drop(1), oneStepFurther.nextState, step, previousActions)
        }
      }
    }
  }
}
