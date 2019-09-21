package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class BaseTest {

	int getEval(String fen) {
		AttacksInfo attacksInfo = new AttacksInfo();
		CompleteEvaluator evaluator = new CompleteEvaluator();
		evaluator.debug = true;
		Board board = new Board();
		board.setFen(fen);
		return evaluator.evaluate(board, attacksInfo);
	}

	/**
	 * Compares the eval of two fens
	 */
	void compareEval(String fenBetter, String fenWorse, int requiredDifference) {
		System.out.println("*\n* Comparing two board evaluations (first must be better for white):\n*");
		int valueBetter = getEval(fenBetter);
		int valueWorse = getEval(fenWorse);
		System.out.println("valueBetter = " + valueBetter);
		System.out.println("valueWorse = " + valueWorse);
		assertTrue(valueBetter > valueWorse + requiredDifference);
	}

	public int getSearchQS(String fen) {
		SearchEngine search = new SearchEngine(new Config());
		search.getBoard().setFen(fen);
		try {
			return search.quiescentSearch(0, -Evaluator.MATE, Evaluator.MATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	long getSearchScore(String fen, int depth) {
		SearchEngine search = new SearchEngine(new Config());
		search.debug = true;
		SearchParameters searchParams;
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(depth);
		search.go(searchParams);
		return search.getBestMoveScore();
	}

	String getSearchBestMoveSan(String fen, int depth) {
		SearchEngine search = new SearchEngine(new Config());
		search.debug = true;
		SearchParameters searchParams;
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(depth);
		search.go(searchParams);
		return Move.toSan(search.getBoard(), search.getBestMove());
	}
}
