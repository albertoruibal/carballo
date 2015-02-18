package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.tt.TranspositionTable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
