package karat

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

public fun <Subject, Test, Formula, Atomic : Formula> trace(
  builder: FormulaBuilder<Subject, Test, Formula, Atomic>,
  block: suspend TraceFormulaBuilder<Subject, Test, Formula, Atomic>.() -> Unit
): Formula {
  val machine = TraceFormulaBuilderImpl<Subject, Test, Formula, Atomic>(builder)
  block.startCoroutine(
    machine,
    Continuation(EmptyCoroutineContext) { machine.done() }
  )
  return builder.always(machine.execute())
}

/**
 * Generic builder for sequential traces of formulae.
 * Use [trace] to turn those into regular formula
 */
public interface TraceFormulaBuilder<Subject, Test, Formula, Atomic : Formula>
  : FormulaBuilder<Subject, Test, Formula, Atomic> {
  public suspend fun remember(): Subject
  public suspend fun whenCurrent(test: Test)
  public suspend fun oneStep()
  public suspend fun zeroOrMoreSteps()
  public suspend fun oneOrMoreSteps() {
    oneStep()
    zeroOrMoreSteps()
  }
  public suspend fun checkCurrent(test: Test)
}

private class TraceFormulaBuilderImpl<Subject, Test, Formula, Atomic : Formula>(
  private val builder: FormulaBuilder<Subject, Test, Formula, Atomic>,
): TraceFormulaBuilder<Subject, Test, Formula, Atomic>, FormulaBuilder<Subject, Test, Formula, Atomic> by builder {
  sealed interface State<Subject, Test, Formula, Atomic> {
    data class Remember<Subject, Test, Formula, Atomic>(
      val continuation: Continuation<Subject>
    ): State<Subject, Test, Formula, Atomic>
    data class WhenCurrent<Subject, Test, Formula, Atomic>(
      val test: Test,
      val continuation: Continuation<Unit>
    ): State<Subject, Test, Formula, Atomic>
    data class OneStep<Subject, Test, Formula, Atomic>(
      val continuation: Continuation<Unit>
    ): State<Subject, Test, Formula, Atomic>
    data class ZeroOrMoreSteps<Subject, Test, Formula, Atomic>(
      val continuation: Continuation<Unit>
    ): State<Subject, Test, Formula, Atomic>
    data class CheckCurrent<Subject, Test, Formula, Atomic>(
      val test: Test,
      val continuation: Continuation<Unit>
    ): State<Subject, Test, Formula, Atomic>
  }

  private var current: State<Subject, Test, Formula, Atomic>? = null

  fun done() {
    current = null
  }
  override suspend fun remember(): Subject = suspendCoroutine {
    current = State.Remember(it)
  }
  override suspend fun whenCurrent(test: Test) = suspendCoroutine {
    current = State.WhenCurrent(test, it)
  }
  override suspend fun oneStep() = suspendCoroutine {
    current = State.OneStep(it)
  }
  override suspend fun zeroOrMoreSteps() = suspendCoroutine {
    current = State.ZeroOrMoreSteps(it)
  }
  override suspend fun checkCurrent(test: Test) = suspendCoroutine {
    current = State.CheckCurrent(test, it)
  }

  fun execute(): Formula = when (val c = current) {
    null -> builder.`true`
    is State.Remember -> builder.remember {
      c.continuation.resume(it)
      execute()
    }
    is State.WhenCurrent -> builder.implies(builder.predicate(c.test), run {
      c.continuation.resume(Unit)
      execute()
    })
    is State.OneStep -> builder.next(run {
      c.continuation.resume(Unit)
      execute()
    })
    is State.ZeroOrMoreSteps -> builder.eventually(run {
      c.continuation.resume(Unit)
      execute()
    })
    is State.CheckCurrent -> builder.and(
      builder.predicate(c.test),
      run {
        c.continuation.resume(Unit)
        execute()
      }
    )
  }
}