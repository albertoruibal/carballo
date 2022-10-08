package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.evaluation.KPKBitbase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KPKBitbaseTest {

	private KPKBitbase kpkBitbase;
	private final Board board = new Board();
	private String fen;

	@BeforeEach
	void setUp() {
		BitboardAttacks.getInstance();
		kpkBitbase = new KPKBitbase();
	}

	@Test
	void testPawnPromotion() {
		fen = "8/5k1P/8/8/8/7K/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue(kpkBitbase.probe(board), "Pawn promotes");

		fen = "8/5k1P/8/8/8/7K/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "Pawn captured after promotion");
	}

	@Test
	void testKingCapturesPawn() {
		fen = "8/6kP/8/8/8/7K/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "Black king captures promoted piece");

		fen = "8/8/4kP2/8/8/7K/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "Black king captures promoted piece");

		fen = "8/8/2Pk4/8/8/K7/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "Black king captures promoted piece");

		fen = "8/8/4Kp2/8/8/7k/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "White king captures promoted piece");

		fen = "8/8/2pK4/8/8/k7/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "White king captures promoted piece");
	}

	@Test
	void testPositions() {
		// Panno vs. Najdorf
		fen = "8/1k6/8/8/8/7K/7P/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue(kpkBitbase.probe(board), "White moves and wins");

		// Barcza vs. Fischer, 1959
		fen = "8/8/8/p7/k7/4K3/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "White moves and draws");

		// Golombek vs. Pomar, 1946
		fen = "6k1/8/6K1/6P1/8/8/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue(kpkBitbase.probe(board), "White moves and wins");

		// Maróczy vs. Marshall, 1903
		fen = "8/8/8/6p1/7k/8/6K1/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertTrue(kpkBitbase.probe(board), "Black moves and wins");

		// ECO vol 1, #17 (reversed)
		fen = "8/8/8/1p6/1k6/8/8/1K6 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "White moves and draws");

		// Kamsky vs. Kramnik, 2009
		fen = "5k2/8/2K1P3/8/8/8/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertFalse(kpkBitbase.probe(board), "Black moves and draws");
	}
}