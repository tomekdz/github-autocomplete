plugins {
    id("githubautocomplete.kotlin.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":autocomplete:domain"))
    api(project(":autocomplete:github-model"))

    api(libs.okhttp)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.okhttp.mockwebserver)
}
