package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;

import junit.framework.TestCase;

public class EvaluatorTest extends TestCase {

	Evaluator evaluator;

	@Override
	protected void setUp() throws Exception {
		evaluator = new ExperimentalEvaluator(new Config());
		((ExperimentalEvaluator) evaluator).debug = true;
	}

	public void testEvaluatorSimmetry1() {
		String fen = "r2q1rk1/ppp2ppp/2n2n2/1B1pp1B1/1b1PP1b1/2N2N2/PPP2PPP/R2Q1RK1 w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int value = evaluator.evaluateBoard(board, -Evaluator.VICTORY, Evaluator.VICTORY);
		System.out.println("value = " + value);
		assertEquals(9, value);
	}

	public void testEvaluatorSimmetry2() {
		String fen = "7k/7p/6p1/3kp3/3PK3/1P6/P7/K7 w - - 0 0";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int value = evaluator.evaluateBoard(board, -Evaluator.VICTORY, Evaluator.VICTORY);
		System.out.println("value = " + value);
		assertEquals(9, value);
	}


	public void testPassedPawn1() {
		String fen = "7k/7p/P7/8/8/6p1/7P/7K w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int value = evaluator.evaluateBoard(board, -Evaluator.VICTORY, Evaluator.VICTORY);
		System.out.println("value = " + value);
		assertTrue(value > 0);
	}

	public void testKnightTrapped() {
		String fen = "NPP5/PPP5/PPP5/8/8/8/8/k6K w - - 0 0";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int value = evaluator.evaluateBoard(board, -Evaluator.VICTORY, Evaluator.VICTORY);
		System.out.println("value = " + value);
		assertTrue(value > 0);
	}

	public void testKingSafety() {
		String fen = "r6k/1R6/8/8/8/8/8/7K w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		System.out.print(board);
		int value = evaluator.evaluateBoard(board, -Evaluator.VICTORY, Evaluator.VICTORY);
		System.out.println("value = " + value);
		assertTrue(value > 0);
	}
}