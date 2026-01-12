Role: Act as a Senior Android and Kotlin Multiplatform Engineer.

Task: Generate a professional, comprehensive README.md for a KMP project.

Project Context:

Project Name: [Insert Name]

Platforms Supported: [e.g., Android, iOS, Desktop (JVM), Web (Wasm)]

Architecture: [e.g., Clean Architecture, MVI, MVVM]

Core Tech Stack:

UI: [e.g., Compose Multiplatform / SwiftUI]

DI: [e.g., Metro, Koin, or Manual]

Networking: [e.g., Ktor]

Database: [e.g., Room, SQLDelight]

Concurrency: [e.g., Kotlin Coroutines & Flow]

Requirements for the README:

Header: Project title and a concise one-sentence pitch.

Platform Matrix: A table showing supported platforms and their current status (e.g., Alpha, Beta, Stable).

Project Structure: Explain the KMP module strategy (e.g., composeApp, shared, iosApp). Use a tree-view diagram style.

Tech Stack Detail: List the libraries used, specifically mentioning Version Catalogs (libs.versions.toml) for dependency management.

Setup & Installation: Provide step-by-step instructions for:

Required Android Studio/IntelliJ versions and plugins.

Xcode requirements for iOS.

Running the kmp-diagnostic or check tasks.

How to Run: Clear commands for running the Android app via Gradle and the iOS app (via Xcode or Fleet).

Contribution Guidelines: A brief section on how to contribute.

Tone: Professional, developer-centric, and highly readable using Markdown best practices (tables, code blocks, and bold text).

Please include a Mermaid.js diagram illustrating the architecture, showing how commonMain interacts with the androidMain and iosMain actual implementations.