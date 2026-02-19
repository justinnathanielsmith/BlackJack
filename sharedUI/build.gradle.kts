import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
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
            implementation(libs.kotlinx.datetime)
            implementation(libs.bundles.coil)
            api(libs.bundles.koin)
            api(libs.koin.compose.viewmodel)
            implementation(libs.compottie)
            implementation(libs.compottie.resources)
            implementation(libs.ktor.client.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.testing)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.google.play.services.ads)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)

            // JavaFX Media for .m4a support on Desktop
            val jfxVersion = libs.versions.javafx.get()
            val classifier = getJavafxClassifier()

            val jfxModules = listOf("media", "graphics", "base", "controls", "swing")
            jfxModules.forEach { module ->
                implementation("org.openjfx:javafx-$module:$jfxVersion")
                implementation("org.openjfx:javafx-$module:$jfxVersion:$classifier")
            }
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

fun getJavafxClassifier(): String {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    return when {
        osName.contains("mac") -> if (osArch.contains("aarch64") || osArch.contains("arm64")) "mac-aarch64" else "mac"
        osName.contains("win") -> "win"
        osName.contains("linux") -> "linux"
        else -> "mac"
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
