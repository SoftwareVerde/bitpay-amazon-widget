#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

# ensures upstream bitpay browser extension project is pulled in
# it isn't necessary to build, but is good to have to keep things roughly in sync
git submodule init

./gradlew makeJar copyDependencies || exit 1

cp -R build/libs/* out/bin
cp -R conf/* out/conf

./scripts/make-www.sh

