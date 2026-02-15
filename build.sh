#!/bin/bash

# Exit on error, undefined variables, and pipe failures
set -euo pipefail

# --- Configuration ---
APP_VERSION="1.0.0"
ANDROID_PACKAGE="io.github.smithjustinn.androidApp"
ANDROID_ACTIVITY=".AppActivity"

# --- UI Theme (Colors & Icons) ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

CHECK="✅"
ERROR="❌"
INFO="ℹ️"
BUILD="🔨"
WATCH="⏱️"
LOG="📝"

# --- Logging Functions ---

log_info()    { echo -e "${BLUE}${INFO} $1${NC}"; }
log_success() { echo -e "${GREEN}${CHECK} $1${NC}"; }
log_warn()    { echo -e "${YELLOW}${INFO} $1${NC}"; }
log_error()   { echo -e "${RED}${ERROR} $1${NC}"; }
log_header()  { echo -e "${PURPLE}=== $1 ===${NC}"; }

# --- Utilities ---

start_timer() { START_TIME=$(date +%s); }

stop_timer() {
    local end_time=$(date +%s)
    local elapsed=$((end_time - START_TIME))
    echo -e "${YELLOW}${WATCH} Task duration: ${elapsed}s${NC}"
}

show_help() {
    cat << EOF
${BLUE}Memory-Match Build & Run Script (v${APP_VERSION})${NC}
Usage: $0 [target] [options]

Targets:
  metadata  Compile common main Kotlin metadata
  android   Build (and optionally run) Android app
  ios       Build iOS application (Simulator)
  desktop   Build (and optionally run) desktop application
  test      Run all tests
  lint      Run detekt linting
  format    Apply spotless formatting
  all       Execute metadata, android, ios, and desktop sequentially
  clean     Clean all build artifacts

Options:
  --run, --launch  Build AND run/install the application (Default is build only)
  --release        Build in release mode
  --build-only     Explicitly set build-only mode (Default behavior)
  --clean          Run clean before the target
  --log            Stream logs after launch (Android)
  --filter=TAG    Filter logs by tag (Android)
  -Pkey=val        Pass custom Gradle property

Example:
  $0 android --run --log --filter=GameEngine
EOF
}

check_dependency() {
    if ! command -v "$1" &> /dev/null; then
        log_error "Dependency '$1' is not installed."
        exit 1
    fi
}

check_env() {
    log_info "Verifying environment..."
    [[ ! -f "./gradlew" ]] && { log_error "gradlew not found. Run from project root."; exit 1; }

    check_dependency "java"
    [[ "$1" == "android" ]] && check_dependency "adb"
    if [[ "$1" == "ios" || "$1" == "all" ]]; then
        [[ "$OSTYPE" != "darwin"* ]] && { log_error "iOS builds require macOS."; exit 1; }
        check_dependency "xcodebuild"
    fi
}

# --- Core Task Runners ---

run_gradle() {
    local task=$1
    shift
    local args=("$@")
    
    log_info "Executing: ./gradlew $task ${args[*]+"${args[*]}"}"
    start_timer
    if ./gradlew "$task" "${args[@]+"${args[@]}"}"; then
        log_success "Task '$task' completed."
        stop_timer
    else
        log_error "Task '$task' failed."
        exit 1
    fi
}

# --- Platform Handlers ---

handle_android() {
    if [[ "$MODE" == "release" ]]; then
        run_gradle ":androidApp:assembleRelease" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}"
        log_info "Artifact: androidApp/build/outputs/apk/release/androidApp-release.apk"
    elif [[ "$SHOULD_RUN" == "true" ]]; then
        run_gradle ":androidApp:installDebug" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}"
        log_info "Launching Android application..."
        adb shell am start -n "$ANDROID_PACKAGE/$ANDROID_PACKAGE$ANDROID_ACTIVITY"
    else
        run_gradle ":androidApp:assembleDebug" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}"
        log_info "Artifact: androidApp/build/outputs/apk/debug/androidApp-debug.apk"
    fi
    
    if [[ "$STREAM_LOGS" == "true" && "$SHOULD_RUN" == "true" ]]; then
        stream_android_logs
    fi
}

handle_ios() {
    log_info "Building iOS application ($GRADLE_MODE) for Simulator..."
    start_timer
    if xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -configuration "$GRADLE_MODE" build; then
        log_success "iOS build successful."
        stop_timer
    else
        log_error "iOS build failed."
        exit 1
    fi
}

handle_desktop() {
    local task=":desktopApp:assemble"
    [[ "$SHOULD_RUN" == "true" ]] && task=":desktopApp:run"
    run_gradle "$task" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}"
}

stream_android_logs() {
    log_info "Waiting for $ANDROID_PACKAGE PID..."
    local pid=""
    for i in {1..10}; do
        pid=$(adb shell pidof -s "$ANDROID_PACKAGE" || true)
        [[ -n "$pid" ]] && break
        sleep 0.5
    done

    local cmd=("adb" "logcat" "-v" "color")
    if [[ -n "$pid" ]]; then
        log_info "Found PID: $pid. Filtering logs..."
        cmd+=("--pid=$pid")
    else
        log_warn "PID not found. Showing all logs..."
    fi

    [[ -n "${LOG_FILTER:-}" ]] && cmd+=("$LOG_FILTER:V" "*:S")
    
    adb logcat -c # Clear buffer
    "${cmd[@]}"
}

# --- Main Execution ---

[[ $# -eq 0 ]] && { show_help; exit 0; }

# Initialize State
TARGET=$1; shift
CLEAN_FIRST=false
SHOULD_RUN=false
MODE="debug"
GRADLE_MODE="Debug"
STREAM_LOGS=false
LOG_FILTER=""
CUSTOM_PROPS=()

# Parse Remaining Options
while [[ $# -gt 0 ]]; do
    case "$1" in
        --clean)         CLEAN_FIRST=true ;;
        --run|--launch)  SHOULD_RUN=true ;;
        --build-only)    SHOULD_RUN=false ;;
        --release)       MODE="release"; GRADLE_MODE="Release" ;;
        --log)           STREAM_LOGS=true ;;
        --filter=*)      LOG_FILTER="${1#*=}" ;;
        -P*)             CUSTOM_PROPS+=("$1") ;;
        *)               log_error "Unknown option: $1"; exit 1 ;;
    esac
    shift
done

check_env "$TARGET"
log_header "Memory-Match v${APP_VERSION} [$TARGET | $MODE]"

# Execution Flow
[[ "$CLEAN_FIRST" == "true" || "$TARGET" == "clean" ]] && {
    log_warn "Cleaning build artifacts..."
    run_gradle "clean"
    [[ "$TARGET" == "clean" ]] && exit 0
}

case "$TARGET" in
    metadata) run_gradle ":sharedUI:compileCommonMainKotlinMetadata" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}" ;;
    android)  handle_android ;;
    ios)      handle_ios ;;
    desktop)  handle_desktop ;;
    test)     run_gradle "test" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}" ;;
    lint)     run_gradle "detekt" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}" ;;
    format)   run_gradle "spotlessApply" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}" ;;
    all)
        run_gradle ":sharedUI:compileCommonMainKotlinMetadata" "${CUSTOM_PROPS[@]+"${CUSTOM_PROPS[@]}"}"
        handle_android
        handle_ios
        handle_desktop
        ;;
    *) log_error "Unknown target: $TARGET"; show_help; exit 1 ;;
esac

log_success "All operations completed successfully!"
