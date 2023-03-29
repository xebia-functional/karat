package internal

import org.gradle.api.Project

val Project.signingKeyId: String?
  get() = project.properties["gpg.signing.keyId"]?.toString() ?: System.getenv("SIGNING_KEY_ID")

val Project.signingKey: String?
  get() = project.properties["gpg.signing.key"]?.toString() ?: System.getenv("SIGNING_KEY")

val Project.signingPassphrase: String?
  get() = project.properties["gpg.signing.passphrase"]?.toString() ?: System.getenv("SIGNING_KEY_PASSPHRASE")
