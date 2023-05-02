@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

plugins {
  id("karat-multiplatform")
  id("karat-publishing")
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
    val commonTest by getting {
      dependencies {
        implementation(libs.turbine)
        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotest.frameworkEngine)
        implementation(libs.kotest.assertionsCore)
      }
    }
    val jvmMain by getting
    val jvmTest by getting {
      dependencies {
        runtimeOnly(libs.kotest.runnerJUnit5)
      }
    }
  }
}
