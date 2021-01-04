#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

git submodule init # make sure we have all the code

./gradlew makeJar copyDependencies || exit 1

cp -R build/libs/* out/bin
cp -R conf/* out/conf

./scripts/make-www.sh

