package karat.concrete

import karat.FormulaBuilder
import karat.TraceFormulaBuilder

public fun <A, R, B> formula(
  block: FormulaBuilder<A, suspend (A) -> R, Formula<A, R>, Atomic<A, R>>.() -> B
): B = karat.formula(ConcreteFormulaBuilder(), block)

public fun <A, R> trace(
  block: suspend TraceFormulaBuilder<A, suspend (A) -> R, Formula<A, R>, Atomic<A, R>>.() -> Unit
): Formula<A, R> =
  karat.trace(ConcreteFormulaBuilder(), block)

public class ConcreteFormulaBuilder<A, R>: FormulaBuilder<A, suspend (A) -> R, Formula<A, R>, Atomic<A, R>> {
  override val `true`: Atomic<A, R> = TRUE
  override val `false`: Atomic<A, R> = FALSE
  override fun predicate(test: suspend (A) -> R): Atomic<A, R> = Predicate(test)
  override fun not(formula: Atomic<A, R>): Formula<A, R> = Not(formula)
  override fun and(formulae: List<Formula<A, R>>): Formula<A, R> = karat.concrete.and(formulae)
  override fun or(formulae: List<Formula<A, R>>): Formula<A, R> = karat.concrete.or(formulae)
  override fun implies(condition: Atomic<A, R>, then: Formula<A, R>): Formula<A, R> = karat.concrete.implies(condition, then)
  override fun next(formula: Formula<A, R>): Formula<A, R> = karat.concrete.next(formula)
  override fun always(formula: Formula<A, R>): Formula<A, R> = karat.concrete.always(formula)
  override fun eventually(formula: Formula<A, R>): Formula<A, R> = karat.concrete.eventually(formula)
  override fun remember(block: (A) -> Formula<A, R>): Formula<A, R> = Remember(block)
}

public sealed interface Formula<in A, out R> {
  public fun <B, S> map(
    inMap: (B) -> A, outMap: (R) -> S
  ): Formula<B, S> = when (this) {
    is Atomic -> map(inMap, outMap)
    is Not -> Not(formula.map(inMap, outMap))
    is Remember -> Remember { x -> block(inMap(x)).map(inMap, outMap) }
    is And -> And(formulae.map { it.map(inMap, outMap)} )
    is Or -> Or(formulae.map { it.map(inMap, outMap)} )
    is Implies -> Implies(condition.map(inMap, outMap), then.map(inMap, outMap))
    is Next -> Next(formula.map(inMap, outMap))
    is Always -> Always(formula.map(inMap, outMap))
    is Eventually -> Eventually(formula.map(inMap, outMap))
  }

  public infix fun and(other: Formula<@UnsafeVariance A, @UnsafeVariance R>): Formula<A, R> =
    ConcreteFormulaBuilder<A, R>().and(this, other)

  public infix fun or(other: Formula<@UnsafeVariance A, @UnsafeVariance R>): Formula<A, R> =
    ConcreteFormulaBuilder<A, R>().or(this, other)
}

public fun <A, R> and(formulae: List<Formula<A, R>>): Formula<A, R> {
  val everything =
    formulae.flatMap { if (it is And) it.formulae else listOf(it) }.filterNot { it == TRUE }
  return when {
    everything.isEmpty() -> TRUE
    FALSE in everything -> FALSE
    everything.size == 1 -> everything.single()
    else -> And(everything)
  }
}

public fun <A, R> or(formulae: List<Formula<A, R>>): Formula<A, R> {
  val everything =
    formulae.flatMap { if (it is Or) it.formulae else listOf(it) }.filterNot { it == FALSE }
  return when {
    everything.isEmpty() -> FALSE
    TRUE in everything -> TRUE
    everything.size == 1 -> everything.single()
    else -> Or(everything)
  }
}

public sealed interface Atomic<in A, out R>: Formula<A, R> {
  public override fun <B, S> map(
    inMap: (B) -> A, outMap: (R) -> S
  ): Atomic<B, S> = when (this) {
    TRUE -> TRUE
    FALSE -> FALSE
    is NonSuspendedPredicate -> NonSuspendedPredicate { x -> outMap(nonSuspendedTest(inMap(x))) }
    is Predicate -> Predicate { x -> outMap(test(inMap(x))) }
  }
}

public object TRUE: Atomic<Any?, Nothing>
public object FALSE: Atomic<Any?, Nothing>

public open class Predicate<in A, out R>(
  public val test: suspend (A) -> R
): Atomic<A, R>

public class NonSuspendedPredicate<in A, out R>(
  public val nonSuspendedTest: (A) -> R
): Predicate<A, R>({ nonSuspendedTest(it) })

public data class Not<in A, out R>(
  val formula: Atomic<A, R>
): Formula<A, R>

public data class Remember<in A, out R>(
  val block: (A) -> Formula<A, R>
): Formula<A, R>

public data class And<in A, out R>(
  val formulae: List<Formula<A, R>>
): Formula<A, R>

public data class Or<in A, out R>(
  val formulae: List<Formula<A, R>>
): Formula<A, R>

public data class Implies<in A, out R>(
  val condition: Atomic<A, R>,
  val then: Formula<A, R>
): Formula<A, R>

public data class Next<in A, out R>(
  val formula: Formula<A, R>
): Formula<A, R>

public data class Always<in A, out R>(
  val formula: Formula<A, R>
): Formula<A, R>

public data class Eventually<in A, out R>(
  val formula: Formula<A, R>
): Formula<A, R>

public fun <A, R> implies(condition: Atomic<A, R>, then: Formula<A, R>): Formula<A, R> =
  Implies(condition, then)

public fun <A, R> next(formula: Formula<A, R>): Formula<A, R> =
  Next(formula)

public fun <A, R> always(formula: Formula<A, R>): Formula<A, R> =
  Always(formula)

public fun <A, R> eventually(formula: Formula<A, R>): Formula<A, R> =
  Eventually(formula)
