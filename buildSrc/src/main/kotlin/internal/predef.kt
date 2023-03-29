package internal

import org.gradle.api.Project

internal val Project.isJavaPlatform: Boolean
  get() = pluginManager.hasPlugin("org.gradle.java-platform")

internal val Project.isKotlinJvm: Boolean
  get() = pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")

internal val Project.isKotlinMultiplatform: Boolean
  get() = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")

internal val Project.isScala: Boolean
  get() = pluginManager.hasPlugin("org.gradle.scala")

internal fun Project.getVariable(propertyName: String, environmentVariableName: String): String? {
  val property: String? = project.properties[propertyName]?.toString()
  val environmentVariable: String? = System.getenv(environmentVariableName)

  val isPublish: Boolean = gradle.startParameter.taskNames.any { it.startsWith("publish") }

  when {
    property.isNullOrBlank() && environmentVariable.isNullOrBlank() && isPublish -> {
      errorMessage(
        "$propertyName Gradle property and " +
          "$environmentVariableName environment variable are missing",
      )
    }
  }

  return property ?: environmentVariable
}

internal fun Project.errorMessage(message: String) = logger.lifecycle("$YELLOW$message$RESET")

private const val RESET = "\u001B[0m"
private const val YELLOW = "\u001B[0;33m"
