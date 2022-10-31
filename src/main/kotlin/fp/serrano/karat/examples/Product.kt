package fp.serrano.karat.examples

import edu.mit.csail.sdg.ast.Attr
import fp.serrano.karat.*

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

fun <A> inProductModule(
  block: context(Product, Status, Cart) () -> A
): A = block(Product, Status, Cart)

val addToCard =
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
    }
  }
