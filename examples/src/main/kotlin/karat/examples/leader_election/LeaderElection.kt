package karat.examples.leader_election

import karat.alloy.alloy
import karat.alloy.exactly
import karat.alloy.ui.visualize
import karat.symbolic.*

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

public interface Node {
  @reflect public val succ: Node
  @reflect public val id: Int
  @reflect public var inbox: Set<Int>
  @reflect public var outbox: Set<Int>

  public companion object {
    @reflect public var Elected: Set<Node> by model

    public fun InstanceFact<Node>.ring(): Formula =
      set<Node>() `in` self / Node::succ.oneOrMoreSteps()
    public fun InstanceFact<Node>.differentIds(): Formula =
      (Node::id % self).atMostOne()
  }
}

public val Expr<Node>.succ: Expr<Node>
  get() = this / Node::succ
public val Expr<Node>.id: Expr<Int>
  get() = this / Node::id
public val Expr<Node>.inbox: Expr<Int>
  get() = (this / Node::inbox).flatten(Flattener.Set())
public val Expr<Node>.outbox: Expr<Int>
  get() = (this / Node::outbox).flatten(Flattener.Set())

public sealed interface LeaderElectionAction

@initial public class Empty: LeaderElectionAction {
  public companion object: Transition0 {
    override fun execute(): Formula = and(
      field(Node::inbox).isEmpty(),
      field(Node::outbox).isEmpty(),
      field(Node.Companion::Elected).isEmpty()
    )
  }
}

@stutter public class Stutter: LeaderElectionAction {
  public companion object: Transition0 {
    override fun execute(): Formula = and(
      set<Node>().all { n -> stays(n.inbox) and stays(n.outbox) },
      stays(Node.Companion::Elected)
    )
  }
}

public class Initiate: LeaderElectionAction {
  public companion object: Transition1<Node> {
    override fun execute(x: Expr<Node>): Formula = and(
      neverBefore(x.id `in` x.outbox),

      next(x.outbox) `==` current(x.outbox) + x.id,
      set<Node>().without(x).all { stays(it.outbox) },

      stays(Node::inbox),
      stays(Node.Companion::Elected)
    )
  }
}

public class Send: LeaderElectionAction {
  public companion object: Transition2<Node, Int> {
    override fun execute(x: Expr<Node>, y: Expr<Int>): Formula = and(
      y `in` current(x.outbox),

      next(x.outbox) `==` current(x.outbox) - y,
      set<Node>().without(x).all { stays(it.outbox) },

      next(x.succ.inbox) `==` current(x.succ.inbox) + y,
      set<Node>().without(x.succ).all { stays(it.inbox) },

      stays(Node.Companion::Elected)
    )
  }
}

public class Read: LeaderElectionAction {
  public companion object: Transition2<Node, Int> {
    override fun execute(x: Expr<Node>, y: Expr<Int>): Formula = and(
      y `in` current(x.inbox),

      next(x.inbox) `==` current(x.inbox) - y,
      set<Node>().without(x).all { stays(it.inbox) },

      (y gt x.id)
        .implies(next(x.outbox) `==` current(x.outbox) + y)
        .orElse(stays(x.outbox)),
      set<Node>().without(x).all { stays(it.outbox) },

      (y `==` x.id)
        .implies(next(Node.Companion::Elected) `==` current(Node.Companion::Elected).union(x))
        .orElse(stays(Node.Companion::Elected))
    )
  }
}

public fun main() {
  alloy {
    stateMachine<LeaderElectionAction>()
    run(overall = 30, bitwidth = 3, scopes = listOf(exactly<Node>(3))) {
      eventually {
        field(Node.Companion::Elected).isNotEmpty()
      }
    }.visualize()
  }
}
