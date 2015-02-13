package com.alonsoruibal.chess.tt;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.search.SearchEngine;

public abstract class TranspositionTable {

	public final static int TYPE_EXACT_SCORE = 1;
	public final static int TYPE_FAIL_LOW = 2;
	public final static int TYPE_FAIL_HIGH = 3;
	public final static int TYPE_EVAL = 4;

	/**
	 * Returns true if key matches with key stored
	 */
	public abstract boolean search(Board board, int distanceToInitialPly, boolean exclusion);

	public abstract int getBestMove();

	public abstract int getNodeType();

	public abstract byte getGeneration();

	public abstract boolean isMyGeneration();

	public abstract byte getDepthAnalyzed();

	public abstract int getScore();

	public void save(Board board, int distanceToInitialPly, int depthAnalyzed, int bestMove, int score, int lowerBound, int upperBound, boolean exclusion) {
		// Fix mate score with the real distance to mate from the current PLY, not from the initial PLY
		int fixedScore = score;
		if (score >= SearchEngine.VALUE_IS_MATE) {
			fixedScore += distanceToInitialPly;
		} else if (score <= -SearchEngine.VALUE_IS_MATE) {
			fixedScore -= distanceToInitialPly;
		}

		if (fixedScore > Evaluator.VICTORY || fixedScore < -Evaluator.VICTORY) {
			System.out.println("The TT score fixed is outside the limits: " + fixedScore);
		}

		if (score <= lowerBound) {
			set(board, TranspositionTable.TYPE_FAIL_LOW, bestMove, fixedScore, (byte) depthAnalyzed, exclusion);
		} else if (score >= upperBound) {
			set(board, TranspositionTable.TYPE_FAIL_HIGH, bestMove, fixedScore, (byte) depthAnalyzed, exclusion);
		} else {
			set(board, TranspositionTable.TYPE_EXACT_SCORE, bestMove, fixedScore, (byte) depthAnalyzed, exclusion);
		}
	}

	public abstract void set(Board board, int nodeType, int bestMove,
							 int score, byte depthAnalyzed, boolean exclusion);

	// called at the start of each search
	public abstract void newGeneration();

	public abstract void clear();

}