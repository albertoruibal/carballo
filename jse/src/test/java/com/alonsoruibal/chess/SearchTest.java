package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

public abstract class SearchTest {

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
