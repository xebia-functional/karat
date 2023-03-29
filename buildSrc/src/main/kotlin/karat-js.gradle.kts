@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
  id("karat-base")
}

kotlin {
  targets {
    js(IR) {
      browser()
      nodejs()

      yarn.ignoreScripts = false
    }
  }

  sourceSets {
    val jsMain by getting {}
    val jsTest by getting {}
  }
}
