enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "karat"

include("karat-common")
project(":karat-common").projectDir = file("common")

include("karat-alloy")
project(":karat-alloy").projectDir = file("alloy")

include("karat-kotlin-test-common")
project(":karat-kotlin-test-common").projectDir = file("kotlin/test-common")

include("karat-kotest")
project(":karat-kotest").projectDir = file("kotlin/kotest")

include("karat-kotest-ktor")
project(":karat-kotest-ktor").projectDir = file("kotlin/ktor")

include("karat-turbine")
project(":karat-turbine").projectDir = file("kotlin/turbine")

include("karat-scalacheck")
project(":karat-scalacheck").projectDir = file("scalacheck")

include("karat-examples")
project(":karat-examples").projectDir = file("examples")
