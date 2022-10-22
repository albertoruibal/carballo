#!/bin/bash

CUTECHESS=/usr/local/cutechess/cutechess-cli.sh

COMMON="-each proto=uci option.Hash=256 tc=5+0.1 -concurrency 5 -ratinginterval 10 -draw movenumber=100 movecount=50 score=20 -resign movecount=5 score=900 -openings file=NoomenTestsuite2012.pgn -repeat -games 1000 -rounds 64 -pgnout /tmp/games_64000.pgn"

$CUTECHESS \
	-engine name=carballo-1.8 cmd=/usr/local/chessengines/carballo-1.8.jar \
	-engine name=carballo-1.7 cmd=/usr/local/chessengines/carballo-1.7.jar \
	$COMMON