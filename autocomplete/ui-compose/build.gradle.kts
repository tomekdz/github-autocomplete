plugins {
    id("githubautocomplete.android.library")
    id("githubautocomplete.android.compose")
}

android {
    namespace = "com.example.tdziergwa.autocomplete.ui"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(project(":autocomplete:domain"))
    api(project(":autocomplete:github-model"))

    implementation(libs.androidx.lifecycle.runtime.compose)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
