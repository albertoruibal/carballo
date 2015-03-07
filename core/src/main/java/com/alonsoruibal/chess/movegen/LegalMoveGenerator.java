package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;

public class LegalMoveGenerator extends MagicMoveGenerator {

	/**
	 * get only LEGAL Operations by testing with domove
	 */
	@Override
	public int generateMoves(Board board, int moves[], int index) {
		int lastIndex = super.generateMoves(board, moves, index);
		int j = index;
		for (int i = 0; i < lastIndex; i++) {
			if (board.doMove(moves[i], true, false)) {
				moves[j++] = moves[i];
				board.undoMove();
			}
		}
		return j;
	}
}