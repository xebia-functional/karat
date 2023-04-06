package karat.turbine

import io.kotest.assertions.shouldFail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBePositive
import io.kotest.matchers.result.shouldBeSuccess
import karat.concrete.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class SimpleTest: StringSpec({
  val alwaysPositive: TurbineFormula<Int> = always(
    predicate {it.shouldBeSuccess().shouldBePositive() }
  )

  val atSomePointPositive: TurbineFormula<Int> = eventually(
    predicate { it.shouldBeSuccess().shouldBePositive() }
  )

  "positive flow" {
    (1 .. 10).asFlow().testFormula { alwaysPositive }
    shouldFail {
      (-1 .. 10).asFlow().testFormula { alwaysPositive }
    }
    (-1 .. 10).asFlow().testFormula { atSomePointPositive }
  }

  val alwaysIncreasing: TurbineFormula<Int> = formula<Result<Int>, _, _> {
    always(
      remember { old ->
        afterwards(
          predicate { new ->
            val oldValue = old.shouldBeSuccess()
            val newValue = new.shouldBeSuccess()
            newValue.shouldBeGreaterThan(oldValue)
          }
        )
      }
    )
  }

  fun Flow<Int>.shakeIt(): Flow<Int> = flatMapConcat {
    flow {
      emit(it)
      if (it % 5 == 0) emit(it)
    }
  }

  "increasing flow" {
    (1 .. 10).asFlow().testFormula { alwaysIncreasing }
    shouldFail {
      (1..10).asFlow().shakeIt().testFormula { alwaysIncreasing }
    }
  }
})
