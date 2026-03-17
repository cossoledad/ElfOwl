#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EXAMPLE_DIR="$ROOT_DIR/example"
BUILD_DIR="$EXAMPLE_DIR/build"
NATIVE_BUILD_DIR="$BUILD_DIR/native-build"
NATIVE_OUT_DIR="$BUILD_DIR/native"
LIBRARY_CLASSES_DIR="$BUILD_DIR/library-classes"
EXAMPLE_CLASSES_DIR="$BUILD_DIR/example-classes"

rm -rf "$BUILD_DIR"
mkdir -p "$NATIVE_OUT_DIR" "$LIBRARY_CLASSES_DIR" "$EXAMPLE_CLASSES_DIR"

cmake -S "$EXAMPLE_DIR" -B "$NATIVE_BUILD_DIR" -DCMAKE_LIBRARY_OUTPUT_DIRECTORY="$NATIVE_OUT_DIR"
cmake --build "$NATIVE_BUILD_DIR" --parallel

javac -d "$LIBRARY_CLASSES_DIR" $(find "$ROOT_DIR/src/main/java" -name '*.java' | sort)
javac -cp "$LIBRARY_CLASSES_DIR" -d "$EXAMPLE_CLASSES_DIR" $(find "$EXAMPLE_DIR/java" -name '*.java' | sort)

java -cp "$LIBRARY_CLASSES_DIR:$EXAMPLE_CLASSES_DIR" org.elfowl.example.NativeDemo "$NATIVE_OUT_DIR"
