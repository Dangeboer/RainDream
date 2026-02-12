#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./scripts_export_frontend.sh /absolute/path/to/Project/rain-dream-frontend
#
# This script exports the standalone frontend folder from this backend repo
# into a completely separate directory, and optionally initializes a new git repo.

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <target_frontend_dir>"
  exit 1
fi

TARGET_DIR="$1"
SOURCE_DIR="rain-dream-frontend"

if [[ ! -d "$SOURCE_DIR" ]]; then
  echo "Error: source folder '$SOURCE_DIR' not found in current directory."
  exit 1
fi

mkdir -p "$TARGET_DIR"

# copy frontend files
rsync -a --delete "$SOURCE_DIR/" "$TARGET_DIR/"

echo "Frontend exported to: $TARGET_DIR"

echo
read -r -p "Initialize as independent git repo in target directory? [y/N] " INIT_GIT
if [[ "$INIT_GIT" =~ ^[Yy]$ ]]; then
  (
    cd "$TARGET_DIR"
    if [[ ! -d .git ]]; then
      git init
      git add .
      git commit -m "chore: initial import of rain-dream frontend"
      echo "Initialized independent git repo at $TARGET_DIR"
    else
      echo "Target already has .git, skip init."
    fi
  )
fi

echo "Done."
