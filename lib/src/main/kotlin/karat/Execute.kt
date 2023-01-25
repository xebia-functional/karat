package karat

import edu.mit.csail.sdg.alloy4.A4Reporter
import edu.mit.csail.sdg.ast.Command
import edu.mit.csail.sdg.ast.CommandScope
import edu.mit.csail.sdg.translator.A4Options
import edu.mit.csail.sdg.translator.A4Solution
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod
import karat.ast.*
import kotlin.experimental.ExperimentalTypeInference

interface Execute {
  fun run(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: KFormula): A4Solution
  fun run(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: () -> KFormula): A4Solution =
    run(overall, bitwidth, maxseq, steps, scopes, formula())
  fun check(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: KFormula): A4Solution
  fun check(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: () -> KFormula): A4Solution =
    check(overall, bitwidth, maxseq, steps, scopes, formula())
}

object ConsoleReporter: A4Reporter() {
  override fun solve(plength: Int, primaryVars: Int, totalVars: Int, clauses: Int) {
    println("Solving for $plength steps ($totalVars vars, $clauses clauses)")
    super.solve(plength, primaryVars, totalVars, clauses)
  }
}

data class ExecuteWithModule(val module: KModule, val options: A4Options, val reporter: A4Reporter = ConsoleReporter):
  Execute {
  override fun run(overall: Int?, bitwidth: Int?, maxseq: Int?, steps: IntRange?, scopes: List<SigScope>, formula: KFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      module.sigs.map { it.sig },
      runCommand(overall, bitwidth, maxseq, steps, scopes, and(module.facts + listOf(formula))),
      options
    )
  override fun check(overall: Int?, bitwidth: Int?, maxseq: Int?, steps: IntRange?, scopes: List<SigScope>, formula: KFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      module.sigs.map { it.sig },
      checkCommand(overall, bitwidth, maxseq, steps, scopes, and(module.facts + listOf(formula))),
      options
    )
}

data class ExecuteWithBuilder(
  val options: A4Options,
  val reporter: A4Reporter = ConsoleReporter
): KModuleBuilder(), Execute {
  override fun run(overall: Int?, bitwidth: Int?, maxseq: Int?, steps: IntRange?, scopes: List<SigScope>, formula: KFormula): A4Solution =
    ExecuteWithModule(build(), options, reporter).run(overall, bitwidth, maxseq, steps, scopes, formula)
  override fun check(overall: Int?, bitwidth: Int?, maxseq: Int?, steps: IntRange?, scopes: List<SigScope>, formula: KFormula): A4Solution =
    ExecuteWithModule(build(), options, reporter).check(overall, bitwidth, maxseq, steps, scopes, formula)
}

@OptIn(ExperimentalTypeInference::class)
fun <A> inModule(
  module: KModule,
  options: A4Options.() -> Unit = { solver = A4Options.SatSolver.SAT4J },
  reporter: A4Reporter = ConsoleReporter,
  @BuilderInference block: Execute.() -> A
): A = ExecuteWithModule(module, A4Options().also(options), reporter).run(block)

@OptIn(ExperimentalTypeInference::class)
fun <A> execute(
  options: A4Options.() -> Unit = { solver = A4Options.SatSolver.SAT4J },
  reporter: A4Reporter = ConsoleReporter,
  @BuilderInference block: ExecuteWithBuilder.() -> A
): A = ExecuteWithBuilder(A4Options().also(options), reporter).run(block)


data class SigScope(val sig: KSig<*>, val exactly: Boolean, val scope: Int)
fun exactly(sig: KSig<*>, scope: Int) = SigScope(sig, true, scope)

fun runCommand(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: KFormula): Command =
  buildCommand(false, overall, bitwidth, maxseq, steps, scopes, formula)

fun checkCommand(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: KFormula): Command =
  buildCommand(true, overall, bitwidth, maxseq, steps, scopes, not(formula))

private fun buildCommand(check: Boolean, overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: KFormula): Command =
  Command(
    null, null, null,
    check, overall ?: -1, bitwidth ?: -1, maxseq ?: -1,
    steps?.first ?: -1, steps?.last ?: -1, -1,
    scopes.map { CommandScope(it.sig.sig, it.exactly, it.scope) },
    emptyList(), formula.expr, null)
