#!/bin/bash
#
# Test tournaments against old versions of the engine using cutechess-cli
# Starting positions from the Noomen Test Suite 2012
#
# Requirements:
#  - cutechess-cli from https://github.com/cutechess/cutechess
#  - binfmt-support (debian) to allow executing jars
#  - The previous versions jars in ../jse built with the script build_previous_versions.sh
#
SCRIPT=$(realpath $0)
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH/../jse/"

CUTECHESS=/usr/local/cutechess-cli/cutechess-cli.sh

chmod +x *.jar

$CUTECHESS -engine name=carballo-1.2 cmd=./carballo-1.2.jar -engine name=carballo-1.1.1 cmd=./carballo-1.1.1.jar -each proto=uci tc=5 -openings file=src/test/resources/NoomenTestsuite2012.pgn -repeat -games 60 -rounds 20
$CUTECHESS -engine name=carballo-1.2 cmd=./carballo-1.2.jar -engine name=carballo-1.1 cmd=./carballo-1.1.jar -each proto=uci tc=5 -openings file=src/test/resources/NoomenTestsuite2012.pgn -repeat -games 60 -rounds 20
$CUTECHESS -engine name=carballo-1.2 cmd=./carballo-1.2.jar -engine name=carballo-0.9 cmd=./carballo-0.9.jar -each proto=uci tc=5 -openings file=src/test/resources/NoomenTestsuite2012.pgn -repeat -games 60 -rounds 20
