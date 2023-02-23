package karat.util

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

public fun <A> resumeContinuation(k: Continuation<A>, value: A): Unit =
  k.resume(value)
