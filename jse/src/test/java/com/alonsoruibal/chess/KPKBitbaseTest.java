package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.evaluation.KPKBitbase;

import junit.framework.TestCase;

public class KPKBitbaseTest extends TestCase {

	KPKBitbase kpkBitbase;
	Board board = new Board();
	String fen;

	@Override
	protected void setUp() throws Exception {
		BitboardAttacks.getInstance();
		kpkBitbase = new KPKBitbase();
	}

	public void testPawnPromotion() {
		fen = "8/5k1P/8/8/8/7K/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue("Pawn promotes", kpkBitbase.probe(board));

		fen = "8/5k1P/8/8/8/7K/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue("Pawn captured after promotion", kpkBitbase.probe(board) == false);
	}

	public void testKingCapturesPawn() {
		fen = "8/6kP/8/8/8/7K/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("Black king captures promoted piece", kpkBitbase.probe(board));

		fen = "8/8/4kP2/8/8/7K/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("Black king captures promoted piece", kpkBitbase.probe(board));

		fen = "8/8/2Pk4/8/8/K7/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("Black king captures promoted piece", kpkBitbase.probe(board));

		fen = "8/8/4Kp2/8/8/7k/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("White king captures promoted piece", kpkBitbase.probe(board));

		fen = "8/8/2pK4/8/8/k7/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("White king captures promoted piece", kpkBitbase.probe(board));
	}

	public void testPositions() {
		// Panno vs. Najdorf
		fen = "8/1k6/8/8/8/7K/7P/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue("White moves and wins ", kpkBitbase.probe(board));

		// Barcza vs. Fischer, 1959
		fen = "8/8/8/p7/k7/4K3/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("White moves and draws", kpkBitbase.probe(board));

		// Golombek vs. Pomar, 1946
		fen = "6k1/8/6K1/6P1/8/8/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue("White moves and wins", kpkBitbase.probe(board));

		// Maróczy vs. Marshall, 1903
		fen = "8/8/8/6p1/7k/8/6K1/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue("Black moves and wins", kpkBitbase.probe(board));

		// ECO vol 1, #17 (reversed)
		fen = "8/8/8/1p6/1k6/8/8/1K6 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("White moves and draws", kpkBitbase.probe(board));

		// Kamsky vs. Kramnik, 2009
		fen = "5k2/8/2K1P3/8/8/8/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse("Black moves and draws", kpkBitbase.probe(board));
	}
}