package com.alonsoruibal.chess;

import junit.framework.TestCase;

import com.alonsoruibal.chess.tt.MultiprobeTranspositionTable;
import com.alonsoruibal.chess.tt.TranspositionTable;

/**
 * @author rui
 */
public class TranspositionTableTest extends TestCase {


	public void testTraspositionTable() {
		Board b = new Board();
		b.startPosition();
		TranspositionTable tt = new MultiprobeTranspositionTable(20);
		
		int nodeType = TranspositionTable.TYPE_EXACT_SCORE;
		int bestMove = Move.getFromString(b, "e2e4", true);
		int score = -100;
		byte depthAnalyzed = 10;
		tt.set(b, nodeType, bestMove, score, depthAnalyzed, false);
		tt.search(b, false);
		assertEquals(nodeType, tt.getNodeType());
		assertEquals(bestMove, tt.getBestMove());
		assertEquals(score, tt.getScore());
		assertEquals(depthAnalyzed, tt.getDepthAnalyzed());
	}

}