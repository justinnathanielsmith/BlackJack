plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.mokkery)
}

val buildAndroid = project.findProperty("target") == "android" || project.findProperty("target") == null
val buildJvm = project.findProperty("target") == "jvm" || project.findProperty("target") == null
val buildIos = project.findProperty("target") == "ios" || project.findProperty("target") == null

kotlin {

    if (buildAndroid) {
        androidLibrary {
            namespace = "io.github.smithjustinn.data"
            compileSdk =
                libs.versions.android.compileSdk
                    .get()
                    .toInt()
            minSdk =
                libs.versions.android.minSdk
                    .get()
                    .toInt()
            androidResources.enable = false // Optimized for data module
        }
    }

    if (buildJvm) {
        jvm()
    }

    if (buildIos) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:core"))
            implementation(libs.bundles.ktor.common)
            api(libs.bundles.room)
            implementation(libs.kermit)
            api(libs.koin.core)
            implementation(libs.compose.resources)
        }

        if (buildAndroid) {
            androidMain.dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        if (buildJvm) {
            jvmMain.dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        if (buildIos) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.testing)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    with(libs.room.compiler) {
        if (buildAndroid) add("kspAndroid", this)
        if (buildJvm) add("kspJvm", this)
        if (buildIos) {
            add("kspIosX64", this)
            add("kspIosArm64", this)
            add("kspIosSimulatorArm64", this)
        }
    }
}
