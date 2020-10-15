#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

./gradlew --warning-mode=all clean test makeJar copyDependencies || exit 1
./scripts/package.sh

