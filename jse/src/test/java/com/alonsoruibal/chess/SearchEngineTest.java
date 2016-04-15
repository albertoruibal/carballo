package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.tt.TranspositionTable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SearchEngineTest {

	@Test
	public void testRefine() {
		SearchEngine search = new SearchEngine(new Config());
		search.getBoard().setFen("3r1rk1/p3qp1p/2bb2p1/2p5/3P4/1P6/PBQN1PPP/2R2RK1 b - -");
		int eval = -11;
		int refine;
		boolean foundTT;

		search.getTT().save(search.getBoard(), 0, 0, 0, 23, 23, 45, eval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(foundTT, eval);
		assertEquals("Must find it in the TT", true, foundTT);
		assertEquals("Must be fail low in the TT", TranspositionTable.TYPE_FAIL_LOW, search.getTT().getNodeType());
		assertEquals("Must not refine", eval, refine);

		search.getTT().save(search.getBoard(), 0, 0, 0, 45, 23, 45, eval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(foundTT, eval);
		assertEquals("Must find it in the TT", true, foundTT);
		assertEquals("Must be fail high in the TT", TranspositionTable.TYPE_FAIL_HIGH, search.getTT().getNodeType());
		assertEquals("Must refine", 45, refine);

		eval = 40;
		search.getTT().save(search.getBoard(), 0, 0, 0, 23, 23, 45, eval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(foundTT, eval);
		assertEquals("Must be fail low in the TT", TranspositionTable.TYPE_FAIL_LOW, search.getTT().getNodeType());
		assertEquals("Must refine", 23, refine);

		eval = 40;
		search.getTT().save(search.getBoard(), 0, 0, 0, 45, 23, 45, eval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(foundTT, eval);
		assertEquals("Must be fail high in the TT", TranspositionTable.TYPE_FAIL_HIGH, search.getTT().getNodeType());
		assertEquals("Must refine", 45, refine);

		eval = 40;
		search.getTT().save(search.getBoard(), 0, 0, 0, 43, 23, 45, eval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(foundTT, eval);
		assertEquals("Must be exact score", TranspositionTable.TYPE_EXACT_SCORE, search.getTT().getNodeType());
		assertEquals("Must refine", 43, refine);
	}

	@Test
	public void testSearchAlreadyMate() {
		SearchEngine search = new SearchEngine(new Config());
		search.getBoard().setFen("r7/4K1q1/r7/1p5p/4k3/8/8/8 w - - 8 75");
		search.go(SearchParameters.get(1));
		assertEquals(Move.NONE, search.getBestMove());
	}

	@Test
	public void testBishopTrapped() {
		SearchEngine search = new SearchEngine(new Config());
		search.getBoard().setFen("4k3/5ppp/8/8/8/8/2B5/K7 w - - 8 75");
		SearchParameters sp = new SearchParameters();
		sp.setDepth(10);
		search.go(sp);
		assertNotEquals(Move.getFromString(search.getBoard(), "Bxh7", true), search.getBestMove());
	}

	@Test
	public void testE2E4() {
		SearchEngine search = new SearchEngine(new Config());
		search.getBoard().setFen(Board.FEN_START_POSITION);
		SearchParameters sp = new SearchParameters();
		sp.setDepth(14);
		search.go(sp);
		assertEquals(Move.getFromString(search.getBoard(), "e2e4", true), search.getBestMove());
	}

	@Test
	public void testAnalysisMateCrash() {
		SearchEngine search = new SearchEngine(new Config());
		SearchParameters analysisParameters = new SearchParameters();
		analysisParameters.setInfinite(true);

		search.getBoard().setFen("8/8/8/8/8/k7/8/K6r w - - 1 1");
		search.go(analysisParameters);
	}

	@Test
	public void testRetiEndgameStudy() {
		SearchEngine search = new SearchEngine(new Config());
		SearchParameters sp = new SearchParameters();
		sp.setDepth(11);
		search.getBoard().setFen("7K/8/k1P5/7p/8/8/8/8 w - -");
		search.go(sp);
		assertEquals(-SearchEngine.CONTEMPT_FACTOR, search.getBestMoveScore());
	}
}
