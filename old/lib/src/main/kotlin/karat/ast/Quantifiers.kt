package karat.ast

import edu.mit.csail.sdg.ast.ExprQt

// quantification

fun <A> `for`(
  op: ExprQt.Op,
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula {
  val arg = x.second.arg(x.first)
  return KFormula(op.make(null, null, listOf(arg.decl), block(arg).expr))
}

fun <A> `for`(
  op: ExprQt.Op,
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(op, fn.parameters[0].name!! to t1, fn)

fun <A, B> `for`(
  op: ExprQt.Op,
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula {
  val arg1 = x.second.arg(x.first)
  val arg2 = y.second.arg(y.first)
  return KFormula(op.make(null, null, listOf(arg1.decl, arg2.decl), block(arg1, arg2).expr))
}

fun <A, B, C> `for`(
  op: ExprQt.Op,
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  z: Pair<String, KSet<C>>,
  block: (KArg<A>, KArg<B>, KArg<C>) -> KFormula
): KFormula {
  val arg1 = x.second.arg(x.first)
  val arg2 = y.second.arg(y.first)
  val arg3 = z.second.arg(z.first)
  return KFormula(op.make(null, null, listOf(arg1.decl, arg2.decl, arg3.decl), block(arg1, arg2, arg3).expr))
}

fun <A, B> `for`(
  op: ExprQt.Op,
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(op, fn.parameters[0].name!! to t1, fn.parameters[1].name!! to t2, fn)

// builder for all and some

fun <A> forAll(
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, block)

fun <A> forAll(
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(ExprQt.Op.ALL, t1, fn)

fun <A, B> forAll(
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, y, block)

fun <A, B> forAll(
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(ExprQt.Op.ALL, t1, t2, fn)

fun <A> forSome(
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, block)

fun <A> forSome(
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(ExprQt.Op.SOME, t1, fn)

fun <A, B> forSome(
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, y, block)

fun <A, B> forSome(
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(ExprQt.Op.SOME, t1, t2, fn)

fun <A> forNo(
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, x, block)

fun <A> forNo(
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(ExprQt.Op.NO, t1, fn)

fun <A, B> forNo(
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.NO, x, y, block)

fun <A, B> forNo(
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(ExprQt.Op.NO, t1, t2, fn)

fun <A> forOne(
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ONE, x, block)

fun <A> forOne(
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(ExprQt.Op.ONE, t1, fn)

fun <A, B> forOne(
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ONE, x, y, block)

fun <A, B> forOne(
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(ExprQt.Op.ONE, t1, t2, fn)

fun <A> forLone(
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.LONE, x, block)

fun <A> forLone(
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(ExprQt.Op.LONE, t1, fn)

fun <A, B> forLone(
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.LONE, x, y, block)

fun <A, B> forLone(
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(ExprQt.Op.LONE, t1, t2, fn)