package com.alonsoruibal.chess;

import junit.framework.TestCase;

/**
 * @author rui
 */
public class SeeTest extends TestCase {

	
	public void test1() { 
		String fen = "1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - -";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int move = Move.getFromString(board, "Rxe5", true);		
		System.out.println("MOVE = " + Move.toStringExt(move));
		int value = board.see(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), Move.PAWN);
		System.out.println("value = " + value);
		assertTrue(value == 100);
	}

	public void test2() {
		String fen = "5K1k/8/8/8/8/8/1r6/Rr6 w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int move = Move.getFromString(board, "a1b1", true);
		int value = board.see(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), Move.ROOK);
		System.out.println("value = " + value);
		assertTrue(value == 0);
	}

	public void test3() {
		String fen = "5K1k/8/8/8/8/8/b7/RrR5 w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int move = Move.getFromString(board, "a1b1", true);
		int value = board.see(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), Move.ROOK);
		System.out.println("value = " + value);
		assertTrue(value == 330);
	}
	
	public void test4() {
		String fen = "1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - -";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int move = Move.getFromString(board, "Ne5", true);
		int value = board.see(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), Move.PAWN);
		System.out.println("value = " + value);
		assertTrue(value == -225);
	}	 
	
}