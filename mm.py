#!/usr/bin/env python3
import argparse
import subprocess
import os
import sys
import shutil
import threading
import time
import re
from pathlib import Path
from datetime import datetime

# --- Configuration ---
APP_VERSION = "1.3.0"
CONFIG_FILES = ["local.properties", ".env", "google-services.json"]
ANDROID_PACKAGE = "io.github.smithjustinn.androidApp"
ANDROID_ACTIVITY = ".AppActivity"

# --- UI / Colors ---
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    PURPLE = '\033[0;35m'
    CYAN = '\033[0;36m'
    GRAY = '\033[0;90m'
    BOLD = '\033[1m'
    ITALIC = '\033[3m'
    NC = '\033[0m'

ASCII_ART = fr"""{Colors.CYAN}
   __  __                                __  __       _       _     
  |  \/  | ___ _ __ ___   ___  _ __ _   _|  \/  | __ _| |_ ___| |__  
  | |\/| |/ _ \ '_ ` _ \ / _ \| '__| | | | |\/| |/ _` | __/ __| '_ \ 
  | |  | |  __/ | | | | | (_) | |  | |_| | |  | | (_| | || (__| | | |
  |_|  |_|\___|_| |_| |_|\___/|_|   \__, |_|  |_|\__,_|\__\___|_| |_|
                                     |___/                           {Colors.NC}"""

class Spinner:
    def __init__(self, message="Working..."):
        self.message = message
        self.stop_event = threading.Event()
        self.thread = threading.Thread(target=self._spin)

    def _spin(self):
        chars = "⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏"
        i = 0
        while not self.stop_event.is_set():
            sys.stdout.write(f"\r{Colors.BLUE}{chars[i % len(chars)]}{Colors.NC} {self.message}")
            sys.stdout.flush()
            time.sleep(0.1)
            i += 1
        sys.stdout.write("\r" + " " * (len(self.message) + 2) + "\r")
        sys.stdout.flush()

    def __enter__(self):
        if sys.stdout.isatty():
            self.thread.start()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if sys.stdout.isatty():
            self.stop_event.set()
            self.thread.join()

# --- Utility Functions ---

def validate_task_name(name):
    if not re.match(r'^[a-zA-Z0-9_-]+$', name):
        error(f"Invalid task name '{name}'. Only alphanumeric characters, hyphens, and underscores are allowed.")
    return name

def log(msg, color=Colors.BLUE):
    print(f"{Colors.GRAY}•{Colors.NC} {color}{Colors.BOLD}{msg}{Colors.NC}")

def success(msg):
    print(f"{Colors.GREEN}✔{Colors.NC} {Colors.GREEN}{Colors.BOLD}{msg}{Colors.NC}")

def warn(msg):
    print(f"{Colors.YELLOW}⚠{Colors.NC} {Colors.YELLOW}{Colors.BOLD}{msg}{Colors.NC}")

def error(msg):
    print(f"{Colors.RED}✘{Colors.NC} {Colors.RED}{Colors.BOLD}{msg}{Colors.NC}")
    sys.exit(1)

def check_command(cmd):
    return shutil.which(cmd) is not None

def run_command(cmd, cwd=None, capture_output=False, shell=False, show_spinner=False, spinner_msg="Running..."):
    if show_spinner:
        with Spinner(spinner_msg):
            return _run_command(cmd, cwd, capture_output, shell)
    return _run_command(cmd, cwd, capture_output, shell)

def _run_command(cmd, cwd=None, capture_output=False, shell=False):
    try:
        result = subprocess.run(
            cmd, 
            cwd=cwd, 
            check=True, 
            text=True, 
            capture_output=capture_output, 
            shell=shell
        )
        return result
    except subprocess.CalledProcessError as e:
        if not capture_output:
            error(f"Command failed: {' '.join(cmd) if isinstance(cmd, list) else cmd}")
        return e

def run_gradle(tasks, params=None, show_spinner=True, msg="Gradle executing..."):
    cmd = ["./gradlew"] + tasks
    if params:
        cmd.extend(params)
    return run_command(cmd, show_spinner=show_spinner, spinner_msg=msg)

def get_project_root():
    try:
        res = run_command(["git", "rev-parse", "--show-toplevel"], capture_output=True)
        return Path(res.stdout.strip())
    except Exception:
        return Path(os.getcwd())

PROJECT_ROOT = get_project_root()
WORKTREES_DIR = PROJECT_ROOT.parent / "worktrees"

# --- Command Handlers ---

def handle_build(args):
    target = args.target
    gradle_args = args.params or []
    
    if args.release:
        gradle_args.append("-Prelease=true")
    
    tasks = []
    if args.clean:
        tasks.append("clean")

    log(f"Building {target}...")
    
    if target == "android":
        task = ":androidApp:assembleRelease" if args.release else ":androidApp:assembleDebug"
        if args.run:
            task = ":androidApp:installDebug"
        
        tasks.append(task)
        run_gradle(tasks, params=gradle_args, msg=f"Building Android ({'Release' if args.release else 'Debug'})...")
        
        if args.run:
            log("Launching Android app...")
            run_command(["adb", "shell", "am", "start", "-n", f"{ANDROID_PACKAGE}/{ANDROID_PACKAGE}{ANDROID_ACTIVITY}"])
            if args.log:
                handle_android_logs(args)
                
    elif target == "ios":
        if sys.platform != "darwin":
            error("iOS builds require macOS.")
        config = "Release" if args.release else "Debug"
        log(f"Building iOS for Simulator ({config})...")
        run_command([
            "xcodebuild", "-project", "iosApp/iosApp.xcodeproj", 
            "-scheme", "iosApp", "-sdk", "iphonesimulator", 
            "-configuration", config, "build"
        ], show_spinner=True, spinner_msg=f"Building iOS ({config})...")
        
    elif target == "desktop":
        task = ":desktopApp:run" if args.run else ":desktopApp:assemble"
        tasks.append(task)
        run_gradle(tasks, params=gradle_args, msg=f"Building Desktop...")
        
    elif target == "metadata":
        run_gradle([":sharedUI:compileCommonMainKotlinMetadata"], params=gradle_args, msg="Compiling KMP Metadata...")

def handle_android_logs(args):
    log("Streaming Android logs...")
    pkg = ANDROID_PACKAGE
    try:
        pid_res = run_command(["adb", "shell", "pidof", "-s", pkg], capture_output=True)
        pid = pid_res.stdout.strip()
        cmd = ["adb", "logcat", "-v", "color"]
        if pid:
            cmd.append(f"--pid={pid}")
        if args.filter:
            cmd.extend([f"{args.filter}:V", "*:S"])
        subprocess.run(cmd) # Streaming
    except KeyboardInterrupt:
        pass

def handle_test(args):
    log(f"Running tests for module: {args.module}...")
    tasks = []
    if args.module == "all":
        tasks = [":sharedUI:allTests", ":androidApp:testDebugUnitTest", ":desktopApp:test"]
    elif args.module == "shared":
        tasks = [":sharedUI:allTests"]
    elif args.module == "android":
        tasks = [":androidApp:testDebugUnitTest"]
    elif args.module == "desktop":
        tasks = [":desktopApp:test"]
    
    run_gradle(tasks, msg=f"Executing {len(tasks)} test suites...")
    success("Tests completed successfully.")

def handle_task(args):
    sub = args.subcommand
    if sub == "new":
        task_type = args.type
        name = validate_task_name(args.name)
        branch = f"{task_type}/{name}"
        target_dir = WORKTREES_DIR / name
        
        if target_dir.exists():
            error(f"Worktree directory already exists: {target_dir}")
        
        log(f"Creating worktree for '{branch}'...")
        WORKTREES_DIR.mkdir(exist_ok=True)
        run_command(["git", "worktree", "add", str(target_dir), "-b", branch])
        
        # Sync configs
        for f in CONFIG_FILES:
            src = PROJECT_ROOT / f
            if src.exists():
                shutil.copy(src, target_dir / f)
        
        success(f"Worktree ready: {target_dir}")
        print(f"Tip: cd {target_dir}")
        
    elif sub == "list":
        run_command(["git", "worktree", "list"])

    elif sub == "locate":
        name = args.name
        res = subprocess.run(["git", "worktree", "list", "--porcelain"], capture_output=True, text=True)
        path = None
        for line in res.stdout.splitlines():
            if line.startswith("worktree") and line.endswith(f"/{name}"):
                path = line.split()[1]
                break
        if path:
            print(path)
        else:
            error(f"Workspace '{name}' not found.")

    elif sub == "clean":
        log("Pruning orphaned worktree metadata...")
        run_command(["git", "worktree", "prune"])
        success("Cleanup complete.")
        
    elif sub == "remove":
        name = args.name
        if not name:
            res = run_command(["git", "worktree", "list", "--porcelain"], capture_output=True)
            worktrees = []
            for line in res.stdout.splitlines():
                if line.startswith("worktree"):
                    path = Path(line.split()[1])
                    if path != PROJECT_ROOT:
                        worktrees.append(path.name)
            
            if not worktrees:
                log("No additional worktrees found.")
                return
                
            print(f"\n{Colors.BOLD}Select worktree to remove:{Colors.NC}")
            for i, wt in enumerate(worktrees):
                print(f"  {i+1}) {wt}")
            
            try:
                choice = input(f"\nSelection (1-{len(worktrees)}, or 'c' to cancel): ")
                if choice.lower() == 'c': return
                idx = int(choice) - 1
                name = worktrees[idx]
            except (ValueError, IndexError):
                error("Invalid selection.")

        validate_task_name(name)
        target_dir = WORKTREES_DIR / name
        if str(target_dir) in os.getcwd():
            error("Cannot remove the current worktree.")
        
        log(f"Removing worktree {name}...")
        if target_dir.exists():
            run_command(["git", "worktree", "remove", str(target_dir), "--force"])
        else:
            # Try removing by name if dir is gone
            run_command(["git", "worktree", "prune"])
        
        # Find and remove branch
        res = run_command(["git", "branch", "--list", f"*{name}"], capture_output=True)
        branches = res.stdout.split()
        for b in branches:
            if b != "*" and b.endswith(name):
                run_command(["git", "branch", "-D", b])
        success(f"Worktree '{name}' and associated branches removed.")

def handle_sync(args):
    log("Syncing config files...")
    res = run_command(["git", "worktree", "list", "--porcelain"], capture_output=True)
    paths = [line.split()[1] for line in res.stdout.splitlines() if line.startswith("worktree")]
    
    for p in paths:
        path = Path(p)
        if path == PROJECT_ROOT:
            continue
        log(f"Syncing to {path.name}...")
        for f in CONFIG_FILES:
            src = PROJECT_ROOT / f
            if src.exists():
                shutil.copy(src, path / f)
    success("All worktrees synchronized.")

def handle_context(args):
    log("Generating AI Context Summary...")
    branch_res = run_command(["git", "rev-parse", "--abbrev-ref", "HEAD"], capture_output=True)
    branch = branch_res.stdout.strip()
    status_res = run_command(["git", "status", "--porcelain"], capture_output=True)
    status = status_res.stdout
    modified_files = [line.split()[-1] for line in status.splitlines()]
    
    print(f"\n{Colors.CYAN}{Colors.BOLD}--- AI CONTEXT SUMMARY ---{Colors.NC}")
    print(f"{Colors.BOLD}Current Branch:{Colors.NC} {Colors.PURPLE}{branch}{Colors.NC}")
    print(f"{Colors.BOLD}Status:{Colors.NC} {Colors.YELLOW}{len(modified_files)}{Colors.NC} files modified/untracked")
    
    # Coverage Info
    report_path = PROJECT_ROOT / "docs/KOVER_COVERAGE_SUMMARY.md"
    if report_path.exists():
        with open(report_path, "r") as f:
            for line in f:
                if "Mission Accomplished" in line or "Line Coverage" in line:
                    coverage_text = line.strip().replace('#', '').strip()
                    print(f"{Colors.BOLD}Coverage:{Colors.NC} {Colors.GREEN}{coverage_text}{Colors.NC}")
                    break

    print(f"\n{Colors.CYAN}{Colors.BOLD}Impacted Modules:{Colors.NC}")
    mapping = {
        "shared/core/": f"{Colors.BLUE}[CORE]{Colors.NC} Business Logic & Models",
        "shared/data/": f"{Colors.BLUE}[DATA]{Colors.NC} Persistence & Network",
        "sharedUI/": f"{Colors.BLUE}[UI]{Colors.NC} Compose Components",
        "androidApp/": f"{Colors.BLUE}[PLATFORM]{Colors.NC} Android",
        "desktopApp/": f"{Colors.BLUE}[PLATFORM]{Colors.NC} Desktop",
        "mm.py": f"{Colors.BLUE}[TOOLING]{Colors.NC} CLI Manager"
    }
    impacted = set()
    for f in modified_files:
        for path, label in mapping.items():
            if f.startswith(path):
                impacted.add(label)
    
    for label in sorted(impacted):
        print(f" {Colors.GRAY}•{Colors.NC} {label}")
            
    print(f"\n{Colors.CYAN}{Colors.BOLD}Recent Changes:{Colors.NC}")
    if status:
        for line in status.splitlines():
            mode, file = line[:2], line[3:]
            color = Colors.GREEN if 'A' in mode or '?' in mode else Colors.YELLOW
            print(f" {color}{mode}{Colors.NC} {file}")
    else:
        print(f" {Colors.GRAY}No uncommitted changes.{Colors.NC}")
    
    print(f"\n{Colors.CYAN}{Colors.BOLD}Recent History (Last 3 Commits):{Colors.NC}")
    hist_res = run_command(["git", "log", "--oneline", "-n", "3"], capture_output=True)
    for line in hist_res.stdout.splitlines():
        sha, msg = line.split(' ', 1)
        print(f" {Colors.YELLOW}{sha}{Colors.NC} {msg}")
    print(f"{Colors.CYAN}--------------------------{Colors.NC}\n")

def handle_done(args):
    log("Finalizing task...")
    log("Running verification...")
    run_gradle(["spotlessApply", "detekt"], msg="Verifying and formatting...")
    
    branch_res = run_command(["git", "rev-parse", "--abbrev-ref", "HEAD"], capture_output=True)
    branch = branch_res.stdout.strip()
    print(f"\n{Colors.BOLD}Final Status:{Colors.NC}")
    run_command(["git", "status", "--short"])
    
    if "/" in branch:
        task_type, desc = branch.split("/", 1)
        desc = desc.replace("-", " ")
        success(f"Suggested command: git commit -m \"{task_type}: {desc}\"")
    else:
        success("Suggested command: git commit -m \"feat: description\"")

def handle_doctor(args):
    log("Checking environment health...")
    checks = [
        ("JDK", ["java", "-version"]),
        ("Git", ["git", "--version"]),
        ("ADB", ["adb", "version"]),
        ("Gradle", ["./gradlew", "--version"]),
    ]
    if sys.platform == "darwin":
        checks.append(("Xcode", ["xcodebuild", "-version"]))

    all_passed = True
    for name, cmd in checks:
        if check_command(cmd[0]):
            try:
                res = subprocess.run(cmd, capture_output=True, text=True)
                lines = (res.stdout or res.stderr).splitlines()
                ver = "Unknown"
                for line in lines:
                    line = line.strip()
                    if line and not line.startswith("-") and not line.startswith("("):
                        ver = line
                        break
                print(f" {Colors.GREEN}✓{Colors.NC} {name:8} : {ver}")
            except Exception:
                print(f" {Colors.YELLOW}?{Colors.NC} {name:8} : Found but could not get version")
        else:
            print(f" {Colors.RED}✗{Colors.NC} {name:8} : Not found")
            all_passed = False

    if all_passed:
        success("Environment looks good!")
    else:
        warn("Some dependencies are missing. Please check your PATH.")

def handle_lint(args):
    tasks = []
    if args.fix:
        log("Applying lint fixes...")
        tasks = ["spotlessApply", "detekt"]
    else:
        log("Checking linting...")
        tasks = ["spotlessCheck", "detekt"]
    
    run_gradle(tasks, msg="Linting...")
    success("Linting complete.")

def handle_clean(args):
    log("Cleaning project...")
    run_gradle(["clean"], msg="Gradle cleaning...")
    success("Project cleaned.")

def handle_open(args):
    if args.target == "android":
        log("Opening in Android Studio...")
        if sys.platform == "darwin":
            run_command(["open", "-a", "Android Studio", "."])
        else:
            run_command(["studio", "."], shell=True)
    elif args.target == "ios":
        if sys.platform != "darwin":
            error("iOS targets require macOS.")
        log("Opening in Xcode...")
        run_command(["open", "iosApp/iosApp.xcodeproj"])

def handle_coverage(args):
    log("Generating coverage report...")
    run_gradle(["koverHtmlReport"], msg="Generating Kover reports...")
    report_path = PROJECT_ROOT / "build/reports/kover/html/index.html"
    if report_path.exists():
        success(f"Report generated: {report_path}")
        if args.open:
            if sys.platform == "darwin":
                run_command(["open", str(report_path)])
            else:
                log(f"View report at: file://{report_path}")
    else:
        error("Coverage report not found. Ensure Kover is correctly configured.")

def handle_status(args):
    log("Project Status Overview")
    print(f"\n{Colors.CYAN}{Colors.BOLD}--- GIT STATUS ---{Colors.NC}")
    status_res = run_command(["git", "status", "--short"], capture_output=True)
    if status_res.stdout:
        for line in status_res.stdout.splitlines():
            mode, file = line[:2], line[3:]
            color = Colors.GREEN if 'A' in mode or '?' in mode else Colors.YELLOW
            print(f" {color}{mode}{Colors.NC} {file}")
    else:
        print(f" {Colors.GREEN}Clean (nothing to commit){Colors.NC}")
    
    print(f"\n{Colors.CYAN}{Colors.BOLD}--- ACTIVE WORKTREES ---{Colors.NC}")
    wt_res = run_command(["git", "worktree", "list"], capture_output=True)
    for line in wt_res.stdout.splitlines():
        parts = line.split()
        path = parts[0]
        sha = parts[1]
        branch = parts[2] if len(parts) > 2 else ""
        print(f" {Colors.BLUE}{path:40}{Colors.NC} {Colors.YELLOW}{sha}{Colors.NC} {Colors.PURPLE}{branch}{Colors.NC}")

# --- Main ---

def main():
    parser = argparse.ArgumentParser(
        description=f"Memory-Match CLI v{APP_VERSION}",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=f"""
{Colors.BOLD}Examples:{Colors.NC}
  {Colors.GREEN}mm build android --run{Colors.NC}      Build and run Android app
  {Colors.GREEN}mm task new feat scoreboard{Colors.NC}   Create a new task workspace
  {Colors.GREEN}mm doctor{Colors.NC}                     Check environment health
  {Colors.GREEN}mm context{Colors.NC}                    Generate AI summary
"""
    )
    parser.add_argument("--no-banner", action="store_true", help="Do not display the ASCII art banner")
    subparsers = parser.add_subparsers(dest="command", help="Available commands")
    
    # ... (rest of subparsers remain the same)
    build_p = subparsers.add_parser("build", help="Build the project for various platforms")
    build_p.add_argument("target", choices=["android", "ios", "desktop", "metadata"], help="Target platform")
    build_p.add_argument("--run", action="store_true", help="Launch the app after build")
    build_p.add_argument("--release", action="store_true", help="Build in release mode")
    build_p.add_argument("--clean", action="store_true", help="Run clean before build")
    build_p.add_argument("--log", action="store_true", help="Stream logs (Android only)")
    build_p.add_argument("--filter", help="Filter logs by tag (Android only)")
    build_p.add_argument("params", nargs="*", help="Extra Gradle parameters")
    
    # Test
    test_p = subparsers.add_parser("test", help="Run project unit tests")
    test_p.add_argument("module", choices=["all", "shared", "android", "desktop"], default="all", nargs="?", 
                        help="Module to test (default: all)")
    
    # Task (Worktree)
    task_p = subparsers.add_parser("task", help="Manage development tasks using Git Worktrees")
    task_sub = task_p.add_subparsers(dest="subcommand", required=True)
    
    new_p = task_sub.add_parser("new", help="Create a new workspace for a feature or fix")
    new_p.add_argument("type", choices=["feat", "fix", "refactor", "chore"], help="Task category")
    new_p.add_argument("name", help="Task name (used for branch and directory)")
    
    task_sub.add_parser("list", help="List all active task workspaces")
    
    locate_p = task_sub.add_parser("locate", help="Find the absolute path of a workspace")
    locate_p.add_argument("name", help="Task name to locate")
    
    task_sub.add_parser("clean", help="Remove metadata for orphaned worktrees")
    
    remove_p = task_sub.add_parser("remove", help="Safely delete a workspace and its branch")
    remove_p.add_argument("name", nargs="?", help="Task name to remove (interactive if omitted)")
    
    # Sync
    subparsers.add_parser("sync", help="Synchronize config files (.env, local.properties) to all workspaces")
    
    # Context
    subparsers.add_parser("context", help="Generate high-signal summary for AI agents")
    
    # Done
    subparsers.add_parser("done", help="Verify task, format code, and prepare commit")

    # Doctor
    subparsers.add_parser("doctor", help="Check development environment for missing dependencies")

    # Lint
    lint_p = subparsers.add_parser("lint", help="Run linting and code style checks")
    lint_p.add_argument("--fix", action="store_true", help="Automatically fix style issues")

    # Clean
    subparsers.add_parser("clean", help="Run gradle clean")

    # Open
    open_p = subparsers.add_parser("open", help="Open project in IDE")
    open_p.add_argument("target", choices=["android", "ios"], help="Target IDE (Android Studio or Xcode)")

    # Coverage
    cov_p = subparsers.add_parser("coverage", help="Generate and view code coverage reports")
    cov_p.add_argument("--open", action="store_true", help="Open HTML report in browser")

    # Status
    subparsers.add_parser("status", help="Show unified project and worktree status")

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(0)
        
    args = parser.parse_args()
    
    if not args.no_banner and args.command != "locate":
        print(ASCII_ART)
        print(f"  {Colors.BOLD}Memory-Match CLI{Colors.NC} {Colors.GRAY}v{APP_VERSION}{Colors.NC}\n")

    if args.command == "build": handle_build(args)
    elif args.command == "test": handle_test(args)
    elif args.command == "task": handle_task(args)
    elif args.command == "sync": handle_sync(args)
    elif args.command == "context": handle_context(args)
    elif args.command == "done": handle_done(args)
    elif args.command == "doctor": handle_doctor(args)
    elif args.command == "lint": handle_lint(args)
    elif args.command == "clean": handle_clean(args)
    elif args.command == "open": handle_open(args)
    elif args.command == "coverage": handle_coverage(args)
    elif args.command == "status": handle_status(args)

if __name__ == "__main__":
    main()
