ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "com.47deg.karat"

ThisBuild / resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .settings(
    name := "Karat SBT example",
    libraryDependencies ++= List(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.8",
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
      "org.typelevel" %% "scalacheck-effect" % "1.0.4" % Test,
      "com.47deg.karat" %% "karat-scalacheck" % "0.1-SNAPSHOT",
    )
  )