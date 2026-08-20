import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

private const val MAX_LINE_LENGTH = "120"

/**
 * Detekt reads the rules of the project, and Spotless holds the format. Every
 * convention plugin calls this, so no module can leave the rules.
 */
internal fun Project.configureQuality() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    pluginManager.apply("com.diffplug.spotless")

    extensions.configure<DetektExtension> {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        reports {
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }

    extensions.configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint(libs.findVersion("ktlint").get().requiredVersion)
                .editorConfigOverride(
                    mapOf(
                        "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                        // Detekt holds the same limit. Without this line ktlint
                        // joins a wrapped line, and detekt then rejects it.
                        "max_line_length" to MAX_LINE_LENGTH,
                    ),
                )
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(libs.findVersion("ktlint").get().requiredVersion)
        }
    }
}
