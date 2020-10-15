#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

./gradlew makeJar copyDependencies || exit 1

cp -R build/libs/* out/bin
cp -R conf/* out/conf

./scripts/make-www.sh

