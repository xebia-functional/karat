package fp.serrano.karat.examples

import edu.mit.csail.sdg.ast.Attr
import fp.serrano.karat.KSig
import fp.serrano.karat.Sigs
import fp.serrano.karat.variable

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