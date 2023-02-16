package karat.examples.increment

import karat.symbolic.*

public interface Counter {
  public fun increment()
  @reflect public val value: Int
}

public val Expr<Counter>.value: Expr<Int>
  get() = this / Counter::value

public class CounterImpl: Counter {
  override var value: Int = 0
  override fun increment() { value++ }
}

public sealed interface CounterAction

public class INCREMENT: CounterAction {
  public companion object: Transition1<Counter> {
    override fun execute(x: Expr<Counter>): Formula =
      next(x.value) `==` current(x.value) + 1
  }
}
