enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "karat"

include("karat-common")
project(":karat-common").projectDir = file("common")

include("karat-alloy")
project(":karat-alloy").projectDir = file("alloy")

include("karat-kotest")
project(":karat-kotest").projectDir = file("kotest")

include("karat-kotest-ktor")
project(":karat-kotest-ktor").projectDir = file("ktor")

include("karat-scalacheck")
project(":karat-scalacheck").projectDir = file("scalacheck")

include("karat-examples")
project(":karat-examples").projectDir = file("examples")
