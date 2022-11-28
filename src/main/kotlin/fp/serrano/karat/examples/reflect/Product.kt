package fp.serrano.karat.examples.reflect

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

@one data class Product(
  @reflect var available: Int
)

@abstract interface Status {
  object Open: Status
  object CheckedOut: Status
}

data class Cart(
  @reflect var status: Status,
  @reflect var amount: Int
)

sealed interface Machine: StateMachine
@initial object Initial: Machine {
  override fun ReflectedModule.execute() =
    forAll { c -> c / Cart::status `==` element<Status.Open>() }
}
data class BuyProduct(val c: KArg<Cart>, val n: KArg<Int>): Machine {
  override fun ReflectedModule.execute() = and {
    + (current(c / Cart::status) `==` element<Status.Open>())
    + (next(c / Cart::status) `==` element<Status.Open>())
    + stays(c / Cart::amount)
    + forAll("x" to set<Cart>() - c) { other ->
      stays(other / Cart::status) and stays(other / Cart::amount)
    }
  }
}
data class CheckOut(val c: KArg<Cart>): Machine {
  override fun ReflectedModule.execute() = and {
    + (current(c / Cart::status) `==` element<Status.Open>())
    + (next(c / Cart::status) `==` element<Status.CheckedOut>())
    + stays(c / Cart::amount)
    + forAll("x" to set<Cart>() - c) { other ->
      stays(other / Cart::status) and stays(other / Cart::amount)
    }
  }
}

/*
fun KModuleBuilder.productMachine() = stateMachine(skip = true) {
  initial {
    forAll { c -> c / Cart::status `==` element<Status.Open>() }
  }
  transition("buys a product") { c, n: KArg<Int> -> and {
      + (current(c / Cart::status) `==` element<Status.Open>())
      + (next(c / Cart::status) `==` element<Status.Open>())
      + stays(c / Cart::amount)
      + forAll("x" to set<Cart>() - c) { other ->
        stays(other / Cart::status) and stays(other / Cart::amount)
      }
    }
  }
  transition("checks out") { c -> and {
      + (current(c / Cart::status) `==` element<Status.Open>())
      + (next(c / Cart::status) `==` element<Status.CheckedOut>())
      + stays(c / Cart::amount)
      + forAll("x" to set<Cart>() - c) { other ->
        stays(other / Cart::status) and stays(other / Cart::amount)
      }
    }
  }
}
*/

fun main() {
  execute {
    reflect(
      Status::class, Status.Open::class, Status.CheckedOut::class,
      Product::class, Cart::class
    )
    // productMachine()
    reflectMachine(Machine::class)

    run(10, 5, 10) {
      eventually {
        forSome { c -> c / Cart::status `==` element<Status.CheckedOut>() }
      }
    }.visualize()
  }
}