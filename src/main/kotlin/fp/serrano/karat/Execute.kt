package fp.serrano.karat

import edu.mit.csail.sdg.alloy4.A4Reporter
import edu.mit.csail.sdg.ast.Command
import edu.mit.csail.sdg.translator.A4Options
import edu.mit.csail.sdg.translator.A4Solution
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod
import fp.serrano.karat.ast.*
import kotlin.experimental.ExperimentalTypeInference

interface Execute {
  fun run(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution
  fun run(`for`: Int, but: Int, seq: Int, formula: () -> KFormula): A4Solution =
    run(`for`, but, seq, formula())
  fun check(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution
  fun check(`for`: Int, but: Int, seq: Int, formula: () -> KFormula): A4Solution =
    check(`for`, but, seq, formula())
}

data class ExecuteWithModule(val module: KModule, val options: A4Options, val reporter: A4Reporter = A4Reporter.NOP): Execute {
  override fun run(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      module.sigs.map { it.primSig },
      runCommand(`for`, but, seq, and(module.facts) and formula),
      options
    )
  override fun check(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      module.sigs.map { it.primSig },
      checkCommand(`for`, but, seq, and(module.facts) and not(formula)),
      options
    )
}

data class ExecuteWithBuilder(
  val options: A4Options,
  val reporter: A4Reporter = A4Reporter.NOP
): KModuleBuilder(), Execute {
  override fun run(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution =
    ExecuteWithModule(build(), options, reporter).run(`for`, but, seq, formula)
  override fun check(`for`: Int, but: Int, seq: Int, formula: KFormula): A4Solution =
    ExecuteWithModule(build(), options, reporter).check(`for`, but, seq, formula)
}

@OptIn(ExperimentalTypeInference::class)
fun <A> inModule(
  module: KModule,
  options: A4Options.() -> Unit = { solver = A4Options.SatSolver.SAT4J },
  reporter: A4Reporter = A4Reporter.NOP,
  @BuilderInference block: Execute.() -> A
): A = ExecuteWithModule(module, A4Options().also(options), reporter).run(block)

@OptIn(ExperimentalTypeInference::class)
fun <A> execute(
  options: A4Options.() -> Unit = { solver = A4Options.SatSolver.SAT4J },
  reporter: A4Reporter = A4Reporter.NOP,
  @BuilderInference block: ExecuteWithBuilder.() -> A
): A = ExecuteWithBuilder(A4Options().also(options), reporter).run(block)

fun runCommand(`for`: Int, but: Int, seq: Int, formula: KFormula): Command =
  Command(false, `for`, but, seq, formula.expr)

fun checkCommand(`for`: Int, but: Int, seq: Int, formula: KFormula): Command =
  Command(true, `for`, but, seq, formula.expr)
