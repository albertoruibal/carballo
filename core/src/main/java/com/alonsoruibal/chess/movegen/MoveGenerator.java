package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;

public interface MoveGenerator {

	public int generateMoves(Board board, int moves[], int index);

}