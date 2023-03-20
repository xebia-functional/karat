@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `karat-publishing-config`
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  explicitApi()

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
    val commonMain by getting
    val jvmMain by getting
    val jsMain by getting
    val linuxX64Main by getting
    val macosX64Main by getting
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")
}
