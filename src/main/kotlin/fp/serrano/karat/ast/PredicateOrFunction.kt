package fp.serrano.karat.ast

import edu.mit.csail.sdg.ast.ExprCall
import edu.mit.csail.sdg.ast.Func

// the functions in this file should only be used if tyou are defining a recursive function within Alloy
// (which is not really that common)
// in any other case just regular Kotlin functions which return a KFormula

open class KFunction<R>(val func: Func)

open class KFunction0<R>(func: Func): KFunction<R>(func) {
  open operator fun invoke(): KExpr<R> =
    KExpr(ExprCall.make(null, null, func, emptyList(), 0))
}
open class KFunction1<A, R>(func: Func): KFunction<R>(func) {
  open operator fun invoke(x: KExpr<A>): KExpr<R> =
    KExpr(ExprCall.make(null, null, func, listOf(x.expr), 0))
}
open class KFunction2<A, B, R>(func: Func): KFunction<R>(func) {
  open operator fun invoke(x: KExpr<A>, y: KExpr<B>): KExpr<R> =
    KExpr(ExprCall.make(null, null, func, listOf(x.expr, y.expr), 0))
}

class KPredicate0(pred: Func): KFunction0<TFormula>(pred) {
  constructor(f: KFunction0<TFormula>): this(f.func)
  override operator fun invoke(): KFormula =
    KFormula(ExprCall.make(null, null, func, emptyList(), 0))
}
class KPredicate1<A>(pred: Func): KFunction1<A, TFormula>(pred) {
  constructor(f: KFunction1<A, TFormula>): this(f.func)
  override operator fun invoke(x: KExpr<A>): KFormula =
    KFormula(ExprCall.make(null, null, func, listOf(x.expr), 0))
}
class KPredicate2<A, B>(pred: Func): KFunction2<A, B, TFormula>(pred) {
  constructor(f: KFunction2<A, B, TFormula>): this(f.func)
  override operator fun invoke(x: KExpr<A>, y: KExpr<B>): KFormula =
    KFormula(ExprCall.make(null, null, func, listOf(x.expr, y.expr), 0))
}

fun <R> func(name: String, returns: KSet<R>?, block: () -> KExpr<R>): KFunction0<R> =
  KFunction0(Func(null, null, name, emptyList(), returns?.expr, block().expr))

fun <A, R> func(
  name: String,
  arg1: Pair<String, KSet<A>>,
  returns: KSet<R>?,
  block: (KArg<A>) -> KExpr<R>
): KFunction1<A, R> {
  val decl1 = arg1.second.arg(arg1.first)
  return KFunction1(Func(null, null, name, listOf(decl1.decl), returns?.expr, block(decl1).expr))
}

fun <A, B, R> func(
  name: String,
  arg1: Pair<String, KSet<A>>,
  arg2: Pair<String, KSet<B>>,
  returns: KSet<R>?,
  block: (KArg<A>, KArg<B>) -> KExpr<R>
): KFunction2<A, B, R> {
  val decl1 = arg1.second.arg(arg1.first)
  val decl2 = arg2.second.arg(arg2.first)
  return KFunction2(Func(null, null, name, listOf(decl1.decl, decl2.decl), returns?.expr, block(decl1, decl2).expr))
}

fun body(block: KParagraphBuilder.() -> Unit): KFormula = block.build()

fun pred(name: String, block: () -> KFormula): KPredicate0 =
  KPredicate0(func(name, null, block))

fun <A> pred(
  name: String,
  arg1: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KPredicate1<A> = KPredicate1(func(name, arg1, null, block))

fun <A, B> pred(
  name: String,
  arg1: Pair<String, KSet<A>>,
  arg2: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KPredicate2<A, B> = KPredicate2(func(name, arg1, arg2,null, block))

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
