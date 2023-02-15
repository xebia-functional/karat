package karat.alloy

import edu.mit.csail.sdg.alloy4.A4Reporter
import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.*
import edu.mit.csail.sdg.ast.Expr as AlloyExpr
import edu.mit.csail.sdg.translator.*
import karat.symbolic.*
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import karat.symbolic.Formula as KaratFormula

public data class SigScope(val type: KType, val exactly: Boolean, val scope: IntRange)
public inline fun <reified T> exactly(amount: Int): SigScope =
  SigScope(typeOf<T>(), true, amount .. amount)
public inline fun <reified T> around(amount: Int): SigScope =
  SigScope(typeOf<T>(), false, amount .. amount)
public inline fun <reified T> range(range: IntRange): SigScope =
  SigScope(typeOf<T>(), false, range)

@OptIn(ExperimentalTypeInference::class)
public fun <A> execute(
  options: A4Options.() -> Unit = { solver = A4Options.SatSolver.SAT4J },
  reporter: A4Reporter = ConsoleReporter,
  @BuilderInference block: AlloyExecutionBuilder.() -> A
): A = AlloyExecutionBuilder(A4Options().also(options), reporter).run(block)

public class AlloyExecutionBuilder(
  private val options: A4Options,
  private val reporter: A4Reporter = ConsoleReporter
): AlloyBuilder() {

  public fun run(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: () -> KaratFormula): A4Solution =
    run(overall, bitwidth, maxseq, steps, scopes, formula())
  public fun run(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: KaratFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      sigs,
      buildCommand(false, overall, bitwidth, maxseq, steps, scopes, ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.AND, facts + formula.translate())),
      options
    )

  public fun check(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: () -> KaratFormula): A4Solution =
    check(overall, bitwidth, maxseq, steps, scopes, formula())
  public fun check(overall: Int? = null, bitwidth: Int? = null, maxseq: Int? = null, steps: IntRange? = null, scopes: List<SigScope> = emptyList(), formula: KaratFormula): A4Solution =
    TranslateAlloyToKodkod.execute_command(
      reporter,
      sigs,
      buildCommand(true, overall, bitwidth, maxseq, steps, scopes, ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.AND, facts + formula.translate().not())),
      options
    )

  private fun buildCommand(
    check: Boolean,
    overall: Int? = null,
    bitwidth: Int? = null,
    maxseq: Int? = null,
    steps: IntRange? = null,
    scopes: List<SigScope> = emptyList(),
    formula: AlloyExpr
  ): Command =
    Command(
      null, null, null,
      check, overall ?: -1, bitwidth ?: -1, maxseq ?: -1,
      steps?.first ?: -1, steps?.last ?: -1, -1,
      scopes.map { CommandScope(null, set(it.type)!!, it.exactly, it.scope.first, it.scope.last, 1) },
      emptyList(), formula, null)
}

public object ConsoleReporter: A4Reporter() {
  override fun solve(plength: Int, primaryVars: Int, totalVars: Int, clauses: Int) {
    println("Solving for $plength steps ($totalVars vars, $clauses clauses)")
    super.solve(plength, primaryVars, totalVars, clauses)
  }
}


