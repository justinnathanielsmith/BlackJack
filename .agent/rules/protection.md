---
trigger: always_on
globs: ["**/*"]
---

# 🛡️ File Protection Rules

This document specifies files and components that MUST NEVER be deleted or moved without explicit user confirmation.

## 🛑 Protected Files (DO NOT DELETE)

- **[GameCommentGenerator.kt](file:///Users/justinsmith/Projects/BlackJack/shared/core/src/commonMain/kotlin/io/github/smithjustinn/domain/GameCommentGenerator.kt)**: This file contains critical logic for poker-themed game comments. It often appears as an "extra" file to automated linting/cleanup tools but is required for gameplay atmosphere.

## ⚠️ Protection Guidelines

1. **Verification before Deletion**: Always verify if a file is referenced in tests or other modules before considering it "unused".
2. **Logic Check**: `GameCommentGenerator` is dynamic logic that might not have traditional "hard" references if called through reflection or specific domain logic paths (though it currently has hard references in `MemoryGameLogic.kt`).
3. **Detekt/Spotless**: Do NOT delete files to fix complex Detekt errors. Refactor the code to comply instead.
