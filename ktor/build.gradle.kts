@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

plugins {
  id("karat-jvm")
  id("karat-native")
  id("karat-publishing")
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.karatKotest)
        implementation(libs.kotest.frameworkEngine)
        implementation(libs.kotest.assertionsCore)
        implementation(libs.kotest.property)
        implementation(libs.ktor.client.resources)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(projects.karatCommon)
        implementation(libs.kotest.frameworkEngine)
        implementation(libs.kotest.assertionsCore)
        implementation(libs.ktor.client.resources)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.server.resources)
        implementation(libs.ktor.server.contentNegotiation)
        implementation(libs.ktor.server.test)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.serialization.json)
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
