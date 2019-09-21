package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.tt.TranspositionTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranspositionTableTest {

	@Test
	void testTraspositionTable() {
		Board b = new Board();
		b.startPosition();
		TranspositionTable tt = new TranspositionTable(20);

		int nodeType = TranspositionTable.TYPE_EXACT_SCORE;
		int bestMove = Move.getFromString(b, "e2e4", true);
		int score = -100;
		int depthAnalyzed = -1;
		short eval = 456;
		tt.set(b, nodeType,
				0, depthAnalyzed,
				bestMove, score, eval, false);
		tt.search(b, 0, false);
		assertEquals(nodeType, tt.getNodeType());
		assertEquals(bestMove, tt.getBestMove());
		assertEquals(score, tt.getScore());
		assertEquals(depthAnalyzed, tt.getDepthAnalyzed());
		assertEquals(eval, tt.getEval());
	}

	@Test
	void testDistanceToInitialPly() {
		TranspositionTable tt = new TranspositionTable(20);
		Board b = new Board();
		b.setFen("8/7K/8/8/8/8/R7/7k w - - 0 1");

		int bestMove = Move.getFromString(b, "a2f2", true);
		int score = Evaluator.MATE - 8;
		int depthAnalyzed = 4;
		// Must store SearchEngine.VALUE_IS_MATE - 4
		tt.set(b, TranspositionTable.TYPE_EXACT_SCORE,
				4, depthAnalyzed,
				bestMove, score, 0, false);
		tt.search(b, 1, false);
		assertEquals(tt.getScore(), Evaluator.MATE - 5, "It does not fix the mate score in the transposition table");
	}
}