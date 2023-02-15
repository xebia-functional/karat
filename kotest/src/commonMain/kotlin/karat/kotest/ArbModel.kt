package karat.kotest

import io.kotest.property.Arb
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.checkAll
import karat.TraceFormulaBuilder
import karat.concrete.ConcreteFormulaBuilder
import karat.concrete.trace

public typealias KotestFormulaBuilder<ConcreteState, Action, Response> =
  ConcreteFormulaBuilder<Result<Info<Action, ConcreteState, Response>>, Unit>

public interface ArbModel<State, Action> {
  public val initial: State
  public fun nexts(state: State): Arb<Action?>
  public fun step(state: State, action: Action): State
}

public fun interface StatelessArbModel<Action>: ArbModel<Unit, Action> {
  override val initial: Unit
    get() = Unit
  override fun step(state: Unit, action: Action): Unit = Unit
  override fun nexts(state: Unit): Arb<Action?> = nexts()

  public fun nexts(): Arb<Action?>
}

public class PrefixShrinker<A>: Shrinker<List<A>> {
  override fun shrink(value: List<A>): List<List<A>> =
    listOf(
      value.take(value.size / 2),
      value.dropLast(1)
    )
}

public fun <State, Action> ArbModel<State, Action>.gen(
  range: IntRange = 1 .. 100
): Arb<List<Action>> = arbitrary(shrinker = PrefixShrinker()) {
  buildList(range.last) {
    var current = initial
    for (step in range) {
      when (val action = nexts(current).bind()) {
        null -> break
        else -> {
          add(action)
          current = step(current, action)
        }
      }
    }
  }
}

public suspend fun <AbstractState, ConcreteState, Action, Response> checkAgainst(
  model: ArbModel<AbstractState, Action>,
  initial: ConcreteState,
  step: suspend (Action, ConcreteState) -> Step<ConcreteState, Response>,
  range: IntRange = 1 .. 100,
  formula: ConcreteFormulaBuilder<Result<Info<Action, ConcreteState, Response>>, Unit>.() -> KotestFormula<Info<Action, ConcreteState, Response>>
) {
  checkAll(model.gen(range)) { actions ->
    ConcreteFormulaBuilder<Result<Info<Action, ConcreteState, Response>>, Unit>()
      .run(formula)
      .check(actions, initial, step)
  }
}

public suspend fun <AbstractState, ConcreteState, Action, Response> checkTraceAgainst(
  model: ArbModel<AbstractState, Action>,
  initial: ConcreteState,
  step: suspend (Action, ConcreteState) -> Step<ConcreteState, Response>,
  range: IntRange = 1 .. 100,
  formula: suspend TraceFormulaBuilder<
      Result<Info<Action, ConcreteState, Response>>,
      suspend (Result<Info<Action, ConcreteState, Response>>) -> Unit,
      KotestFormula<Info<Action, ConcreteState, Response>>,
      KotestAtomic<Info<Action, ConcreteState, Response>>
    >.() -> Unit
) {
  checkAll(model.gen(range)) { actions ->
    val translated = trace(formula)
    translated.check(actions, initial, step)
  }
}
