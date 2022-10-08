package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.search.*;
import com.alonsoruibal.chess.tt.TranspositionTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest extends BaseTest {

	@Test
	void testRefine() {
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
		assertTrue(foundTT, "Must find it in the TT");
		assertEquals(TranspositionTable.TYPE_FAIL_LOW, search.getTT().getNodeType(), "Must be fail low in the TT");
		assertEquals(search.nodes[0].staticEval, refine, "Must not refine");

		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_FAIL_HIGH,
				0, 0,
				Move.NONE, 45, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertTrue(foundTT, "Must find it in the TT");
		assertEquals(TranspositionTable.TYPE_FAIL_HIGH, search.getTT().getNodeType(), "Must be fail high in the TT");
		assertEquals(45, refine, "Must refine");

		search.nodes[0].staticEval = 40;
		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_FAIL_LOW,
				0, 0,
				Move.NONE, 23, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertEquals(TranspositionTable.TYPE_FAIL_LOW, search.getTT().getNodeType(), "Must be fail low in the TT");
		assertEquals(23, refine, "Must refine");

		search.nodes[0].staticEval = 40;
		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_FAIL_HIGH,
				0, 0,
				Move.NONE, 45, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertEquals(TranspositionTable.TYPE_FAIL_HIGH, search.getTT().getNodeType(), "Must be fail high in the TT");
		assertEquals(45, refine, "Must refine");

		search.nodes[0].staticEval = 40;
		search.getTT().set(search.getBoard(), TranspositionTable.TYPE_EXACT_SCORE,
				0, 0,
				Move.NONE, 43, search.nodes[0].staticEval, false);
		foundTT = search.getTT().search(search.getBoard(), 0, false);
		refine = search.refineEval(search.nodes[0], foundTT);
		assertEquals(TranspositionTable.TYPE_EXACT_SCORE, search.getTT().getNodeType(), "Must be exact score");
		assertEquals(43, refine, "Must refine");
	}

	@Test
	void testSearchAlreadyMate() {
		assertEquals("none", getSearchBestMoveSan("r7/4K1q1/r7/1p5p/4k3/8/8/8 w - - 8 75", 1));
	}

	@Test
	void testBishopTrapped() {
		String san = getSearchBestMoveSan("4k3/5ppp/8/8/8/8/2B5/K7 w - - 8 75", 10);
		assertNotEquals("Bxh7", san);
	}

	@Test
	void testOpeningWithE4OrD4() {
		String san = getSearchBestMoveSan(Board.FEN_START_POSITION, 14);
		assertTrue("e4".equals(san) || "d4".equals(san));
	}

	@Test
	void testAnalysisMateCrash() {
		getSearchScore("8/8/8/8/8/k7/8/K6r w - - 1 1", 15);
	}

	@Test
	void testRetiEndgameStudy() {
		assertEquals(getSearchScore("7K/8/k1P5/7p/8/8/8/8 w - -", 15), Evaluator.DRAW);
	}

	@Test
	void testSameResultsAfterClear() {
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

	static class DetectBestMoveSearchObserver implements SearchObserver {
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
	void testDoNotSendBestBoveWithPonder() {
		// A mate in 2 puzzle should end ponder
		String fen = "2bqkbn1/2pppp2/np2N3/r3P1p1/p2N2B1/5Q2/PPPPKPP1/RNB2r2 w KQkq - 0 1";

		SearchEngineThreaded search = new SearchEngineThreaded(new Config());
		search.debug = true;
		search.getBoard().setFen(fen);

		DetectBestMoveSearchObserver searchObserver = new DetectBestMoveSearchObserver();
		search.setObserver(searchObserver);

		SearchParameters searchParams = new SearchParameters();
		searchParams.setPonder(true);

		search.go(searchParams);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		assertFalse(searchObserver.notifiedBestMove);

		search.stop();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		assertTrue(searchObserver.notifiedBestMove);
	}
}