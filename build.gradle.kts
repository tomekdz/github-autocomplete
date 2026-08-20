// The convention plugins in build-logic apply these by id. Declaring them here with
// `apply false` puts the plugin implementations on the build classpath exactly once,
// which is what removes the "Kotlin Gradle plugin was loaded multiple times" warning.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt.gradle) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
}
