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

fun KModuleBuilder.addToCart2(c: KSet<Cart>, n: KSet<Int>) = and {
  +(current(c / Cart::status) `==` element<Status.Open>())
  +(next(c / Cart::status) `==` element<Status.Open>())
  +stays(c / Cart::amount)
}

fun KModuleBuilder.checkOut2(c: KSet<Cart>) = and {
  +(current(c / Cart::status) `==` element<Status.Open>())
  +(next(c / Cart::status) `==` element<Status.CheckedOut>())
  +stays(c / Cart::amount)
}

fun KModuleBuilder.productMachine() = stateMachine(skip = true) {
  initial {
    forAll("c") { c -> c / Cart::status `==` element<Status.Open>() }
  }
  transition2(::addToCart2)
  transition1(::checkOut2)
}

fun main() {
  execute {
    reflect(
      Status::class, Status.Open::class, Status.CheckedOut::class,
      Product::class, Cart::class
    )
    productMachine()

    run(4, 4, 4) {
      eventually {
        forSome("c") { c -> c / Cart::status `==` element<Status.CheckedOut>() }
      }
    }.visualize()
  }
}