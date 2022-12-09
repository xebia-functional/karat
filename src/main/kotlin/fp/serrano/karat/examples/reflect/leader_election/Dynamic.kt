package fp.serrano.karat.examples.reflect.leader_election

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.common.*
import fp.serrano.karat.ui.visualize

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

interface Node {
  val succ: Node
  val id: Id
  var inbox: Set<Id>
  var outbox: Set<Id>

  companion object {
    var Elected: Set<Node> by model

    fun Fact.ring(): KFormula = and {
      // nodes form a ring
      + forAll { n -> set<Node>() `in` n / oneOrMore(Node::succ) }
      // all nodes have unique id's
      + forAll { i -> atMostOne(Node::id / i) }
    }
  }
}

sealed interface Transition: StateMachine

@initial
object Empty: Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    // initially inbox and outbox are empty
    + empty(Node::inbox)
    + empty(Node::outbox)
    // initially there are no elected nodes
    + empty(Node::Elected)
  }
}

data class Initiate(val n: KArg<Node>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + neverBefore { n / Node::id `in` n / Node::outbox }
    // effect on n.outbox
    + (next(n / Node::outbox) `==` current(n / Node::outbox) + n / Node::id)
    // effect on the outboxes of other nodes
    + forAll(set<Node>() - n) { stays(it / Node::outbox) }

    + stays(Node::inbox)   // frame condition on inbox
    + stays(Node::Elected) // frame condition on Elected
  }
}

data class Send(val n: KArg<Node>, val i: KArg<Id>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + (i `in` current(n / Node::outbox))

    + (next(n / Node::outbox) `==` current(n / Node::outbox) - i)
    + forAll(set<Node>() - n) { stays(it / Node::outbox) }

    + (next((n / Node::succ) / Node::inbox) `==` current(n / Node::succ / Node::inbox) + i)
    + forAll(set<Node>() - (n / Node::succ)) { stays(it / Node::inbox) }

    + stays(Node::Elected)
  }
}

data class Read(val n: KArg<Node>, val i: KArg<Id>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + (i `in` current(n / Node::inbox))

    + (next(n / Node::inbox) `==` current(n / Node::inbox) - i)
    + forAll(set<Node>() - n) { m -> stays(m / Node::inbox) }

    + (i gt n / Node::id).ifThen(
      ifTrue = next(n / Node::outbox) `==` current(n / Node::outbox) + i,
      ifFalse = stays(n / Node::outbox)
    )
    + forAll(set<Node>() - n) { stays(it / Node::outbox) }

    + (i `==` n / Node::id).ifThen(
      ifTrue = next(Node::Elected) `==` current(Node::Elected) + n,
      ifFalse = stays(Node::Elected)
    )
  }
}

fun main() {
  execute {
    reflect(reflectAll = true, Node::class, Id::class)
    reflectMachine(Transition::class, transitionSigName = "Event", skipName = "Stutter")

    // find a trace which satisfies the formula
    run(overall = 30, bitwidth = 10, scopes = listOf(exactly<Node>(3), exactly<Id>(3))) {
      eventually { some(global(Node::Elected)) }
    }.visualize()

    // try to find a counterexample
    // check(overall = 4, steps = 1 .. 20) {
    //   eventually { some(set<Elected>()) }
    // }.visualize()

  }
}