package internal

import org.gradle.api.Project

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
