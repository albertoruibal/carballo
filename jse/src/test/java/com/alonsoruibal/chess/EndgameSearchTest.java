package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EndgameSearchTest extends SearchTest {

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