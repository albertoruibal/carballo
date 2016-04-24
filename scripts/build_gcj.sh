#!/bin/bash
#
# Build a GCJ-compiled native binary in Linux
#
# Requirements in Debian:
#
# apt-get install gcj-native-helper
#
SCRIPT=$(realpath $0)
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH/../jse/"

gradle clean proguard

VERSION=$(cat ../build.gradle | grep version | cut -d\' -f 2)

gcj -O3 carballo-$VERSION.jar --main=com.alonsoruibal.chess.uci.Uci -o carballo-$VERSION