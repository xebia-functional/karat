package fp.serrano.karat

import edu.mit.csail.sdg.alloy4.A4Reporter
import edu.mit.csail.sdg.ast.Command
import edu.mit.csail.sdg.translator.A4Options
import edu.mit.csail.sdg.translator.A4Solution
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod
import kotlin.experimental.ExperimentalTypeInference

data class Execution(val sigs: List<KPrimSig>, val options: A4Options, val reporter: A4Reporter = A4Reporter.NOP) {
  fun run(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      sigs.map { it.primSig },
      runCommand(`for`, but, seq, formula),
      options
    )

  fun check(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      sigs.map { it.primSig },
      checkCommand(`for`, but, seq, formula),
      options
    )
}

@OptIn(ExperimentalTypeInference::class)
fun <A> execute(
  sigs: List<KPrimSig>,
  options: A4Options,
  reporter: A4Reporter = A4Reporter.NOP,
  @BuilderInference block: Execution.() -> A
): A = Execution(sigs, options, reporter).run(block)

fun options(block: A4Options.() -> Unit): A4Options =
  A4Options().also(block)

fun runCommand(`for`: Int, but: Int, seq: Int, formula: KFormula): Command =
  Command(false, `for`, but, seq, formula.expr)

fun checkCommand(`for`: Int, but: Int, seq: Int, formula: KFormula): Command =
  Command(true, `for`, but, seq, formula.expr)
