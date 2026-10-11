#!/usr/bin/env bash
set -euo pipefail
./gradlew clean :backend:test :backend:bootJar
curl --fail http://localhost:8080/actuator/health || true
