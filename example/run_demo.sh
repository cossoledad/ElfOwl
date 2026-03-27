#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EXAMPLE_DIR="$ROOT_DIR/example"
BUILD_DIR="$EXAMPLE_DIR/build"
NATIVE_BUILD_DIR="$BUILD_DIR/native-build"
NATIVE_OUT_DIR="$BUILD_DIR/native"
ELFOWL_VERSION="${ELFOWL_VERSION:-v0.1.1}"

rm -rf "$BUILD_DIR"
mkdir -p "$NATIVE_OUT_DIR"

cmake -S "$EXAMPLE_DIR" -B "$NATIVE_BUILD_DIR" -DCMAKE_LIBRARY_OUTPUT_DIRECTORY="$NATIVE_OUT_DIR"
cmake --build "$NATIVE_BUILD_DIR" --parallel

mvn --batch-mode -f "$EXAMPLE_DIR/pom.xml" \
  -Dexec.args="$NATIVE_OUT_DIR" \
  clean compile exec:java
