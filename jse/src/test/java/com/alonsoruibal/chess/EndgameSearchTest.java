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

	@Before
	public void setUp() throws Exception {
		search = new SearchEngine(new Config());
	}

	@Test
	public void testKPk() {
		String fen = "5k2/8/2K1P3/8/8/8/8/8 b - - 0 0";
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(10);
		search.go(searchParams);
		assertEquals("Black moves and draws", search.getBestMoveScore(), -SearchEngine.CONTEMPT_FACTOR);
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
		String fen = "8/7K/8/8/8/8/R7/7k w - - 0 1";
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(21);
		search.go(searchParams);
		assertEquals("Rook mate in 15 PLY", search.getBestMoveScore(), Evaluator.MATE - 15);
	}

	@Test
	public void testKQk() {
		String fen = "8/8/8/4k3/8/8/8/KQ6 w - - 0 0";
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(20);
		search.go(searchParams);
		assertEquals("Queen mate in 17 PLY", search.getBestMoveScore(), Evaluator.MATE - 17);
	}
}