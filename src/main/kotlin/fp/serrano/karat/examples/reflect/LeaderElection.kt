package fp.serrano.karat.examples.reflect

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

interface Node {
  @reflect val succ: Node
  @reflect val id: Id
  @reflect var inbox: Set<Id>
  @reflect var outbox: Set<Id>
}

@variable @subset interface Elected: Node

interface Id {
  @reflect val next: Id?
}

@one @subset interface first: Id
@one @subset interface last: Id

sealed interface Transition: StateMachine

@initial object Empty: Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    // initially inbox and outbox are empty
    + no(field(Node::inbox))
    + no(field(Node::outbox))
    // initially there are no elected nodes
    + no(set<Elected>())
  }
}

/*
pred initiate[n : Node] {
  // node n initiates the protocol

  historically n.id not in n.outbox          // guard

  n.outbox' = n.outbox + n.id                // effect on n.outbox
  all m : Node - n | m.outbox' = m.outbox    // effect on the outboxes of other nodes

  inbox'   = inbox                           // frame condition on inbox
  Elected' = Elected                         // frame condition on Elected
}
*/
data class Initiate(val n: KArg<Node>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + historically { not(n / Node::id `in` n / Node::outbox) }
    // effect on n.outbox
    + ( next(n / Node::outbox) `==` current(n / Node::outbox) + n / Node::id )
    // effect on the outboxes of other nodes
    + forAll(set<Node>() - n) { m -> stays(m / Node::outbox) }

    + stays(field(Node::inbox))  // frame condition on inbox
    + stays(set<Elected>())      // frame condition on Elected
  }
}

/*
pred send[n : Node, i : Id] {
  // i is sent from node n to its successor

  i in n.outbox                              // guard

  n.outbox' = n.outbox - i                   // effect on n.outbox
  all m : Node - n | m.outbox' = m.outbox    // effect on the outboxes of other nodes

  n.succ.inbox' = n.succ.inbox + i           // effect on n.succ.inbox
  all m : Node - n.succ | m.inbox' = m.inbox // effect on the inboxes of other nodes

  Elected' = Elected                         // frame condition on Elected
}
*/
data class Send(val n: KArg<Node>, val i: KArg<Id>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + (i `in` n / Node::outbox)

    + ( next(n / Node::outbox) `==` current(n / Node::outbox) - n / Node::id )
    + forAll(set<Node>() - n) { m -> stays(m / Node::outbox) }

    + ( next(n / Node::succ / Node::inbox) `==` current(n / Node::succ / Node::inbox) + i )
    + forAll(set<Node>() - n / Node::succ) { m -> stays(m / Node::inbox) }

    + stays(set<Elected>())
  }
}

/*
pred process[n : Node, i : Id] {
  // i is read and processed by node n

  i in n.inbox                                // guard

  n.inbox' = n.inbox - i                      // effect on n.inbox
  all m : Node - n | m.inbox' = m.inbox       // effect on the inboxes of other nodes

  gt[i,n.id] implies n.outbox' = n.outbox + i // effect on n.outbox
             else    n.outbox' = n.outbox
  all m : Node - n | m.outbox' = m.outbox     // effect on the outboxes of other nodes

  i = n.id implies Elected' = Elected + n     // effect on Elected
           else Elected' = Elected
  }
 */
/*
data class Process(val n: KArg<Node>, val i: KArg<Id>): Transition {
  override fun ReflectedModule.execute(): KFormula = and {
    + (i `in` n / Node::inbox)

    + ( next(n / Node::inbox) `==` current(n / Node::inbox) - i )
    + forAll(set<Node>() - n) { m -> stays(m / Node::inbox) }

    // + ( next(n / Node::succ / Node::inbox) `==` current(n / Node::succ / Node::inbox) + i )
    + forAll(set<Node>() - n) { m -> stays(m / Node::outbox) }

    // + stays(set<Elected>())
  }
}
*/

fun main() {
  execute {
    reflect(Node::class, Elected::class, Id::class, first::class, last::class)

    fact {
      forAll { n -> set<Node>() `in` n / closure(Node::succ) }
    }

    fact { some(set<Node>()) }
    fact { forAll { n -> not(n / Node::succ `==` n) } }

    fact { no (set<last>() / Id::next) }
    fact { set<Id>() `in` set<first>() / reflexiveClosureOptional(Id::next) }

    fact {
      forAll { i -> lone(Node::id / i) }
    }

    reflectMachine(Transition::class, skipName = "Stutter")

    run(4, 4, 4) { Constants.TRUE }.visualize()
  }
}