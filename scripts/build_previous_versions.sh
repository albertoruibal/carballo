#!/bin/bash
#
# Build previous Carballo Uci versions
#
SCRIPT=$(realpath $0)
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH/../jse/"

git checkout v1.3
gradle clean proguard

git checkout v1.2
gradle clean proguard

git checkout v1.1
gradle clean proguard

git checkout v0.9
gradle clean proguard

# Back to the original git status
git checkout master