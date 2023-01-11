package fp.serrano.karat.examples.reflect.sw_abstractions.chapter_2

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

@abstract interface Target2
interface Addr2: Target2
@abstract interface Name2: Target2
interface Alias2: Name2
interface Group2: Name2
interface Book2 {
  val addr: Map<Name2, Target2>

  companion object {
    fun InstanceFact<Book2>.noCycles(): KFormula =
      forNo<Name> { n ->
        n `in` n / closure((self / Book2::addr).asRelation)
      }
  }
}

fun main() {
  execute {
    reflect(reflectAll = true, Target2::class, Addr2::class, Name2::class, Alias2::class, Group2::class, Book2::class)
  }
}