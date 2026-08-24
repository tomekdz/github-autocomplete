import java.util.Properties

plugins {
    id("githubautocomplete.android.application")
    id("githubautocomplete.android.compose")
    id("githubautocomplete.android.hilt")
}

val githubToken: String =
    Properties()
        .apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use(::load)
        }.getProperty("github.token")
        .orEmpty()

android {
    namespace = "com.example.tdziergwa"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.tdziergwa"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.example.tdziergwa.core.testing.HiltTestRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "GITHUB_TOKEN", "\"$githubToken\"")
        }

        getByName("release") {
            buildConfigField("String", "GITHUB_TOKEN", "\"\"")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":feature-home"))
    implementation(project(":autocomplete:github-ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    androidTestImplementation(project(":core-testing"))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    kspAndroidTest(libs.hilt.compiler)
}
