package karat.alloy.ui

import kotlin.io.path.createTempFile
import edu.mit.csail.sdg.alloy4.Computer
import edu.mit.csail.sdg.alloy4viz.VizGUI
import edu.mit.csail.sdg.translator.A4Solution
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString

@Suppress("UNCHECKED_CAST")
public fun A4Solution.visualize() {
  var solution = this
  if (solution.satisfiable()) {
    val tempPath = createTempFile("karat_")
    // write the first solution
    solution.writeXML(tempPath.pathString)
    var viz: VizGUI? = null
    val computer = Computer { args ->
      val xmlFilename = (args as Array<String>).first()
      solution = solution.next()
      solution.writeXML(xmlFilename)
      viz?.loadXML(xmlFilename, true)
    }
    viz = VizGUI(true, tempPath.pathString, null, computer, null, 2)
    viz.frame.addWindowListener(object: WindowListener {
      override fun windowClosed(e: WindowEvent?) {
        // at the end remove the file
        tempPath.deleteIfExists()
      }
      override fun windowOpened(e: WindowEvent?) { }
      override fun windowClosing(e: WindowEvent?) { }
      override fun windowIconified(e: WindowEvent?) { }
      override fun windowDeiconified(e: WindowEvent?) { }
      override fun windowActivated(e: WindowEvent?) { }
      override fun windowDeactivated(e: WindowEvent?) { }
    })
  } else {
    println("unsatisfiable")
  }
}