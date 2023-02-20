package karat.scalacheck

import karat.concrete.*
import karat.concrete.progression.StepResultManager
import org.scalacheck.Prop
import scala.util.Try
import scala.jdk.CollectionConverters._

type ScalacheckAtomic[A] = Atomic[A, Prop.Result]
type ScalacheckFormula[A] = Formula[A, Prop.Result]

class ScalacheckStepResultManager[A] extends StepResultManager[Try[A], Prop.Result, Prop.Result] {
  override def getEverythingOk: Prop.Result = Prop.Result(Prop.True)
  override def getFalseFormula: Prop.Result = Prop.Result(Prop.False)
  override def getUnknown: Prop.Result = Prop.Result(Prop.Undecided)
  override def isOk(result: Prop.Result): Boolean = result.success
  override def andResults(results: java.util.List[Prop.Result]): Prop.Result =
    results.asScala.fold(Prop.Result(Prop.True))((x: Prop.Result, y: Prop.Result) => x && y)
  override def orResults(results: java.util.List[Prop.Result]): Prop.Result =
    results.asScala.fold(Prop.Result(Prop.False))((x: Prop.Result, y: Prop.Result) => x || y)
}