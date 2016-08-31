package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

public abstract class BaseTest {

	public int getEval(String fen) {
		AttacksInfo attacksInfo = new AttacksInfo();
		ExperimentalEvaluator evaluator = new ExperimentalEvaluator();
		evaluator.debug = true;
		Board board = new Board();
		board.setFen(fen);
		return evaluator.evaluate(board, attacksInfo);
	}

	public long getSearchScore(String fen, int depth) {
		SearchEngine search = new SearchEngine(new Config());
		SearchParameters searchParams;
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(depth);
		search.go(searchParams);
		return search.getBestMoveScore();
	}

	public String getSearchBestMoveSan(String fen, int depth) {
		SearchEngine search = new SearchEngine(new Config());
		SearchParameters searchParams;
		search.getBoard().setFen(fen);
		searchParams = new SearchParameters();
		searchParams.setDepth(depth);
		search.go(searchParams);
		return Move.toSan(search.getBoard(), search.getBestMove());
	}
}
