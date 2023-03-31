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
        api(libs.kotlin.test)
      }
    }
  }
}
