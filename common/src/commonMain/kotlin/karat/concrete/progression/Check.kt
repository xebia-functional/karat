package karat.concrete.progression

import karat.concrete.Formula
import kotlinx.coroutines.runBlocking

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
}

public data class FormulaStep<A, R, E>(val result: E, val next: Formula<A, R>)

public data class Step<out State, out Response>(
  val state: State,
  val response: Response
)
public data class Info<out Action, out State, out Response>(
  val action: Action,
  val previousState: State,
  val nextState: State,
  val response: Response
)

public data class Problem<Action, State, Error>(
  val actions: List<Action>,
  val state: State,
  val error: Error
)

internal fun <Info, Action, State, Test, Error> StepResultManager<Info, Test, Error>.problem(
  error: Error,
  actions: List<Action>,
  state: State,
): Problem<Action, State, Error>? =
  if (error.isOk) null else Problem(actions, state, error)
