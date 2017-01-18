package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;
import com.alonsoruibal.chess.tt.TranspositionTable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SearchEngineTest extends BaseTest {

	@Test
	public void testRefine() {
		SearchEngine search = new SearchEngine(new Config());
		search.getBoard().setFen("3r1rk1/p3qp1p/2bb2p1/2p5/3P4/1P6/PBQN1PPP/2R2RK1 b - -");
		search.nodes[0].staticEval = -11;
		int refine;
		boolean foundTT;

		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_FAIL_LOW,
				0, 0,
				Move.NONE, 23, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertEquals("Must find it in the TT", true, foundTT);
		assertEquals("Must be fail low in the TT", TranspositionTable.TYPE_FAIL_LOW, search.getTT().getNodeType());
		assertEquals("Must not refine", search.nodes[0].staticEval, refine);

		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_FAIL_HIGH,
				0, 0,
				Move.NONE, 45, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertEquals("Must find it in the TT", true, foundTT);
		assertEquals("Must be fail high in the TT", TranspositionTable.TYPE_FAIL_HIGH, search.getTT().getNodeType());
		assertEquals("Must refine", 45, refine);

		search.nodes[0].staticEval = 40;
		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_FAIL_LOW,
				0, 0,
				Move.NONE, 23, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertEquals("Must be fail low in the TT", TranspositionTable.TYPE_FAIL_LOW, search.getTT().getNodeType());
		assertEquals("Must refine", 23, refine);

		search.nodes[0].staticEval = 40;
		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_FAIL_HIGH,
				0, 0,
				Move.NONE, 45, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertEquals("Must be fail high in the TT", TranspositionTable.TYPE_FAIL_HIGH, search.getTT().getNodeType());
		assertEquals("Must refine", 45, refine);

		search.nodes[0].staticEval = 40;
		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_EXACT_SCORE,
				0, 0,
				Move.NONE, 43, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
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

	@Test
	public void testSameResultsAfterClear() {
		String fen = "rnq1nrk1/pp3pbp/6p1/3p4/3P4/5N2/PP2BPPP/R1BQK2R w KQ - 0 1";

		SearchEngine search = new SearchEngine(new Config());
		search.debug = true;
		search.getBoard().setFen(fen);

		SearchParameters searchParams = new SearchParameters();
		searchParams.setDepth(10);
		search.go(searchParams);
		long nodes1 = search.getNodeCount();

		search.clear();

		search.go(searchParams);
		long nodes2 = search.getNodeCount();
		assertEquals(nodes1, nodes2);
	}

	public class DetectBestMoveSearchObserver implements SearchObserver {
		boolean notifiedBestMove = false;

		@Override
		public void info(SearchStatusInfo info) {
		}

		@Override
		public void bestMove(int bestMove, int ponder) {
			notifiedBestMove = true;
		}
	}

	@Test
	public void testDoNotSendBestBoveWithPonder() {
		// A mate in 2 puzzle should end ponder
		String fen = "2bqkbn1/2pppp2/np2N3/r3P1p1/p2N2B1/5Q2/PPPPKPP1/RNB2r2 w KQkq - 0 1";

		SearchEngine search = new SearchEngine(new Config());
		search.debug = true;
		search.getBoard().setFen(fen);

		DetectBestMoveSearchObserver searchObserver = new DetectBestMoveSearchObserver();
		search.setObserver(searchObserver);

		SearchParameters searchParams = new SearchParameters();
		searchParams.setPonder(true);
		search.go(searchParams);

		assertEquals(false, searchObserver.notifiedBestMove);
	}
}