package karat.reflect

import karat.*
import karat.ast.*
import karat.ui.visualize

data class Thing<A>(val thing: A)
interface Flu
interface Flo

data class TwoOfThem(
  val flu: Thing<Flu>,
  val flo: Thing<Flo>
)

fun main() {
  execute {
    reflect(reflectAll = true,
      type<Flu>(), type<Flo>(), type<Thing<Flu>>(), type<Thing<Flo>>(), type<TwoOfThem>()
    )

    run(10) {
      one(set<TwoOfThem>())
    }.visualize()
  }
}