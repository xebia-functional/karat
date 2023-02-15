package karat.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum

enum class Action {
  INCREMENT, READ
}

val model: ArbModel<Unit, Action> = StatelessArbModel { Arb.enum<Action>() }

fun right(action: Action, state: Int): Step<Int, Int> =
  when (action) {
    Action.INCREMENT -> Step(state = state + 1, response = 0)
    Action.READ -> Step(state = state, response = state + 1)
  }

fun wrong(action: Action, state: Int): Step<Int, Int> =
  when (action) {
    Action.INCREMENT -> Step(state = state + 1, response = 0)
    Action.READ -> {
      Step(state = state, response = if (state == 2) 0 else state + 1)
    }
  }

class StateMachineSpec: StringSpec({
  "at least zero" {
    checkAgainst(model, -2, ::right) {
      always(
        implies(
          should { it.action shouldBe Action.READ },
          should { it.response.shouldBeGreaterThanOrEqual(0) }
        )
      )
    }
  }
  "at least zero, version 2" {
    checkTraceAgainst(model, -2, ::right) {
      whenCurrent { item<Action, Int, Int> { it.action shouldBe Action.READ } }
      checkCurrent { item<Action, Int, Int> { it.response.shouldBeGreaterThanOrEqual(0) } }
    }
  }
  "always increasing" {
    checkAgainst(model, 0, ::wrong) {
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
    }
  }
  "always increasing, version 2" {
    checkTraceAgainst(model, 0, ::wrong) {
      whenCurrent { item<Action, Int, Int> { it.action shouldBe Action.READ } }
      val previousResponse = remember().getOrNull()!!.response
      oneOrMoreSteps()
      whenCurrent { item<Action, Int, Int> { it.action shouldBe Action.READ } }
      checkCurrent { item<Action, Int, Int> { it.response.shouldBeGreaterThanOrEqual(previousResponse) } }
    }
  }
})
