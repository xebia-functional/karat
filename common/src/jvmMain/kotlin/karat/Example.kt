package karat

import karat.concrete.trace

private val example = trace<Int, Boolean> {
  whenCurrent { it > 0 }
  oneOrMoreSteps()
  checkCurrent { it > 1 }
}

public fun main() {
  println(example)
}