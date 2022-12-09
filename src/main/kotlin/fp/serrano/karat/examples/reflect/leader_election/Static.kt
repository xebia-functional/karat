package fp.serrano.karat.examples.reflect.leader_election

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

interface SNode {
  val succ: SNode
  val id: Int

  companion object {
    fun Fact.ring(): KFormula = and {
      // SNodes form a ring
      + forAll { n -> set<SNode>() `in` n / oneOrMore(SNode::succ) }
      // all SNodes have unique id's
      + forAll { i -> atMostOne(SNode::id / i) }
    }
  }
}

fun main() {
  execute {
    reflect(reflectAll = true, SNode::class)

    run(overall = 30, bitwidth = 3, scopes = listOf(exactly<SNode>(3))) {
      Constants.TRUE
    }.visualize()
  }
}
