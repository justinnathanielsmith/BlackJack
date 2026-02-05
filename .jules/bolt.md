## 2024-05-22 - PersistentList Optimization Pitfall
**Learning:** Assuming `id == index` in an immutable list is dangerous if ANY operation (like shuffling or swapping) changes order without updating IDs.
**Action:** Always verify invariants or use `indexOfFirst` (O(N) scan) before `set` (O(log N) update) when optimizing immutable list updates. The allocation savings (O(1) vs O(N)) are still worth the O(N) scan.

## 2024-05-24 - Unstable Lambdas in Frequent Updates
**Learning:** In Compose, passing unstable lambdas to expensive components (like Grids) causes full recomposition even if data is stable. This is critical when parent state updates frequently (e.g., a timer).
**Action:** Always wrap event handler lambdas in `remember(dependencies)` when passing them to complex sub-components, especially if the parent composable has high-frequency state updates.
