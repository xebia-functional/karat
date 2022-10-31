package fp.serrano.karat

import edu.mit.csail.sdg.alloy4.Computer
import edu.mit.csail.sdg.alloy4viz.VizGUI
import edu.mit.csail.sdg.ast.Attr

// http://alloytools.org/documentation/code/ExampleUsingTheAPI.java.html
// http://alloytools.org/documentation/code/ExampleUsingTheCompiler.java.html

object A: KSig<A>("A", Attr.ABSTRACT) {
  val f = field("f", B `lone --# lone` B)
  val g = field("g", oneOf(B))

  init {
    // fact { no(it / g) }  // unsatisfiable, since there must be one g
  }
}

object A1: KSig<A1>("A1", extends = A)

object B: KSig<B>("B") {
  val h = field("h", someOf(A))
}

val world = module {
  sigs(A, A1, B)
  fact(no (A / A.g))
}

fun main() {
  var solution = execute(world) {
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
