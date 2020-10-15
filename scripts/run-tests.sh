#!/bin/bash

DIR=$(dirname $0)
cd $DIR/..

./gradlew clean test

