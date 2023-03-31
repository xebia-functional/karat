@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotest.multiplatform)
}

kotlin {
  explicitApi()

  // set targets
  jvm {
    jvmToolchain(8)
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.karatCommon)
        api(projects.karatKotlinTestCommon)
        implementation(libs.kotest.frameworkEngine)
        implementation(libs.kotest.assertionsCore)
        implementation(libs.kotest.property)
        // implementation(libs.kotlinx.coroutines)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(projects.karatCommon)
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
