package com.alonsoruibal.chess;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoardTest {

	@Test
	public void testNoChess960StartPosition() {
		Board b = new Board();
		b.startPosition();
		assertEquals(false, b.chess960);
	}

	@Test
	public void testChess960StartPosition() {
		Board b = new Board();
		b.startPosition(545);
		System.out.println(b);
		assertEquals("brnbknqr/pppppppp/8/8/8/8/PPPPPPPP/BRNBKNQR w KQkq - 0 1", b.getFen());
	}

	@Test
	public void testChess960Castling() {
		Board b = new Board();
		b.setFen("nqrkbbnr/pppppppp/8/8/8/8/PPPPPPPP/NQRKBBNR w KQkq - 0 1");
		System.out.println(b);
		int move = Move.getFromString(b, "O-O-O", false);
		b.doMove(move, false, false);
		System.out.println(b);
		assertEquals("nqrkbbnr/pppppppp/8/8/8/8/PPPPPPPP/NQKRBBNR b kq - 1 1", b.getFen());
	}

	@Test
	public void testChess960CastlingKingSameSquare() {
		Board b = new Board();
		b.setFen("r1krbnqb/1pp1pppp/1p1p4/8/3P4/8/PPP1PPPP/NRK1RNQB w Qk - 0 1");
		System.out.println(b);
		int move = Move.getFromString(b, "O-O-O", false);
		b.doMove(move, false, false);
		System.out.println(b);
		assertEquals("r1krbnqb/1pp1pppp/1p1p4/8/3P4/8/PPP1PPPP/N1KRRNQB b k - 1 1", b.getFen());
	}

	@Test
	public void testChess960CastlingRookSameSquare() {
		Board b = new Board();
		b.setFen("7k/pppppppp/8/8/8/8/PPPPPPPP/3RK3 w Q - 0 1");
		System.out.println(b);
		int move = Move.getFromString(b, "O-O-O", false);
		b.doMove(move, false, false);
		System.out.println(b);
		assertEquals("7k/pppppppp/8/8/8/8/PPPPPPPP/2KR4 b - - 1 1", b.getFen());
	}

	@Test
	public void testXFen() {
		// http://en.wikipedia.org/wiki/X-FEN
		Board b = new Board();
		b.setFen("rn2k1r1/ppp1pp1p/3p2p1/5bn1/P7/2N2B2/1PPPPP2/2BNK1RR w Gkq - 4 11");
		System.out.println(b);
		assertEquals(1L << 1, b.castlingRooks[0]);
		b.doMove(Move.getFromString(b, "O-O", true));
		System.out.println(b);
		assertEquals("rn2k1r1/ppp1pp1p/3p2p1/5bn1/P7/2N2B2/1PPPPP2/2BN1RKR b kq - 5 11", b.getFen());
	}

	@Test
	public void testMoveNumber1() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w QKqk - 0 1");
		assertEquals(b.moveNumber, 0);
	}

	@Test
	public void testMoveNumber2() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b QKqk - 0 1");
		assertEquals(b.moveNumber, 1);
	}

	@Test
	public void testMoveNumber3() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w QKqk - 0 1");
		b.setFenMove("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b QKqk - 0 1", "e2e4");
		assertEquals(b.initialMoveNumber, 0);
		assertEquals(b.moveNumber, 1);
	}

	@Test
	public void testUndo() {
		Board b = new Board();
		b.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		b.doMove(Move.getFromString(b, "e2e4", true));
		b.doMove(Move.getFromString(b, "e7e5", true));
		b.undoMove();
		b.undoMove();
		assertEquals(b.getFen(), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	}

	@Test
	public void testCastling() {
		// Must keep history after castling
		Board b = new Board();
		b.setFen("rnbqk2r/ppp1bppp/4pn2/3p4/2PP4/3QP3/PP1B1PPP/RN2KBNR b QKqk - 2 5");
		b.setFenMove("rnbq1rk1/ppp1bppp/4pn2/3p4/2PP4/3QP3/PP1B1PPP/RN2KBNR w QK - 0 6", "O-O");
		assertEquals(b.initialMoveNumber, 9);
	}

	@Test
	public void testDrawDetection() {
		Board b = new Board();
		b.setFen("7k/8/8/8/8/8/8/7K w - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/8/8/8/8/8/6BK b - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/8/8/8/8/8/6NK b - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/nn6/8/8/8/8/8K b - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/Nn6/8/8/8/8/8K b - - 0 0");
		assertEquals(b.isDraw(), false);
		b.setFen("7k/7p/8/8/8/8/8/6NK b - - 0 0");
		assertEquals(b.isDraw(), false);
	}

	@Test
	public void testPassedPawn() {
		Board b = new Board();
		// Position from http://en.wikipedia.org/wiki/Passed_pawn
		b.setFen("7k/8/7p/1P2Pp1P/2Pp1PP1/8/8/7K w - - 0 0");
		System.out.print(b);
		assertEquals(b.isPassedPawn(25), false);
		assertEquals(b.isPassedPawn(26), false);
		assertEquals(b.isPassedPawn(28), true);
		assertEquals(b.isPassedPawn(29), true);
		assertEquals(b.isPassedPawn(32), false);
		assertEquals(b.isPassedPawn(34), false);
		assertEquals(b.isPassedPawn(35), true);
		assertEquals(b.isPassedPawn(38), true);
		assertEquals(b.isPassedPawn(40), false);
	}

	@Test
	public void testAdjacentColumnBug() {
		Board b = new Board();
		b.setFen("7k/8/2p5/1P6/8/8/8/7K w - - 0 0");
		System.out.print(b);
		assertEquals(b.isPassedPawn(38), false);
	}

	@Test
	public void testCheckDetection() {
		Board b = new Board();
		b.setFen("4k3/8/8/8/8/2q5/1P6/4K3 w - - 0 1");
		System.out.println(b.toString());
		assertTrue("Position must be check", b.getCheck());
	}
}