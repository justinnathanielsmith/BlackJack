# 📚 Rules Index: Project Standards

To maintain the high quality of the Memory-Match project, follow these specialized rules based on the area you are working on.

---

## 🏛️ 1. Core Architecture
*   **[Core General](core-general.md)**: High-level protocol, Kotlin/Compose versions, and prohibited patterns.
*   **[Core KMP](core-kmp.md)**: Shared code standards, `expect/actual` usage, and commonMain restrictions.
*   **[Core Kotlin](core-kotlin.md)**: Idiomatic Kotlin patterns (2026), DSL markers, and error handling.
*   **[Core Logging](core-logging.md)**: Standardizing Kermit 3.x for unified cross-platform logs.
*   **[DI Koin](di-koin.md)**: Dependency injection patterns and `context(Service)` usage.
*   **[DI Room Integration](di-room-integration.md)**: Specific patterns for injecting Room databases into shared logic.

---

## 🎨 2. UI Layer (sharedUI)
*   **[UI Compose](ui-compose.md)**: Compose Multiplatform best practices, state management, and modifiers.
*   **[UI Resources](ui-resources.md)**: Handing strings, images, and fonts via `Res`.
*   **[UI Android](ui-android.md)**: Platform-specific hooks for the Android application.
*   **[UI iOS](ui-ios.md)**: Platform-specific UI considerations for Swift/Compose interop.
*   **[Lottie Animations](lottie-compose-kmp.md)**: Integrating Compottie for animations.

---

## 💾 3. Data & Networking
*   **[Data Room](data-room.md)**: Persistence rules, migrations, and Room KMP usage.
*   **[Data Network](data-network.md)**: Ktor 3.x configuration and API client standards.

---

## 🧪 4. Testing & Quality
*   **[Test General](test-general.md)**: Overall testing strategy and suite organization.
*   **[Test Turbine](test-turbine.md)**: Validating Flows and state transitions.
*   **[Test Mokkery](test-mokkery.md)**: KSP-based mocking standards.
*   **[Test Decompose](test-decompose.md)**: Specialized patterns for lifecycle-aware component testing.
*   **[Core Linting](core-linting.md)**: Detekt and Spotless configuration.

---

## 🛠️ 5. Specialized Workflows
*   **[Feature Creation](feature-creation.md)**: Mandatory checklist for adding new components.
*   **[Protection Rules](protection.md)**: Files that must never be deleted/moved.
*   **[Shop Economy](shop-economy.md)**: Adding items to the in-game shop.

---
*Always start with the Index before researching specific implementations.*
