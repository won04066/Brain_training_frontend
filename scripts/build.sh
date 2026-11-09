#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_root"
mkdir -p build/classes
find src -name '*.java' -print > build/sources.list
javac --release 21 -encoding UTF-8 -Xlint:all -d build/classes @build/sources.list
