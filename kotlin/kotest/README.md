# Karat 💜 Kotest

> Black-box testing of stateful systems using properties

This library provides the ability to generate _traces_ of actions to test _properties_ of a _stateful_ system.
`karat-kotest` builds upon the [property-based testing](https://kotest.io/docs/proptest/property-based-testing.html)
capabilities of Kotest. In order to express how the system evolves over time, new _temporal_ operators have been
introduced, such as `always` and `eventually`.

When we test using properties, the inputs are [generated](https://kotest.io/docs/proptest/property-test-generators.html)
randomly, and then fed to the test body, that performs the corresponding checks. In any case, generators represent a
_single_ value. With `karat-kotest` you gain the ability to produce arbitrary _traces_ of _actions_, which are applied
in sequence. At each step, properties can be checked.

## Example: testing a counter

As an example of how the library works, let's test a _counter_. Any implementation should simply provide a means
to increment the inner counter, and to return the current value.

```kotlin
interface Counter {
  fun increment()
  val value: Int
}
```

The first step to apply `kotest-trace` is to define the _actions_ which can be performed over this interface. In this
case they closely correspond to the members of the interface, but if your system is more complex -- like a HTTP server --
this may not be the case.

```kotlin
enum class Action {
  INCREMENT, READ
}
```

The next step is to define a _model generator_ for this trace. The simplest option is to generate each simple action
independently of the previous ones. Since `Action` is an enumeration, we can use Kotest's `Arb.enum`.

```kotlin
val model: ArbModel<Unit, Action> = 
  StatelessArbModel { Arb.enum<Action>() }
```

In more complex scenarios the steps are not independent of each other -- for example, you may want to generate two
steps relating to the same identifier --, so you need to keep a small piece of information aside. Those cases are
covered by implementing the full `ArbModel` interface.

We still need to link our `Action`s with the particular implementation, by providing a function which takes a single
action and the current state, and generates the new state and a _response_. If you are familiar with reducers (or folds)
in other languages, this is the same idea.

```kotlin
fun apply(action: Action, counter: Counter): Step<Counter, Int> =
  when (action) {
    Action.INCREMENT -> Step(counter.increment(), 0)
    Action.READ -> Step(counter, counter.value)
  }
```

The fact that we have a response is important, because in our properties we're only able to check how the responses look
like, inspecting the state is not possible. In other words, we approach the system as a _black-box_, with which we can
only communicate with actions and responses.

We're ready to put the pieces together! Using `checkAgainst` we provide the model generator, the initial state for the
system under test, and the function to evolve it at every step.

```kotlin
"let's test" {
  checkAgainst(model, CounterUnderTest(), ::apply) {
    // property
  }
}
```

But how do we express a property of this implementation?

### Temporal properties

Let's begin with a simple one: we expect every `READ` to return a non-negative number. Since this is a property which
should be satisfied at every step, we use the `always` operator.

```kotlin
always(
  should {
    if (it.action == Action.READ)
      it.response.shouldBeGreaterThanOrEqual(0)
  }
)
```

This example shows two important characteristics of how we express properties. The first one is the aforementioned usage
of `always` to make the property apply to every step (if you are wondering; without it, it only applies to the _first_
step). The second is that we need to wrap checks with `should`, inside that block we can access both the action which was
applied and the response we obtained.

If the system under test works correctly, Kotest is happy. Otherwise, a problem is reported.

```
Property test failed for inputs

0) [READ, READ, READ, READ, READ, INCREMENT, READ, READ, READ, INCREMENT, INCREMENT, READ, READ, READ, READ, INCREMENT, INCREMENT, INCREMENT, INCREMENT, READ, ...and 80 more (set the 'kotest.assertions.collection.print.size' JVM property to see more / less items)]

Caused by TraceAssertionError(
  trace=[READ],
  state=Success(Info(action=READ, previousState=-2, nextState=-2, response=-1)),
  problems=[java.lang.AssertionError: -1 should be >= 0]) at
	com.fortyseven.kotest.trace.formula.ValidateKt.throwIfFailed(Validate.kt:40)
	com.fortyseven.kotest.trace.formula.ValidateKt.check(Validate.kt:29)
	com.fortyseven.kotest.trace.formula.ValidateKt.check$default(Validate.kt:16)
	com.fortyseven.kotest.trace.ArbModelKt$checkAgainst$2.invokeSuspend(ArbModel.kt:60)

Attempting to shrink arg [READ, READ, READ, READ, READ, INCREMENT, READ, READ, READ, INCREMENT, INCREMENT, READ, READ, READ, READ, INCREMENT, INCREMENT, INCREMENT, INCREMENT, READ, ...and 80 more (set the 'kotest.assertions.collection.print.size' JVM property to see more / less items)]
Shrink #1: [READ, READ, READ, READ, READ, INCREMENT, READ, READ, READ, INCREMENT, INCREMENT, READ, READ, READ, READ, INCREMENT, INCREMENT, INCREMENT, INCREMENT, READ, ...and 30 more (set the 'kotest.assertions.collection.print.size' JVM property to see more / less items)] fail
Shrink #2: [READ, READ, READ, READ, READ, INCREMENT, READ, READ, READ, INCREMENT, INCREMENT, READ, READ, READ, READ, INCREMENT, INCREMENT, INCREMENT, INCREMENT, READ, ...and 5 more (set the 'kotest.assertions.collection.print.size' JVM property to see more / less items)] fail
Shrink #3: [READ, READ, READ, READ, READ, INCREMENT, READ, READ, READ, INCREMENT, INCREMENT, READ] fail
Shrink #4: [READ, READ, READ, READ, READ, INCREMENT] fail
Shrink #5: [READ, READ, READ] fail
Shrink #6: [READ] fail
Shrink #7: [] pass
Shrink result (after 7 shrinks) => [READ]

```

As you can see, the first trace where this property failed is quite long, but the _shrinking_ process found that a
sequence with a single `READ` is already wrong. It also tells which of the formulae in the property failed -- in this
case, `shouldBeGreaterThanOrEqual(0)`. You can have more than one `should` block in your property, and each of them is
checked independently.

Now let's move to a more complex property, which showcases how you can relate different steps in the sequence. In this
case the property should state that if you read at a given moment, any later read should return a value at least as
large as the one you obtained.

```kotlin
always(
  implies(
    should { it.action shouldBe Action.READ },
    remember { current ->
      val rememberedResponse = current.getOrNull()!!.response
      afterwards(
        implies(
          should { it.action shouldBe Action.READ },
          should { it.response.shouldBeGreaterThanOrEqual(rememberedResponse) }
        )
      )
    }
  )
)
```

The outer operator is `always` again, since this is a property to apply at every step. We've introduced `implies`, which
specifies a conditional property, something which should only be checked when the condition holds. Using `implies`
instead of `if` is important for performance reasons, since the library is able to drop checks in the whole sequence
of states. Going back to our example, it says that if the `action` was `READ`, then we should continue with further
checks. The next operator, `remember`, allow us to save information from a previous state to be used in a following one.
We move to such a following state using `aftwards`, which specifies a property which should hold in _every_ step _after_
the current one. The final element is the test of the new response being larger than the previous one.

## Comparison with...

### ...white-box testing

There are other libraries geared towards testing of stateful systems, like [Hedgehog](https://hedgehogqa.github.io/scala-hedgehog/docs/guides-state-tutorial/) for Scala, and [quickcheck-state-machine](https://github.com/stevana/quickcheck-state-machine#readme) for Haskell. Those libraries emphasize a different approach of testing,
ín which the _state_ of the implementation is tested against the state of the model. This is more similar to
_white-box_ testing, that is, in testing in which the inner workings of the model are available to checks.

`kotest-trace` takes another approach, emphasizing the action/response model. There are few reasons that makes this
approach interesting. Those reasons stem from the fact that you don't need a _full_ model of the system in order to
test it, only a model of how actions are generated. Instead, the power lies on the temporal operators provided by the
library, which allow us to test "at a distance", relating different steps in the same trace.

One of the most difficult things to practice when learning about property-based testing is to not simply _replicate_
the desired implementation, but instead define properties which give enough room to different implementations, given
that they satisfy whatever is needed for the rest of the system to behave correctly. This problem is more pressing if
you use white-box testing, because you want to test _step-by-step_ that the inners of the implementation correspond to
the inner of the model. You're thus prone to overfit against the model.

The last reason to use black-box testing is that modelling some systems would require a huge investment, yet
specifying properties about traces of inputs is relatively easy. Think of an HTTP server, or a set of microservices.

### ...non-deterministic testing

Kotest already ships with some temporal operators, like [`eventually`](https://kotest.io/docs/assertions/eventually.html)
or [`continually`](https://kotest.io/docs/assertions/continually.html). Those operators are related to the ones in
`kotest-trace`, but apply to a completely different realm. Kotest built-in one talk about _actual time_, so you can
express things like "if we increment the counter, any read after 50 ms should return a greater number". This is
useful when testing the _concurrent_ properties of a piece of code.

In `kotest-trace` the temporal operators refer to _logical time_; each action counts as one further step, there's no
relation to a certain step taking more or less time. As such, the usage is different, because it talks about the
logical behavior of the code, not about its concurrency characteristics.