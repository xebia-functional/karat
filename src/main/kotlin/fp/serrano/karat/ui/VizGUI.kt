package fp.serrano.karat.ui

import edu.mit.csail.sdg.alloy4.Computer
import edu.mit.csail.sdg.alloy4viz.VizGUI
import edu.mit.csail.sdg.translator.A4Solution

fun A4Solution.visualize() {
  var solution = this
  if (solution.satisfiable()) {
    solution.writeXML("thingy.xml")
    var viz: VizGUI? = null
    val computer = Computer { args ->
      val xmlFilename = (args as Array<String>).first()
      solution = solution.next()
      solution.writeXML(xmlFilename)
      viz?.loadXML(xmlFilename, true)
    }
    viz = VizGUI(true, "thingy.xml", null, computer, null, 1)
  } else {
    println("unsatisfiable")
  }
}