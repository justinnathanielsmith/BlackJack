import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Locale

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes")
    }

    android {
        namespace = "io.github.smithjustinn"
        compileSdk = 36
        minSdk = 26
        androidResources.enable = true
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:core"))
            api(project(":shared:data"))
            api(libs.bundles.compose.ui)
            api(libs.kermit)
            api(libs.decompose)
            api(libs.decompose.extensions.compose)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.bundles.coil)
            api(libs.bundles.koin)
            api(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.testing)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)

            // JavaFX Media for .m4a support on Desktop
            implementation(libs.javafx.media)
            implementation(libs.javafx.graphics)
            implementation(libs.javafx.base)
            
            implementation(libs.javafx.swing)

            // Determine current OS and Architecture to include the correct JavaFX natives
            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            val osArch = System.getProperty("os.arch").lowercase(Locale.getDefault())
            
            val isMac = osName.contains("mac")
            val isWin = osName.contains("windows")
            val isLinux = osName.contains("linux")
            val isArm64 = osArch.contains("aarch64") || osArch.contains("arm64")

            val classifier = when {
                isMac && isArm64 -> "mac-aarch64"
                isMac -> "mac"
                isWin -> "win"
                isLinux -> "linux"
                else -> "mac" // Fallback
            }
            
            val jfxVersion = libs.versions.javafx.get()
            implementation("org.openjfx:javafx-media:$jfxVersion:$classifier")
            implementation("org.openjfx:javafx-graphics:$jfxVersion:$classifier")
            implementation("org.openjfx:javafx-base:$jfxVersion:$classifier")
            implementation("org.openjfx:javafx-controls:$jfxVersion:$classifier")
            implementation("org.openjfx:javafx-swing:$jfxVersion:$classifier")
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "SharedUI"
                    isStatic = true
                }
            }
        }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
