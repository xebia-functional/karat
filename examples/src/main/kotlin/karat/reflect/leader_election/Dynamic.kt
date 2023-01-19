package karat.reflect.leader_election

import karat.*
import karat.ast.*
import karat.ui.visualize

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

interface Node {
  val succ: Node
  val id: Int
  var inbox: Set<Int>
  var outbox: Set<Int>

  companion object {
    var Elected: Set<Node> by model

    fun Fact.ring(): KFormula = karat.ast.and {
      // nodes form a ring
      +forAll { n -> set<Node>() `in` n / oneOrMore(Node::succ) }
      // all nodes have unique id's
      +forAll { i -> atMostOne(Node::id % i) }
    }
  }
}

sealed interface Transition: StateMachine

@initial object Empty: Transition {
  override fun ReflectedModule.execute(): KFormula = karat.ast.and {
    // initially inbox and outbox are empty
    +empty(Node::inbox)
    +empty(Node::outbox)
    // initially there are no elected nodes
    +empty(Node.Companion::Elected)
  }
}

data class Initiate(val n: KArg<Node>): Transition {
  override fun ReflectedModule.execute(): KFormula = karat.ast.and {
    +neverBefore { n / Node::id `in` n / Node::outbox }
    // effect on n.outbox
    +(karat.ast.next(n / Node::outbox) `==` karat.ast.current(n / Node::outbox) + n / Node::id)
    // effect on the outboxes of other nodes
    +forAll(set<Node>() - n) { karat.ast.stays(it / Node::outbox) }

    +stays(Node::inbox)   // frame condition on inbox
    +stays(Node.Companion::Elected) // frame condition on Elected
  }
}

data class Send(val n: KArg<Node>, val i: KArg<Int>): Transition {
  override fun ReflectedModule.execute(): KFormula = karat.ast.and {
    +(i `in` karat.ast.current(n / Node::outbox))

    +(karat.ast.next(n / Node::outbox) `==` karat.ast.current(n / Node::outbox) - i)
    +forAll(set<Node>() - n) { karat.ast.stays(it / Node::outbox) }

    +(karat.ast.next((n / Node::succ) / Node::inbox) `==` karat.ast.current(n / Node::succ / Node::inbox) + i)
    +forAll(set<Node>() - (n / Node::succ)) { karat.ast.stays(it / Node::inbox) }

    +stays(Node.Companion::Elected)
  }
}

data class Read(val n: KArg<Node>, val i: KArg<Int>): Transition {
  override fun ReflectedModule.execute(): KFormula = karat.ast.and {
    +(i `in` karat.ast.current(n / Node::inbox))

    +(karat.ast.next(n / Node::inbox) `==` karat.ast.current(n / Node::inbox) - i)
    +forAll(set<Node>() - n) { m -> karat.ast.stays(m / Node::inbox) }

    +(i gt n / Node::id).ifThen(
      ifTrue = karat.ast.next(n / Node::outbox) `==` karat.ast.current(n / Node::outbox) + i,
      ifFalse = karat.ast.stays(n / Node::outbox)
    )
    +forAll(set<Node>() - n) { karat.ast.stays(it / Node::outbox) }

    +(i `==` n / Node::id).ifThen(
      ifTrue = next(Node.Companion::Elected) `==` current(Node.Companion::Elected) + n,
      ifFalse = stays(Node.Companion::Elected)
    )
  }
}

fun main() {
  execute {
    reflect(reflectAll = true, type<Node>())
    reflectMachine<Transition>(transitionSigName = "Event", skipName = "Stutter")

    // find a trace which satisfies the formula
    run(overall = 30, bitwidth = 3, scopes = listOf(exactly<Node>(3))) {
      eventually { some(global(Node.Companion::Elected)) }
    }.visualize()

    // try to find a counterexample
    // check(overall = 4, steps = 1 .. 20) {
    //   eventually { some(set<Elected>()) }
    // }.visualize()

  }
}