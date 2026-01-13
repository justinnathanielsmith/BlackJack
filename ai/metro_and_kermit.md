Using **Kermit** with **Metro DI** in a Clean Architecture Kotlin Multiplatform (KMP) app involves providing a pre-configured `Logger` instance through Metro's dependency graph. This allows you to use constructor injection across your layers while maintaining the ability to swap the real logger for a `TestLogWriter` in unit tests.

### 1. Dependency Injection Setup (Infrastructure Layer)

In Metro, you define an entry point for your dependencies using `@DependencyGraph` and group logical providers into `@BindingContainer`s.

#### Define the Logger Provider

Create a container in your `commonMain` source set that provides the standard Kermit `Logger` using the platform-specific writer (e.g., Logcat on Android, NSLog on iOS).

```kotlin
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
interface LoggingContainer {
    @Provides
    @SingleIn(AppScope::class) // Ensures a single instance per scope
    fun provideLogger(): Logger {
        return Logger(
            config = LoggerConfig.default.copy(
                logWriterList = listOf(platformLogWriter())
            ),
            tag = "AppTag"
        )
    }
}

```

### 2. Usage in Clean Architecture Layers

To keep your code testable and follow Clean Architecture, use **Constructor Injection** via Metro's `@Inject` annotation.

#### Data Layer (Repository Implementation)

Inject the `Logger` directly into your repository implementation to log network errors or database operations.

```kotlin
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject

@Inject
class MyRepositoryImpl(
    private val api: MyApi,
    private val logger: Logger // Injected by Metro
) : MyRepository {
    override suspend fun getData() {
        logger.d { "Fetching data from API..." }
        // ... implementation
    }
}

```

#### Domain Layer (Use Cases)

If you need logging in your business logic (Use Cases), inject the `Logger` there as well. Because Kermit is a multiplatform library, it can reside safely in the domain layer without breaking pure-Kotlin rules.

```kotlin
@Inject
class GetUserDataUseCase(
    private val repository: MyRepository,
    private val logger: Logger
) {
    operator fun invoke() {
        logger.i { "Executing GetUserDataUseCase" }
        // ...
    }
}

```

### 3. Testability (Test Source Set)

One of Metro's core features for testability is the ability to **replace** contributed binding containers. In your test source set, you can provide a different `LoggingContainer` that uses Kermit's `TestLogWriter`.

#### Create the Test Logger Provider

```kotlin
import co.touchlab.kermit.TestLogWriter

@BindingContainer
@ContributesTo(
    AppScope::class, 
    replaces = [LoggingContainer::class] // Metro replaces the real provider with this one
)
interface TestLoggingContainer {
    @Provides
    @SingleIn(AppScope::class)
    fun provideTestLogger(): Logger {
        return Logger(
            config = LoggerConfig.default.copy(
                logWriterList = listOf(TestLogWriter()) // Captures logs for assertion
            )
        )
    }
}

```

#### Verification in Tests

You can then retrieve the `TestLogWriter` from the graph to verify that specific logs were emitted during the test.

```kotlin
class MyRepositoryTest {
    @Test
    fun testDataLogging() = runTest {
        val testWriter = TestLogWriter()
        val logger = Logger(config = LoggerConfig.default.copy(logWriterList = listOf(testWriter)))
        
        val repository = MyRepositoryImpl(mockApi, logger)
        repository.getData()

        // Assert that the expected log was written
        testWriter.assertCount(1)
        testWriter.assertLast().message.contains("Fetching data")
    }
}

```

### Summary of Benefits

* **Decoupling:** Your classes don't depend on a global static logger, making them easier to instantiate in isolation.
* **Multiplatform Safety:** Metro and Kermit both support `commonMain`, ensuring your Clean Architecture layers remain platform-agnostic.
* **Testable Logs:** By using Metro's `replaces` feature, you can automatically swap loggers in your test dependency graph without changing any production code.