@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

plugins {
  `karat-publishing-config`
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotest.multiplatform)
}

kotlin {
  explicitApi()

  // set targets
  jvm {
    jvmToolchain(8)
    withJava()
  }

  js(IR) {
    browser()
    nodejs()
  }

  linuxX64()
  macosX64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.karatCommon)
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
    val jsMain by getting
    val jsTest by getting
    val linuxX64Main by getting
    val linuxX64Test by getting
    val macosX64Main by getting
    val macosX64Test by getting
  }
}
