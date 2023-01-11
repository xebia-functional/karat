package fp.serrano.karat.examples.reflect.sw_abstractions.chapter_2

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

// sections 2.1 and 2.2

interface Name
interface Address
interface Book {
  val addr: Map<Name, Address?>
}

fun ReflectedModule.add(b1: KArg<Book>, b2: KArg<Book>, n: KArg<Name>, a: KArg<Address>): KFormula =
  (b2 / Book::addr).asRelationNullable `==` (b1 / Book::addr).asRelationNullable + (n to a)

fun ReflectedModule.del(b1: KArg<Book>, b2: KArg<Book>, n: KArg<Name>): KFormula =
  (b2 / Book::addr).asRelationNullable `==` (b1 / Book::addr).asRelationNullable - (n to set<Address>())

fun ReflectedModule.lookup(b: KArg<Book>, n: KArg<Name>): KSet<Address> =
  n / (b / Book::addr).asRelationNullable

fun ReflectedModule.delUndoesAdd(): KFormula =
  forAll<Book, Book, Book> { b1, b2, b3 ->
    forAll<Name, Address> { n, a ->
      (no(n / (b1 / Book::addr).asRelationNullable)
              and add(b1, b2, n, a)
              and del(b2, b3, n)) implies (b1 / Book::addr `==` b3 / Book::addr)
    }
  }

fun ReflectedModule.addIdempotent(): KFormula =
  forAll<Book, Book, Book> { b1, b2, b3 ->
    forAll<Name, Address> { n, a ->
      (add(b1, b2, n, a) and add(b2, b3, n, a)) implies (b2 / Book::addr `==` b3 / Book::addr)
    }
  }

fun ReflectedModule.addLocal(): KFormula =
  forAll<Book, Book> { b1, b2 ->
    forAll<Name, Name, Address> { n1, n2, a ->
      (add(b1, b2, n1, a) and (n1 `!=` n2)) implies (lookup(b1, n2) `==` lookup(b2, n2))
    }
  }

fun main() {
  execute {
    reflect(reflectAll = true, Name::class, Address::class, Book::class)
    /*
    run(overall = 3, scopes = listOf(exactly<Book>(1))) {
      forSome<Book> { b ->
        and {
          + (cardinality(b / Book::addr) gt 1)
          + (cardinality( set<Name>() / (b / Book::addr).asRelationNullable) gt 1)
        }
      }
    }.visualize()
    */

    check(overall = 10, scopes = listOf(exactly<Book>(3))) { delUndoesAdd() }.visualize()
    check(overall = 3) { addIdempotent() }.visualize()
    check(overall = 3, scopes = listOf(exactly<Book>(2))) { addLocal() }.visualize()
  }
}