package karat.scalacheck

import cats.effect.IO
import karat.concrete.FormulaKt.{always, predicate}
import karat.concrete.progression.{Info, Step}
import karat.scalacheck.Scalacheck.{Formula, checkFormula}
import munit.ScalaCheckEffectSuite
import org.scalacheck.Prop._
import org.scalacheck.effect.PropF
import org.scalacheck.{Arbitrary, Gen, Prop}

class TestCounter extends ScalaCheckEffectSuite {

  object Action extends Enumeration {
    type Action = Value
    val Increment, Read = Value
  }
  import Action._

  val model: ArbModel[Unit, Action] = new StatelessArbModel[Action] {
    override def nexts(): Arbitrary[Option[Action]] = Arbitrary(Gen.some(Gen.oneOf(Action.Increment, Action.Read)))
  }

  val gen: Gen[Action.Value] = Gen.oneOf(Action.Increment, Action.Read)

  def right(action: Action, state: Int): Option[Step[Int, Int]] = Some(action match {
    case Action.Increment => new Step(state + 1, 0)
    case Action.Read => new Step(state, state)
  })

  def wrong(action: Action, state: Int): Option[Step[Int, Int]] = Some(action match {
    case Action.Increment =>
      new Step(state + 1, 0)
    case Action.Read =>
      new Step(state, if (state == 10) -1 else state)
  })

  def error(action: Action, state: Int): Option[Step[Int, Int]] = Some(action match {
    case Action.Increment => new Step(state + 1, 0)
    case Action.Read =>
      new Step(
        state,
        {
          if (state == 10) throw new RuntimeException("ERROR!")
          state + 1
        }
      )
  })

  def formula: Formula[Info[Action, Int, Int]] =
    always {
      predicate(
        (item: Info[Action, Int, Int]) => {
          // TODO: provide better accessors
          val status = item.getAction match {
            case Action.Read if item.getResponse >= 0 => Prop.True
            case Action.Read => Prop.False
            case _ => Prop.True
          }
          Prop.Result(status)
        }
      )
    }

  val initialState: Int = 0
  val stepAction: (Action, Int) => Option[Step[Int, Int]] = right
  val initialFormula: Formula[Info[Action, Int, Int]] = formula

  property("checkRight") {
    forAll(model.gen) { actions =>
      val result = checkFormula(actions, initialState, stepAction)(initialFormula)
      assert(result.success)
    }
  }

  test("checkRightIO") {
    PropF.forAllF[IO, List[Action], IO[Unit]](model.gen) { actions =>
      checkFormula(actions, IO(initialState), (action: Action, state: Int) => IO(stepAction(action, state)))(initialFormula).map { x =>
        assert(!x.success)
      }
    }
  }

  // property("checkWrong") = forAll(model.gen) { actions =>
  //   checkFormula(actions, initialState, wrong)(initialFormula)
  // }

  // property("checkThrow") = forAll(model.gen) { actions =>
  //   checkFormula(actions, initialState, error)(initialFormula)
  // }

}