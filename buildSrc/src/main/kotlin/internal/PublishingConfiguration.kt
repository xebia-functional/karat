package internal

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

internal fun Project.configurePublish() {
  afterEvaluate {
    when {
      isJavaPlatform ->
        configurePublishingExtension(
          artifacts = emptyList(),
          components = listOf("javaPlatform"),
        )

      isKotlinMultiplatform ->
        configurePublishingExtension(
          artifacts = listOf(docsJar),
        )

      isKotlinJvm ->
        configurePublishingExtension(
          artifacts = listOf(docsJar, sourcesJar),
          components = listOf("java"),
        )
    }
  }
}

fun Project.configurePublishingExtension(
  artifacts: List<Jar>,
  components: List<String> = emptyList(),
) {
  configure<PublishingExtension> {
    publications {
      for (component in components) {
        create<MavenPublication>("maven") {
          from(this@configurePublishingExtension.components[component])
        }
      }

      withType<MavenPublication> {
        artifacts.forEach(::artifact)

        pom {
          name.set(pomName)
          description.set(pomDescription)
          url.set(pomUrl)

          licenses {
            license {
              name.set(pomLicenseName)
              url.set(pomLicenseUrl)
            }
          }

          developers {
            developer {
              id.set(pomDeveloperId)
              name.set(pomDeveloperName)
            }
          }

          scm {
            url.set(pomSmcUrl)
            connection.set(pomSmcConnection)
            developerConnection.set(pomSmcDeveloperConnection)
          }
        }
      }
    }
  }
}
