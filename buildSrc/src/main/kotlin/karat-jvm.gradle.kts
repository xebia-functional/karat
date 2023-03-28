@file:Suppress("UNUSED_VARIABLE")

plugins {
  id("karat-base")
}

kotlin {
  jvm {
    withJava()

    compilations.all {
      kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
      }
    }
  }

  sourceSets {
    val jvmMain by getting {}
    val jvmTest by getting {}
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}
