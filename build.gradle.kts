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
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":sharedUI"))
}

kover {
    reports {
        filters {
            excludes {
                // UI Packages - Pure presentation code
                packages(
                    "io.github.smithjustinn.ui",
                    "io.github.smithjustinn.ui.*",
                    "io.github.smithjustinn.ui.**",
                    "io.github.smithjustinn.theme",
                    "io.github.smithjustinn.theme.*",
                    "io.github.smithjustinn.services",  // AudioService - platform-specific
                    "io.github.smithjustinn.services.*",
                    "io.github.smithjustinn.di",        // DI code - generated
                    "io.github.smithjustinn.di.*",
                    "io.github.smithjustinn.di.**",
                    "memory_match.*",                   // Generated resources
                    "memory_match.**"
                )
                
                // Annotation-based exclusions (Best Practice for Compose)
                annotatedBy(
                    "androidx.compose.ui.tooling.preview.Preview",
                    "androidx.compose.runtime.Composable"  // Exclude all @Composable functions
                )
                
                // Generated and framework classes
                classes(
                    // Generated code patterns
                    "*Generated*",
                    "*_Factory",
                    "*_Impl",
                    "*_Module",
                    
                    // DI patterns
                    "*.di.*",
                    "*MetroFactory*",
                    
                    // Resource classes
                    "Res",
                    "Res$*",
                    
                    // App entry points
                    "*.AppKt",
                    "*.AppKt$*",
                    
                    // Compose-specific
                    "*.ComposableSingletons*",
                    "*ComponentScopeKt*",
                    
                    // Database generated classes (Room)
                    "*Dao_Impl*",
                    "*Database_Impl*",
                    
                    // Test utilities (if in commonTest)
                    "*Test*Util*",
                    "*TestHelper*",
                    "*Fake*",
                    "*Mock*",
                    
                    // Platform-specific expect/actual
                    "*PlatformUtils*"
                )
            }
        }
        
        total {
            xml {
                onCheck = true
            }
            html {
                onCheck = true
            }
            verify {
                rule("Minimum coverage") {
                    minBound(80)  // Updated to 80% - currently at 90.9%
                }
            }
        }
    }
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
            targetExclude("${layout.buildDirectory}/**/*.kt")
            
            // Enable ktlint with specific version
            ktlint(libs.versions.ktlint.get())
                .setEditorConfigPath(rootProject.file(".editorconfig"))
                // Add Compose-specific rules for ktlint
                .customRuleSets(
                    listOf(libs.compose.rules.get().toString())
                )

            trimTrailingWhitespace()
            endWithNewline()
        }
        
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(libs.versions.ktlint.get())
        }
    }
}
