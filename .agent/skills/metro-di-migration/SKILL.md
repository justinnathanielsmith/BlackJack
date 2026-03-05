---
name: metro-di-migration
description: Guides the migration from Koin to Metro DI in a Kotlin Multiplatform project. Covers concept mapping, annotation patterns, platform-specific setup, and testing.
---

# Metro DI Migration Skill

## Overview
This skill provides patterns and instructions for migrating a KMP project from **Koin** to **Metro DI** (v0.11.2+). Metro is a **compile-time** DI framework using a Kotlin compiler plugin.

## Prerequisites
- **Kotlin 2.3.0+** (required for Metro compiler plugin)
- Metro Gradle plugin applied to all modules: `id("dev.zacsweers.metro") version "0.11.2"`

## Core Concepts

### Scope Marker
Define a shared scope object used across all annotations:
```kotlin
// shared/core — commonMain
package io.github.smithjustinn.di

object AppScope
```

### DependencyGraph (replaces KoinAppGraph)
```kotlin
@DependencyGraph(AppScope::class)
interface AppGraph {
    val myService: MyService

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AppGraph
    }
}

// Usage:
val graph = createGraph<AppGraph>()               // no inputs
val graph = createGraphFactory<AppGraph.Factory>() // with inputs
    .create(androidContext)
```

### @Inject (replaces singleOf constructor-injection)
```kotlin
// BEFORE (Koin):
// singleOf(::MyRepositoryImpl) { bind<MyRepository>() }

// AFTER (Metro):
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class MyRepositoryImpl(
    private val dao: MyDao,
    private val logger: Logger,
) : MyRepository
```

### @Provides (replaces single { })
```kotlin
// BEFORE (Koin):
// module { single { Json { ignoreUnknownKeys = true } } }

// AFTER (Metro):
@ContributesTo(AppScope::class)
interface DataProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}
```

### @ContributesTo (replaces Koin modules)
```kotlin
// BEFORE (Koin):
// val androidUiModule = module {
//     singleOf(::AndroidHapticsServiceImpl) { bind<HapticsService>() }
// }

// AFTER (Metro):
@ContributesTo(AppScope::class)
interface AndroidProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        val dbFile = context.getDatabasePath("memory_match.db")
        return Room.databaseBuilder<AppDatabase>(context, dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
```

### @ContributesBinding (replaces singleOf + bind)
```kotlin
// For classes with a SINGLE supertype:
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AndroidHapticsServiceImpl(private val context: Context) : HapticsService

// For classes with MULTIPLE supertypes, specify which to bind:
@ContributesBinding(AppScope::class, binding = binding<HapticsService>())
```

### Graph Factory (replaces initKoin with platform inputs)
```kotlin
// BEFORE:
fun createAndroidGraph(application: Application): AppGraph {
    initKoin(androidUiModule) { androidContext(application) }
    return KoinAppGraph()
}

// AFTER:
fun createAndroidGraph(application: Application): AppGraph {
    return createGraphFactory<AppGraph.Factory>().create(application)
}
```

### Dynamic Graphs for Testing (replaces startKoin/stopKoin in tests)
```kotlin
// BEFORE:
startKoin { modules(module { single { fakeRepo } }) }

// AFTER:
@BindingContainer
object TestBindings {
    @Provides fun provideRepo(): Repository = FakeRepository()
}

val testGraph = createDynamicGraph<AppGraph>(TestBindings)
```

## Removing KoinComponent
Classes using `KoinComponent` + `by inject()` must switch to constructor injection:

```kotlin
// BEFORE:
class DefaultShopComponent(...) : KoinComponent {
    private val buyItemUseCase: BuyItemUseCase by inject()
}

// AFTER:
class DefaultShopComponent(
    private val buyItemUseCase: BuyItemUseCase,  // passed from AppGraph
    ...
)
```

## Platform-Specific Caveats

> [!WARNING]
> **iOS/Native**: `@ContributesTo` / `@ContributesBinding` aggregation is NOT supported on Native targets until Kotlin 2.3.20-Beta1. For Kotlin 2.3.0, use explicit `@Includes` on the graph factory instead of relying on auto-aggregation for iOS modules.

## Migration Checklist
1. [ ] Apply Metro Gradle plugin to all modules
2. [ ] Create `AppScope` object in `shared:core`
3. [ ] Convert Koin modules → `@ContributesTo` interfaces with `@Provides`
4. [ ] Annotate impl classes with `@Inject` + `@ContributesBinding`
5. [ ] Convert `AppGraph` to `@DependencyGraph(AppScope::class)`
6. [ ] Replace `initKoin()`/`createXxxGraph()` with `createGraph`/`createGraphFactory`
7. [ ] Remove `KoinComponent` from all classes
8. [ ] Update tests to use `createDynamicGraph` instead of `startKoin`/`stopKoin`
9. [ ] Remove all Koin dependencies from Gradle
10. [ ] Update agent rules (GEMINI.md, core-general.md)
