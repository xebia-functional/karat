package fp.serrano.karat.common

import fp.serrano.karat.*
import fp.serrano.karat.ast.*

interface Id {
  val next: Id?

  companion object {
    val first: Id by model
    val last: Id by model

    fun Fact.finiteId(): KFormula = and {
      + no (global(::last) / Id::next)
      + (set<Id>() `in` global(::first) / zeroOrMore(Id::next))
    }
  }
}

context(ReflectedModule)
infix fun KSet<Id>.gt(other: KSet<Id>): KFormula =
  this `in` (other / closureOptional(Id::next))