#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
bash "$project_root/scripts/build.sh"
cd "$project_root"
mkdir -p build/checks
find checks -name '*.java' -print > build/check-sources.list
javac --release 21 -encoding UTF-8 -Xlint:all -cp build/classes -d build/checks @build/check-sources.list
java -Djava.awt.headless=true -cp build/classes:build/checks brainclassroom.frontend.FrontendCheck

java -Djava.awt.headless=true -cp build/classes:build/checks brainclassroom.onboarding.StarterAssessmentCheck
java -Djava.awt.headless=true -cp build/classes:build/checks brainclassroom.frontend.OnboardingCheck
