package karat.ast

import edu.mit.csail.sdg.ast.ExprCall
import edu.mit.csail.sdg.ast.Func

// the functions in this file should only be used if tyou are defining a recursive function within Alloy
// (which is not really that common)
// in any other case just regular Kotlin functions which return a KFormula

abstract class KFunctionOrPredicate(val func: Func)

abstract class KFunction<R>(func: Func): KFunctionOrPredicate(func)

class KFunction0<R>(func: Func): KFunction<R>(func) {
  operator fun invoke(): KSet<R> =
    KSet(ExprCall.make(null, null, func, emptyList(), 0))
}
class KFunction1<A, R>(func: Func): KFunction<R>(func) {
  operator fun invoke(x: KSet<A>): KSet<R> =
    KSet(ExprCall.make(null, null, func, listOf(x.expr), 0))
}
class KFunction2<A, B, R>(func: Func): KFunction<R>(func) {
  operator fun invoke(x: KSet<A>, y: KSet<B>): KSet<R> =
    KSet(ExprCall.make(null, null, func, listOf(x.expr, y.expr), 0))
}
class KFunction3<A, B, C, R>(func: Func): KFunction<R>(func) {
  operator fun invoke(x: KSet<A>, y: KSet<B>, z: KSet<C>): KSet<R> =
    KSet(ExprCall.make(null, null, func, listOf(x.expr, y.expr, z.expr), 0))
}

abstract class KPredicate(val pred: Func): KFunctionOrPredicate(pred)

class KPredicate0(pred: Func): KPredicate(pred) {
  operator fun invoke(): KFormula =
    KFormula(ExprCall.make(null, null, pred, emptyList(), 0))
}
class KPredicate1<A>(pred: Func): KPredicate(pred) {
  operator fun invoke(x: KSet<A>): KFormula =
    KFormula(ExprCall.make(null, null, pred, listOf(x.expr), 0))
}
class KPredicate2<A, B>(pred: Func): KPredicate(pred) {
  operator fun invoke(x: KSet<A>, y: KSet<B>): KFormula =
    KFormula(ExprCall.make(null, null, pred, listOf(x.expr, y.expr), 0))
}
class KPredicate3<A, B, C>(pred: Func): KPredicate(pred) {
  operator fun invoke(x: KSet<A>, y: KSet<B>, z: KSet<C>): KFormula =
    KFormula(ExprCall.make(null, null, pred, listOf(x.expr, y.expr, z.expr), 0))
}

fun <R> func(name: String, returns: KSet<R>?, block: () -> KSet<R>): KFunction0<R> =
  KFunction0(Func(null, null, name, emptyList(), returns?.expr, block().expr))

fun <A, R> func(
  name: String,
  arg1: Pair<String, KSet<A>>,
  returns: KSet<R>?,
  block: (KArg<A>) -> KSet<R>
): KFunction1<A, R> {
  val decl1 = arg1.second.arg(arg1.first)
  return KFunction1(Func(null, null, name, listOf(decl1.decl), returns?.expr, block(decl1).expr))
}

fun <A, B, R> func(
  name: String,
  arg1: Pair<String, KSet<A>>,
  arg2: Pair<String, KSet<B>>,
  returns: KSet<R>?,
  block: (KArg<A>, KArg<B>) -> KSet<R>
): KFunction2<A, B, R> {
  val decl1 = arg1.second.arg(arg1.first)
  val decl2 = arg2.second.arg(arg2.first)
  return KFunction2(Func(null, null, name, listOf(decl1.decl, decl2.decl), returns?.expr, block(decl1, decl2).expr))
}

fun body(block: KParagraphBuilder.() -> Unit): KFormula = block.build()

fun pred(name: String, block: () -> KFormula): KPredicate0 =
  KPredicate0(Func(null, null, name, emptyList(), null, block().expr))

fun <A> pred(
  name: String,
  arg1: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KPredicate1<A> {
  val decl1 = arg1.second.arg(arg1.first)
  return KPredicate1(Func(null, null, name, listOf(decl1.decl), null, block(decl1).expr))
}

fun <A, B> pred(
  name: String,
  arg1: Pair<String, KSet<A>>,
  arg2: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KPredicate2<A, B> {
  val decl1 = arg1.second.arg(arg1.first)
  val decl2 = arg2.second.arg(arg2.first)
  return KPredicate2(Func(null, null, name, listOf(decl1.decl, decl2.decl), null, block(decl1, decl2).expr))
}

fun predicate(name: String, block: KParagraphBuilder.() -> Unit): KPredicate0 =
  pred(name) { block.build() }

fun <A> predicate(
  name: String,
  arg1: Pair<String, KSet<A>>,
  block: KParagraphBuilder.(KArg<A>) -> Unit
): KPredicate1<A> = pred(name, arg1) { x -> block.build(x) }

fun <A, B> predicate(
  name: String,
  arg1: Pair<String, KSet<A>>,
  arg2: Pair<String, KSet<B>>,
  block: KParagraphBuilder.(KArg<A>, KArg<B>) -> Unit
): KPredicate2<A, B> = pred(name, arg1, arg2) { x, y -> block.build(x, y) }
