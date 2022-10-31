package fp.serrano.karat

import edu.mit.csail.sdg.ast.Expr
import edu.mit.csail.sdg.ast.ExprConstant

interface TFormula

open class KFormula(expr: Expr): KExpr<TFormula>(expr)

// paragraphs are sets of formulae in conjunction

class KParagraphBuilder {
  val formulae = mutableListOf<KFormula>()

  operator fun KFormula.unaryPlus() {
    formulae.add(this)
  }

  fun build(): KFormula = and(formulae)
}

fun (KParagraphBuilder.() -> Unit).build(): KFormula =
  KParagraphBuilder().also(this).build()

fun <A> (KParagraphBuilder.(A) -> Unit).build(x: A): KFormula =
  KParagraphBuilder().apply { this@build(x) }.build()

fun paragraph(block: KParagraphBuilder.() -> Unit): KFormula =
  block.build()

// basic boolean constructions

object Constants {
  val TRUE: KFormula  = KFormula(ExprConstant.TRUE)
  val FALSE: KFormula = KFormula(ExprConstant.FALSE)
}

fun not(formula: KFormula): KFormula =
  KFormula(formula.expr.not())

infix fun KFormula.or(other: KFormula): KFormula =
  KFormula(this.expr.or(other.expr))

fun or(formulae: Collection<KFormula>): KFormula =
  when {
    formulae.isEmpty() -> Constants.FALSE
    else -> formulae.reduce { acc, f -> acc.or(f) }
  }

infix fun KFormula.and(other: KFormula): KFormula =
  KFormula(this.expr.and(other.expr))

fun and(formulae: Collection<KFormula>): KFormula =
  when {
    formulae.isEmpty() -> Constants.TRUE
    else -> formulae.reduce { acc, f -> acc.and(f) }
  }

fun and(block: KParagraphBuilder.() -> Unit): KFormula =
  paragraph(block)

infix fun KFormula.iff(other: KFormula): KFormula =
  KFormula(this.expr.iff(other.expr))

infix fun KFormula.implies(other: KFormula): KFormula =
  KFormula(this.expr.implies(other.expr))