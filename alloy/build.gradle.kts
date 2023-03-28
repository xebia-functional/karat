@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("karat-publishing")
  id(libs.plugins.kotlin.jvm.get().pluginId)
}

dependencies {
  api(projects.karatCommon)
  api(libs.alloy)
  implementation(libs.javax.validation)
  implementation(kotlin("reflect"))
}

tasks.withType<KotlinCompile> {
  kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")
}
