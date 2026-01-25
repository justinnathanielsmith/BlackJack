---
trigger: model_decision
description: Room KMP Standards
---

# Room KMP Standards
- **Source Set Location:** Entities, DAOs, and the `@Database` abstract class must reside in `commonMain`.
- **Constructor Pattern:** Use the `@ConstructedBy` annotation with a companion `expect object` for the `RoomDatabaseConstructor`.
- **Driver:** Always use `BundledSQLiteDriver()` for maximum consistency across Android and iOS.
- **Platform Builders:** - In `androidMain`: Use `Context.getDatabasePath("name.db")`.
  - In `iosMain`: Use `NSHomeDirectory() + "/name.db"`.
- **Threading:** Always set `.setQueryCoroutineContext(Dispatchers.IO)` in the common database factory.
- **Agent Instruction:** "When adding a new Entity, the agent must update the `@Database` class's `entities` array and add a corresponding `abstract fun` for the new DAO."