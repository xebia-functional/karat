package com.`47deg`.karat.examples.reflect

import com.`47deg`.karat.*
import com.`47deg`.karat.ast.*
import com.`47deg`.karat.ui.visualize

data class Person(
  val name: String
)

data class Course(
  val name: String,
  @com.`47deg`.karat.reflect val teacher: Person,
  @com.`47deg`.karat.reflect val students: Set<Person>
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
