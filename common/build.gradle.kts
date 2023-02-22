@file:Suppress("DSL_SCOPE_VIOLATION", "UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

dependencies {

}

kotlin {
  explicitApi()
  jvm {
    jvmToolchain(8)
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines)
      }
    }
    val jvmMain by getting {
      dependencies {
        api(libs.kotlin.stdlibJdk)
        api(libs.kotlinx.coroutinesJvm)
        api(libs.kotlinx.coroutinesJdk)
      }
    }
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")
}
