package karat.kotest

import io.kotest.assertions.fail
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import karat.concrete.*
import karat.concrete.progression.*
import kotlin.reflect.KClass

public typealias KotestAtomic<A> = Atomic<Result<A>, Unit>
public typealias KotestFormula<A> = Formula<Result<A>, Unit>

public class KotestStepResultManager<A>: StepResultManager<Result<A>, Unit, List<AssertionError>?> {
  override val List<AssertionError>?.isOk: Boolean
    get() = this == null
  override val everythingOk: List<AssertionError>? = null
  override val falseFormula: List<AssertionError> = listOf(FalseError)
  override val unknown: List<AssertionError> = listOf(DoneUnknown)
  override fun negationWasTrue(formula: Formula<Result<A>, Unit>): List<AssertionError> = listOf(NegationWasTrue(formula))
  override fun shouldHoldEventually(formula: Formula<Result<A>, Unit>): List<AssertionError> = listOf(ShouldHoldEventually(formula))
  override fun andResults(results: List<List<AssertionError>?>): List<AssertionError>? =
    if (results.all { it.isOk }) everythingOk else results.flatMap { it.orEmpty() }
  override fun orResults(results: List<List<AssertionError>?>): List<AssertionError>? =
    if (results.any { it.isOk }) everythingOk else results.flatMap { it.orEmpty() }
  override suspend fun predicate(test: suspend (Result<A>) -> Unit, value: Result<A>): List<AssertionError>? =
    try {
      test(value)
      null
    } catch (e: AssertionError) {
      listOf(e)
    }
}

public fun <A> onRight(test: suspend (A) -> Unit): suspend (Result<A>) -> Unit = {
  it.shouldBeSuccess()
  test(it.getOrNull()!!)
}

/**
 * Basic formula which checks that an item is produced, and satisfies the [test].
 */
public fun <A> should(test: suspend (A) -> Unit): KotestAtomic<A> =
  Predicate(onRight(test))

/**
 * Basic formula which checks that an item is produced, and satisfies the [predicate].
 */
public fun <A> holds(predicate: suspend (A) -> Boolean): KotestAtomic<A> =
  should { predicate(it).shouldBeTrue() }

/**
 * Basic formula which checks that an item is produced, and satisfies the [predicate].
 */
public fun <A> holds(message: String, predicate: suspend (A) -> Boolean): KotestAtomic<A> =
  should { if (!predicate(it)) fail(message) }

/**
 * Basic formula which checks that an exception of type [T] has been thrown.
 */
public fun <T: Throwable> throws(klass: KClass<T>, test: suspend (T) -> Unit = { }): KotestAtomic<Any?> =
  Predicate {
    it.shouldBeFailure()
    it.exceptionOrNull()!!.should(beInstanceOf(klass))
    @Suppress("UNCHECKED_CAST")
    test(it as T)
  }

/**
 * Basic formula which checks that an exception of type [T] has been thrown.
 */
public inline fun <reified T: Throwable> throws(crossinline test: suspend (T) -> Unit = { }): KotestAtomic<Any?> =
  Predicate {
    it.shouldBeFailure<T>()
    test(it as T)
  }

public fun <Action, State, Response> item(
  test: suspend (Info<Action, State, Response>) -> Unit
): suspend (Result<Info<Action, State, Response>>) -> Unit = onRight(test)
