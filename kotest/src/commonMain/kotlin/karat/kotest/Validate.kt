package karat.kotest

import io.kotest.assertions.fail
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import karat.concrete.*
import karat.kotest.internal.*
import kotlin.reflect.KClass
import kotlin.runCatching

public typealias KotestAtomic<A> = Atomic<Result<A>, Unit>
public typealias KotestFormula<A> = Formula<Result<A>, Unit>

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

public fun <Action, State, Response> item(
  test: suspend (Info<Action, State, Response>) -> Unit
): suspend (Result<Info<Action, State, Response>>) -> Unit = onRight(test)

public tailrec suspend fun <Action, State, Response> KotestFormula<Info<Action, State, Response>>.check(
  actions: List<Action>,
  current: State,
  step: suspend (Action, State) -> Step<State, Response>,
  previousActions: MutableList<Action> = mutableListOf()
): Unit = when {
  actions.isEmpty() ->
    this.leftToProve().throwIfFailed(previousActions, current)
  else -> {
    val action = actions.first()
    val oneStepFurther = runCatching { step(action, current) }.map { Info(action, current, it.state, it.response) }
    val progress = this.check(oneStepFurther)
    previousActions.add(action)
    progress.result.throwIfFailed(previousActions, oneStepFurther)
    when (val next = oneStepFurther.getOrNull()) {
      null -> progress.next.leftToProve().throwIfFailed(previousActions, progress.next)
      else -> progress.next.check(actions.drop(1), next.nextState, step, previousActions)
    }
  }
}

private fun <A, T> FormulaStepResult.throwIfFailed(actions: List<A>, state: T) {
  when (this) {
    null -> { } // ok
    else -> throw TraceAssertionError(actions, state, this)
  }
}