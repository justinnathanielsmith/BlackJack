## 2024-05-22 - PersistentList Optimization Pitfall
**Learning:** Assuming `id == index` in an immutable list is dangerous if ANY operation (like shuffling or swapping) changes order without updating IDs.
**Action:** Always verify invariants or use `indexOfFirst` (O(N) scan) before `set` (O(log N) update) when optimizing immutable list updates. The allocation savings (O(1) vs O(N)) are still worth the O(N) scan.
