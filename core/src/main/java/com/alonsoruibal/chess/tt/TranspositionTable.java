package com.alonsoruibal.chess.tt;

import com.alonsoruibal.chess.Board;

public abstract class TranspositionTable {

	public final static int TYPE_EXACT_SCORE = 1;
	public final static int TYPE_FAIL_LOW = 2;
	public final static int TYPE_FAIL_HIGH = 3;
	public final static int TYPE_EVAL = 4;
	
	/**
	 * Returns true if key matches with key stored
	 */
	public abstract boolean search(Board board, boolean exclusion);

	public abstract int getBestMove();

	public abstract int getNodeType();

	public abstract byte getGeneration();

	public abstract boolean isMyGeneration();
	
	public abstract byte getDepthAnalyzed();

	public abstract int getScore();

	public void save(Board board, byte depthAnalyzed, int bestMove, int score, int lowerBound, int upperBound, boolean exclusion) {
		if (score <= lowerBound) {
			set(board, TranspositionTable.TYPE_FAIL_LOW, bestMove, score, depthAnalyzed, exclusion);
		} else if (score >= upperBound) {
			set(board, TranspositionTable.TYPE_FAIL_HIGH, bestMove, score, depthAnalyzed, exclusion);
		} else { 	
			set(board, TranspositionTable.TYPE_EXACT_SCORE, bestMove, score, depthAnalyzed, exclusion);
		}
	}

	public abstract void set(Board board, int nodeType, int bestMove,
			int score, byte depthAnalyzed, boolean exclusion);

	// called at the start of each search
	public abstract void newGeneration();

	public abstract void clear();

}