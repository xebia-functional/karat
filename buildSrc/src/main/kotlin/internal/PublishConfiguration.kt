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
          components = mapOf("maven" to "javaPlatform"),
        )
      isKotlinMultiplatform ->
        configurePublishingExtension(
          artifacts = listOf(docsJar),
        )
      isKotlinJvm || isScala ->
        configurePublishingExtension(
          artifacts = listOf(docsJar, sourcesJar),
          components = mapOf("maven" to "java"),
        )
    }
  }
}

fun Project.configurePublishingExtension(
  artifacts: List<Jar>,
  components: Map<String, String> = emptyMap(),
) {
  val publishingExtension: PublishingExtension? = extensions.findByType()

  if (publishingExtension == null) {
    errorMessage("`maven-publish` plugin is not being applied")
  } else {
    configure<PublishingExtension> {
      publications {
        for ((name, component) in components) {
          create<MavenPublication>(name) { from(this@configurePublishingExtension.components[component]) }
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
}

val Project.pomName: String?
  get() = getVariable("pom.name", "POM_NAME")

val Project.pomDescription: String?
  get() = getVariable("pom.description", "POM_DESCRIPTION")

val Project.pomUrl: String?
  get() = getVariable("pom.url", "POM_URL")

val Project.pomLicenseName: String?
  get() = getVariable("pom.license.name", "POM_LICENSE_NAME")

val Project.pomLicenseUrl: String?
  get() = getVariable("pom.license.url", "POM_LICENSE_URL")

val Project.pomDeveloperId: String?
  get() = getVariable("pom.developer.id", "POM_DEVELOPER_ID")

val Project.pomDeveloperName: String?
  get() = getVariable("pom.developer.name", "POM_DEVELOPER_NAME")

val Project.pomDeveloperEmail: String?
  get() = getVariable("pom.developer.email", "POM_DEVELOPER_EMAIL")

val Project.pomSmcUrl: String?
  get() = getVariable("pom.smc.url", "POM_SMC_URL")

val Project.pomSmcConnection: String?
  get() = getVariable("pom.smc.connection", "POM_SMC_CONNECTION")

val Project.pomSmcDeveloperConnection: String?
  get() = getVariable("pom.smc.developerConnection", "POM_SMC_DEVELOPER_CONNECTION")


