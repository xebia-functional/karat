package fp.serrano.karat.examples

import edu.mit.csail.sdg.ast.Attr
import edu.mit.csail.sdg.ast.ExprQt
import fp.serrano.karat.*
import fp.serrano.karat.ui.visualize

// this is the actual definition

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

val productModule = module {
  sigs(Product, Status, Status.Open, Status.CheckedOut, Cart)
}

// this allows us to use the field names directly
fun <A> inProductModule(
  block: context(Product, Status, Cart) () -> A
): A = block(Product, Status, Cart)

// definition of functions within Alloy

val addToCart =
  inProductModule {
    predicate("addToCart", "c" to Cart, "n" to Sigs.SIGINT) { c, n ->
      +(c / status `==` Status.Open)
      +(next(c / status) `==` Status.Open)
    }
  }

val skip =
  inProductModule {
    predicate("skip") {
      +(next(Product / available) `==` Product / available)
      +(`for`(ExprQt.Op.ALL, "c" to Cart) { c ->
        and {
          +(next(c / status) `==` c / status)
          +(next(c / amount) `==` c / amount)
        }
      })
    }
  }

// should we really model predicates, or just use regular functions?
// we can always use a plug-in to translate functions to the definitions

fun addToCart2(c: KSet<Cart>, n: KSet<Int>) = and {
  +(c / Cart.status `==` Status.Open)
  +(next(c / Cart.status) `==` Status.Open)
}

fun skip2() = and {
  +(next(Product / Product.available) `==` Product / Product.available)
  +(`for`(ExprQt.Op.ALL, "c" to Cart) { c ->
    and {
      +(next(c / Cart.status) `==` c / Cart.status)
      +(next(c / Cart.amount) `==` c / Cart.amount)
    }
  })
}

fun main() {
  inModule(productModule) {
    run(4, 4, 4) {
      always { skip2() }
    }.visualize()
  }
}
