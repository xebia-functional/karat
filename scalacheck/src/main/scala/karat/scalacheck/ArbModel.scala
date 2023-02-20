package karat.scalacheck

import org.scalacheck.{Arbitrary, Gen, Prop, Shrink, Test}

trait ArbModel[State, Action] {
  def initial: State
  def nexts(state: State): Arbitrary[Option[Action]]
  def step(state: State, action: Action): State

  def gen: Gen[List[Action]] = Gen.sized { size =>
    (1 to size)
      .foldLeft(Gen.const[(List[Action], State, Boolean)]((Nil, initial, false))) { case (genState, _) =>
        genState.flatMap {
          case (l, state, true) => Gen.const((l, state, true))
          case (l, state, false) =>
            nexts(state).arbitrary
              .map(_.fold((l, state, true))(a => (l :+ a, step(state, a), false)))
        }
      }.map(_._1)
  }
}

trait StatelessArbModel[Action] extends ArbModel[Unit, Action] {
  val initial: Unit = {}
  def nexts(state: Unit): Arbitrary[Option[Action]] = nexts()
  def step(state: Unit, action: Action): Unit = {}
  def nexts(): Arbitrary[Option[Action]]
}