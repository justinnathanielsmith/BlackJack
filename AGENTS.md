# 🤖 AGENTS.md: Project Dashboard

This document provides a high-level overview for AI agents and human developers working on the Memory-Match project.

> [!IMPORTANT]
> Detailed architectural rules, coding standards, and prohibited patterns have been moved to the [**.agent/rules/**][def] directory. Always refer to these specialized files for implementation specifics.

---

## 📋 1. Feature Checklist

When generating a new feature, ensure all layers are covered:

1. [ ] `domain/model/X.kt` (Data Class)
2. [ ] `domain/state/XStateMachine.kt` (State Machine logic)
3. [ ] `domain/repository/XRepository.kt` (Interface)
4. [ ] `data/repository/XRepositoryImpl.kt` (Implementation)
5. [ ] `domain/usecase/GetXUseCase.kt` (Logic)
6. [ ] `ui/x/XComponent.kt` & `XContent.kt` (Decompose)
7. [ ] `ui/x/XModels.kt` (State & Events)
8. [ ] **Koin Update**: Add `factory` or `single` definition in relevant Module.
9. [ ] **Tests**: `ui/x/XStateMachineTest.kt` and `ui/x/XComponentTest.kt`.

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