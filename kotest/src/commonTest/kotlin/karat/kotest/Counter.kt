package karat.kotest

import io.kotest.assertions.shouldFail
import io.kotest.common.Platform
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import karat.concrete.progression.Info
import karat.concrete.progression.Step

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

inline fun shouldFailOn(platform: Platform, block: () -> Any?) {
  if (io.kotest.common.platform == platform)
    shouldFail(block)
  else
    block()
}

class StateMachineSpec: StringSpec({
  "at least zero" {
    shouldFail {
      checkAgainst(model, -2, ::right) {
        always(
          implies(
            should { it.action shouldBe Action.READ },
            should { it.response.shouldBeGreaterThanOrEqual(0) }
          )
        )
      }
    }
  }
  "at least zero, version 2" {
    shouldFailOn(Platform.JVM) {
      checkTraceAgainst(model, -2, ::right) {
        whenCurrent { should<Info<Action, Int, Int>> { it.action shouldBe Action.READ } }
        checkCurrent { should<Info<Action, Int, Int>> { it.response.shouldBeGreaterThanOrEqual(0) } }
      }
    }
  }
  "always increasing" {
    shouldFail {
      checkAgainst(model, 0, ::wrong) {
        always(
          implies(
            should { it.action shouldBe Action.READ },
            remember { current ->
              val rememberedResponse = current.response
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
  }
  "always increasing, version 2" {
    shouldFail {
      checkTraceAgainst(model, 0, ::wrong) {
        whenCurrent { should<Info<Action, Int, Int>> { it.action shouldBe Action.READ } }
        val previousResponse = remember().response
        oneOrMoreSteps()
        whenCurrent { should<Info<Action, Int, Int>> { it.action shouldBe Action.READ } }
        checkCurrent { should<Info<Action, Int, Int>> { it.response.shouldBeGreaterThanOrEqual(previousResponse) } }
      }
    }
  }
})
