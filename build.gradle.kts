@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotest.multiplatform) apply false
}

allprojects {
  repositories {
     mavenCentral()
  }

  group = "com.47deg.karat"
  version = "0.1-SNAPSHOT"

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }
}