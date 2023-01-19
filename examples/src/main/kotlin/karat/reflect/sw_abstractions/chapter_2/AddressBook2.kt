package karat.reflect.sw_abstractions.chapter_2

import karat.*
import karat.ast.*
import karat.ui.visualize

@abstract interface Target2
interface Addr2: Target2
@abstract interface Name2: Target2
interface Alias2: Name2
interface Group2: Name2
interface Book2 {
  val names: Set<Name2>
  val addr: Map<Name2, Set<Target2>>

  companion object {
    fun InstanceFact<Book2>.noCycles(): KFormula = karat.ast.and {
      val addr = self / Book2::addr.asRelationSetR
      +(domain(addr) `==` self / Book2::names.flattenR)
      +forNo<Name2> { n -> n `in` n / closure(addr) }
      +forAll<Name2> { n -> some(n / addr) }
      +forAll<Alias2> { a -> lone(a / addr) }
    }
  }
}

fun ReflectedModule.lookup(b: KArg<Book2>, n: KArg<Name2>): KSet<Addr2> =
  limit<_, Addr2>(n / closure(b / Book2::addr.asRelationSetR))

fun main() {
  execute {
    reflect(reflectAll = true, type<Target2>(), type<Addr2>(), type<Name2>(), type<Alias2>(), type<Group2>(), type<Book2>())

    check(overall = 4, scopes = listOf(exactly<Book2>(1))) {
      forAll<Book2> { b ->
        forAll(b / Book2::names.flattenR) { n ->
          some(lookup(b, n))
        }
      }
    }.visualize()
  }
}