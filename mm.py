#!/usr/bin/env python3
import argparse
import subprocess
import os
import sys
import shutil
from pathlib import Path
from datetime import datetime

# --- Configuration ---
APP_VERSION = "1.2.0"
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
    BOLD = '\033[1m'
    ITALIC = '\033[3m'
    NC = '\033[0m'

# --- Utility Functions ---

def log(msg, color=Colors.BLUE):
    print(f"{color}{Colors.BOLD}[mm]{Colors.NC} {msg}")

def success(msg):
    print(f"{Colors.GREEN}{Colors.BOLD}[SUCCESS]{Colors.NC} {msg}")

def warn(msg):
    print(f"{Colors.YELLOW}{Colors.BOLD}[WARN]{Colors.NC} {msg}")

def error(msg):
    print(f"{Colors.RED}{Colors.BOLD}[ERROR]{Colors.NC} {msg}")
    sys.exit(1)

def run_command(cmd, cwd=None, capture_output=False, shell=False):
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

def get_project_root():
    try:
        res = subprocess.run(["git", "rev-parse", "--show-toplevel"], capture_output=True, text=True, check=True)
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
    
    log(f"Building {target}...")
    
    if target == "android":
        task = ":androidApp:assembleRelease" if args.release else ":androidApp:assembleDebug"
        if args.run:
            task = ":androidApp:installDebug"
        
        run_command(["./gradlew", task] + gradle_args)
        
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
        ])
        
    elif target == "desktop":
        task = ":desktopApp:run" if args.run else ":desktopApp:assemble"
        run_command(["./gradlew", task] + gradle_args)
        
    elif target == "metadata":
        run_command(["./gradlew", ":sharedUI:compileCommonMainKotlinMetadata"] + gradle_args)

def handle_android_logs(args):
    log("Streaming Android logs...")
    pkg = ANDROID_PACKAGE
    try:
        pid_res = subprocess.run(["adb", "shell", "pidof", "-s", pkg], capture_output=True, text=True)
        pid = pid_res.stdout.strip()
        cmd = ["adb", "logcat", "-v", "color"]
        if pid:
            cmd.append(f"--pid={pid}")
        if args.filter:
            cmd.extend([f"{args.filter}:V", "*:S"])
        subprocess.run(cmd)
    except KeyboardInterrupt:
        pass

def handle_test(args):
    log("Running tests...")
    tasks = []
    if args.module == "all":
        tasks = [":sharedUI:allTests", ":androidApp:testDebugUnitTest", ":desktopApp:test"]
    elif args.module == "shared":
        tasks = [":sharedUI:allTests"]
    elif args.module == "android":
        tasks = [":androidApp:testDebugUnitTest"]
    elif args.module == "desktop":
        tasks = [":desktopApp:test"]
    
    for task in tasks:
        log(f"Executing {task}...")
        run_command(["./gradlew", task])
    success("Tests completed.")

def handle_task(args):
    sub = args.subcommand
    if sub == "new":
        task_type = args.type
        name = args.name
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
        target_dir = WORKTREES_DIR / name
        if str(target_dir) in os.getcwd():
            error("Cannot remove the current worktree.")
        
        log(f"Removing worktree {name}...")
        if target_dir.exists():
            run_command(["git", "worktree", "remove", str(target_dir), "--force"])
        
        # Find branch
        res = subprocess.run(["git", "branch", "--list", f"*{name}"], capture_output=True, text=True)
        branches = res.stdout.split()
        for b in branches:
            if b != "*":
                run_command(["git", "branch", "-D", b])
        success("Cleanup complete.")

def handle_sync(args):
    log("Syncing config files...")
    res = subprocess.run(["git", "worktree", "list", "--porcelain"], capture_output=True, text=True)
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
    branch_res = subprocess.run(["git", "rev-parse", "--abbrev-ref", "HEAD"], capture_output=True, text=True)
    branch = branch_res.stdout.strip()
    status_res = subprocess.run(["git", "status", "--porcelain"], capture_output=True, text=True)
    status = status_res.stdout
    modified_files = [line.split()[-1] for line in status.splitlines()]
    
    print(f"\n{Colors.BOLD}--- AI CONTEXT SUMMARY ---{Colors.NC}")
    print(f"Current Branch: {branch}")
    print(f"Status: {len(modified_files)} files modified/untracked")
    
    print(f"\n{Colors.BOLD}Impacted Modules:{Colors.NC}")
    mapping = {
        "shared/core/": "[CORE] Business Logic & Models",
        "shared/data/": "[DATA] Persistence & Network",
        "sharedUI/": "[UI] Compose Components",
        "androidApp/": "[PLATFORM] Android",
        "desktopApp/": "[PLATFORM] Desktop",
        "mm.py": "[TOOLING] CLI Manager"
    }
    for path, label in mapping.items():
        if any(f.startswith(path) or f == path for f in modified_files):
            print(f" - {label}")
            
    print(f"\n{Colors.BOLD}Recent Changes:{Colors.NC}")
    print(status if status else "No changes.")
    
    print(f"\n{Colors.BOLD}Recent History:{Colors.NC}")
    hist_res = subprocess.run(["git", "log", "--oneline", "-n", "3"], capture_output=True, text=True)
    print(hist_res.stdout)
    print(f"--------------------------\n")

def handle_done(args):
    log("Finalizing task...")
    log("Running verification...")
    run_command(["./gradlew", "spotlessApply", "detekt"])
    
    branch_res = subprocess.run(["git", "rev-parse", "--abbrev-ref", "HEAD"], capture_output=True, text=True)
    branch = branch_res.stdout.strip()
    print(f"\n{Colors.BOLD}Final Status:{Colors.NC}")
    run_command(["git", "status", "--short"])
    
    if "/" in branch:
        task_type, desc = branch.split("/", 1)
        desc = desc.replace("-", " ")
        success(f"Suggested command: git commit -m \"{task_type}: {desc}\"")
    else:
        success("Suggested command: git commit -m \"feat: description\"")

# --- Main ---

def main():
    parser = argparse.ArgumentParser(
        description=f"Memory-Match CLI v{APP_VERSION}",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=f"""
{Colors.BOLD}Examples:{Colors.NC}
  {Colors.GREEN}mm build android --run{Colors.NC}      Build and run Android app
  {Colors.GREEN}mm task new feat scoreboard{Colors.NC}   Create a new task workspace
  {Colors.GREEN}cd $(mm task locate scoreboard){Colors.NC} Jump to a task workspace
  {Colors.GREEN}mm context{Colors.NC}                    Generate AI summary
"""
    )
    subparsers = parser.add_subparsers(dest="command", help="Available commands")
    
    # Build
    build_p = subparsers.add_parser("build", help="Build the project for various platforms")
    build_p.add_argument("target", choices=["android", "ios", "desktop", "metadata"], help="Target platform")
    build_p.add_argument("--run", action="store_true", help="Launch the app after build")
    build_p.add_argument("--release", action="store_true", help="Build in release mode")
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
    remove_p.add_argument("name", help="Task name to remove")
    
    # Sync
    subparsers.add_parser("sync", help="Synchronize config files (.env, local.properties) to all workspaces")
    
    # Context
    subparsers.add_parser("context", help="Generate high-signal summary for AI agents")
    
    # Done
    subparsers.add_parser("done", help="Verify task, format code, and prepare commit")

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(0)
        
    args = parser.parse_args()
    
    if args.command == "build": handle_build(args)
    elif args.command == "test": handle_test(args)
    elif args.command == "task": handle_task(args)
    elif args.command == "sync": handle_sync(args)
    elif args.command == "context": handle_context(args)
    elif args.command == "done": handle_done(args)

if __name__ == "__main__":
    main()
