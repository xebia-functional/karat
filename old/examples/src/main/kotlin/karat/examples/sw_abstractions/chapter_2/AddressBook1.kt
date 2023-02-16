package karat.examples.sw_abstractions.chapter_2

import karat.*
import karat.ast.*
import karat.reflection.*
import karat.ui.visualize

// sections 2.1 and 2.2

interface Name
interface Address
interface Book {
  val addr: Map<Name, Address?>
}

context(ReflectedModule) fun add(b1: KArg<Book>, b2: KArg<Book>, n: KArg<Name>, a: KArg<Address>): KFormula =
  (b2 / Book::addr).asRelationNullable `==` (b1 / Book::addr).asRelationNullable + (n to a)

context(ReflectedModule) fun del(b1: KArg<Book>, b2: KArg<Book>, n: KArg<Name>): KFormula =
  (b2 / Book::addr).asRelationNullable `==` (b1 / Book::addr).asRelationNullable - (n to set<Address>())

context(ReflectedModule) fun lookup(b: KArg<Book>, n: KArg<Name>): KSet<Address> =
  n / (b / Book::addr).asRelationNullable

context(ReflectedModule) fun delUndoesAdd(): KFormula =
  forAll<Book, Book, Book> { b1, b2, b3 ->
    forAll<Name, Address> { n, a ->
      (no(n / (b1 / Book::addr).asRelationNullable)
              and add(b1, b2, n, a)
              and del(b2, b3, n)) implies (b1 / Book::addr `==` b3 / Book::addr)
    }
  }

context(ReflectedModule) fun addIdempotent(): KFormula =
  forAll<Book, Book, Book> { b1, b2, b3 ->
    forAll<Name, Address> { n, a ->
      (add(b1, b2, n, a) and add(b2, b3, n, a)) implies (b2 / Book::addr `==` b3 / Book::addr)
    }
  }

context(ReflectedModule) fun addLocal(): KFormula =
  forAll<Book, Book> { b1, b2 ->
    forAll<Name, Name, Address> { n1, n2, a ->
      (add(b1, b2, n1, a) and (n1 `!=` n2)) implies (lookup(b1, n2) `==` lookup(b2, n2))
    }
  }

fun main() {
  execute {
    reflect(reflectAll = true, type<Name>(), type<Address>(), type<Book>())
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