plugins {
    id("githubautocomplete.kotlin.library")
}

dependencies {
    api(project(":autocomplete:github-model"))
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.turbine)
}
