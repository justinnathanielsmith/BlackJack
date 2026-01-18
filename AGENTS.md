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

## 📏 2. Coding Standards (2026)

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

### 🔊 Audio, Haptics & Navigation (One-Time Events)

* **Immediate UX Feedback**: Trigger directly from the UI (Composable).
    * *Example*: Button clicks, toggle switches.
* **Logic Result Events**: Trigger via ViewModel using a `Channel` or `SharedFlow` collected in the UI.
    * *Example*: Match success, game won, error buzzer.
* **Navigation Events**: Trigger via ViewModel using a `Channel` or `SharedFlow` collected in the UI.
    * *Example*: Navigating to a game screen after checking for a saved game.
* **Implementation**: 
    * Define a `UiEvent` sealed class in the UI package.
    * Use a `Channel(Channel.BUFFERED)` in the `ScreenModel`.
    * Collect in the Composable using `LaunchedEffect(Unit)`.

---

## 💉 3. Metro DI Guidelines (Strict)

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

## 🧪 4. Testing Standards (Turbine & Mokkery)

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

## 🚫 5. Prohibited Patterns (The "Kill List")

| Pattern | Why it's banned | Fix |
| --- | --- | --- |
| `viewModelScope` | Doesn't exist in KMP Voyager. | Use `screenModelScope`. |
| `java.*` / `android.*` | Breaks iOS/Desktop. | Use `kotlinx.*` or `expect/actual`. |
| Hardcoded Strings | Unprofessional. | Use `Res.string.my_key`. |
| `!!` | Unsafe. | Use `requireNotNull` or `?.`. |
| Logic in UI | Breaks Clean Arch. | Move to `ScreenModel` or `UseCase`. |
| `ConstraintLayout` | Performance heavy in Compose. | Use `Column`, `Row`, `Box` (standard in 2026). |

---

## 🛠 6. Platform Specifics

### iOS (Kotlin/Native)

* **Interop**: Use `@OptIn(ExperimentalForeignApi::class)` only in `iosMain`.
* **Resources**: Ensure `sharedUI/src/commonMain/composeResources` is updated.
* **Audio**: Use `platform.AVFAudio.AVAudioPlayer` (NOT `AVFoundation`).

### Android

* **Activity**: Single Activity architecture (`MainActivity`).
* **Context**: Pass `ApplicationContext` via Metro graph only if absolutely necessary.

---

## 📋 7. Feature Checklist

When the user asks for **"Feature X"**, generate:

1. [ ] `domain/model/X.kt` (Data Class)
2. [ ] `domain/repository/XRepository.kt` (Interface)
3. [ ] `data/repository/XRepositoryImpl.kt` (Implementation)
4. [ ] `domain/usecase/GetXUseCase.kt` (Logic)
5. [ ] `ui/x/XScreen.kt` (Voyager Screen)
6. [ ] `ui/x/XScreenModel.kt` (State Holder)
7. [ ] **Metro Update**: Add `@BindingContainer` or `@Provides` entry.
8. [ ] **Tests**: `ui/x/XScreenModelTest.kt` (Turbine + Mokkery).

---

## 🏗️ 8. Build & Test Commands

### 🧪 Running Tests
Use the provided helper script to run all tests across platforms:
```bash
./run_tests.sh
```

Or run specific module tests via Gradle:
* **Shared Module (All Platforms)**: `./gradlew :sharedUI:allTests`
* **Android App**: `./gradlew :androidApp:testDebugUnitTest`
* **Desktop App**: `./gradlew :desktopApp:test`

### 🏗 Building & Running
* **Build All**: `./gradlew build`
* **Run Android**: `./gradlew :androidApp:installDebug`
* **Run Desktop**: `./gradlew :desktopApp:run`
* **iOS**: Open `iosApp/iosApp.xcworkspace` in Xcode.

### 🧹 Maintenance
* **Clean**: `./gradlew clean`
* **Refresh Dependencies**: `./gradlew build --refresh-dependencies`
