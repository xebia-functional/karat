enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "karat"

include("karat-common")
project(":karat-common").projectDir = file("common")

include("karat-alloy")
project(":karat-alloy").projectDir = file("alloy")

include("karat-kotest")
project(":karat-kotest").projectDir = file("kotest")

include("karat-kotest-ktor")
project(":karat-kotest-ktor").projectDir = file("ktor")

include("karat-lib")
project(":karat-lib").projectDir = file("lib")

include("karat-examples")
project(":karat-examples").projectDir = file("examples")
