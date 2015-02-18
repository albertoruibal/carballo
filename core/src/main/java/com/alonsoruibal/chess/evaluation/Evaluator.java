package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;

public abstract class Evaluator {
	public final static int NO_VALUE = Short.MAX_VALUE;
	public final static int VICTORY = 30000;
	public final static int KNOWN_WIN = 20000;
	public final static int DRAW = 0;

	public BitboardAttacks bbAttacks;

	public Evaluator() {
		bbAttacks = BitboardAttacks.getInstance();
	}

	/**
	 * Board evaluator
	 */
	public abstract int evaluate(Board board);

	public static int oe(int opening, int endgame) {
		return (((short) (opening)) << 16) + (short) (endgame);
	}

	public static int o(int oe) {
		return oe >> 16;
	}

	public static int e(int oe) {
		return (short) (oe & 0xffff);
	}
}