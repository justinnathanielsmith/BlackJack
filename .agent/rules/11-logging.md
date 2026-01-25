# 🪵 Logging Standards

## Primary Rule: Use Kermit Logger

All application logging MUST be performed using the `co.touchlab.kermit.Logger` instance retrieved from the dependency graph via `appGraph.logger`.

### Prohibited Pattern: Standard Output

Direct use of standard output for logging or debugging is strictly prohibited, as it is inconsistent with the multiplatform logging setup and is not controllable by the application's configuration.

- **DO NOT USE:**
  ```kotlin
  println("Debug info: $value")
  System.out.println("Error details...")
  e.printStackTrace()
  ```

### Preferred Pattern: Use `Logger`

Ensure the `Logger` is available in your component or use case (typically passed in via the constructor) and use its structured logging functions (`i`, `e`, `w`, etc.).

- **DO USE:**
  ```kotlin
  // In a component/class that has a logger property
  logger.e(e) { "Error processing deep link: $url" } 
  logger.i { "Configuration loaded successfully." }
  ```