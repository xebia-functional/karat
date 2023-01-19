package karat.common

import karat.*
import karat.ast.*

interface Id {
  val next: Id?

  companion object {
    val first: Id by model
    val last: Id by model

    fun Fact.finiteId(): KFormula = and {
      +no(global(Companion::last) / Id::next)
      +(set<Id>() `in` global(Companion::first) / zeroOrMore(Id::next))
    }
  }
}

context(ReflectedModule)
infix fun KSet<Id>.gt(other: KSet<Id>): KFormula =
  this `in` (other / closureOptional(Id::next))