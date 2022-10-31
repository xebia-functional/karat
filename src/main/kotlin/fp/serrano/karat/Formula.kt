package fp.serrano.karat

import edu.mit.csail.sdg.ast.Expr
import java.lang.IllegalStateException

interface TFormula

open class KFormula(expr: Expr): KExpr<TFormula>(expr)

class KFormulaBuilder {
  val formulae = mutableListOf<KFormula>()

  operator fun KFormula.unaryPlus() {
    formulae.add(this)
  }

  fun build(): KFormula = when {
    formulae.isEmpty() -> throw IllegalStateException("you need at least one formula")
    else -> formulae.reduce { acc, f -> acc.and(f) }
  }
}

fun (KFormulaBuilder.() -> Unit).build(): KFormula =
  KFormulaBuilder().also(this).build()

fun <A> (KFormulaBuilder.(A) -> Unit).build(x: A): KFormula =
  KFormulaBuilder().apply { this@build(x) }.build()

fun paragraph(block: KFormulaBuilder.() -> Unit): KFormula =
  block.build()

infix fun KFormula.or(other: KFormula): KFormula =
  KFormula(this.expr.or(other.expr))

infix fun KFormula.and(other: KFormula): KFormula =
  KFormula(this.expr.and(other.expr))

infix fun KFormula.iff(other: KFormula): KFormula =
  KFormula(this.expr.iff(other.expr))

infix fun KFormula.implies(other: KFormula): KFormula =
  KFormula(this.expr.implies(other.expr))