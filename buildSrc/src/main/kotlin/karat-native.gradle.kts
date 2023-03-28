@file:Suppress("UNUSED_VARIABLE")

plugins {
  id("karat-base")
}

kotlin {
  targets {
    linuxX64()
    macosX64()
  }

  sourceSets {
    val commonMain by getting {}
    val commonTest by getting {}
    val linuxX64Main by getting {}
    val linuxX64Test by getting {}
    val macosX64Main by getting {}
    val macosX64Test by getting {}
  }
}
