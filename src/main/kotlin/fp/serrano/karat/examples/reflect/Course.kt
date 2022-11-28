package fp.serrano.karat.examples.reflect

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

data class Person(
  val name: String
)

data class Course(
  val name: String,
  @reflect val teacher: Person,
  @reflect val students: Set<Person>
) {
  companion object {
    fun Fact<Course>.teacherIsNotAStudent(): KFormula =
      no ( (self / Course::teacher) `&` (self / Course::students))
  }
}

fun main() {
  execute {
    reflect(Person::class, Course::class)
    run(4, 4, 4) {
      Constants.TRUE
    }.visualize()
  }
}