# Kover Code Coverage Research Report - Memory Match KMP Project

## Current Situation (February 2026)

### Current Coverage
- **Line Coverage**: **90.9%** (520/572) ✅
- **Class Coverage**: **92.5%** (62/67) ✅
- **Domain Layer Coverage**: **100%** 🎯

### Verification Target
- Current minimum bound: **80%** (Increased from 70%)
- Desired target: **80%+**

> [!NOTE]
> We successfully jumped from 23.2% to 90.9% by applying the exclusion strategies researched in this document. See **[KOVER_COVERAGE_SUMMARY.md](KOVER_COVERAGE_SUMMARY.md)** for the implementation summary.

---

## Analysis of Current Configuration

### What's Already Configured ✅

Your [build.gradle.kts](../build.gradle.kts#L21-L69) already has good Kover configuration:

1. **Package Exclusions**:
   - `io.github.smithjustinn.ui.*` - UI components
   - `io.github.smithjustinn.theme.*` - Theme files
   - `io.github.smithjustinn.services.*` - Services
   - `io.github.smithjustinn.di.*` - Dependency injection
   - `memory_match.*` - Generated resources

2. **Class Pattern Exclusions**:
   - Generated classes (`*Generated*`, `*_Factory`, `*_Impl`, `*_Module`)
   - DI classes (`*.di.*`, `*MetroFactory*`)
   - Resource classes (`Res`, `Res$*`)
   - Compose singletons (`*.ComposableSingletons*`)
   - Component scopes (`*ComponentScopeKt*`)
   - App entry points (`*.AppKt`, `*.AppKt$*`)

3. **Verification Rules**:
   - Minimum bound: 70%
   - HTML/XML reports on check

---

## Why Coverage is Currently Low (23.2%)

### Root Causes

1. **UI Code is Included**: Even though you've excluded UI packages, Kover for KMP **only measures JVM targets**. UI code in `commonMain` might still be counted but not tested with unit tests.

2. **Missing Test Coverage**: Based on your open test files, you have tests for:
   - ✅ Domain models (`CardStateTest`, `MemoryGameStateTest`)
   - ✅ Components (`GameComponentTest`)
   - ✅ Repositories (`GameStateRepositoryTest`, `DailyChallengeRepositoryTest`)
   
   But likely missing tests for:
   - ❌ Use cases (40+ use case files found)
   - ❌ Repository implementations
   - ❌ Domain logic (`MemoryGameLogic.kt`)
   - ❌ Data layer implementations

3. **Exclusions Might Not Be Working**: The wildcard patterns might not be catching all UI and generated code.

---

## Recommendations to Reach 80% Coverage

### 1. Refine Kover Exclusions 🎯

#### Add Annotation-Based Exclusions

Kover supports excluding functions by annotation. This is **the best practice** for Compose code:

```kotlin
kover {
    reports {
        filters {
            excludes {
                // Existing package exclusions...
                
                // Add annotation-based exclusions
                annotatedBy(
                    "androidx.compose.ui.tooling.preview.Preview",
                    "androidx.compose.runtime.Composable"
                )
                
                // Existing class pattern exclusions...
            }
        }
    }
}
```

> [!WARNING]
> While `@Composable` exclusion will remove ALL Composable functions from coverage, this is reasonable for a KMP project since:
> - UI testing should be done via UI tests, not unit tests
> - Compose functions are declarative and hard to unit test effectively
> - Your focus should be on domain/data layer coverage

#### Alternative: More Targeted UI Exclusions

If you want to keep some Composable coverage (like for components that have business logic), use more specific patterns:

```kotlin
excludes {
    packages(
        "io.github.smithjustinn.ui.components",
        "io.github.smithjustinn.ui.game.components",
        "io.github.smithjustinn.ui.start.components",
        "io.github.smithjustinn.ui.stats.components"
    )
    
    // Exclude specific UI files that are pure presentation
    classes(
        "*Content",
        "*Screen",
        "*Preview*",
        "*Theme*"
    )
}
```

### 2. Focus Testing on High-Value Code 🧪

Based on your project structure, prioritize writing tests for:

#### Domain Layer (Highest Priority)
- ✅ Already have: `CardState`, `MemoryGameState`
- ❌ Need tests for:
  - `MemoryGameLogic.kt` - **Critical business logic**
  - All use cases in `domain/usecases/`:
    - `CalculateFinalScoreUseCase`
    - `FlipCardUseCase`
    - `StartNewGameUseCase`
    - `SaveGameResultUseCase`
    - etc. (40+ files)

#### Data Layer (Medium Priority)  
- ✅ Already have: Repository tests
- ❌ Need tests for:
  - Repository implementations (`*RepositoryImpl.kt`)
  - DAOs (if not already tested via repository tests)

#### Platform Layer (Lower Priority)
- `PlatformUtils.kt` - Consider excluding via `expect/actual` pattern or test separately

### 3. Exclude Test Utilities from Coverage 📦

If you have test utilities in `commonTest`, exclude them:

```kotlin
excludes {
    classes(
        "*Test*Util*",
        "*TestHelper*",
        "*Fake*",
        "*Mock*"
    )
}
```

### 4. Update Verification Threshold Gradually 📈

Don't jump straight to 80%. Use incremental targets:

```kotlin
verify {
    rule("Minimum Line Coverage") {
        bound {
            minValue = 75  // Start here, then increase to 80
            coverageUnits = CoverageUnit.LINE
            aggregationForGroup = AggregationForGroup.COVERED_PERCENTAGE
        }
    }
}
```

### 5. Generate Coverage Report by Package 📊

Add this to see which packages need the most work:

```kotlin
kover {
    reports {
        total {
            html {
                onCheck = true
                reportDir = layout.buildDirectory.dir("reports/kover/html")
                // Group by package to identify gaps
            }
        }
    }
}
```

---

## Recommended Updated Configuration

Here's the complete recommended Kover configuration for [build.gradle.kts](../build.gradle.kts#L21-L69):

```kotlin
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
                    // OPTIONAL: Uncomment to exclude ALL @Composable functions
                    // "androidx.compose.runtime.Composable"
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
                    "*_Impl*",
                    "*Dao_Impl*",
                    
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
                rule("Minimum line coverage") {
                    bound {
                        minValue = 75  // Start at 75%, increase to 80% after adding tests
                        coverageUnits = CoverageUnit.LINE
                        aggregationForGroup = AggregationForGroup.COVERED_PERCENTAGE
                    }
                }
                
                // Optional: Add branch coverage requirement
                rule("Minimum branch coverage") {
                    bound {
                        minValue = 60
                        coverageUnits = CoverageUnit.BRANCH
                        aggregationForGroup = AggregationForGroup.COVERED_PERCENTAGE
                    }
                }
            }
        }
    }
}
```

---

## Important KMP Kover Limitations ⚠️

> [!IMPORTANT]
> **Kover only measures coverage for JVM targets in KMP projects** (as of Jan 2026)
> 
> This means:
> - ✅ `commonMain` code IS included (executed via JVM tests)
> - ✅ `androidMain` code IS included
> - ✅ `jvmMain` code IS included
> - ❌ `iosMain` code is NOT included
> - ❌ `jsMain` code is NOT included
> - ❌ Tests from `iosTest`, `jsTest` are NOT collected

This is actually a **benefit** for your project since you're excluding most platform-specific code anyway.

---

## Action Plan Progress 🎯

### Phase 1: Refine Exclusions (Completed) ✅
- Updated `build.gradle.kts` with annotation-based exclusions.
- Added Room DAO implementation exclusions.
- Coverage jumped to **90.9%** after excluding UI code properly.

### Phase 2: Test Domain Layer (Completed) ✅
- Wrote tests for `MemoryGameLogic.kt`.
- Wrote tests for use cases.
- **Result**: Domain layer achieved **100% coverage**.

### Phase 3: Test Data Layer (Completed) ✅
- Wrote tests for repository implementations.
- Verified DAO coverage.

### Phase 4: Maintenance (Ongoing) 🔄
- Identify remaining gaps from Kover HTML report.
- Maintain the 80% threshold in CI.

---

## Best Practices for KMP Testing with Kover 🏆

1. **Focus on `commonTest`**: Write most tests in `commonTest` to ensure cross-platform correctness
2. **Exclude UI liberally**: UI code should be tested with UI tests, not unit tests
3. **Test business logic extensively**: Domain layer should have 90%+ coverage
4. **Use `@OptIn` annotations carefully**: Some experimental APIs might need exclusion
5. **Monitor branch coverage**: Aim for at least 60-70% branch coverage for complex logic
6. **Use verification rules in CI**: Fail builds if coverage drops below threshold

---

## Tools and Commands 🛠️

### Generate Coverage Report
```bash
./gradlew koverHtmlReport
```

Report location: `build/reports/kover/html/index.html`

### Verify Coverage Meets Threshold
```bash
./gradlew koverVerify
```

### Generate XML Report (for CI/CD tools)
```bash
./gradlew koverXmlReport
```

### See Coverage by Module
```bash
./gradlew :sharedUI:koverHtmlReport
```

---

## References 📚

1. [Official Kover Documentation](https://kotlin.github.io/kotlinx-kover/gradle-plugin/)
2. [Kover GitHub Repository](https://github.com/Kotlin/kotlinx-kover)
3. [KMP Testing Best Practices (Baeldung)](https://www.baeldung.com/kotlin/kover-code-coverage)
4. [Excluding Compose Previews from Coverage](https://github.com/Kotlin/kotlinx-kover/issues/)

---

## Expected Outcome 🎯

With these changes, coverage should progress as follows:

| Phase    | Current | After Exclusions | After Domain Tests | After Data Tests | Target    |
| -------- | ------- | ---------------- | ------------------ | ---------------- | --------- |
| Coverage | 23.2%   | ~45-50%          | ~70-75%            | ~80-85%          | **80%** ✅ |

> [!TIP]
> The quickest path to 80% is:
> 1. Add annotation-based exclusions for `@Preview` functions
> 2. Optionally exclude all `@Composable` functions  
> 3. Write tests for the 40+ use cases in your domain layer
> 
> This should get you from 23% → 80% with focused effort on high-value code!
