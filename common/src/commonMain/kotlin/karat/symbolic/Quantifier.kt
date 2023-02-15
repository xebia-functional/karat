package karat.symbolic

public fun <A> Expr<A>.all(predicate: (Expr<A>) -> Formula): Formula =
  Quantified(Quantifier.ALL, this, predicate)
public fun <A> Expr<A>.any(predicate: (Expr<A>) -> Formula): Formula =
  Quantified(Quantifier.EXISTS, this, predicate)
public fun <A> Expr<A>.single(predicate: (Expr<A>) -> Formula = { TRUE }): Formula =
  Quantified(Quantifier.SINGLE, this, predicate)
public fun <A> Expr<A>.atMostOne(predicate: (Expr<A>) -> Formula = { TRUE }): Formula =
  Quantified(Quantifier.OPTIONAL, this, predicate)
public fun <A> Expr<A>.optional(predicate: (Expr<A>) -> Formula): Formula =
  Quantified(Quantifier.OPTIONAL, this, predicate)
public fun <A> Expr<A>.none(predicate: (Expr<A>) -> Formula): Formula =
  Quantified(Quantifier.NO, this, predicate)

public fun <A> Expr<A>.filter(predicate: (Expr<A>) -> Formula): Expr<A> =
  SuchThat(this, predicate)

public fun <A> Expr<A>.isEmpty(): Formula = this.none { TRUE }
public fun <A> Expr<A>.isNotEmpty(): Formula = this.any { TRUE }