package karat.alloy.example

import karat.alloy.exactly
import karat.alloy.execute
import karat.alloy.ui.visualize
import karat.symbolic.*

public interface SNode {
  @reflect public val succ: SNode
  @reflect public val id: Int

  public companion object {
    public fun InstanceFact<SNode>.ring(): Formula =
      set<SNode>() `in` self / SNode::succ.oneOrMoreSteps()
    public fun InstanceFact<SNode>.differentIds(): Formula =
      (SNode::id % self).atMostOne()
  }
}

public fun main() {
  execute {
    run(overall = 30, bitwidth = 3, scopes = listOf(exactly<SNode>(3))) {
      set<SNode>().isNotEmpty()
    }.visualize()
  }
}