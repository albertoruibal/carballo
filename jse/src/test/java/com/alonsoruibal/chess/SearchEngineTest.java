package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.tt.TranspositionTable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SearchEngineTest extends SearchTest {

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
		assertTrue("none".equals(getSearchBestMoveSan("r7/4K1q1/r7/1p5p/4k3/8/8/8 w - - 8 75", 1)));
	}

	@Test
	public void testBishopTrapped() {
		String san = getSearchBestMoveSan("4k3/5ppp/8/8/8/8/2B5/K7 w - - 8 75", 10);
		assertNotEquals("Bxh7", san);
	}

	@Test
	public void testOpeningWithE4OrD4() {
		String san = getSearchBestMoveSan(Board.FEN_START_POSITION, 14);
		assertTrue("e4".equals(san) || "d4".equals(san));
	}

	@Test
	public void testAnalysisMateCrash() {
		getSearchScore("8/8/8/8/8/k7/8/K6r w - - 1 1", 15);
	}

	@Test
	public void testRetiEndgameStudy() {
		assertTrue(getSearchScore("7K/8/k1P5/7p/8/8/8/8 w - -", 15) == Evaluator.DRAW);
	}
}