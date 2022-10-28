package fp.serrano.karat

import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.translator.A4Options

// http://alloytools.org/documentation/code/ExampleUsingTheAPI.java.html

object A: KPrimSig("A", Attr.ABSTRACT) {
  val f: KField = field("f", B lone_arrow_lone B)
  val g: KField = field("g", oneOf(B))
}

object B: KPrimSig("B") {
  val h: KField = field("h", someOf(A))
}

fun main() {
  val solution = execute(
    sigs = listOf(A, B),
    options = { solver = A4Options.SatSolver.SAT4J }
  ) {
    run(3, 3, 3, some(A))
  }
  println(solution.toString())
}
