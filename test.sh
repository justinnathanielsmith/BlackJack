#!/bin/bash

# Exit on error
set -e

echo "🚀 Starting all tests for Memory-Match..."

# 1. Run Shared Module Tests (Common, Android, JVM, iOS)
echo "📦 Running Shared UI tests..."
./gradlew :sharedUI:allTests

# 2. Run Android App Tests
echo "🤖 Running Android App unit tests..."
./gradlew :androidApp:testDebugUnitTest

# 3. Run Desktop App Tests
echo "💻 Running Desktop App tests..."
./gradlew :desktopApp:test

echo "✅ All tests passed successfully!"
