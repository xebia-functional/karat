@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  alias(libs.plugins.kotlinx.serialization) apply false
  alias(libs.plugins.kotest.multiplatform) apply false
  alias(libs.plugins.gradle.nexus.publish) apply true
}

allprojects {
  repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
  }

  group = "com.47deg.karat"
  version = "0.1.0-SNAPSHOT"

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }

  tasks.withType<ScalaCompile>().configureEach {
    targetCompatibility = ""
  }
}

nexusPublishing {
  repositories {
    sonatype {
      username.set(System.getenv("SONATYPE_USER"))
      password.set(System.getenv("SONATYPE_PWD"))
      packageGroup.set("com.47deg.karat")
    }
  }
}
