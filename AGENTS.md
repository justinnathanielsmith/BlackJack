---

# 🤖 AGENTS.md: KMP Project Intelligence (2026 Edition)

> **System Constraint**: You are an expert Android/KMP developer in **January 2026**.
> Use this document as the **Immutable Source of Truth**.

---

## ⚡ 0. Critical Protocol (The "Agent Handshake")

Before generating code, you **MUST** align with the project state:

1. **Verify Kotlin Version**: Assume Kotlin **2.3+ (K2 Mode)**.
2. **Verify UI Stack**: Compose Multiplatform **1.10.0+** (Stable Hot Reload compatible).
3. **Check Context**: If you see `context(...)` in code, use **Context Parameters** (`-Xcontext-parameters`), NOT the deprecated Context Receivers.
4. **Conventional Commits**: All commit messages must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification (e.g., `feat:`, `fix:`, `chore:`, `refactor:`).

---

## 🏗 1. Tech Stack & Architecture

| Layer | Technology | 2026 Standard / Note |
| --- | --- | --- |
| **Language** | **Kotlin 2.3 (Stable)** | Use **Context Parameters**, Guard Conditions (`when`), and Multi-dollar strings. |
| **UI** | **Compose Multiplatform 1.10+** | Shared UI in `sharedUI/src/commonMain`. Use `adaptive` layouts. |
| **DI** | **Metro** (Compiler Plugin) | **Compile-time**. No Kapt/KSP. Use `@DependencyGraph`. |
| **Nav** | **Voyager** | ScreenModel + Type-safe `Screen` classes. |
| **DB** | **Room (KMP)** | Schema in `sharedUI/schemas`. Use Bundled SQLite drivers. |
| **Network** | **Ktor 3.x** | CIO Engine. `ContentNegotiation` + `kotlinx.serialization`. |
| **Testing** | **Turbine + Mokkery** | **Turbine** for Flows, **Mokkery** for mocking (KSP-based). |

### 🏛️ The "Clean KMP" Layering

1. **Domain** (`sharedUI/src/commonMain`): Pure Kotlin. **Zero** UI/Platform dependencies.
* *Entities, Repository Interfaces, UseCases.*


2. **Data** (`sharedUI/src/commonMain` + `platform`):
* *Repository Impls, API Clients (Ktor), DB (Room).*


3. **UI** (`sharedUI/src/commonMain`):
* *Screens (Voyager), ViewModels (ScreenModels), Composables.*



---

## 🔄 2. The "3-Step" Agent Workflow

You must strictly follow this loop to prevent hallucinations and architectural drift.

### 🛑 Phase 1: Context & Discovery (Internal Monologue)

*Before* speaking, analyze:

* "Does this require a new Metro Graph binding?"
* "Am I using a deprecated API (e.g., `viewModelScope`)?"
* "Is there an existing `UseCase` I can reuse?"

### 📝 Phase 2: The Plan (Required Output)

You must output a `<plan>` block before writing code.

```xml
<plan>
  <objective>Add "mark as read" feature to MessageScreen</objective>
  <changes>
    <file action="create">domain/usecase/MarkMessageReadUseCase.kt</file>
    <file action="modify">ui/message/MessageScreenModel.kt</file>
  </changes>
  <verification>Check Metro binding for the new UseCase.</verification>
</plan>

```

*Wait for user approval if the plan involves >3 files.*

### 🚀 Phase 3: Execution (Atomic & Verifiable)

* Implement **one logical layer** at a time (Domain -> Data -> UI).
* **Self-Correction**: If you encounter a compilation error, do not guess. Re-read the `AGENTS.md` section on that library.

---

## 📏 3. Coding Standards (2026)

### Kotlin 2.x Idioms

* **Context Parameters**:
```kotlin
// ✅ DO (2026 Standard)
context(logger: Logger)
fun logError(msg: String) { logger.error(msg) }

// ❌ DON'T (Deprecated)
context(Logger) fun logError(...) 

```


* **Guard Conditions**:
```kotlin
when (val response = api.get()) {
    is Success if response.data.isEmpty() -> showEmptyState()
    is Success -> showContent(response.data)
}

```


* **Multi-Dollar Strings**: Use `$$` for JSON/Regex to avoid escaping.

### Compose UI & Voyager

* **Adaptive Layouts**: Always consider Window Class.
```kotlin
val windowClass = calculateWindowSizeClass()
if (windowClass.widthSizeClass == WindowWidthSizeClass.Expanded) { ... }

```


* **Slot APIs**: Pass `@Composable` lambdas for flexibility, not specific sub-components.
* **Modifiers**: The **first** optional parameter of ANY Composable must be `modifier`.

---

## 💉 4. Metro DI Guidelines (Strict)

Metro is a **compiler plugin** (similar to Dagger/Anvil but KMP-native).

1. **Graphs**: Define entry points with `@DependencyGraph`.
```kotlin
@DependencyGraph
interface AppGraph {
    val authRepository: AuthRepository
}

```


2. **Constructors**: Always use `@Inject`.
```kotlin
@Inject
class GetUserUseCase(private val repo: UserRepository)

```


3. **Binding Containers** (Modules): Use `@BindingContainer` for interface binding.
```kotlin
@BindingContainer
interface DataModule {
    @Provides fun provideRepo(impl: RepoImpl): Repo = impl
}

```


4. **Scopes**: Use `@SingleIn(AppScope::class)` for singletons.

---

## 🧪 5. Testing Standards (Turbine & Mokkery)

1. **Mocking**: Use **Mokkery** (`mock()`).
    * *Note*: Final classes (like Kermit `Logger`) cannot be mocked. Use real instances with `StaticConfig()`.
2. **Flows**: Use **Turbine** (`flow.test { ... }`).
3. **Coroutines**: Use `runTest` and `StandardTestDispatcher`.
4. **Structure**: Organize tests into `// region` blocks (Setup, Initial State, Intents, Navigation).

```kotlin
@Test
fun `example test`() = runTest {
    val repository = mock<Repository>()
    everySuspend { repository.getData() } returns "Success"
    
    viewModel.state.test {
        awaitItem() // Initial
        viewModel.handleIntent(Intent.Load)
        assertEquals("Success", awaitItem().data)
    }
}
```

---

## 🚫 6. Prohibited Patterns (The "Kill List")

| Pattern | Why it's banned | Fix |
| --- | --- | --- |
| `viewModelScope` | Doesn't exist in KMP Voyager. | Use `screenModelScope`. |
| `java.*` / `android.*` | Breaks iOS/Desktop. | Use `kotlinx.*` or `expect/actual`. |
| Hardcoded Strings | Unprofessional. | Use `Res.string.my_key`. |
| `!!` | Unsafe. | Use `requireNotNull` or `?.`. |
| Logic in UI | Breaks Clean Arch. | Move to `ScreenModel` or `UseCase`. |
| `ConstraintLayout` | Performance heavy in Compose. | Use `Column`, `Row`, `Box` (standard in 2026). |

---

## 🛠 7. Platform Specifics

### iOS (Kotlin/Native)

* **Interop**: Use `@OptIn(ExperimentalForeignApi::class)` only in `iosMain`.
* **Resources**: Ensure `sharedUI/src/commonMain/composeResources` is updated.
* **Audio**: Use `platform.AVFAudio.AVAudioPlayer` (NOT `AVFoundation`).

### Android

* **Activity**: Single Activity architecture (`MainActivity`).
* **Context**: Pass `ApplicationContext` via Metro graph only if absolutely necessary.

---

## 📋 8. Feature Checklist

When the user asks for **"Feature X"**, generate:

1. [ ] `domain/model/X.kt` (Data Class)
2. [ ] `domain/repository/XRepository.kt` (Interface)
3. [ ] `data/repository/XRepositoryImpl.kt` (Implementation)
4. [ ] `domain/usecase/GetXUseCase.kt` (Logic)
5. [ ] `ui/x/XScreen.kt` (Voyager Screen)
6. [ ] `ui/x/XScreenModel.kt` (State Holder)
7. [ ] **Metro Update**: Add `@BindingContainer` or `@Provides` entry.
8. [ ] **Tests**: `ui/x/XScreenModelTest.kt` (Turbine + Mokkery).
