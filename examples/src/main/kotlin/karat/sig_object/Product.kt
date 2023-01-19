package karat.sig_object

import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.ExprQt
import karat.*
import karat.ast.*
import karat.ui.visualize

// this is the actual definition

object Product: KPrimSig<Product>("Product", Attr.ONE) {
  val available = variable("available", Sigs.SIGINT)
}

// the definition of an enumeration
object Status: KPrimSig<Status>("Status", Attr.ABSTRACT) {
  object Open: KPrimSig<Open>("Open", extends = Status, Attr.ONE)
  object CheckedOut: KPrimSig<CheckedOut>("CheckedOut", extends = Status, Attr.ONE)
}

object Cart: KPrimSig<Cart>("Cart") {
  val status = variable("status", Status)
  val amount = variable("amount", Sigs.SIGINT)
}

fun addToCart2(c: KSet<Cart>, n: KSet<Int>) = and {
  +(current(c / Cart.status) `==` Status.Open)
  +(next(c / Cart.status) `==` Status.Open)
  +stays(c / Cart.amount)
}

fun checkOut2(c: KSet<Cart>) = and {
  +(current(c / Cart.status) `==` Status.Open)
  +(next(c / Cart.status) `==` Status.CheckedOut)
  +stays(c / Cart.amount)
}

val productModule: KModule = module {
  sigs(Product, Status, Status.Open, Status.CheckedOut, Cart)
  stateMachine(skip = true) {
    initial {
      `for`(ExprQt.Op.ALL, "c" to Cart) {
          c -> c / Cart.status `==` Status.Open
      }
    }
    transition(Cart, Sigs.SIGINT, ::addToCart2)
    transition(Cart, ::checkOut2)
  }
}

fun main() {
  inModule(productModule) {
    run(4, 4, 4) {
      eventually {
        `for`(ExprQt.Op.SOME, "c" to Cart) {
            c -> c / Cart.status `==` Status.CheckedOut
        }
      }
    }.visualize()
  }
}
