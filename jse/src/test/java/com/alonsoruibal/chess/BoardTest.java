package com.alonsoruibal.chess;

import junit.framework.TestCase;

public class BoardTest extends TestCase {

	public void testMoveNumber1() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w QKqk - 0 1");
		assertEquals(b.moveNumber, 0);
	}

	public void testMoveNumber2() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b QKqk - 0 1");
		assertEquals(b.moveNumber, 1);
	}

	public void testMoveNumber3() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w QKqk - 0 1");
		b.setFenMove("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b QKqk - 0 1", "e2e4");
		assertEquals(b.initialMoveNumber, 0);
		assertEquals(b.moveNumber, 1);
	}

	public void testUndo() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w QKqk - 0 1");
		b.doMove(Move.getFromString(b, "e2e4", true));
		b.doMove(Move.getFromString(b, "e7e5", true));
		b.undoMove();
		b.undoMove();
		assertEquals(b.getFen(), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w QKqk - 0 1");
	}

	public void testCastling() {
		// Must keep history after castling
		Board b = new Board();
		b.setFen("rnbqk2r/ppp1bppp/4pn2/3p4/2PP4/3QP3/PP1B1PPP/RN2KBNR b QKqk - 2 5");
		b.setFenMove("rnbq1rk1/ppp1bppp/4pn2/3p4/2PP4/3QP3/PP1B1PPP/RN2KBNR w QK - 0 6", "O-O");
		assertEquals(b.initialMoveNumber, 9);
	}
}