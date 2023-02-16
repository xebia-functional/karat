@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlinx.serialization) apply false
}

allprojects {
  repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
  }

  group = "com.47deg.karat"
  version = "0.1-SNAPSHOT"

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }
}
