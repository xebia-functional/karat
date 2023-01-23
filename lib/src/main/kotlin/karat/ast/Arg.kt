package karat.ast

import edu.mit.csail.sdg.alloy4.Pos
import edu.mit.csail.sdg.ast.Decl
import edu.mit.csail.sdg.ast.ExprVar

class KArg<out A>(val decl: Decl): KSet<A>(decl.get()) {
  val label: String = decl.get().label
}

internal fun <R> KSet<R>.arg(name: String): KArg<R> =
  KArg(Decl(null, null, null, null, listOf(ExprVar.make(Pos.UNKNOWN, name, expr.type())), this.expr))