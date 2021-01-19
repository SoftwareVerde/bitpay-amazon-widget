#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

# ensures upstream bitpay browser extension project is pulled in
# it isn't necessary to build, but is good to have to keep things roughly in sync
git submodule init

./gradlew --warning-mode=all clean test makeJar copyDependencies || exit 1
./scripts/package.sh

