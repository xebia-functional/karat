import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("multiplatform")
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
  }
}

kotlin {
  explicitApi()
  jvmToolchain(8)

  sourceSets {
    all {
      languageSettings {
        optIn("kotlin.RequiresOptIn")
      }
    }
  }
}
