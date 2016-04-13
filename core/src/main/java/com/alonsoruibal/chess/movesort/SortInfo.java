package com.alonsoruibal.chess.movesort;

import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.search.SearchEngine;

import java.util.Arrays;

public class SortInfo {

	public static final int HISTORY_MAX = Integer.MAX_VALUE - 1;

	// Two killer move slots
	public int[] killerMove1;
	public int[] killerMove2;

	private int[][] history; // By piece type and destiny square

	public SortInfo() {
		killerMove1 = new int[SearchEngine.MAX_DEPTH];
		killerMove2 = new int[SearchEngine.MAX_DEPTH];

		history = new int[6][64];
	}

	public void clear() {
		Arrays.fill(killerMove1, 0);
		Arrays.fill(killerMove2, 0);
		for (int i = 0; i < 6; i++) {
			Arrays.fill(history[i], 0);
		}
	}

	/**
	 * we are informed of the score produced by the move at any level
	 */
	public void betaCutoff(int move, int depth) {
		// removes captures and promotions from killers
		if (move == Move.NONE || Move.isTactical(move)) {
			return;
		}

		if (move != killerMove1[depth]) {
			killerMove2[depth] = killerMove1[depth];
			killerMove1[depth] = move;
		}

		history[Move.getPieceMoved(move) - 1][Move.getToIndex(move)]++;

		// Detect history overflows and divide all values by two
		if (history[Move.getPieceMoved(move) - 1][Move.getToIndex(move)] >= HISTORY_MAX) {
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 64; j++) {
					history[i][j] >>>= 1;
				}
			}
		}
	}

	public int getMoveScore(int move) {
		return history[Move.getPieceMoved(move) - 1][Move.getToIndex(move)];
	}
}