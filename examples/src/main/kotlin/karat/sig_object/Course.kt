package karat.sig_object

import karat.*
import karat.ast.*
import karat.ui.visualize

object Person: KPrimSig<Person>("Person")
object Course: KPrimSig<Course>("Course") {
  val teacher = field("teacher", Person)
  val students = field("students", karat.ast.setOf(Person))

  init {
    fact { no (teacher[it] `&` students[it]) }
  }
}

val courseWorld = module {
  sigs(Person, Course)
}

fun main() {
  inModule(courseWorld) {
    run(4, 4, 4) {
      Constants.TRUE
    }.visualize()
  }
}