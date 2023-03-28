@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  scala
  id("karat-publishing")
  alias(libs.plugins.scala.multiversion)
}

dependencies {
  implementation(projects.karatCommon)
  implementation(libs.scalacheck.core)
  implementation(libs.cats.core)
  testImplementation(libs.cats.effect)
  testImplementation(libs.scalacheck.effect)
  testImplementation(libs.munit.core)
  testImplementation(libs.munit.cats.effect)
  testImplementation(libs.scalacheck.effectMunit)
}

tasks.withType<Test>().configureEach {
  useJUnit()
}
