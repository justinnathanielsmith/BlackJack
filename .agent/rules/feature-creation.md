---
trigger: always_on
description: Feature Creation Checklist for Agents
---

# 🚀 Feature Creation Checklist

When an agent is asked to create a new feature or component, it MUST follow this structured checklist to ensure all project layers are correctly implemented.

## 1. Domain Layer
- [ ] **Data Model**: Create data classes in `shared:core` (`commonMain/kotlin/.../domain/model/`).
- [ ] **State Machine**: Create `XStateMachine` in `shared:core` to manage logic and state transitions.
- [ ] **Repository Interface**: Define in `shared:core`.

## 2. Data Layer
- [ ] **Repository Implementation**: Implement the interface in `shared:data`.
- [ ] **Persistence (If needed)**: Update Room entities and DAOs in `shared:data`.
- [ ] **Networking (If needed)**: Create Ktor services/APIs in `shared:data`.

## 3. UI Layer (sharedUI)
- [ ] **Decompose Component**: Create `XComponent` and `DefaultXComponent`.
- [ ] **UI Content**: Create `XContent` (the `@Composable` view).
- [ ] **Models**: Create `XModels.kt` for UI State, Actions, and Events.

## 4. Integration
- [ ] **Dependency Injection**: Add the new component, use cases, and repositories to Koin modules.
- [ ] **Navigation**: Integrate the new component into the `RootComponent` or relevant parent.

## 5. Verification
- [ ] **State Machine Tests**: `XStateMachineTest.kt`.
- [ ] **Component Tests**: `XComponentTest.kt`.
- [ ] **Logic Tests**: Tests for any new business logic in domain/usecase.
- [ ] **Linting**: Run `./gradlew spotlessApply` and `./gradlew detekt`.
