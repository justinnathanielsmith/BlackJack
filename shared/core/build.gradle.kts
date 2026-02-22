plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kover)
    alias(libs.plugins.mokkery)
}

val buildAndroid = project.findProperty("target") == "android" || project.findProperty("target") == null
val buildJvm = project.findProperty("target") == "jvm" || project.findProperty("target") == null
val buildIos = project.findProperty("target") == "ios" || project.findProperty("target") == null

kotlin {

    if (buildAndroid) {
        android {
            namespace = "io.github.smithjustinn.core"
            compileSdk =
                libs.versions.android.compileSdk
                    .get()
                    .toInt()
            minSdk =
                libs.versions.android.minSdk
                    .get()
                    .toInt()
            androidResources.enable = false // Optimized for core library
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
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.collections.immutable)
            api(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            api(libs.kermit)
            api(libs.decompose)
            implementation(libs.compose.runtime)
            implementation(libs.compose.resources)
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.testing)
        }
    }
}

compose.resources {
    packageOfResClass = "io.github.smithjustinn.resources"
    publicResClass = true
}
