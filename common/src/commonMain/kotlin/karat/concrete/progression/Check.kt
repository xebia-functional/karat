package karat.concrete.progression

import karat.concrete.Formula
import kotlinx.coroutines.runBlocking

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

public fun <Action, State, Response, Test, Error>
  StepResultManager<Result<Info<Action, State, Response>>, Test, Error>.check(
  formula: Formula<Result<Info<Action, State, Response>>, Test>,
  actions: List<Action>,
  current: State,
  step: (Action, State) -> Step<State, Response>,
  previousActions: MutableList<Action> = mutableListOf()
): Problem<Action, State, Error>? = runBlocking {
  check(formula, actions, current, step, previousActions)
}

public tailrec suspend fun <Action, State, Response, Test, Error>
  StepResultManager<Result<Info<Action, State, Response>>, Test, Error>.checkSuspend(
  formula: Formula<Result<Info<Action, State, Response>>, Test>,
  actions: List<Action>,
  current: State,
  step: suspend (Action, State) -> Step<State, Response>,
  previousActions: MutableList<Action> = mutableListOf()
): Problem<Action, State, Error>? = when {
  actions.isEmpty() -> problem(leftToProve(formula), previousActions, current)
  else -> {
    val action = actions.first()
    val oneStepFurther = runCatching { step(action, current) }.map { Info(action, current, it.state, it.response) }
    val progress = check(formula, oneStepFurther)
    val next = oneStepFurther.getOrNull()
    previousActions.add(action)
    when {
      !progress.result.isOk -> Problem(previousActions, current, progress.result)
      next == null -> problem(leftToProve(progress.next), previousActions, current)
      else -> checkSuspend(progress.next, actions.drop(1), next.nextState, step, previousActions)
    }
  }
}

private fun <Info, Action, State, Test, Error> StepResultManager<Info, Test, Error>.problem(
  error: Error,
  actions: List<Action>,
  state: State,
): Problem<Action, State, Error>? =
  if (error.isOk) null else Problem(actions, state, error)
