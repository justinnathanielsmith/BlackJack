#!/bin/bash

# Usage: ./setup_agent_branch.sh <branch-name> <folder-name>
BRANCH_NAME=$1
FOLDER_NAME=$2

if [ -z "$BRANCH_NAME" ] || [ -z "$FOLDER_NAME" ]; then
    echo "Usage: ./setup_agent_branch.sh <branch-name> <folder-name>"
    exit 1
fi

# 1. Create the worktree
echo "Creating worktree for branch '$BRANCH_NAME' in '../$FOLDER_NAME'..."
git worktree add ../$FOLDER_NAME -b $BRANCH_NAME

# 2. Copy configuration files (Android SDK paths, API keys, etc.)
echo "Copying local configuration files..."
FILES_TO_COPY=("local.properties" ".env" "google-services.json")

for FILE in "${FILES_TO_COPY[@]}"; do
    if [ -f "$FILE" ]; then
        cp "$FILE" "../$FOLDER_NAME/"
        echo " - Copied $FILE"
    else
        echo " - Skipping $FILE (not found)"
    fi
done

# 3. Move into the new directory and sync
cd ../$FOLDER_NAME
echo "Starting Gradle sync..."
./gradlew help --daemon

echo "----------------------------------------------------"
echo "Setup Complete!"
echo "Workspace: $(pwd)"
echo "----------------------------------------------------"
