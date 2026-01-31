---
name: clean-code
description: comprehensively cleans the codebase by running Spotless (formatting) and Detekt (static analysis), and attempting to fix common issues.
---

# Clean Code Skill

This skill orchestrates the project's quality control tools to ensure code is formatted and free of common static analysis violations.

## Workflow

### 1. Auto-Format with Spotless
First, apply standard formatting to fix potential whitespace, import, and syntax style issues.
```bash
./gradlew spotlessApply
```

### 2. Analyze with Detekt
Run the static analysis to identify code smells and complexity issues.
```bash
./gradlew detekt
```
If violations are found, the build will fail. Check the output for specific file paths and rules (e.g., `LongMethod`, `MagicNumber`).

### 3. Fix Detekt Violations
Refer to the `detekt-fixer` skill or specific rule documentation to resolve issues. Common fixes:
- **MagicNumber**: Extract to `private const val`.
- **LongMethod**: Extract logic into helper functions or sub-composables.
- **MaxLineLength**: Wrap lines or extract variables.

### 4. Verify
Run the full check to ensure a clean state.
```bash
./gradlew spotlessCheck detekt :sharedUI:compileCommonMainKotlinMetadata
```
