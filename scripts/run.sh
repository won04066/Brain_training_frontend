#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
bash "$project_root/scripts/build.sh"
exec java -cp "$project_root/build/classes" brainclassroom.frontend.FrontendApp
