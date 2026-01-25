# 🛠 Linting & Code Smell Standards

Expert instructions for maintaining code quality, formatting, and static analysis standards in the Memory Match project using **Spotless** and **Detekt**.

## 📋 General Principles
- **Clean Code First**: Prioritize readability and maintainability.
- **Automated Enforcements**: Follow both `Spotless` (formatting via `ktlint`) and `Detekt` (static analysis). 
- **Zero Violation Policy**: No new PRs should introduce linting errors or code smells.
- **Nomenclature**: Use standard Kotlin/Compose naming conventions.

## 🎨 Spotless & Ktlint (Formatting)
We use `Spotless` to enforce `ktlint` standards across the project. 

- **Auto-Formatting**: Run `./gradlew spotlessApply` to automatically fix most formatting issues.
- **Ktlint Rules**:
    - **Line Length**: Max 120 chars.
    - **Indentation**: 4 spaces.
    - **Trailing Whitespace**: Prohibited.
    - **Wildcard Imports**: **PROHIBITED** (always use explicit imports).
- **Naming Exceptions**: 
    - **PascalCase** for `@Composable` functions.
    - **Backing Properties** (e.g., `_myProperty`) for private state.

## 🔍 Detekt Standards (Static Analysis & Code Smells)
Detekt identifies potential bugs and "code smells" that affect maintainability.

### 👃 Common Code Smells to Avoid
- **Magic Numbers**: Avoid hardcoded numbers. **Allowed**: -1, 0, 1, 2. (Ignored in tests, `.kts`, and UI resources like `Color.kt`).
- **Long Parameter List**: Max 5 parameters for functions, 6 for constructors. Consider using data classes for larger state.
- **Complex Condition**: Max 3 conditions in a single `if` or `when` entry. Refactor complex logic into descriptive variables or functions.
- **Large Class/Method**: Max 600 lines per class, 60 lines per method.
- **Nested Block Depth**: Max 4 levels. Deep nesting usually indicates a need for refactoring.

### 🐛 Potential Bugs & Exceptions
- **Generic Exceptions**: Never catch `Exception` or `Throwable` without a specific reason.
- **Swallowed Exceptions**: Never leave catch blocks empty. Provide a log or a `TODO`.
- **Return Count**: Max 2 return statements per function (excluding guards/lambdas).

## 🚀 Enforcement Protocol
When working on the codebase:
1. **Apply Formatting**: Run `./gradlew spotlessApply` frequently.
2. **Check Analysis**: Run `./gradlew detekt` before committing.
3. **Continuous Correction**: If you witness a code smell (e.g., a magic number), refactor it immediately rather than ignoring it.
4. **CI Compliance**: The build will fail if `spotlessCheck` or `detekt` finds issues.
