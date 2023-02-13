package karat

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

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
}

/**
 * Generic builder for sequential traces of formulae.
 * Use [trace] to turn those into regular formula
 */
public interface TraceFormulaBuilder<Subject, Test> {
  public suspend fun remember(): Subject
  public suspend fun whenCurrent(test: Test)
  public suspend fun oneStep()
  public suspend fun oneOrMoreSteps()
  public suspend fun checkCurrent(test: Test)
  public suspend fun fromNowOn(
    builder: suspend TraceFormulaBuilder<Subject, Test>.() -> Unit
  )
}

public fun <Subject, Test, Formula, Atomic : Formula> trace(
  builder: FormulaBuilder<Subject, Test, Formula, Atomic>,
  block: suspend TraceFormulaBuilder<Subject, Test>.() -> Unit
): Formula {
  val machine = TraceFormulaBuilderImpl<Subject, Test, Formula, Atomic>(builder)
  block.startCoroutine(
    machine,
    Continuation(EmptyCoroutineContext) { machine.done() }
  )
  return machine.execute()
}

private class TraceFormulaBuilderImpl<Subject, Test, Formula, Atomic : Formula>(
  private val builder: FormulaBuilder<Subject, Test, Formula, Atomic>,
): TraceFormulaBuilder<Subject, Test> {
  sealed interface State<Subject, Test> {
    data class Remember<Subject, Test>(
      val continuation: Continuation<Subject>
    ): State<Subject, Test>
    data class WhenCurrent<Subject, Test>(
      val test: Test,
      val continuation: Continuation<Unit>
    ): State<Subject, Test>
    data class OneStep<Subject, Test>(
      val continuation: Continuation<Unit>
    ): State<Subject, Test>
    data class OneOrMoreSteps<Subject, Test>(
      val continuation: Continuation<Unit>
    ): State<Subject, Test>
    data class CheckCurrent<Subject, Test>(
      val test: Test,
      val continuation: Continuation<Unit>
    ): State<Subject, Test>
    data class FromNowOn<Subject, Test>(
      val builder: suspend TraceFormulaBuilder<Subject, Test>.() -> Unit,
      val continuation: Continuation<Unit>
    ): State<Subject, Test>
  }

  private var current: State<Subject, Test>? = null

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
  override suspend fun oneOrMoreSteps() = suspendCoroutine {
    current = State.OneOrMoreSteps(it)
  }
  override suspend fun checkCurrent(test: Test) = suspendCoroutine {
    current = State.CheckCurrent(test, it)
  }
  override suspend fun fromNowOn(builder: suspend TraceFormulaBuilder<Subject, Test>.() -> Unit) = suspendCoroutine {
    current = State.FromNowOn(builder, it)
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
    is State.OneOrMoreSteps -> builder.next(
      builder.eventually(run {
        c.continuation.resume(Unit)
        execute()
      })
    )
    is State.CheckCurrent -> builder.and(
      builder.predicate(c.test),
      run {
        c.continuation.resume(Unit)
        execute()
      }
    )
    is State.FromNowOn -> builder.always(run {
      c.continuation.resume(Unit)
      execute()
    })
  }
}