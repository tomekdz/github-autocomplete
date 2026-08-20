pluginManagement {
    // Without this line every convention plugin in build-logic is dead code.
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
// The Gradle toolchain needs a resolver, or a JDK that is absent fails the build.
// Gradle 10 makes this an error, and not a warning.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "github-autocomplete"

include(":app")
include(":autocomplete:domain")
include(":autocomplete:github-data")
include(":autocomplete:github-model")
include(":autocomplete:github-ui")
include(":autocomplete:ui-compose")
include(":core-testing")
include(":core-ui")
include(":feature-home")
