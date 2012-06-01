package com.alonsoruibal.chess.search;

public interface SearchObserver {

	public void info(SearchStatusInfo info);
	
	public void bestMove(int bestMove, int ponder);
	
}