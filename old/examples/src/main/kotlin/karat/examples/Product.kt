package karat.examples

import karat.*
import karat.ast.*
import karat.reflection.*
import karat.ui.visualize

@one
data class Product(
  @reflect var available: Int
)

@abstract
interface Status {
  object Open: Status
  object CheckedOut: Status
}

data class Cart(
  @reflect var status: Status,
  @reflect var amount: Int
)

sealed interface Machine: StateMachineTransition
@initial object Initial: Machine {
  context(ReflectedModule) override fun execute() =
    forAll { c -> c / Cart::status `==` element<Status.Open>() }
}
@stutter object Stutter: Machine {
  context(ReflectedModule) override fun execute(): KFormula = and {
    +forAll { c -> stays(c / Cart::status) and stays(c / Cart::amount) }
    +stays(set<Product>() / Product::available)
  }
}
data class BuyProduct(val c: KArg<Cart>, val n: KArg<Int>): Machine {
  context(ReflectedModule) override fun execute() = and {
    +(current(c / Cart::status) `==` element<Status.Open>())
    +(next(c / Cart::status) `==` element<Status.Open>())
    +stays(c / Cart::amount)
    // things that stay the same
    +forAll(set<Cart>() - c) { other ->
      stays(other / Cart::status) and stays(other / Cart::amount)
    }
    +stays(set<Product>() / Product::available)
  }
}
data class CheckOut(val c: KArg<Cart>): Machine {
  context(ReflectedModule) override fun execute() = and {
    +(current(c / Cart::status) `==` element<Status.Open>())
    +(next(c / Cart::status) `==` element<Status.CheckedOut>())
    +stays(c / Cart::amount)
    // things that stay the same
    +forAll(set<Cart>() - c) { other ->
      stays(other / Cart::status) and stays(other / Cart::amount)
    }
    +stays(set<Product>() / Product::available)
  }
}

fun main() {
  execute {
    reflect(
      type<Status>(), type<Status.Open>(), type<Status.CheckedOut>(),
      type<Product>(), type<Cart>()
    )
    reflectMachineFromClass<Machine>()

    run(10, 5, 10) {
      eventually {
        forSome { c -> c / Cart::status `==` element<Status.CheckedOut>() }
      }
    }.visualize()
  }
}

// OLD VERSION
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