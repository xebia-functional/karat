package com.`47deg`.karat.examples.sig_object

import com.`47deg`.karat.*
import com.`47deg`.karat.ast.*
import com.`47deg`.karat.ui.visualize

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