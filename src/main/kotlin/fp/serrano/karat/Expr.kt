package fp.serrano.karat

import edu.mit.csail.sdg.ast.Expr

open class KExpr<A>(val expr: Expr)

open class KExprWithName<A>(expr: Expr): KExpr<A>(expr)