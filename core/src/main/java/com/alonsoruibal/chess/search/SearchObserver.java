package com.alonsoruibal.chess.search;

public interface SearchObserver {

	void info(SearchStatusInfo info);

	void bestMove(int bestMove, int ponder);

}