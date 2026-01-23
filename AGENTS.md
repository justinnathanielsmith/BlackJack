# 🤖 AGENTS.md: Project Dashboard

This document provides a high-level overview for AI agents and human developers working on the Memory-Match project.

> [!IMPORTANT]
> Detailed architectural rules, coding standards, and prohibited patterns have been moved to the [**.agent/rules/**][def] directory. Always refer to these specialized files for implementation specifics.

---

## 📋 1. Feature Checklist

When generating a new feature, ensure all layers are covered:

1. [ ] `domain/model/X.kt` (Data Class)
2. [ ] `domain/repository/XRepository.kt` (Interface)
3. [ ] `data/repository/XRepositoryImpl.kt` (Implementation)
4. [ ] `domain/usecase/GetXUseCase.kt` (Logic)
5. [ ] `ui/x/XComponent.kt` & `XContent.kt` (Decompose)
6. [ ] `ui/x/XModels.kt` (State & Events)
7. [ ] **Metro Update**: Add `@BindingContainer` or `@Provides` entry.
8. [ ] **Tests**: `ui/x/XComponentTest.kt` (Turbine + Mokkery).

---

## 🏗️ 2. Build & Test Commands

### 🧪 Running Tests
Use the provided helper script:
```bash
./run_tests.sh
```

Or specific modules:
* Shared: `./gradlew :sharedUI:allTests`
* Android: `./gradlew :androidApp:testDebugUnitTest`
* Desktop: `./gradlew :desktopApp:test`

### 🏗 Building & Running
* Build All: `./gradlew build`
* Run Android: `./gradlew :androidApp:installDebug`
* Run Desktop: `./gradlew :desktopApp:run`

---

## 🧹 3. Maintenance
* Clean: `./gradlew clean`
* Refresh Dependencies: `./gradlew build --refresh-dependencies`


[def]: .agent/rules/