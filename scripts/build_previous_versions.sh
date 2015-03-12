#!/bin/bash
#
# Build previous Carballo Uci versions
#
SCRIPT=$(realpath $0)
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH/../jse/"

git checkout v1.1.1
gradle clean proguard
mv carballo-1.1.jar carballo-1.1.1.jar

git checkout v1.1
gradle clean proguard

git checkout v0.9
gradle clean proguard

# Back to original git status
git checkout HEAD