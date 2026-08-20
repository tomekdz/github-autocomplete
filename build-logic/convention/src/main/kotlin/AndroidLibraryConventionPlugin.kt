import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.configure<LibraryExtension> {
                compileSdk = ProjectConfig.COMPILE_SDK
                defaultConfig {
                    minSdk = ProjectConfig.MIN_SDK
                    consumerProguardFiles("consumer-rules.pro")
                }
                compileOptions {
                    sourceCompatibility = ProjectConfig.JAVA_VERSION
                    targetCompatibility = ProjectConfig.JAVA_VERSION
                }
                buildFeatures {
                    aidl = false
                    buildConfig = false
                    shaders = false
                }
            }
            configureQuality()
            tasks.withType<KotlinJvmCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
            // A JUnit 4 test in src/test does not run after this line. Compose and
            // instrumented tests must stay in androidTest.
            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
            dependencies {
                "testImplementation"(libs.findLibrary("junit-jupiter-api").get())
                "testImplementation"(libs.findLibrary("junit-jupiter-params").get())
                "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
                "testRuntimeOnly"(libs.findLibrary("junit-jupiter-engine").get())
                // Gradle 9 needs the launcher on the test runtime classpath.
                "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
            }
        }
    }
}
