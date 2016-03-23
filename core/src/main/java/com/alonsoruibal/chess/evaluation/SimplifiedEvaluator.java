package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.AttacksInfo;

/**
 * Piece square values from Tomasz Michniewski, got from:
 * http://chessprogramming.wikispaces.com/Simplified+evaluation+function
 *
 * @author rui
 */
public class SimplifiedEvaluator extends Evaluator {

	static final int PAWN = 100;
	static final int KNIGHT = 320;
	static final int BISHOP = 330;
	static final int ROOK = 500;
	static final int QUEEN = 900;

	// Values are rotated for whites, so when white is playing is like shown in the code TODO at the moment must be symmetric
	public static final int[] pawnSquare = { //
			0, 0, 0, 0, 0, 0, 0, 0, //
			50, 50, 50, 50, 50, 50, 50, 50, //
			10, 10, 20, 30, 30, 20, 10, 10, //
			5, 5, 10, 25, 25, 10, 5, 5, //
			0, 0, 0, 20, 20, 0, 0, 0, //
			5, -5, -10, 0, 0, -10, -5, 5, //
			5, 10, 10, -20, -20, 10, 10, 5, //
			0, 0, 0, 0, 0, 0, 0, 0 //
	};

	public static final int[] knightSquare = { //
			-50, -40, -30, -30, -30, -30, -40, -50, //
			-40, -20, 0, 0, 0, 0, -20, -40, //
			-30, 0, 10, 15, 15, 10, 0, -30, //
			-30, 5, 15, 20, 20, 15, 5, -30, //
			-30, 0, 15, 20, 20, 15, 0, -30, //
			-30, 5, 10, 15, 15, 10, 5, -30, //
			-40, -20, 0, 5, 5, 0, -20, -40, //
			-50, -40, -30, -30, -30, -30, -40, -50, //
	};

	public static final int[] bishopSquare = { //
			-20, -10, -10, -10, -10, -10, -10, -20, //
			-10, 0, 0, 0, 0, 0, 0, -10, //
			-10, 0, 5, 10, 10, 5, 0, -10, //
			-10, 5, 5, 10, 10, 5, 5, -10, //
			-10, 0, 10, 10, 10, 10, 0, -10, //
			-10, 10, 10, 10, 10, 10, 10, -10, //
			-10, 5, 0, 0, 0, 0, 5, -10, //
			-20, -10, -10, -10, -10, -10, -10, -20, //
	};

	public static final int[] rookSquare = { //
			0, 0, 0, 0, 0, 0, 0, 0, //
			5, 10, 10, 10, 10, 10, 10, 5, //
			-5, 0, 0, 0, 0, 0, 0, -5, //
			-5, 0, 0, 0, 0, 0, 0, -5, //
			-5, 0, 0, 0, 0, 0, 0, -5, //
			-5, 0, 0, 0, 0, 0, 0, -5, //
			-5, 0, 0, 0, 0, 0, 0, -5, //
			0, 0, 0, 5, 5, 0, 0, 0 //
	};

	public static final int[] queenSquare = { //
			-20, -10, -10, -5, -5, -10, -10, -20, //
			-10, 0, 0, 0, 0, 0, 0, -10, //
			-10, 0, 5, 5, 5, 5, 0, -10, //
			-5, 0, 5, 5, 5, 5, 0, -5, //
			0, 0, 5, 5, 5, 5, 0, -5, //
			-10, 5, 5, 5, 5, 5, 0, -10, //
			-10, 0, 5, 0, 0, 0, 0, -10, //
			-20, -10, -10, -5, -5, -10, -10, -20 //
	};

	public static final int[] kingSquareOpening = { //
			-30, -40, -40, -50, -50, -40, -40, -30, //
			-30, -40, -40, -50, -50, -40, -40, -30, //
			-30, -40, -40, -50, -50, -40, -40, -30, //
			-30, -40, -40, -50, -50, -40, -40, -30, //
			-20, -30, -30, -40, -40, -30, -30, -20, //
			-10, -20, -20, -20, -20, -20, -20, -10, //
			20, 20, 0, 0, 0, 0, 20, 20, //
			20, 30, 10, 0, 0, 10, 30, 20 //
	};

	public static final int[] kingSquareEndGame = { //
			-50, -40, -30, -20, -20, -30, -40, -50, //
			-30, -20, -10, 0, 0, -10, -20, -30, //
			-30, -10, 20, 30, 30, 20, -10, -30, //
			-30, -10, 30, 40, 40, 30, -10, -30, //
			-30, -10, 30, 40, 40, 30, -10, -30, //
			-30, -10, 20, 30, 30, 20, -10, -30, //
			-30, -30, 0, 0, 0, 0, -30, -30, //
			-50, -30, -30, -30, -30, -30, -30, -50 //
	};


	public int evaluate(Board board, AttacksInfo attacksInfo) {
		long all = board.getAll();

		int materialValue[] = {0, 0};
		int pawnMaterialValue[] = {0, 0};
		int pcsqValue[] = {0, 0};
		int pcsqOpeningValue[] = {0, 0};
		int pcsqEndgameValue[] = {0, 0};

		boolean noQueen[] = {true, true};

		long square = 1;
		byte index = 0;
		while (square != 0) {
			boolean isWhite = ((board.whites & square) != 0);
			int color = (isWhite ? 0 : 1);
			int pcsqIndex = (isWhite ? 63 - index : index);

			if ((square & all) != 0) {

				if ((square & board.pawns) != 0) {
					pawnMaterialValue[color] += PAWN;
					pcsqValue[color] += pawnSquare[pcsqIndex];

				} else if ((square & board.knights) != 0) {
					materialValue[color] += KNIGHT;
					pcsqValue[color] += knightSquare[pcsqIndex];

				} else if ((square & board.bishops) != 0) {
					materialValue[color] += BISHOP;
					pcsqValue[color] += bishopSquare[pcsqIndex];

				} else if ((square & board.rooks) != 0) {
					materialValue[color] += ROOK;
					pcsqValue[color] += rookSquare[pcsqIndex];

				} else if ((square & board.queens) != 0) {
					pcsqValue[color] += queenSquare[pcsqIndex];
					materialValue[color] += QUEEN;
					noQueen[color] = false;

				} else if ((square & board.kings) != 0) {
					pcsqOpeningValue[color] += kingSquareOpening[pcsqIndex];
					pcsqEndgameValue[color] += kingSquareEndGame[pcsqIndex];
				}
			}
			square <<= 1;
			index++;
		}

		int value = 0;
		value += pawnMaterialValue[0] - pawnMaterialValue[1];
		value += materialValue[0] - materialValue[1];
		value += pcsqValue[0] - pcsqValue[1];

		// Endgame
		// 1. Both sides have no queens or
		// 2. Every side which has a queen has additionally no other pieces or one minorpiece maximum.
		if ((noQueen[0] || materialValue[0] <= QUEEN + BISHOP) && (noQueen[1] || materialValue[1] <= QUEEN + BISHOP)) {
			value += pcsqEndgameValue[0] - pcsqEndgameValue[1];
		} else {
			value += pcsqOpeningValue[0] - pcsqOpeningValue[1];
		}

		return value;
	}
}