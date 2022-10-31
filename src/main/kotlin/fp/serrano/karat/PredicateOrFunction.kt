package fp.serrano.karat

import edu.mit.csail.sdg.ast.Decl
import edu.mit.csail.sdg.ast.ExprVar
import edu.mit.csail.sdg.ast.Func

open class KFunction<R>(val func: Func)

class KFunction0<R>(func: Func): KFunction<R>(func)
class KFunction1<A, R>(func: Func): KFunction<R>(func)
class KFunction2<A, B, R>(func: Func): KFunction<R>(func)

typealias KPredicate = KFunction<TFormula>
typealias KPredicate0 = KFunction0<TFormula>
typealias KPredicate1<A> = KFunction1<A, TFormula>
typealias KPredicate2<A, B> = KFunction2<A, B, TFormula>

class KArg<A>(val decl: Decl): KSet<A>(decl.get()), KHasName {
  override val label: String = decl.get().label
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
  func(name, null, block)

fun <A> pred(
  name: String,
  arg1: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KPredicate1<A> = func(name, arg1, null, block)

fun <A, B> pred(
  name: String,
  arg1: Pair<String, KSet<A>>,
  arg2: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KPredicate2<A, B> = func(name, arg1, arg2,null, block)

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

fun <R> KSet<R>.arg(name: String): KArg<R> =
  KArg(Decl(null, null, null, null, listOf(ExprVar.make(null, name)), this.expr))