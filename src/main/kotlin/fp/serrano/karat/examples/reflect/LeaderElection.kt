package fp.serrano.karat.examples.reflect

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

interface Node {
  val succ: Node
  val id: Id
  var inbox: Set<Id>
  var outbox: Set<Id>
}

var Elected: Set<Node> by model

interface Id {
  val next: Id?
}

val first: Id by model
val last: Id by model

fun ReflectedModule.gt(one: KSet<Id>, other: KSet<Id>): KFormula =
  one `in` (other / closureOptional(Id::next))

sealed interface Transition: StateMachine

@initial object Empty: Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    // initially inbox and outbox are empty
    + no(field(Node::inbox))
    + no(field(Node::outbox))
    // initially there are no elected nodes
    + no(global(::Elected))
  }
}

data class Initiate(val n: KArg<Node>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + historically { not(n / Node::id `in` n / Node::outbox) }
    // effect on n.outbox
    + ( next(n / Node::outbox) `==` current(n / Node::outbox) + n / Node::id )
    // effect on the outboxes of other nodes
    + forAll(set<Node>() - n) { m -> stays(m / Node::outbox) }

    + stays(field(Node::inbox))  // frame condition on inbox
    + stays(global(::Elected))      // frame condition on Elected
  }
}

data class Send(val n: KArg<Node>, val i: KArg<Id>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + (i `in` current(n / Node::outbox))

    + ( next(n / Node::outbox) `==` current(n / Node::outbox) - i )
    + forAll(set<Node>() - n) { m -> stays(m / Node::outbox) }

    + ( next((n / Node::succ) / Node::inbox) `==` (current((n / Node::succ) / Node::inbox) + i) )
    + forAll(set<Node>() - (n / Node::succ)) { m -> stays(m / Node::inbox) }

    + stays(global(::Elected))
  }
}

data class Read(val n: KArg<Node>, val i: KArg<Id>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + (i `in` n / Node::inbox)

    + ( next(n / Node::inbox) `==` current(n / Node::inbox) - i )
    + forAll(set<Node>() - n) { m -> stays(m / Node::inbox) }

    + gt(i, n / Node::id).ifThen(
      ifTrue  = next(n / Node::outbox) `==` current(n / Node::outbox) + i,
      ifFalse = stays(n / Node::outbox)
    )
    + forAll(set<Node>() - n) { m -> stays(m / Node::outbox) }

    + (i `==` n / Node::id).ifThen(
      ifTrue = next(global(::Elected)) `==` current(global(::Elected)) + n,
      ifFalse = stays(global(::Elected))
    )
  }
}

fun main() {
  execute {
    reflect(reflectAll = true, Node::class, Id::class)
    reflectGlobal(::Elected, ::first, ::last)

    fact {
      forAll { n -> set<Node>() `in` n / closure(Node::succ) }
    }

    fact { no (global(::last) / Id::next) }
    fact { set<Id>() `in` global(::first) / reflexiveClosureOptional(Id::next) }

    fact {
      forAll { i -> lone(Node::id / i) }
    }

    reflectMachine(Transition::class, transitionSigName = "Event", skipName = "Stutter")

    // find a trace which satisfies the formula
    run(overall = 30, bitwidth = 10, scopes = listOf(exactly<Node>(3), exactly<Id>(3))) {
      eventually { some(global(::Elected)) }
    }.visualize()

    // try to find a counterexample
    // check(overall = 4, steps = 1 .. 20) {
    //   eventually { some(set<Elected>()) }
    // }.visualize()

  }
}