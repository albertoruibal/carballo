package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.util.StringUtils;

public abstract class Evaluator {
	public static final int W = 0;
	public static final int B = 1;

	public static final int NO_VALUE = Short.MAX_VALUE;
	public static final int MATE = 30000;
	public static final int KNOWN_WIN = 20000;
	public static final int DRAW = 0;

	public static final int PAWN = 100;
	public static final int KNIGHT = 325;
	public static final int BISHOP = 325;
	public static final int BISHOP_PAIR = 50; // Bonus by having two bishops in different colors
	public static final int ROOK = 500;
	public static final int QUEEN = 975;
	public static final int NON_PAWN_MATERIAL_ENDGAME_MIN = QUEEN + ROOK;
	public static final int NON_PAWN_MATERIAL_MIDGAME_MAX = 2 * KNIGHT + 2 * BISHOP + 4 * ROOK + 2 * QUEEN;

	public BitboardAttacks bbAttacks;

	public Evaluator() {
		bbAttacks = BitboardAttacks.getInstance();
	}

	/**
	 * Board evaluator
	 */
	public abstract int evaluate(Board board, AttacksInfo attacksInfo);

	/**
	 * Merges two short Opening - Ending values in one int
	 */
	public static int oe(int opening, int endgame) {
		return (opening << 16) | (endgame & 0xffff);
	}

	/**
	 * Multiply with negative numbers (in the factor or in one of the oe components) cannot be done directly
	 */
	public static int oeMul(int factor, int oeValue) {
		return (((oeValue >> 16) * factor) << 16) | ((oeValue & 0xffff) * factor) & 0xffff;
	}

	/**
	 * shift right each part by factor positions
	 */
	public static int oeShr(int factor, int oeValue) {
		return (((oeValue >> (16 + factor))) << 16) | ((oeValue & 0xffff) >> factor) & 0xffff;
	}

	public static int o(int oe) {
		return oe >> 16;
	}

	public static int e(int oe) {
		return (short) (oe & 0xffff);
	}

	String formatOE(int value) {
		return StringUtils.padLeft(String.valueOf(o(value)), 8) + " " + StringUtils.padLeft(String.valueOf(e(value)), 8);
	}
}