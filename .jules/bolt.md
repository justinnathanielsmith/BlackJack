## 2024-05-22 - PersistentList Optimization Pitfall
**Learning:** Assuming `id == index` in an immutable list is dangerous if ANY operation (like shuffling or swapping) changes order without updating IDs.
**Action:** Always verify invariants or use `indexOfFirst` (O(N) scan) before `set` (O(log N) update) when optimizing immutable list updates. The allocation savings (O(1) vs O(N)) are still worth the O(N) scan.

## 2024-05-24 - Unstable Lambdas in Frequent Updates
**Learning:** In Compose, passing unstable lambdas to expensive components (like Grids) causes full recomposition even if data is stable. This is critical when parent state updates frequently (e.g., a timer).
**Action:** Always wrap event handler lambdas in `remember(dependencies)` when passing them to complex sub-components, especially if the parent composable has high-frequency state updates.

## 2026-02-06 - Unstable Modifiers in Lists
**Learning:** `Modifier.onGloballyPositioned { ... }` creates a new `Modifier.Element` on every composition. When used in a list (like `items` or `Grid`), this forces the child component to recompose every time the parent does, even if other props are stable.
**Action:** Wrap complex modifiers (especially those with lambdas like `onGloballyPositioned`) in `remember` within the list item scope to ensure stability and enable skipping.

## 2026-10-24 - Unstable Modifiers in Frequent Parent Updates
**Learning:** Even outside of lists, `Modifier.onGloballyPositioned` forces recomposition. If a parent component recomposes frequently (e.g., due to a timer), any child with an unstabilized `onGloballyPositioned` modifier will also recompose, even if its data hasn't changed.
**Action:** Wrap modifiers containing `onGloballyPositioned` in `remember` (keyed by the callback) to allow the child composable to skip recomposition during frequent parent updates.

## 2026-02-08 - FQNs in Function Parameters and Linting
**Learning:** Using fully qualified names (FQNs) in function parameters can lead to brittle code and linting violations (like MaxLineLength) when combined with complex `remember` keys. It also violates project standards for explicit imports.
**Action:** Always use explicit imports for types used in function signatures and Composable parameters to keep signatures clean and maintainable.

## 2026-02-09 - Deferring State Reads to Draw Phase
**Learning:** Frequent animations (like rotation) cause recomposition every frame if the animated value is read in the composition phase. Passing `State<T>` to child composables and reading `.value` only inside `drawWithContent` or `graphicsLayer` skips composition and layout entirely.
**Action:** When animating visual properties (color, rotation, alpha) that don't affect layout size, pass `State<T>` and read it inside drawing modifiers.

## 2024-05-22 - [Optimized TimeProgressBar Animation]
**Learning:** Animated values in Compose (e.g., `animateFloatAsState`) cause recomposition on every frame if read in the composition scope. Using `Modifier.layout` (or `graphicsLayer`) allows reading the state value inside the layout/draw phase, skipping the composition phase entirely.
**Action:** When animating layout properties like width/height based on state, prefer `Modifier.layout` over `Modifier.width`/`fillMaxWidth` to avoid recomposition loops.

## 2024-10-24 - Canvas Path Allocation
**Learning:** Creating `Path` objects inside `Canvas` or `drawBehind` lambda allocates new native objects every frame, causing GC churn and CPU overhead.
**Action:** Use `Modifier.drawWithCache` to create `Path` objects and other drawing resources once (or when size changes), and reuse them in `onDrawBehind`.

## 2026-10-26 - Optimized AuroraEffect with drawWithCache
**Learning:** Infinite animations driven by `rememberInfiniteTransition` cause recomposition on every frame if state is read in the composition body. Moving state reads to `onDrawBehind` (inside `drawWithCache`) and caching expensive objects (Path, Brush) eliminates recomposition and allocation churn.
**Action:** For infinite visual effects, always use `drawWithCache` + deferred state reads to skip Composition and Layout phases.
