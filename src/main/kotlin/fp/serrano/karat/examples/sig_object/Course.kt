package fp.serrano.karat.examples.sig_object

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

object Person: KPrimSig<Person>("Person")
object Course: KPrimSig<Course>("Course") {
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
  inModule(courseWorld) {
    run(4, 4, 4) {
      Constants.TRUE
    }.visualize()
  }
}