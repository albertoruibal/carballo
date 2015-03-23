package com.alonsoruibal.chess;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SeeTest {

	private void testSee(String fen, String moveString, int expectedSee) {
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int move = Move.getFromString(board, moveString, true);
		int calculatedSee = board.see(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), Move.getPieceCaptured(board, move));
		System.out.println(moveString + " SEE = " + calculatedSee);
		assertEquals("Bad SEE", expectedSee, calculatedSee);
	}

	@Test
	public void test1() {
		testSee("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - -", "Rxe5", 100);
	}

	@Test
	public void test2() {
		testSee("5K1k/8/8/8/8/8/1r6/Rr6 w QKqk - 0 0", "a1b1", 0);
	}

	@Test
	public void test3() {
		testSee("5K1k/8/8/8/8/8/b7/RrR5 w QKqk - 0 0", "a1b1", 330);
	}

	@Test
	public void test4() {
		testSee("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - -", "Ne5", -225);
	}

	@Test
	public void testNoOtherPiecesAttack() {
		testSee("rq2r1k1/5pp1/p7/5NP1/1p2P2P/8/PQ4K1/5R1R b - - 0 2", "Re8xe4", 100);
	}

	@Test
	public void testSeeError() {
		testSee("8/1kb2p2/4b1p1/8/2Q2NB1/8/8/K7 w - - 0 1", "Nf4xe6", 105);
	}

	@Test
	public void testSeeEnpassant() {
		testSee("7k/8/8/8/2pP1n2/8/2B5/7K b - d3 0 1", "c4xd3", 100);
	}
}