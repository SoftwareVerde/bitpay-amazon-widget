#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

mkdir -p out/www/
rm -rf out/www/*
cd bitpay-web/
yarn build && \
    cd - && \
    cp -R bitpay-web/dist/* out/www/

