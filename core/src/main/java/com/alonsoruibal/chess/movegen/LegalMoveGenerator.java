package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;

public class LegalMoveGenerator extends MagicMoveGenerator {

	/**
	 * Get only LEGAL moves testing with doMove
	 * The moves are returned with the check flag set
	 */
	@Override
	public int generateMoves(Board board, int[] moves, int index) {
		int lastIndex = super.generateMoves(board, moves, index);
		int j = index;
		// Iterate over the freshly generated pseudo-legal moves [index, lastIndex),
		// not from 0: the slots below `index` contain stale data from previous fills
		// and re-running doMove on them would corrupt board state and emit bogus moves.
		for (int i = index; i < lastIndex; i++) {
			if (board.doMove(moves[i], true, false)) {
				moves[j++] = board.getCheck() ? moves[i] | Move.CHECK_MASK : moves[i];
				board.undoMove();
			}
		}
		return j;
	}
}