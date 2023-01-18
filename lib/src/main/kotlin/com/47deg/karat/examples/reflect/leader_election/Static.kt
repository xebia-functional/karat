package com.`47deg`.karat.examples.reflect.leader_election

import com.`47deg`.karat.*
import com.`47deg`.karat.ast.*
import com.`47deg`.karat.ui.visualize

// based on https://haslab.github.io/formal-software-design/protocol-design/index.html

interface SNode {
  val succ: SNode
  val id: Int

  companion object {
    fun Fact.ring(): KFormula = and {
      // SNodes form a ring
      + forAll<Node> { n -> set<SNode>() `in` n / oneOrMore(SNode::succ) }
      // all SNodes have unique id's
      + forAll { i -> atMostOne(SNode::id % i) }
    }
  }
}

fun main() {
  execute {
    reflect(reflectAll = true, type<SNode>())

    run(overall = 30, bitwidth = 3, scopes = listOf(exactly<SNode>(3))) {
      Constants.TRUE
    }.visualize()
  }
}
