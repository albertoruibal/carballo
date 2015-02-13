package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.tt.MultiprobeTranspositionTable;
import com.alonsoruibal.chess.tt.TranspositionTable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TranspositionTableTest {

	@Test
	public void testTraspositionTable() {
		Board b = new Board();
		b.startPosition();
		TranspositionTable tt = new MultiprobeTranspositionTable(20);

		int nodeType = TranspositionTable.TYPE_EXACT_SCORE;
		int bestMove = Move.getFromString(b, "e2e4", true);
		int score = -100;
		byte depthAnalyzed = 10;
		tt.set(b, nodeType, bestMove, score, depthAnalyzed, false);
		tt.search(b, 0, false);
		assertEquals(nodeType, tt.getNodeType());
		assertEquals(bestMove, tt.getBestMove());
		assertEquals(score, tt.getScore());
		assertEquals(depthAnalyzed, tt.getDepthAnalyzed());
	}

	@Test
	public void testDistanceToInitialPly() {
		TranspositionTable tt = new MultiprobeTranspositionTable(20);
		Board b = new Board();
		b.setFen("8/7K/8/8/8/8/R7/7k w - - 0 1");

		int bestMove = Move.getFromString(b, "a2f2", true);
		int score = Evaluator.VICTORY - 8;
		int depthAnalyzed = 4;
		// Must store SearchEngine.VALUE_IS_MATE - 4
		tt.save(b, 4, depthAnalyzed, bestMove, score, -Evaluator.VICTORY, +Evaluator.VICTORY, false);
		tt.search(b, 1, false);
		assertEquals("It does not fix the mate score in the transposition table", tt.getScore(), Evaluator.VICTORY - 5);
	}
}