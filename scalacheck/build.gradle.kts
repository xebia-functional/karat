plugins {
  scala
}

dependencies {
  implementation(projects.karatCommon)
  implementation(libs.scalacheck)
  implementation(libs.junitApi)
  implementation(libs.junitEngine)
}
