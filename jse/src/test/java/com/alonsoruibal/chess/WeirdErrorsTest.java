package com.alonsoruibal.chess;

import junit.framework.TestCase;

public class WeirdErrorsTest extends TestCase {

	static int legalMoves[] = new int[256];
	
	public void testGenerateMoves1() {
		Board board = new Board();
		int legalMoveCount;

		// board.setFen("rnbqk1nr/pppp1ppp/8/4p3/1b1P4/7N/PPP1PPPP/RNBQKB1R w QKqk - 2 0");
		// legalMoveCount = board.getLegalMoves(legalMoves);
		board.setFen("r1bqkbnr/pppp1ppp/n7/4p3/3P4/7N/PPP1PPPP/RNBQKB1R w QKqk - 2 0");		
		legalMoveCount = board.getLegalMoves(legalMoves);		
		assertEquals(legalMoveCount, 29);
	}
}