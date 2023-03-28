@file:Suppress("UNUSED_VARIABLE")

plugins {
  id("karat-base")
}

kotlin {
  jvm {
    withJava()

    compilations.all {
      kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.majorVersion
      }
    }
  }

  sourceSets {
    val jvmMain by getting {}
    val jvmTest by getting {}
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}
