package karat.turbine

import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import karat.concrete.Atomic
import karat.concrete.Formula
import karat.concrete.progression.suspend.SuspendStepResultManager
import karat.concrete.progression.suspend.check
import karat.concrete.progression.suspend.leftToProve
import karat.kotlin.test.KotlinTestStepResultManager
import karat.kotlin.test.StateAssertionError
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

public typealias TurbineAtomic<A> = Atomic<Result<A>, Unit>
public typealias TurbineFormula<A> = Formula<Result<A>, Unit>

/**
 * Checks that the formula holds over the [Flow].
 */
public suspend fun <A> Flow<A>.testFormula(
  timeout: Duration? = null,
  name: String? = null,
  block: () -> TurbineFormula<A>
) {
  this.test(timeout, name) { formula(block) }
}

/**
 * Checks that the formula holds. This version can be used inside Turbine's [test].
 */
public suspend fun <A> ReceiveTurbine<A>.formula(block: () -> TurbineFormula<A>) {
  formula(block())
}

/**
 * Required until we get context receivers.
 */
private class TurbineStepResultManager<A>(
  val turbine: ReceiveTurbine<A>,
  val manager: KotlinTestStepResultManager<Result<A>>
): ReceiveTurbine<A> by turbine, SuspendStepResultManager<Result<A>, Unit, List<AssertionError>?> by manager

public suspend fun <A> ReceiveTurbine<A>.formula(
  formula: TurbineFormula<A>
) {
  with(TurbineStepResultManager(this, KotlinTestStepResultManager())) {
    when (val result = checkFormula(formula)) {
      null -> { /* everything ok */ }
      else -> throw StateAssertionError(result.first, result.second)
    }
  }
}

private tailrec suspend fun <A> TurbineStepResultManager<A>.checkFormula(
  formula: TurbineFormula<A>
): Pair<Event<A>, List<AssertionError>>? =
  when (val e = awaitEvent()) {
    is Event.Complete -> {
      leftToProve(formula)?.let { e to it }
    }
    is Event.Item -> {
      val step = check(formula, Result.success(e.value))
      when {
        !step.result.isOk -> step.result?.let { e to it }
        else -> checkFormula(step.next)
      }
    }
    is Event.Error -> {
      val step = check(formula, Result.failure(e.throwable))
      when {
        !step.result.isOk -> step.result?.let { e to it }
        else -> leftToProve(formula)?.let { e to it }
      }
    }
  }
