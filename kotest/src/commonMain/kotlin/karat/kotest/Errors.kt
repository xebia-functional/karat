package karat.kotest

public data class TraceAssertionError(
  public val trace: List<*>,
  public val state: Any?,
  public val problems: List<AssertionError>
): AssertionError()

public object FalseError: AssertionError("false formula")

public data class NegationWasTrue(
  public val formula: KotestFormula<*>
): AssertionError("negated condition was true") {
  public companion object {
    public operator fun <A> invoke(formula: KotestFormula<A>): NegationWasTrue =
      NegationWasTrue(formula as KotestFormula<*>)
  }
}

public data class ShouldHoldEventually(
  public val formula: KotestFormula<*>
): AssertionError("should hold eventually") {
  public companion object {
    public operator fun <A> invoke(formula: KotestFormula<A>): ShouldHoldEventually =
      ShouldHoldEventually(formula as KotestFormula<*>)
  }
}

public object DoneUnknown: AssertionError("cannot prove that it's finished")
