import internal.configurePublish
import internal.signingKey
import internal.signingKeyId
import internal.signingPassphrase
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing

plugins {
  signing
  `maven-publish`
}

configurePublish()

val publications: PublicationContainer = extensions.getByName<PublishingExtension>("publishing").publications

signing {
  useGpgCmd()
  useInMemoryPgpKeys(signingKeyId, signingKey, signingPassphrase)
  sign(publications)
}

tasks.withType<AbstractPublishToMaven>() {
  dependsOn(tasks.withType<Sign>())
}
