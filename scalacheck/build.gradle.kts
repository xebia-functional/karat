plugins {
  scala
}

dependencies {
  implementation(projects.karatCommon)
  implementation(libs.scalacheck)
  testImplementation(libs.scalacheckContrib)
  testRuntimeOnly(libs.junit.jupiter)
}
