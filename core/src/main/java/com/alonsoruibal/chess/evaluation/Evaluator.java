package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.util.StringUtils;

public abstract class Evaluator {
	public final static int W = 0;
	public final static int B = 1;

	public final static int NO_VALUE = Short.MAX_VALUE;
	public final static int MATE = 30000;
	public final static int KNOWN_WIN = 20000;
	public final static int DRAW = 0;

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