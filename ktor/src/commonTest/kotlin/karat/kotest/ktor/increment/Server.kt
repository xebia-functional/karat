package karat.kotest.ktor.increment

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.counter() {
  install(ContentNegotiation) { json() }
  install(Resources)

  var counter = 0
  routing {
    get<Routes.Value> {
      call.respond(counter)
    }
    post<Routes.Increment> { req ->
      counter += req.amount ?: 1
      call.respond(true)
    }
  }
}
