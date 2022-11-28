package fp.serrano.karat.ast

import edu.mit.csail.sdg.ast.Expr

open class KExpr<out A>(val expr: Expr)

interface KHasName {
  val label: String
}