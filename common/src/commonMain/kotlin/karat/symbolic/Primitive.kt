package karat.symbolic

public fun <A> Expr<List<A>>.isEmpty(): Formula = ListIsEmpty(this)
public fun <A> Expr<List<A>>.isNotEmpty(): Formula = !this.isEmpty()

public val <A> Expr<List<A>>.elements: Expr<A>
  get() = ListElements(this)
public fun <A> Expr<List<A>>.filter(formula: (Expr<A>) -> Formula): Expr<A> =
  this.elements.filter(formula)

public fun <A> Expr<List<A>>.first(): Expr<A> = ListFirst(this)
public fun <A> Expr<List<A>>.rest(): Expr<List<A>> = ListRest(this)
public fun <A> Expr<List<A>>.add(x: Expr<A>): Expr<List<A>> = ListAdd(x, this)

public fun Int.expr(): Expr<Int> = NumberLiteral(this)

public infix fun Expr<Int>.gt(other: Expr<Int>): Formula = NumberComparison(NumberRelation.GT, this, other)
public infix fun Expr<Int>.gte(other: Expr<Int>): Formula = NumberComparison(NumberRelation.GTE, this, other)
public infix fun Expr<Int>.lt(other: Expr<Int>): Formula = NumberComparison(NumberRelation.LT, this, other)
public infix fun Expr<Int>.lte(other: Expr<Int>): Formula = NumberComparison(NumberRelation.LTE, this, other)

public infix fun Expr<Int>.gt(other: Int): Formula = NumberComparison(NumberRelation.GT, this, other.expr())
public infix fun Expr<Int>.gte(other: Int): Formula = NumberComparison(NumberRelation.GTE, this, other.expr())
public infix fun Expr<Int>.lt(other: Int): Formula = NumberComparison(NumberRelation.LT, this, other.expr())
public infix fun Expr<Int>.lte(other: Int): Formula = NumberComparison(NumberRelation.LTE, this, other.expr())