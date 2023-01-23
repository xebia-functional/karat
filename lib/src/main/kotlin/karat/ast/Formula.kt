package karat.ast

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.Expr
import edu.mit.csail.sdg.ast.ExprConstant
import edu.mit.csail.sdg.ast.ExprITE
import edu.mit.csail.sdg.ast.ExprList

open class KFormula(val expr: Expr)

// paragraphs are sets of formulae in conjunction

class KParagraphBuilder {
  private val formulae = mutableListOf<KFormula>()

  operator fun KFormula.unaryPlus() {
    formulae.add(this)
  }

  fun build(): KFormula = and(formulae)
}

fun (KParagraphBuilder.() -> Unit).build(): KFormula =
  KParagraphBuilder().also(this).build()

fun <A> (KParagraphBuilder.(A) -> Unit).build(x: A): KFormula =
  KParagraphBuilder().apply { this@build(x) }.build()

fun <A, B> (KParagraphBuilder.(A, B) -> Unit).build(x: A, y: B): KFormula =
  KParagraphBuilder().apply { this@build(x, y) }.build()

fun paragraph(block: KParagraphBuilder.() -> Unit): KFormula =
  block.build()

// basic boolean constructions

object Constants {
  val TRUE: KFormula = KFormula(ExprConstant.TRUE)
  val FALSE: KFormula = KFormula(ExprConstant.FALSE)
}

fun not(formula: KFormula): KFormula =
  KFormula(formula.expr.not())

infix fun KFormula.or(other: KFormula): KFormula =
  KFormula(this.expr.or(other.expr))

fun or(formulae: Collection<KFormula>): KFormula =
  KFormula(ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.OR, formulae.map { it.expr }))

infix fun KFormula.and(other: KFormula): KFormula =
  KFormula(this.expr.and(other.expr))

fun and(formulae: Collection<KFormula>): KFormula =
  KFormula(ExprList.make(Pos.UNKNOWN, Pos.UNKNOWN, ExprList.Op.AND, formulae.map { it.expr }))

fun and(block: KParagraphBuilder.() -> Unit): KFormula =
  paragraph(block)

infix fun KFormula.iff(other: KFormula): KFormula =
  KFormula(this.expr.iff(other.expr))

infix fun KFormula.implies(other: KFormula): KFormula =
  KFormula(this.expr.implies(other.expr))

fun KFormula.ifThen(ifTrue: KFormula, ifFalse: KFormula): KFormula =
  KFormula(ExprITE.make(Pos.UNKNOWN, this.expr, ifTrue.expr, ifFalse.expr))

// comparisons

infix fun <A> KSet<A>.`==`(other: KSet<A>): KFormula =
  KFormula(this.expr.equal(other.expr))

infix fun <A> KSet<A>.`!=`(other: KSet<A>): KFormula =
  not(KFormula(this.expr.equal(other.expr)))

infix fun <A> KSet<A>.`in`(other: KSet<A>): KFormula =
  KFormula(this.expr.`in`(other.expr))
