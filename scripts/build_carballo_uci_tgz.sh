#!/bin/bash
SCRIPT=$(realpath $0)
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH/../jse/"

gradle clean proguard

VERSION=$(cat ../build.gradle | grep version | cut -d\' -f 2)

tar cvfz ../carballo-uci-$VERSION.tgz carballo-$VERSION.jar carballo.bat  carballo.sh ../carballo.png ../readme.md ../license.txt
