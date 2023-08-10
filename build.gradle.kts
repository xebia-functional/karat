@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  alias(libs.plugins.kotlinx.serialization) apply false
  alias(libs.plugins.kotest.multiplatform) apply false
  alias(libs.plugins.gradle.nexus.publish)
}

allprojects {
  repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
  }

  group = "com.47deg.karat"

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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
