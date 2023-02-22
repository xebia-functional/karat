package karat.scalacheck

import karat.concrete.NonSuspendedPredicate
import org.scalacheck.Prop

import scala.util.{Failure, Success}

object Formula {
  /**
   * Basic formula which checks that an item is produced, and satisfies the [predicate].
   */
  def holds[A](predicate: A => Prop.Result): Scalacheck.Atomic[A] =
    new NonSuspendedPredicate({
      case Success (value) => predicate(value)
      case Failure (_) => Prop.Result(Prop.False).label ("unexpected exception")
    })
}
