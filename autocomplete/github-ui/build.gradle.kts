plugins {
    id("githubautocomplete.android.library")
    id("githubautocomplete.android.compose")
    id("githubautocomplete.android.hilt")
}

android {
    namespace = "com.example.tdziergwa.autocomplete.github.ui"

    defaultConfig {
        testInstrumentationRunner = "com.example.tdziergwa.core.testing.HiltTestRunner"
    }
}

dependencies {
    implementation(project(":autocomplete:domain"))
    api(project(":autocomplete:github-model"))
    implementation(project(":autocomplete:github-data"))
    implementation(project(":autocomplete:ui-compose"))
    implementation(project(":core-ui"))

    implementation(libs.okhttp)

    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    androidTestImplementation(project(":core-testing"))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
