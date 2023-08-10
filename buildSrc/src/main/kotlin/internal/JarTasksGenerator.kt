package internal

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File

internal val Project.docsJar: Jar
  get() =
    tasks.create<Jar>("docsJar") {
      group = "build"
      description = "Assembles Javadoc jar file from for publishing"
      archiveClassifier.set("javadoc")

      if (tasks.names.contains("javadoc")) {
        from(tasks.named("javadoc"))
      }
    }

internal val Project.sourcesJar: Jar
  get() =
    tasks.create<Jar>("sourcesJar") {
      group = "build"
      description = "Assembles Sources jar file for publishing"
      archiveClassifier.set("sources")

      val sources: Iterable<File> =
        when {
          isKotlinMultiplatform -> emptySet()
          isJavaPlatform -> emptySet()
          isKotlinJvm -> {
            (project.properties["sourceSets"] as SourceSetContainer)["main"].allSource
          }
          else -> emptySet()
        }
      if (sources.toList().isNotEmpty()) from(sources)
    }
