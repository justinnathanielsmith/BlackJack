---
description: Run all quality checks and tests
---

# 🚀 Full Project Verification

Execute this workflow to ensure the codebase is clean, formatted, and all tests pass.

// turbo-all

1. **Apply Formatting**:
   ```bash
   ./gradlew spotlessApply
   ```

2. **Run Static Analysis**:
   ```bash
   ./gradlew detekt
   ```

3. **Run All Unit Tests**:
   ```bash
   ./run_tests.sh
   ```

4. **Verify Build**:
   ```bash
   ./gradlew assembleDebug
   ```
