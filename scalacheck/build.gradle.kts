plugins {
  scala
}

dependencies {
  implementation(projects.karatCommon)
  implementation(libs.scalacheck)
  implementation(libs.cats.core)
  testImplementation(libs.scalacheckContrib)
  testRuntimeOnly(libs.junit.jupiter)
}
