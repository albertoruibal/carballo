package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EndgameTest extends BaseTest {

	@Test
	public void testKXK() {
		assertTrue("Does not indentify a KNOWN_WIN", getEval("6k1/8/4K3/8/R7/8/8/8 w - - 0 0") > Evaluator.KNOWN_WIN);
	}

	@Test
	public void testKNBK() {
		int value1 = getEval("7k/8/4K3/8/NB6/8/8/8 w - - 0 0");
		int value2 = getEval("k7/8/3K4/8/NB6/8/8/8 w - - 0 0");
		System.out.println("value1 = " + value1);
		System.out.println("value2 = " + value2);
		assertTrue("It does not return a known win", value2 > Evaluator.KNOWN_WIN);
		assertTrue("It does not drive the king to the right corner", value1 > value2);
	}

	@Test
	public void testPawnAfterPromotionIsBetter() {
		int value1 = getEval("1Q6/2K5/7k/8/8/8/8/8 w - - 0 0");
		int value2 = getEval("8/1PK5/7k/8/8/8/8/8 w - - 0 0");
		System.out.println("value1 = " + value1);
		System.out.println("value2 = " + value2);
		assertTrue("Pawn after promotion must be better", value1 > value2);
	}

	@Test
	public void testKPK() {
		int value;
		value = getEval("8/5k1P/8/8/8/7K/8/8 w - - 0 0");
		assertTrue("Pawn promotes but value=" + value, value >= Evaluator.KNOWN_WIN);

		value = getEval("8/8/7k/8/8/8/5K1p/8 w - - 0 0");
		assertTrue("Pawn promotes but value=" + value, value <= Evaluator.KNOWN_WIN);

		value = getEval("8/5k1P/8/8/8/7K/8/8 b - - 0 0");
		assertTrue("Pawn captured after promotion but value = " + value, value == Evaluator.DRAW);

		// Panno vs. Najdorf
		value = getEval("8/1k6/8/8/8/7K/7P/8 w - - 0 0");
		assertTrue("White moves and wins = " + value, value >= Evaluator.KNOWN_WIN);

		// Barcza vs. Fischer, 1959
		value = getEval("8/8/8/p7/k7/4K3/8/8 w - - 0 0");
		assertTrue("White moves and draws = " + value, value == Evaluator.DRAW);

		// Golombek vs. Pomar, 1946
		value = getEval("6k1/8/6K1/6P1/8/8/8/8 w - - 0 0");
		assertTrue("White moves and wins = " + value, value >= Evaluator.KNOWN_WIN);

		// Maróczy vs. Marshall, 1903
		value = getEval("8/8/8/6p1/7k/8/6K1/8 b - - 0 0");
		assertTrue("Black moves and wins = " + value, value <= Evaluator.KNOWN_WIN);

		// ECO vol 1, #17 (reversed)
		value = getEval("8/8/8/1p6/1k6/8/8/1K6 w - - 0 0");
		assertTrue("White moves and draws = " + value, value == Evaluator.DRAW);

		// Kamsky vs. Kramnik, 2009
		value = getEval("5k2/8/2K1P3/8/8/8/8/8 b - - 0 0");
		assertTrue("Black moves and draws = " + value, value == Evaluator.DRAW);
	}

	@Test
	public void testKRPKRDraw() {
		assertEquals("Philidor position", Evaluator.DRAW, getEval("5k2/8/r7/1R6/5K2/5P2/8/8 w - - 4 70"));
		assertEquals("Philidor position 2", Evaluator.DRAW, getEval("5k2/3R4/r7/5PK1/8/8/8/8 b - - 5 70"));
	}

	@Test
	public void testPawnRam() {
		// https://chessprogramming.wikispaces.com/Blockage+Detection
		assertEquals("Black moves and draws", Evaluator.DRAW, getEval("4k3/8/3pPp2/1p1P1P1p/1P5P/5P2/3K4/8 w - -"));
	}

	@Test
	public void testKPk() {
		assertTrue("Black moves and draws", getSearchScore("5k2/8/2K1P3/8/8/8/8/8 b - - 0 0", 10) == Evaluator.DRAW);
	}

	@Test
	public void testKPkp() {
		// Whites always win
		assertTrue("Whites capture the pawn in 6th and win", getSearchScore("8/4k3/4p3/4P3/1K6/8/8/8 b - - 0 0", 15) < -Evaluator.QUEEN);
	}

	@Test
	public void testKRKR() {
		assertTrue("Most KR vs KR positions are draw", getSearchScore("rk6/8/8/8/8/8/8/RK6 w - - 0 0", 10) == Evaluator.DRAW);
		assertTrue("Wins capturing with the other rook with my rook", getSearchScore("8/8/8/4k3/K7/R3r3/8/8 w - - 0 0", 10) >= Evaluator.KNOWN_WIN);
		assertTrue("Cannot capture with the rook because it is defended by the other king", getSearchScore("8/8/8/8/K7/R3r3/5k2/8 w - - 0 0", 10) == Evaluator.DRAW);
		assertTrue("Both kings capture rooks", getSearchScore("8/8/8/3R4/2k5/4r3/5K2/8 w - - 0 0", 10) == Evaluator.DRAW);
		assertTrue("First king captures the rook, the second cannot", getSearchScore("8/5K2/4r3/3R4/2k5/8/8/8 w - - 0 0", 10) >= Evaluator.KNOWN_WIN);
		assertTrue("Moving my king to capture the rook allows the other king to capture my rook", getSearchScore("8/8/8/3R4/2k5/1r6/2K5/8 b - - 0 0", 10) == Evaluator.DRAW);
	}

	@Test
	public void testKRk() {
		assertEquals("Rook mate in 15 PLY", getSearchScore("8/7K/8/8/8/8/R7/7k w - - 0 1", 21), Evaluator.MATE - 15);
	}

	@Test
	public void testKQk() {
		assertEquals("Queen mate in 17 PLY", getSearchScore("8/8/8/4k3/8/8/8/KQ6 w - - 0 0", 20), Evaluator.MATE - 17);
	}

	@Test
	public void testKRPKP() {
		assertTrue("White wins", getSearchScore("2r5/8/5k2/8/2P5/2K5/8/4R3 w - - 0 1", 15) > 50);
		assertTrue("Back Rank defence", getSearchScore("8/8/8/8/6r1/1pk5/8/1K2R3 w - - 0 1", 15) == Evaluator.DRAW);
		assertTrue("Draw with a knight pawn", getSearchScore("1r6/8/4k3/8/1P6/1K6/8/3R4 w - - 0 1", 15) == Evaluator.DRAW);
	}

	@Test
	public void testKQKP() {
		assertTrue("Pawn in knight column draws", getSearchScore("2K5/2P5/8/4k3/3q4/8/8/8 w - - 0 1", 15) == Evaluator.DRAW);
	}
}