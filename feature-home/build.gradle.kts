plugins {
    id("githubautocomplete.android.library")
    id("githubautocomplete.android.compose")
}

android {
    namespace = "com.example.tdziergwa.feature.home"

    defaultConfig {
        testInstrumentationRunner = "com.example.tdziergwa.core.testing.HiltTestRunner"
    }
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":autocomplete:github-ui"))

    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
