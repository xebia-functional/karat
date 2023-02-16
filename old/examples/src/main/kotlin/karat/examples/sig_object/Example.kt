package karat.examples.sig_object

import edu.mit.csail.sdg.ast.Attr
import karat.*
import karat.ast.*
import karat.ui.visualize

// http://alloytools.org/documentation/code/ExampleUsingTheAPI.java.html
// http://alloytools.org/documentation/code/ExampleUsingTheCompiler.java.html

object A: KPrimSig<A>("A", Attr.ABSTRACT) {
  val f = field("f", B `lone --# lone` B)
  val g = field("g", oneOf(B))

  init {
    // fact { no(it / g) }  // unsatisfiable, since there must be one g
  }
}

object A1: KPrimSig<A1>("A1", extends = A)

object B: KPrimSig<B>("B") {
  val h = field("h", someOf(A))
}

val world = module {
  sigs(A, A1, B)
  fact { no (A / A.g) }
}

fun main() {
  inModule(world) {
    run(3, 3, 3) {
      some(A)
    }.visualize()
  }
}
