package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EndgameTest {

	AttacksInfo attacksInfo;
	ExperimentalEvaluator evaluator;
	String fen;
	Board board = new Board();

	@Before
	public void setUp() throws Exception {
		attacksInfo = new AttacksInfo();
		evaluator = new ExperimentalEvaluator();
		evaluator.debug = true;
	}

	@Test
	public void testKXK() {
		String fen1 = "6k1/8/4K3/8/R7/8/8/8 w - - 0 0";
		board.setFen(fen1);
		int value = evaluator.evaluate(board, attacksInfo);
		System.out.println("value = " + value);
		assertTrue("Does not indentify a KNOWN_WIN", value > Evaluator.KNOWN_WIN);
	}

	@Test
	public void testKNBK() {
		String fen1 = "7k/8/4K3/8/NB6/8/8/8 w - - 0 0";
		String fen2 = "k7/8/3K4/8/NB6/8/8/8 w - - 0 0";
		board.setFen(fen1);
		System.out.println(board);
		int value1 = evaluator.evaluate(board, attacksInfo);
		board.setFen(fen2);
		System.out.println(board);
		int value2 = evaluator.evaluate(board, attacksInfo);
		System.out.println("value1 = " + value1);
		System.out.println("value2 = " + value2);
		assertTrue("It does not return a known win", value2 > Evaluator.KNOWN_WIN);
		assertTrue("It does not drive the king to the right corner", value1 > value2);
	}

	@Test
	public void testPawnAfterPromotionIsBetter() {
		String fen1 = "1Q6/2K5/7k/8/8/8/8/8 w - - 0 0";
		String fen2 = "8/1PK5/7k/8/8/8/8/8 w - - 0 0";
		board.setFen(fen1);
		System.out.println(board);
		int value1 = evaluator.evaluate(board, attacksInfo);
		board.setFen(fen2);
		System.out.println(board);
		int value2 = evaluator.evaluate(board, attacksInfo);
		System.out.println("value1 = " + value1);
		System.out.println("value2 = " + value2);
		assertTrue("Pawn after promotion must be better", value1 > value2);
	}

	@Test
	public void testKPK() {
		int value;
		fen = "8/5k1P/8/8/8/7K/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("Pawn promotes but value=" + value, value >= Evaluator.KNOWN_WIN);

		fen = "8/8/7k/8/8/8/5K1p/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("Pawn promotes but value=" + value, value <= Evaluator.KNOWN_WIN);

		fen = "8/5k1P/8/8/8/7K/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("Pawn captured after promotion but value = " + value, value == Evaluator.DRAW);

		// Panno vs. Najdorf
		fen = "8/1k6/8/8/8/7K/7P/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("White moves and wins = " + value, value >= Evaluator.KNOWN_WIN);

		// Barcza vs. Fischer, 1959
		fen = "8/8/8/p7/k7/4K3/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("White moves and draws = " + value, value == Evaluator.DRAW);

		// Golombek vs. Pomar, 1946
		fen = "6k1/8/6K1/6P1/8/8/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("White moves and wins = " + value, value >= Evaluator.KNOWN_WIN);

		// Maróczy vs. Marshall, 1903
		fen = "8/8/8/6p1/7k/8/6K1/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("Black moves and wins = " + value, value <= Evaluator.KNOWN_WIN);

		// ECO vol 1, #17 (reversed)
		fen = "8/8/8/1p6/1k6/8/8/1K6 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("White moves and draws = " + value, value == Evaluator.DRAW);

		// Kamsky vs. Kramnik, 2009
		fen = "5k2/8/2K1P3/8/8/8/8/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		value = evaluator.evaluate(board, attacksInfo);
		assertTrue("Black moves and draws = " + value, value == Evaluator.DRAW);
	}

	@Test
	public void testKRKR() {
		fen = "rk6/8/8/8/8/8/8/RK6 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertEquals("Most KR vs KR positions are draw", Evaluator.DRAW, evaluator.evaluate(board, attacksInfo));

		fen = "8/8/8/4k3/K7/R3r3/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertEquals("Wins capturing with the other rook with my rook", Evaluator.KNOWN_WIN, evaluator.evaluate(board, attacksInfo));

		fen = "8/8/8/8/K7/R3r3/5k2/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertEquals("Cannot capture with the rook because it is defended by the other king", Evaluator.DRAW, evaluator.evaluate(board, attacksInfo));

		fen = "8/8/8/3R4/2k5/4r3/5K2/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertEquals("Both kings capture rooks", Evaluator.DRAW, evaluator.evaluate(board, attacksInfo));

		fen = "8/5K2/4r3/3R4/2k5/8/8/8 w - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertEquals("First king captures the rook, the second cannot", Evaluator.KNOWN_WIN, evaluator.evaluate(board, attacksInfo));

		fen = "8/8/8/3R4/2k5/1r6/2K5/8 b - - 0 0";
		board.setFen(fen);
		System.out.print(board.toString());
		assertEquals("Moving my king to capture the rook allows the other king to capture my rook", Evaluator.DRAW, evaluator.evaluate(board, attacksInfo));
	}
}