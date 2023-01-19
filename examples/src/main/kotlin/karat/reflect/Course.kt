package karat.reflect

import karat.*
import karat.ast.*
import karat.ui.visualize

data class Person(
  val name: String
)

data class Course(
  val name: String,
  @reflect val teacher: Person,
  @reflect val students: Set<Person>
) {
  companion object {
    fun InstanceFact<Course>.teacherIsNotAStudent(): KFormula =
      no ( (self / Course::teacher) `&` (self / Course::students))
  }
}

fun main() {
  execute {
    reflect(type<Person>(), type<Course>())
    run(4, 4, 4) {
      Constants.TRUE
    }.visualize()
  }
}
