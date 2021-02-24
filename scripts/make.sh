#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

if [[ -z $(which npm) ]]; then
    echo 'Please install npm.' 1>&2
    exit 1
fi

# ensures upstream bitpay browser extension project is pulled in
# it isn't necessary to build, but is good to have to keep things roughly in sync
git submodule init

export PATH=${PATH}:$(npm bin)

if [[ -z $(which yarn) ]]; then
    npm install yarn
fi

cd bitpay-web
yarn install
cd -

./gradlew --warning-mode=all clean test makeJar copyDependencies || exit 1
./scripts/package.sh

