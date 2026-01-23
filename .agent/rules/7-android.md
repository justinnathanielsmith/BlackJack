---
trigger: glob
globs: ["**/androidMain/**/*.kt"]
---

# 🤖 Android Platform Specifics

## Architecture
- **Single Activity**: Most logic should happen in `MainActivity`.

## Context
- Pass `ApplicationContext` via the Metro graph only when strictly required for platform APIs.
- Prefer common abstractions over direct Android Context usage.