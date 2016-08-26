package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EndgameSearchTest {

	SearchEngine search;
	SearchParameters searchParams;

	private long getSearchScore(String fen, int depth) {
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(depth);
		search.go(searchParams);
		return search.getBestMoveScore();
	}

	@Before
	public void setUp() throws Exception {
		search = new SearchEngine(new Config());
	}

	@Test
	public void testKPk() {
		assertEquals("Black moves and draws", getSearchScore("5k2/8/2K1P3/8/8/8/8/8 b - - 0 0", 10), -SearchEngine.CONTEMPT_FACTOR);
	}

	@Test
	public void testKPpk() {
		// Whites always win
		String fen = "8/4k3/4p3/4P3/1K6/8/8/8 b - - 0 0";
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setNodes(30000);
		search.go(searchParams);
		assertTrue("Whites capture the pawn in 6th and win", search.getBestMoveScore() < -Evaluator.QUEEN);
	}

	@Test
	public void testKRk() {
		assertEquals("Rook mate in 15 PLY", getSearchScore("8/7K/8/8/8/8/R7/7k w - - 0 1", 21), Evaluator.MATE - 15);
	}

	@Test
	public void testKQk() {
		assertEquals("Queen mate in 17 PLY", getSearchScore("8/8/8/4k3/8/8/8/KQ6 w - - 0 0", 20), Evaluator.MATE - 17);
	}

	@Test
	public void testKRPKP() {
		assertTrue("White wins", getSearchScore("2r5/8/5k2/8/2P5/2K5/8/4R3 w - - 0 1", 15) > 50);
		assertTrue("Back Rank defence", getSearchScore("8/8/8/8/6r1/1pk5/8/1K2R3 w - - 0 1", 15) == Evaluator.DRAW);
		assertTrue("Draw with a knight pawn", getSearchScore("1r6/8/4k3/8/1P6/1K6/8/3R4 w - - 0 1", 15) == Evaluator.DRAW);
	}
}