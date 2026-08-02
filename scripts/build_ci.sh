#!/bin/bash
set -euo pipefail

PROJECT="AlarmaFutura/AlarmaFutura.xcodeproj"
SCHEME="AlarmaFutura"
DERIVED_DATA="${DERIVED_DATA_PATH:-${RUNNER_TEMP:-/tmp}/AlarmaFuturaDerivedData}"
LOG_FILE="${BUILD_LOG_PATH:-xcodebuild.log}"

echo "=== Entorno ==="
sw_vers || true
xcodebuild -version
swift --version

echo "=== Proyecto y esquemas ==="
xcodebuild -list -project "$PROJECT"

echo "=== Compilación para simulador, sin firma ==="
set -o pipefail
xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath "$DERIVED_DATA" \
  CODE_SIGNING_ALLOWED=NO \
  CODE_SIGNING_REQUIRED=NO \
  build 2>&1 | tee "$LOG_FILE"

echo "=== Aplicación compilada ==="
find "$DERIVED_DATA/Build/Products" -maxdepth 3 -name 'AlarmaFutura.app' -print
