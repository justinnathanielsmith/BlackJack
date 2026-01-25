plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.metro).apply(false)
    alias(libs.plugins.room).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()

    // Configure source directories for KMP
    source.setFrom(
        files(
            "sharedUI/src/commonMain/kotlin",
            "sharedUI/src/androidMain/kotlin",
            "sharedUI/src/iosMain/kotlin",
            "androidApp/src/main/kotlin",
            "desktopApp/src/main/kotlin",
        ),
    )

    config.setFrom("config/detekt/detekt.yml")
    baseline.set(file("config/detekt/baseline.xml"))
    buildUponDefaultConfig = true
    parallel = true
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**")
            ktlint()
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_filename" to "disabled",
                        "ktlint_standard_no-wildcard-imports" to "disabled",
                    ),
                )
        }
        kotlinGradle {
            target("**/*.kts")
            targetExclude("**/build/**")
            ktlint()
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_filename" to "disabled",
                        "ktlint_standard_no-wildcard-imports" to "disabled",
                    ),
                )
        }
    }
}
