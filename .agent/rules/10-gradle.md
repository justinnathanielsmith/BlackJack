---
trigger: glob
globs: ["**/gradle.properties"]
---

# 🐘 Gradle 2026 Best Practices

Follow these rules for all `gradle.properties` files in 2026 to ensure maximum performance, stability, and compatibility with Kotlin 2.3+ and Compose Multiplatform 1.10+.

## 1. Core Performance Flags
- `org.gradle.jvmargs`: Minimum 8GB for modern KMP projects. Use `-XX:+UseParallelGC` for better build performance.
- `org.gradle.caching=true`: Always enable the build cache.
- `org.gradle.configuration-cache=true`: Mandatory for fast iterative builds.
- `org.gradle.parallel=true`: Ensure tasks run in parallel.

## 2. Project Isolation (The 2026 Standard)
Project Isolation is critical for large-scale performance and IDE responsiveness.
- `org.gradle.unsafe.isolated-projects=true`: Enable this once plugins are compatible.

## 3. Kotlin & KMP Ergonomics
- `kotlin.daemon.jvmargs`: Match the Gradle JVM args (e.g., `-Xmx8G`).
- `kotlin.incremental.wasm=true`: Essential if targeting WASM.
- `ksp.useKSP2=true`: KSP1 is deprecated; always use KSP2.

## 4. Android & Compose Multiplatform
- `android.nonTransitiveRClass=true`: Faster builds, less R class pollution.
- `android.defaults.buildfeatures.buildconfig=false`: [DEPRECATED in AGP 9.0] Disable `BuildConfig` in `build.gradle.kts` instead.

- `compose.resources.generateResClass=always`: Ensures consistent resource class generation for library modules.

## 5. Prohibited Properties
- Avoid `org.gradle.configureondemand=true` as it is incompatible with Configuration Cache / Project Isolation.
- Avoid legacy `kotlin.mpp.enableCompatibilityMetadataVariant`.

## Template `gradle.properties` (2026)
```properties
# Gradle Core
org.gradle.jvmargs=-Xmx8G -XX:+UseParallelGC -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.daemon=true
org.gradle.parallel=true

# Project Isolation (Experimental/Stable in 2026)
org.gradle.unsafe.isolated-projects=true

# Kotlin
kotlin.daemon.jvmargs=-Xmx8G
kotlin.native.jvmArgs=-Xmx8G
kotlin.incremental.wasm=true
kotlin.daemon.performance.metrics=true

# KSP
ksp.useKSP2=true

# Android
android.nonTransitiveRClass=true
android.defaults.buildfeatures.buildconfig=false

# Compose Multiplatform
compose.resources.generateResClass=always
```
