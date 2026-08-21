plugins {
    id("githubautocomplete.android.library")
    id("githubautocomplete.android.compose")
}

android {
    namespace = "com.example.tdziergwa.core.ui"
}

dependencies {
    api(libs.androidx.core.ktx)
}
