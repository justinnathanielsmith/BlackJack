## 2025-01-28 - Avoid Iterator allocation in withFrameNanos loop
**Learning:** `forEach` inside `withFrameNanos` allocates an Iterator object every frame.
**Action:** Replace `forEach` with an index-based `for` loop (e.g., `for (i in 0 until list.size)`) to avoid GC overhead and O(N^2) shifting inside hot loops.
