package com.`47deg`.karat.ast

import edu.mit.csail.sdg.ast.ExprQt
import com.`47deg`.karat.ReflectedModule
import com.`47deg`.karat.set
import kotlin.reflect.typeOf

// temporal formulae

// just a trick to make code more readable
fun <A> current(e: KSet<A>): KSet<A> =
  e

fun <A> next(e: KSet<A>): KSet<A> =
  KSet(e.expr.prime())

fun <A> stays(e: KSet<A>): KFormula =
  next(e) `==` current(e)

fun always(formula: KFormula): KFormula =
  KFormula(formula.expr.always())

fun always(formula: () -> KFormula): KFormula =
  always(formula())

fun eventually(formula: KFormula): KFormula =
  KFormula(formula.expr.eventually())

fun eventually(formula: () -> KFormula): KFormula =
  eventually(formula())

fun historically(formula: KFormula): KFormula =
  KFormula(formula.expr.historically())

fun historically(formula: () -> KFormula): KFormula =
  historically(formula())

fun neverBefore(formula: KFormula): KFormula =
  historically(not(formula))

fun neverBefore(formula: () -> KFormula): KFormula =
  historically(not(formula()))

fun after(formula: KFormula): KFormula =
  KFormula(formula.expr.after())

fun after(formula: () -> KFormula): KFormula =
  after(formula())

fun before(formula: KFormula): KFormula =
  KFormula(formula.expr.before())

fun before(formula: () -> KFormula): KFormula =
  before(formula())

fun once(formula: KFormula): KFormula =
  KFormula(formula.expr.once())

fun once(formula: () -> KFormula): KFormula =
  once(formula())

infix fun KFormula.until(other: KFormula) =
  KFormula(this.expr.until(other.expr))

infix fun KFormula.releases(other: KFormula) =
  KFormula(this.expr.releases(other.expr))

infix fun KFormula.since(other: KFormula) =
  KFormula(this.expr.since(other.expr))

infix fun KFormula.triggered(other: KFormula) =
  KFormula(this.expr.triggered(other.expr))

// builder for temporal formulae

fun temporal(block: KTemporalFormulaBuilder.() -> Unit): KFormula =
  KTemporalFormulaBuilder().also(block).build()

class KTemporalFormulaBuilder {
  private val initials = mutableListOf<KFormula>()
  private val transitions = mutableListOf<KFormula>()
  private val checks = mutableListOf<KFormula>()

  fun initial(block: () -> KFormula) {
    initials.add(block())
  }

  fun transition(block: () -> KFormula) {
    transitions.add(block())
  }

  fun check(block: () -> KFormula) {
    checks.add(block())
  }

  fun build(): KFormula = and {
    + and(initials)
    + always(or(transitions))
    + and(checks)
  }

  fun KModule.skipTransition(): Unit =
    transition { skip() }

  fun <A> transition(
    x: Pair<String, KSet<A>>,
    block: (KArg<A>) -> KFormula
  ): Unit = transition {
    `for`(ExprQt.Op.SOME, x, block)
  }

  inline fun <reified A: Any> ReflectedModule.transition(
    name: String? = null,
    noinline block: (KArg<A>) -> KFormula
  ): Unit = transition(nextUnique(typeOf<A>()) to set<A>(), block)

  fun <A> transition(
    t1: KSet<A>,
    fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
  ): Unit = transition {
    `for`(ExprQt.Op.SOME, t1, fn)
  }

  inline fun <reified A: Any> ReflectedModule.transition1(
    fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
  ): Unit = transition(set<A>(), fn)

  fun <A, B> transition(
    x: Pair<String, KSet<A>>,
    y: Pair<String, KSet<B>>,
    block: (KArg<A>, KArg<B>) -> KFormula
  ): Unit = transition {
    `for`(ExprQt.Op.SOME, x, y, block)
  }

  inline fun <reified A: Any, reified B: Any> ReflectedModule.transition(
    name: String? = null,
    noinline block: (KArg<A>, KArg<B>) -> KFormula
  ): Unit = transition(nextUnique(typeOf<A>()) to set<A>(), nextUnique(typeOf<B>()) to set<B>(), block)

  fun <A, B> transition(
    t1: KSet<A>,
    t2: KSet<B>,
    fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
  ): Unit = transition {
    `for`(ExprQt.Op.SOME, t1, t2, fn)
  }

  inline fun <reified A: Any, reified B: Any> ReflectedModule.transition2(
    fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
  ): Unit = transition(set<A>(), set<B>(), fn)
}