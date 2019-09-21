package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndgameTest extends BaseTest {

	@Test
	void testKXK() {
		assertTrue(getEval("6k1/8/4K3/8/R7/8/8/8 w - - 0 0") > Evaluator.KNOWN_WIN, "Does not identify a KNOWN_WIN");
	}

	@Test
	void testKNBK() {
		int value1 = getEval("7k/8/4K3/8/NB6/8/8/8 w - - 0 0");
		int value2 = getEval("k7/8/3K4/8/NB6/8/8/8 w - - 0 0");
		System.out.println("value1 = " + value1);
		System.out.println("value2 = " + value2);
		assertTrue(value2 > Evaluator.KNOWN_WIN, "It does not return a known win");
		assertTrue(value1 > value2, "It does not drive the king to the right corner");
	}

	@Test
	void testPawnAfterPromotionIsBetter() {
		int value1 = getEval("1Q6/2K5/7k/8/8/8/8/8 w - - 0 0");
		int value2 = getEval("8/1PK5/7k/8/8/8/8/8 w - - 0 0");
		System.out.println("value1 = " + value1);
		System.out.println("value2 = " + value2);
		assertTrue(value1 > value2, "Pawn after promotion must be better");
	}

	@Test
	void testKPK() {
		int value;
		value = getEval("8/5k1P/8/8/8/7K/8/8 w - - 0 0");
		assertTrue(value >= Evaluator.KNOWN_WIN, "Pawn promotes but value=" + value);

		value = getEval("8/8/7k/8/8/8/5K1p/8 w - - 0 0");
		assertTrue(value <= Evaluator.KNOWN_WIN, "Pawn promotes but value=" + value);

		value = getEval("8/5k1P/8/8/8/7K/8/8 b - - 0 0");
		assertEquals(value, Evaluator.DRAW, "Pawn captured after promotion but value = " + value);

		// Panno vs. Najdorf
		value = getEval("8/1k6/8/8/8/7K/7P/8 w - - 0 0");
		assertTrue(value >= Evaluator.KNOWN_WIN, "White moves and wins = " + value);

		// Barcza vs. Fischer, 1959
		value = getEval("8/8/8/p7/k7/4K3/8/8 w - - 0 0");
		assertEquals(value, Evaluator.DRAW, "White moves and draws = " + value);

		// Golombek vs. Pomar, 1946
		value = getEval("6k1/8/6K1/6P1/8/8/8/8 w - - 0 0");
		assertTrue(value >= Evaluator.KNOWN_WIN, "White moves and wins = " + value);

		// Maróczy vs. Marshall, 1903
		value = getEval("8/8/8/6p1/7k/8/6K1/8 b - - 0 0");
		assertTrue(value <= Evaluator.KNOWN_WIN, "Black moves and wins = " + value);

		// ECO vol 1, #17 (reversed)
		value = getEval("8/8/8/1p6/1k6/8/8/1K6 w - - 0 0");
		assertEquals(value, Evaluator.DRAW, "White moves and draws = " + value);

		// Kamsky vs. Kramnik, 2009
		value = getEval("5k2/8/2K1P3/8/8/8/8/8 b - - 0 0");
		assertEquals(value, Evaluator.DRAW, "Black moves and draws = " + value);
	}

	@Test
	void testKRPKRDraw() {
		assertEquals(Evaluator.DRAW, getEval("5k2/8/r7/1R6/5K2/5P2/8/8 w - - 4 70"), "Philidor position");
		assertEquals(Evaluator.DRAW, getEval("5k2/3R4/r7/5PK1/8/8/8/8 b - - 5 70"), "Philidor position 2");
	}

	@Test
	void testKPk() {
		assertEquals(getSearchScore("5k2/8/2K1P3/8/8/8/8/8 b - - 0 0", 10), Evaluator.DRAW, "Black moves and draws");
	}

	@Test
	void testKPkp() {
		// Whites always win
		assertTrue(getSearchScore("8/4k3/4p3/4P3/1K6/8/8/8 b - - 0 0", 15) < -Evaluator.QUEEN, "Whites capture the pawn in 6th and win");
	}

	@Test
	void testKRKR() {
		assertEquals(getSearchScore("rk6/8/8/8/8/8/8/RK6 w - - 0 0", 10), Evaluator.DRAW, "Most KR vs KR positions are draw");
		assertTrue(getSearchScore("8/8/8/4k3/K7/R3r3/8/8 w - - 0 0", 10) >= Evaluator.KNOWN_WIN, "Wins capturing with the other rook with my rook");
		assertEquals(getSearchScore("8/8/8/8/K7/R3r3/5k2/8 w - - 0 0", 10), Evaluator.DRAW, "Cannot capture with the rook because it is defended by the other king");
		assertEquals(getSearchScore("8/8/8/3R4/2k5/4r3/5K2/8 w - - 0 0", 10), Evaluator.DRAW, "Both kings capture rooks");
		assertTrue(getSearchScore("8/5K2/4r3/3R4/2k5/8/8/8 w - - 0 0", 10) >= Evaluator.KNOWN_WIN, "First king captures the rook, the second cannot");
		assertEquals(getSearchScore("8/8/8/3R4/2k5/1r6/2K5/8 b - - 0 0", 10), Evaluator.DRAW, "Moving my king to capture the rook allows the other king to capture my rook");
	}

	@Test
	void testKRk() {
		assertEquals(getSearchScore("8/7K/8/8/8/8/R7/7k w - - 0 1", 21), Evaluator.MATE - 15, "Rook mate in 15 PLY");
	}

	@Test
	void testKQk() {
		assertEquals(getSearchScore("8/8/8/4k3/8/8/8/KQ6 w - - 0 0", 21), Evaluator.MATE - 17, "Queen mate in 17 PLY");
	}

	@Test
	void testKRPKP() {
		assertTrue(getSearchScore("2r5/8/5k2/8/2P5/2K5/8/4R3 w - - 0 1", 15) > 50, "White wins");
		assertEquals(getSearchScore("8/8/8/8/6r1/1pk5/8/1K2R3 w - - 0 1", 15), Evaluator.DRAW, "Back Rank defense");
	}

	@Test
	void testKQKP() {
		assertEquals(getSearchScore("2K5/2P5/8/4k3/3q4/8/8/8 w - - 0 1", 15), Evaluator.DRAW, "Pawn in knight column draws");
	}
}