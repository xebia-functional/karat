plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  api(libs.semver.gradlePlugin)
  implementation(libs.kotlin.gradle.plugin)
}
