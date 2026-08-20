import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(17))
                }
            }
            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
            configureQuality()
            dependencies {
                "testImplementation"(libs.findLibrary("junit-jupiter-api").get())
                "testImplementation"(libs.findLibrary("junit-jupiter-params").get())
                "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
                "testRuntimeOnly"(libs.findLibrary("junit-jupiter-engine").get())
                "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
            }
        }
    }
}
