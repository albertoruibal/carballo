package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EndgameTest {

	SearchEngine search;
	SearchParameters searchParams;

	@Before
	public void setUp() throws Exception {
		search = new SearchEngine(new Config());
	}

	@Test
	public void testKPpk() {
		// Whites always win
		String fen = "8/4k3/4p3/4P3/1K6/8/8/8 b - - 0 0";
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setNodes(30000);
		search.go(searchParams);
		assertTrue("Whites capture the pawn in 6th and win", search.getBestMoveScore() < -ExperimentalEvaluator.QUEEN);
	}

	@Test
	public void testKQk() {
		String fen = "8/8/8/4k3/8/8/8/KQ6 w - - 0 0";
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setNodes(5000000);
		search.go(searchParams);
		assertTrue("Queen mate", search.getBestMoveScore() > ExperimentalEvaluator.KNOWN_WIN);
	}

//	@Test
//	public void testKPk() {
//		String fen = "5k2/8/2K1P3/8/8/8/8/8 b - - 0 0";
//		search.getBoard().setFen(fen);
//		searchParams = new SearchParameters();
//		searchParams.setNodes(500);
//		search.go(searchParams);
//		assertTrue("Black moves and draws", search.getBestMoveScore() > -Config.DEFAULT_CONTEMPT_FACTOR);
//	}
}