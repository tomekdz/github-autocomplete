plugins {
    `kotlin-dsl`
}

group = "com.example.tdziergwa.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "githubautocomplete.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "githubautocomplete.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "githubautocomplete.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "githubautocomplete.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "githubautocomplete.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}
