@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

plugins {
  id("karat-multiplatform")
  id("karat-publishing")
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
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
