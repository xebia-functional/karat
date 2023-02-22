package karat.scalacheck

import karat.concrete.progression.regular.{CheckKt, RegularStepResultManager}
import karat.concrete.progression.{Info, Step}
import karat.scalacheck.KotlinUtils.resultToTry
import kotlin.jvm.functions
import org.scalacheck.Prop

import scala.util.Try
import scala.jdk.CollectionConverters._

object Scalacheck {
  type Atomic[A] = karat.concrete.Atomic[Try[A], Prop.Result]
  type Formula[A] = karat.concrete.Formula[Try[A], Prop.Result]

  class ScalacheckStepResultManager[A] extends RegularStepResultManager[A, Prop.Result, Prop.Result] {
    override def getEverythingOk: Prop.Result = Prop.Result(Prop.True)
    override def getFalseFormula: Prop.Result = Prop.Result(Prop.False)
    override def getUnknown: Prop.Result = Prop.Result(Prop.Undecided)
    override def isOk(result: Prop.Result): Boolean = result.success
    override def andResults(results: java.util.List[_ <: Prop.Result]): Prop.Result =
      results.asScala.fold(Prop.Result(Prop.True))((x: Prop.Result, y: Prop.Result) => x && y)
    override def orResults(results: java.util.List[_ <: Prop.Result]): Prop.Result =
      results.asScala.fold(Prop.Result(Prop.False))((x: Prop.Result, y: Prop.Result) => x || y)
    override def negationWasTrue(formula: karat.concrete.Formula[_ >: A, _ <: Prop.Result]): Prop.Result =
      Prop.Result(Prop.False).label("negation was true")
    override def shouldHoldEventually(formula: karat.concrete.Formula[_ >: A, _ <: Prop.Result]): Prop.Result =
      Prop.Result(Prop.False).label("should hold eventually")
    override def predicate(test: functions.Function1[_ >: A, _ <: Prop.Result], value: A): Prop.Result =
      test.invoke(value)
  }

  def checkFormula[Action, State, Response](actions: List[Action], initial: State, step: (Action, State) => Step[State, Response])(
    formula: Formula[Info[Action, State, Response]]
  ): Prop = {
    val problem = CheckKt.check[Action, State, Response, Prop.Result, Prop.Result](
      new ScalacheckStepResultManager(),
      formula.map(x => resultToTry(x.asInstanceOf[kotlin.Result[Info[Action, State, Response]]]), x => x),
      actions.asJava,
      initial,
      (action, current) => { step(action, current) },
      new java.util.ArrayList()
    )
    if (problem == null) Prop.passed else Prop(problem.getError)
  }
}