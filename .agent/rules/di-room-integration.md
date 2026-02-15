---
trigger: glob
globs: ["**/*Database.kt", "**/*Dao.kt", "**/*Entity.kt", "**/*Schema*"]
description: Koin DI & Room Integration
---

# Koin DI & Room Integration
- **Platform Modules:** Define an `expect fun platformModule(): Module` in `commonMain`.
  - In `androidMain`, the actual module provides `RoomDatabase.Builder<AppDatabase>` using the Android Context.
  - In `iosMain`, provide the builder directly.
- **Dependency Injection:** The `AppDatabase` instance should be a `single` in the `commonMain` Koin module.
  - DAOs should be provided as `single` by calling `get<AppDatabase>().daoName()`.
- **Agent Instruction:** Use Koin for all dependency injection. Ensure new DAOs are registered in `DataModule.kt` or equivalent.
