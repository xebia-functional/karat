package fp.serrano.karat

import edu.mit.csail.sdg.alloy4.Computer
import edu.mit.csail.sdg.alloy4viz.VizGUI
import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.translator.A4Options

// http://alloytools.org/documentation/code/ExampleUsingTheAPI.java.html

object A: KPrimSig<A>("A", Attr.ABSTRACT) {
  val f = field("f", B `lone --# lone` B)
  val g = field("g", oneOf(B))

  init {
    // fact { no(it / g) }  // unsatisfiable, since there must be one g
  }
}

object B: KPrimSig<B>("B") {
  val h = field("h", someOf(A))
}

fun main() {
  var solution = execute(
    sigs = listOf(A, B),
    options = { solver = A4Options.SatSolver.SAT4J }
  ) {
    run(3, 3, 3) {
      +some(A)
    }
  }
  println(solution.toString())

  if (solution.satisfiable()) {
    solution.writeXML("thingy.xml")
    var viz: VizGUI? = null
    val computer = Computer { args ->
      val xmlFilename = (args as Array<String>).first()
      solution = solution.next()
      solution.writeXML(xmlFilename)
      viz?.loadXML(xmlFilename, true)
    }
    viz = VizGUI(false, "thingy.xml", null, computer, null, 1)
  }
}
