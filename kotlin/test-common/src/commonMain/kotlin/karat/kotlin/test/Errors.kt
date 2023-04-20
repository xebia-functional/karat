package karat.kotlin.test

public open class StateAssertionError(
  public val state: Any?,
  public val problems: List<AssertionError>
): AssertionError() {
  override fun toString(): String =
    "StateAssertionError on $state" + problems.joinToString(prefix = "\n- ", separator = "\n- ")
}

public class TraceAssertionError(
  public val trace: List<*>,
  state: Any?,
  problems: List<AssertionError>
): StateAssertionError(state, problems)

public object FalseError: AssertionError("false formula")

public class NegationWasTrue(
  public val formula: KotlinTestFormula<*>
): AssertionError("negated condition was true") {
  public companion object {
    public operator fun <A> invoke(formula: KotlinTestFormula<A>): NegationWasTrue =
      NegationWasTrue(formula as KotlinTestFormula<*>)
  }
}

public class ShouldHoldEventually(
  public val formula: KotlinTestFormula<*>
): AssertionError("should hold eventually") {
  public companion object {
    public operator fun <A> invoke(formula: KotlinTestFormula<A>): ShouldHoldEventually =
      ShouldHoldEventually(formula as KotlinTestFormula<*>)
  }
}

public object DoneUnknown: AssertionError("cannot prove that it's finished")
