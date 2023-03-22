@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `karat-publishing-config`
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(projects.karatCommon)
  api(libs.alloy)
  implementation(libs.javax.validation)
  implementation(kotlin("reflect"))
}

kotlin {
  jvmToolchain(8)
  explicitApi()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")
}
