package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;

public class LegalMoveGenerator extends MagicMoveGenerator {

	/**
	 * Get only LEGAL moves testing with doMove
	 * The moves are returned with the check flag set
	 */
	@Override
	public int generateMoves(Board board, int moves[], int index) {
		int lastIndex = super.generateMoves(board, moves, index);
		int j = index;
		for (int i = 0; i < lastIndex; i++) {
			if (board.doMove(moves[i], true, false)) {
				moves[j++] = board.getCheck() ? moves[i] | Move.CHECK_MASK : moves[i];
				board.undoMove();
			}
		}
		return j;
	}
}