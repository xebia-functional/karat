@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(files("../vendor/org.alloytools.alloy.dist.jar"))
  implementation(kotlin("reflect"))
}

kotlin {
  jvmToolchain(8)
}

tasks.withType<KotlinCompile> {
  kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")
}