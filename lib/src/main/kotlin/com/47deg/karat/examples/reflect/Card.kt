package com.`47deg`.karat.examples.reflect

import com.`47deg`.karat.*
import com.`47deg`.karat.ast.*
import com.`47deg`.karat.ui.visualize

interface Name

@com.`47deg`.karat.abstract
sealed interface Power {
  object FIRE: Power
  object WATER: Power
  object AIR: Power
  object GROUND: Power
}

data class Attack(
  val name: Name,
  val cost: Set<Power>
)

@com.`47deg`.karat.abstract
sealed interface Card
data class MonsterCard(
  val name: Name,
  val attacks: Set<Attack>,
  val mutatesFrom: MonsterCard?
): Card {
  companion object {
    fun InstanceFact<MonsterCard>.noSelfMutation(): KFormula =
      not(self `in` self / MonsterCard::mutatesFrom)
  }
}
data class PowerCard(
  val type: Power
)

fun main() {
  execute {
    reflect(reflectAll = true,
      type<Power>(), type<Power.FIRE>(), type<Power.WATER>(), type<Power.AIR>(), type<Power.GROUND>(),
      type<Name>(), type<Attack>(),
      type<Card>(), type<MonsterCard>(), type<PowerCard>()
    )

    run(10) {
      Constants.TRUE
    }.visualize()
  }
}