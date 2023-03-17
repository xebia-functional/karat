plugins {
  scala
}

dependencies {
  implementation(projects.karatCommon)
  implementation(libs.scalacheck.core)
  implementation(libs.cats.core)
  testImplementation(libs.cats.effect)
  testImplementation(libs.scalacheck.contrib)
  testImplementation(libs.scalacheck.effect)
  testRuntimeOnly(libs.junit.jupiter)
}
