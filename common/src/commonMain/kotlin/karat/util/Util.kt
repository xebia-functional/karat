package karat.util

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

public fun <A> resumeContinuation(k: Continuation<A>, value: A): Unit =
  k.resume(value)

public fun <A> resultGetOrNull(x: Result<A>): A? = x.getOrNull()

public fun <A> resultExceptionOrNull(x: Result<A>): Throwable? = x.exceptionOrNull()
