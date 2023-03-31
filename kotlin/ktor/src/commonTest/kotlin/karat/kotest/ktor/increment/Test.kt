package karat.kotest.ktor.increment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choose
import io.kotest.property.arbitrary.constant
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import karat.kotest.ktor.*
import karat.kotest.StatelessArbModel
import karat.kotlin.test.should

inline fun <reified R : Any> performTest(
  model: StatelessArbModel<RequestInfo<R>>,
  noinline formula: HttpFormulaBuilder<R>.() -> HttpFormula<R>
): Unit = testApplication {
  application { counter() }
  val client = createClient {
    install(ContentNegotiation) { json() }
    install(Resources)
  }
  client.checkAgainst(model) { formula() }
}

val simpleModel: StatelessArbModel<RequestInfo<Any>> = StatelessArbModel {
  Arb.choose(
    1 to Arb.constant(Trace.get(Routes.Value())),
    1 to Arb.constant(Trace.post(Routes.Increment()))
  )
}

class StateMachineSpec: StringSpec({
  "at least zero" {
    performTest(simpleModel) {
      always(
        should {
          when (it.action.resource) {
            is Routes.Value -> it.response.body<Int>().shouldBeGreaterThanOrEqual(0)
            else -> { }
          }
        }
      )
    }
  }
})
