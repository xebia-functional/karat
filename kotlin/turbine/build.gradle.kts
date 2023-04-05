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
        api(projects.karatKotlinTestCommon)
        api(libs.turbine)
      }
    }
    val commonTest by getting
    val jvmMain by getting
    val jvmTest by getting {
      dependencies {
        runtimeOnly(libs.kotest.runnerJUnit5)
      }
    }
  }
}
