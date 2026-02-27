#!/usr/bin/env bash
set -euo pipefail

REPO="grayankit/task-planner"
INSTALL_DIR="$HOME/.local/bin/TaskPlanner"
DESKTOP_FILE="$HOME/.local/share/applications/taskplanner.desktop"

# Get version: use argument, or fetch latest from GitHub
if [[ $# -ge 1 ]]; then
    VERSION="$1"
else
    VERSION=$(gh release view --repo "$REPO" --json tagName -q '.tagName')
fi

ASSET="TaskPlanner-${VERSION#v}-linux-x64.tar.gz"
TMP_DIR=$(mktemp -d)

cleanup() { rm -rf "$TMP_DIR"; }
trap cleanup EXIT

echo ":: Installing Task Planner $VERSION"

# Download
echo ":: Downloading $ASSET..."
gh release download "$VERSION" --repo "$REPO" --pattern "$ASSET" --dir "$TMP_DIR"

# Extract
echo ":: Extracting..."
rm -rf "$INSTALL_DIR"
mkdir -p "$(dirname "$INSTALL_DIR")"
tar xzf "$TMP_DIR/$ASSET" -C "$(dirname "$INSTALL_DIR")"
chmod +x "$INSTALL_DIR/bin/TaskPlanner"

# Desktop entry
echo ":: Creating desktop entry..."
mkdir -p "$(dirname "$DESKTOP_FILE")"
cat > "$DESKTOP_FILE" << EOF
[Desktop Entry]
Name=Task Planner
Comment=Offline-first task planner with sync
Exec=$INSTALL_DIR/bin/TaskPlanner
Icon=$INSTALL_DIR/lib/TaskPlanner.png
Terminal=false
Type=Application
Categories=Office;ProjectManagement;
StartupWMClass=TaskPlanner
EOF
update-desktop-database "$(dirname "$DESKTOP_FILE")" 2>/dev/null || true

echo ":: Task Planner $VERSION installed successfully"
