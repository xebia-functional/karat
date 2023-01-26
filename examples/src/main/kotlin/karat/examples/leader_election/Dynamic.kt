package karat.examples.leader_election

import karat.*
import karat.ast.*
import karat.reflection.*
import karat.ui.visualize

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

interface Node {
  val succ: Node
  val id: Int
  var inbox: Set<Int>
  var outbox: Set<Int>

  companion object {
    var Elected: Set<Node> by model

    fun Fact.ring(): KFormula = and {
      // nodes form a ring
      +forAll { n -> set<Node>() `in` n / oneOrMore(Node::succ) }
      // all nodes have unique id's
      +forAll { i -> atMostOne(Node::id % i) }
    }
  }
}

context(ReflectedModule) class TransitionMethod: StateMachineDefinition {
  override fun init(): KFormula = and {
    // initially inbox and outbox are empty
    +empty(Node::inbox)
    +empty(Node::outbox)
    // initially there are no elected nodes
    +empty(Node.Companion::Elected)
  }

  override fun stutter(): KFormula = and {
    +forAll { n ->
      stays(n / Node::inbox) and stays(n / Node::outbox)
    }
    +stays(Node.Companion::Elected)
  }

  fun initiate(n: KArg<Node>) = and {
    +neverBefore { n / Node::id `in` n / Node::outbox }
    // effect on n.outbox
    +(next(n / Node::outbox) `==` current(n / Node::outbox) + n / Node::id)
    // effect on the outboxes of other nodes
    +forAll(set<Node>() - n) { stays(it / Node::outbox) }

    +stays(Node::inbox)   // frame condition on inbox
    +stays(Node.Companion::Elected) // frame condition on Elected
  }

  fun send(n: KArg<Node>, i: KArg<Int>) = and {
    +(i `in` current(n / Node::outbox))

    +(next(n / Node::outbox) `==` current(n / Node::outbox) - i)
    +forAll(set<Node>() - n) { stays(it / Node::outbox) }

    +(next((n / Node::succ) / Node::inbox) `==` current(n / Node::succ / Node::inbox) + i)
    +forAll(set<Node>() - (n / Node::succ)) { stays(it / Node::inbox) }

    +stays(Node.Companion::Elected)
  }

  fun read(n: KArg<Node>, i: KArg<Int>) = and {
    +(i `in` current(n / Node::inbox))

    +(next(n / Node::inbox) `==` current(n / Node::inbox) - i)
    +forAll(set<Node>() - n) { m -> stays(m / Node::inbox) }

    +(i gt n / Node::id).ifThen(
      ifTrue = next(n / Node::outbox) `==` current(n / Node::outbox) + i,
      ifFalse = stays(n / Node::outbox)
    )
    +forAll(set<Node>() - n) { stays(it / Node::outbox) }

    +(i `==` n / Node::id).ifThen(
      ifTrue = next(Node.Companion::Elected) `==` current(Node.Companion::Elected) + n,
      ifFalse = stays(Node.Companion::Elected)
    )
  }
}


sealed interface Transition: StateMachineTransition

@initial object Empty: Transition {
  context(ReflectedModule) override fun execute(): KFormula = and {
    // initially inbox and outbox are empty
    +empty(Node::inbox)
    +empty(Node::outbox)
    // initially there are no elected nodes
    +empty(Node.Companion::Elected)
  }
}

@stutter object Stutter: Transition {
  context(ReflectedModule) override fun execute(): KFormula = and {
    +forAll { n ->
      stays(n / Node::inbox) and stays(n / Node::outbox)
    }
    +stays(Node.Companion::Elected)
  }
}

data class Initiate(val n: KArg<Node>): Transition {
  context(ReflectedModule) override fun execute(): KFormula = and {
    +neverBefore { n / Node::id `in` n / Node::outbox }
    // effect on n.outbox
    +(next(n / Node::outbox) `==` current(n / Node::outbox) + n / Node::id)
    // effect on the outboxes of other nodes
    +forAll(set<Node>() - n) { stays(it / Node::outbox) }

    +stays(Node::inbox)   // frame condition on inbox
    +stays(Node.Companion::Elected) // frame condition on Elected
  }
}

data class Send(val n: KArg<Node>, val i: KArg<Int>): Transition {
  context(ReflectedModule) override fun execute(): KFormula = and {
    +(i `in` current(n / Node::outbox))

    +(next(n / Node::outbox) `==` current(n / Node::outbox) - i)
    +forAll(set<Node>() - n) { stays(it / Node::outbox) }

    +(next((n / Node::succ) / Node::inbox) `==` current(n / Node::succ / Node::inbox) + i)
    +forAll(set<Node>() - (n / Node::succ)) { stays(it / Node::inbox) }

    +stays(Node.Companion::Elected)
  }
}

data class Read(val n: KArg<Node>, val i: KArg<Int>): Transition {
  context(ReflectedModule) override fun execute(): KFormula = and {
    +(i `in` current(n / Node::inbox))

    +(next(n / Node::inbox) `==` current(n / Node::inbox) - i)
    +forAll(set<Node>() - n) { m -> stays(m / Node::inbox) }

    +(i gt n / Node::id).ifThen(
      ifTrue = next(n / Node::outbox) `==` current(n / Node::outbox) + i,
      ifFalse = stays(n / Node::outbox)
    )
    +forAll(set<Node>() - n) { stays(it / Node::outbox) }

    +(i `==` n / Node::id).ifThen(
      ifTrue = next(Node.Companion::Elected) `==` current(Node.Companion::Elected) + n,
      ifFalse = stays(Node.Companion::Elected)
    )
  }
}

fun main() {
  execute {
    reflect(reflectAll = true, type<Node>())
    reflectMachineFromMethods(TransitionMethod(), transitionSigName = "Event")
    // reflectMachine<Transition>(transitionSigName = "Event")

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