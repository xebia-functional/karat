package karat.alloy.example

import karat.alloy.exactly
import karat.alloy.execute
import karat.alloy.ui.visualize
import karat.symbolic.*

public interface SNode {
  @reflect public val succ: SNode
  @reflect public val id: Int

  public companion object {
    public fun Fact.ring(): Formula = and(
      set<SNode>().all { n -> set<SNode>() `in` n / SNode::succ.oneOrMoreSteps() },
      set<SNode>().all { i -> (SNode::id % i).atMostOne() }
    )
  }
}

public fun main() {
  execute {
    run(overall = 30, bitwidth = 3, scopes = listOf(exactly<SNode>(3))) {
      set<SNode>().isNotEmpty()
    }.visualize()
  }
}