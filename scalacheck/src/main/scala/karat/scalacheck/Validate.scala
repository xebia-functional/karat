package karat.scalacheck

import karat.concrete.*
import karat.concrete.progression.{ProgressionKt, StepResultManager}
import kotlin.coroutines.Continuation
import kotlin.jvm.functions
import org.scalacheck.Prop

import scala.util.Try
import scala.jdk.CollectionConverters.*

type ScalacheckAtomic[A] = Atomic[A, Prop.Result]
type ScalacheckFormula[A] = Formula[A, Prop.Result]

class ScalacheckStepResultManager[A] extends StepResultManager[Try[A], Prop.Result, Prop.Result] {
  override def getEverythingOk: Prop.Result = Prop.Result(Prop.True)
  override def getFalseFormula: Prop.Result = Prop.Result(Prop.False)
  override def getUnknown: Prop.Result = Prop.Result(Prop.Undecided)
  override def isOk(result: Prop.Result): Boolean = result.success
  override def andResults(results: java.util.List[? <: Prop.Result]): Prop.Result =
    results.asScala.fold(Prop.Result(Prop.True))((x: Prop.Result, y: Prop.Result) => x && y)
  override def orResults(results: java.util.List[? <: Prop.Result]): Prop.Result =
    results.asScala.fold(Prop.Result(Prop.False))((x: Prop.Result, y: Prop.Result) => x || y)
  override def negationWasTrue(formula: Formula[? >: Try[A], ? <: Prop.Result]): Prop.Result =
    Prop.Result(Prop.False).label("negation was true")
  override def shouldHoldEventually(formula: Formula[? >: Try[A], ? <: Prop.Result]): Prop.Result =
    Prop.Result(Prop.False).label("should hold eventually")
  override def predicate(test: functions.Function1[? >: Try[A], ? <: Prop.Result], value: Try[A], k: Continuation[? >: Prop.Result]): Any =
    ProgressionKt.resumeContinuation[Prop.Result](k, test.invoke(value))
}