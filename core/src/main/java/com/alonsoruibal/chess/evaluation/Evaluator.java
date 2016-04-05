package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Color;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.util.StringUtils;

public abstract class Evaluator {
	public static final int W = Color.W;
	public static final int B = Color.B;

	public static final int NO_VALUE = Short.MAX_VALUE;
	public static final int MATE = 30000;
	public static final int KNOWN_WIN = 20000;
	public static final int DRAW = 0;

	public static final int PAWN = 100;
	public static final int KNIGHT = 325;
	public static final int BISHOP = 325;
	public static final int ROOK = 500;
	public static final int QUEEN = 975;
	public static final int[] PIECE_VALUES = {0, PAWN, KNIGHT, BISHOP, ROOK, QUEEN};
	public static final int BISHOP_PAIR = Evaluator.oe(50, 50); // Bonus by having two bishops in different colors
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
		return ((opening < 0 ? opening - 1 : opening) << 16) | (endgame & 0xffff);
	}

	/**
	 * Get the "Opening" part
	 */
	public static int o(int oe) {
		int i = oe >> 16;
		return i < 0 ? i + 1 : i;
	}

	/**
	 * Get the "Endgame" part
	 */
	public static int e(int oe) {
		return (short) (oe & 0xffff);
	}

	/**
	 * Shift right each part by factor positions
	 */
	public static int oeShr(int factor, int oeValue) {
		return oe(o(oeValue) >> factor, e(oeValue) >> factor);
	}

	String formatOE(int value) {
		return StringUtils.padLeft(String.valueOf(o(value)), 8) + " " + StringUtils.padLeft(String.valueOf(e(value)), 8);
	}
}