#!/bin/bash
#
# Test tournaments against previous versions of the engine using cutechess-cli
# Starting positions from the Noomen Test Suite 2012
#
# Requirements:
#  - cutechess-cli from https://github.com/cutechess/cutechess
#  - binfmt-support and jarwrapper (debian) to allow executing jars directly
#  - The previous versions jars in ../jse are built with the script build_previous_versions.sh
#
SCRIPT=$(realpath $0)
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH/../jse/"

CUTECHESS=/usr/local/cutechess/cutechess-cli.sh
COMMON="-each proto=uci option.Hash=256 tc=5+0.1 -concurrency 2 -ratinginterval 100 -draw movenumber=100 movecount=50 score=20 -resign movecount=5 score=900 -openings file=src/test/resources/NoomenTestsuite2012.pgn -repeat -games 60 -rounds 20"

chmod +x *.jar

$CUTECHESS -engine name=carballo-1.8 cmd=./carballo-1.8.jar -engine name=carballo-1.7 cmd=./carballo-1.7.jar $COMMON
