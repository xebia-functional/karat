package karat.scalacheck

import karat.util.UtilKt.{resultGetOrNull, resultExceptionOrNull}
import scala.util.Try
import scala.util.{Failure, Success, Try}

object KotlinUtils {
  def resultToTry[A](x: kotlin.Result[A]): Try[A] =
    if (resultGetOrNull(x) != null)
      Success(resultGetOrNull(x))
    else
      Failure(resultExceptionOrNull(x))
}