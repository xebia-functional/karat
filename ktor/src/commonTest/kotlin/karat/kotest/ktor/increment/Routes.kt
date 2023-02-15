package karat.kotest.ktor.increment

import kotlinx.serialization.Serializable
import io.ktor.resources.*

object Routes {
  @Serializable
  @Resource("/increment")
  class Increment(val amount: Int? = null)

  @Serializable
  @Resource("/value")
  class Value()
}
