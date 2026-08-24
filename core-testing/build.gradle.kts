plugins {
    id("githubautocomplete.android.library")
}

android {
    namespace = "com.example.tdziergwa.core.testing"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.hilt.android.testing)
}
