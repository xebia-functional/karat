package karat.kotlin.test

import karat.concrete.*
import karat.concrete.progression.Info
import karat.concrete.progression.suspend.SuspendStepResultManager
import kotlin.test.assertTrue

public typealias KotlinTestAtomic<A> = Atomic<A, Unit>
public typealias KotlinTestFormula<A> = Formula<A, Unit>

public typealias KotlinTestFormulaBuilder<ConcreteState, Action, Response> =
  ConcreteFormulaBuilder<Info<Action, ConcreteState, Response>, Unit>

public class KotlinTestStepResultManager<A>: SuspendStepResultManager<A, Unit, List<AssertionError>?> {
  override val List<AssertionError>?.isOk: Boolean
    get() = this == null
  override val everythingOk: List<AssertionError>? = null
  override val falseFormula: List<AssertionError> = listOf(FalseError)
  override val unknown: List<AssertionError> = listOf(DoneUnknown)
  override fun negationWasTrue(formula: Formula<A, Unit>): List<AssertionError> = listOf(NegationWasTrue(formula))
  override fun shouldHoldEventually(formula: Formula<A, Unit>): List<AssertionError> = listOf(ShouldHoldEventually(formula))
  override fun andResults(results: List<List<AssertionError>?>): List<AssertionError>? =
    if (results.all { it.isOk }) everythingOk else results.flatMap { it.orEmpty() }
  override fun orResults(results: List<List<AssertionError>?>): List<AssertionError>? =
    if (results.any { it.isOk }) everythingOk else results.flatMap { it.orEmpty() }
  override suspend fun predicate(test: suspend (A) -> Unit, value: A): List<AssertionError>? =
    try {
      test(value)
      null
    } catch (e: AssertionError) {
      listOf(e)
    }
}

/**
 * Basic formula which checks that an item is produced, and satisfies the [test].
 */
public fun <A> should(test: suspend (A) -> Unit): KotlinTestAtomic<A> =
  Predicate(test)

/**
 * Basic formula which checks that an item is produced, and satisfies the [predicate].
 */
public fun <A> holds(predicate: suspend (A) -> Boolean): KotlinTestAtomic<A> =
  should { x -> assertTrue(predicate(x)) }

/**
 * Basic formula which checks that an item is produced, and satisfies the [predicate].
 */
public fun <A> holds(message: String, predicate: suspend (A) -> Boolean): KotlinTestAtomic<A> =
  should { x -> assertTrue(message) { predicate(x) } }
