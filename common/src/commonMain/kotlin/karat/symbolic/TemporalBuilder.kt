package karat.symbolic


public class TemporalFormulaBuilder {
  private val initials = mutableListOf<Formula>()
  private val transitions = mutableListOf<Formula>()
  private val checks = mutableListOf<Formula>()

  public fun initial(block: () -> Formula) {
    initials.add(block())
  }
  public fun check(block: () -> Formula) {
    checks.add(block())
  }
  public fun transition(block: () -> Formula) {
    transitions.add(block())
  }

  public fun build(): Formula = and(
    and(initials),
    always(or(transitions)),
    and(checks)
  )

  public inline fun <reified A> transition(
    noinline block: (Expr<A>) -> Formula
  ): Unit = transition(set(), block)
  public fun <A> transition(
    set: Expr<A>,
    block: (Expr<A>) -> Formula
  ): Unit = transition { set.any(block) }

  public inline fun <reified A, reified B> transition(
    noinline block: (Expr<A>, Expr<B>) -> Formula
  ): Unit = transition(set(), set(), block)
  public fun <A, B> transition(
    xs: Expr<A>, ys: Expr<B>,
    block: (Expr<A>, Expr<B>) -> Formula
  ): Unit = transition { xs.any { x -> ys.any { y -> block(x, y) } } }

  public inline fun <reified A, reified B, reified C> transition(
    noinline block: (Expr<A>, Expr<B>, Expr<C>) -> Formula
  ): Unit = transition(set(), set(), set(), block)
  public fun <A, B, C> transition(
    xs: Expr<A>, ys: Expr<B>, zs: Expr<C>,
    block: (Expr<A>, Expr<B>, Expr<C>) -> Formula
  ): Unit =
    transition { xs.any { x -> ys.any { y -> zs.any { z -> block(x, y, z) } } } }

  public inline fun <reified A, reified B, reified C, reified D> transition(
    noinline block: (Expr<A>, Expr<B>, Expr<C>, Expr<D>) -> Formula
  ): Unit = transition(set(), set(), set(), set(), block)
  public fun <A, B, C, D> transition(
    xs: Expr<A>, ys: Expr<B>, zs: Expr<C>, us: Expr<D>,
    block: (Expr<A>, Expr<B>, Expr<C>, Expr<D>) -> Formula
  ): Unit =
    transition { xs.any { x -> ys.any { y -> zs.any { z -> us.any { u -> block(x, y, z, u) } } } } }

}