@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

plugins {
  id("karat-multiplatform")
  alias(libs.plugins.kotest.multiplatform)
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.karatCommon)
        api(libs.kotlin.test)
      }
    }
  }
}
