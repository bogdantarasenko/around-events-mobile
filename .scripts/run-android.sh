#!/bin/sh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

./gradlew :androidApp:installDebug --quiet --console=plain
adb shell monkey -p events.around.app.debug -c android.intent.category.LAUNCHER 1
