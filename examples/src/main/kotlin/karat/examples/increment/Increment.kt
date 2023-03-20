package karat.examples.increment

import karat.symbolic.*

public interface Counter {
  public fun increment()
  @reflect public val value: Int
}

public val Expr<Counter>.value: Expr<Int>
  get() = this / Counter::value

public class GoodCounterImpl: Counter {
  override var value: Int = 0
  override fun increment() { value++ }
}

public class WrongCounterImpl: Counter {
  private var actualValue: Int = 0
  override val value: Int
    get() = if (actualValue % 3 == 0) 0 else actualValue
  override fun increment() { actualValue++ }
}

public sealed interface CounterAction

public class INCREMENT: CounterAction {
  public companion object: Transition1<Counter> {
    override fun execute(x: Expr<Counter>): Formula =
      next(x.value) eq current(x.value) + 1
  }
}

public class READ: CounterAction {
  // answers are additional arguments
  public companion object: Transition2<Counter, Int> {
    override fun execute(x: Expr<Counter>, y: Expr<Int>): Formula =
      y eq x.value
  }
}
