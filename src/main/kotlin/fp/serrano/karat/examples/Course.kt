package fp.serrano.karat.examples

import fp.serrano.karat.*
import fp.serrano.karat.ui.visualize

object Person: KSig<Person>("Person")
object Course: KSig<Course>("Course") {
  val teacher = field("teacher", Person)
  val students = field("students", setOf(Person))

  init {
    fact { no (teacher[it] `&` students[it]) }
  }
}

val courseWorld = module {
  sigs(Person, Course)
}

fun main() {
  with(courseWorld) {
    run(4, 4, 4) {
      Constants.TRUE
    }.visualize()
  }
}