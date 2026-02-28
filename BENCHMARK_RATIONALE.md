# Performance Benchmark Rationale

## Objective
Optimize `AndroidAudioServiceImpl.kt` by replacing the blocking `prepare()` call with `prepareAsync()` to prevent UI jank during media preparation.

## Rationale
The `MediaPlayer.prepare()` method is a synchronous call that fetches and decodes media metadata. On the Android main thread, this operation blocks UI rendering, leading to dropped frames and a poor user experience. `MediaPlayer.prepareAsync()` offloads this work to a background thread and invokes a callback when ready, ensuring the main thread remains responsive.

## Verification Constraints
Running a precise performance benchmark for this change requires an Android environment (emulator or physical device) with UI rendering capabilities to measure frame drops or execution time on the main thread. The current development environment lacks these capabilities.

## Verification Strategy
Instead of a runtime benchmark, the optimization is verified through:
1. **Static Analysis:** Confirming the use of `prepareAsync()` and correct callback implementation.
2. **Compilation Checks:** Ensuring the code compiles without errors using `./gradlew :sharedUI:assembleDebug`.
3. **Logic Verification:** Reviewing the code to ensure thread safety and correct state handling (e.g., handling `stop()` calls during preparation).
4. **Existing Tests:** Running the existing test suite via `./run_tests.sh` to ensure no regressions.

This approach ensures the structural correctness of the optimization while acknowledging the limitations of the current environment for runtime profiling.

## Objective
Optimize `PlayingCard.kt` by replacing `Modifier.offset` with `graphicsLayer { translationX = ... }` for the shake animation.

## Rationale
In Compose, animating `Modifier.offset` using a continuous state (like `shakeOffset.value`) causes recomposition and triggers the Layout phase on every frame of the animation. This leads to layout thrashing and increased CPU/memory usage. By moving the translation logic into the `graphicsLayer` block, the animation only triggers the Draw phase, bypassing the expensive Composition and Layout phases. This significantly improves performance, especially during high-frequency animations like card shaking.

## Verification Strategy
Instead of a runtime benchmark, the optimization is verified through:
1. **Static Analysis:** Confirming the use of `graphicsLayer { translationX = ... }` and removal of `Modifier.offset` for the continuous animation.
2. **Compilation Checks:** Ensuring the code compiles without errors.
3. **Existing Tests:** Running the test suite to ensure visual components still function as expected.
