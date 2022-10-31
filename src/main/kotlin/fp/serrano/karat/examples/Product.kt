package fp.serrano.karat.examples

import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.ExprQt
import fp.serrano.karat.*
import fp.serrano.karat.ui.visualize

// this is the actual definition

val productModule = module {
  sigs(Product, Status, Status.Open, Status.CheckedOut, Cart)
}

object Product: KSig<Product>("Product", Attr.ONE) {
  val available = variable("available", Sigs.SIGINT)
}

// the definition of an enumeration
object Status: KSig<Status>("Status", Attr.ABSTRACT) {
  object Open: KSig<Open>("Open", extends = Status, Attr.ONE) { }
  object CheckedOut: KSig<CheckedOut>("CheckedOut", extends = Status, Attr.ONE) { }
}

object Cart: KSig<Cart>("Cart") {
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

// this kinds of predicates could be derived automatically
fun skip2() = and {
  +stays(Product / Product.available)
  +`for`(ExprQt.Op.ALL, "c" to Cart) { c ->
    and {
      +stays(c / Cart.status)
      +stays(c / Cart.amount)
    }
  }
}

fun main() {
  inModule(productModule) {
    run(4, 4, 4) {
      and {
        // initial state
        + `for`(ExprQt.Op.ALL, "c" to Cart) { c -> c / Cart.status `==` Status.Open }
        // transition (could be auto-derived)
        + always {
          skip2() or
                  `for`(ExprQt.Op.SOME, "c" to Cart, "n" to Sigs.SIGINT, ::addToCart2) or
                  `for`(ExprQt.Op.SOME, "c" to Cart, ::checkOut2)
        }
        // things we want to happen
        + eventually {
          `for`(ExprQt.Op.SOME, "c" to Cart) { c -> c / Cart.status `==` Status.CheckedOut }
        }
      }
    }.visualize()
  }
}
