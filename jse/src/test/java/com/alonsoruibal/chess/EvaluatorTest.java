package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EvaluatorTest extends BaseTest {

	static int countSubstring(String subStr, String str) {
		return (str.length() - str.replace(subStr, "").length()) / subStr.length();
	}

	@Test
	void testEvaluatorSimmetry1() {
		assertEquals(Evaluator.o(CompleteEvaluator.TEMPO), getEval("r2q1rk1/ppp2ppp/2n2n2/1B1pp1B1/1b1PP1b1/2N2N2/PPP2PPP/R2Q1RK1 w QKqk - 0 0"));
	}

	@Test
	void testEvaluatorSimmetry2() {
		assertEquals(Evaluator.e(CompleteEvaluator.TEMPO), getEval("7k/7p/6p1/3Np3/3Pn3/1P6/P7/K7 w - - 0 0"));
	}

	@Test
	void testPawnClassification() {
		Board board = new Board();
		AttacksInfo attacksInfo = new AttacksInfo();
		CompleteEvaluator evaluator = new CompleteEvaluator();
		evaluator.debug = true;
		evaluator.debugPawns = true;

		board.setFen("7k/8/7p/1P2Pp1P/2Pp1PP1/8/8/7K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(4, countSubstring("passed ", evaluator.debugSB.toString()), "Four passers");
		assertEquals(1, countSubstring("outside ", evaluator.debugSB.toString()), "One outside passed");
		assertEquals(3, countSubstring("supported ", evaluator.debugSB.toString()), "Three supported");
		assertEquals(6, countSubstring("connected ", evaluator.debugSB.toString()), "Six connected");
		assertEquals(2, countSubstring("isolated ", evaluator.debugSB.toString()), "Two isolated");
		assertEquals(4, countSubstring("opposed ", evaluator.debugSB.toString()), "Four opposed");

		board.setFen("7k/p6p/PP6/6P1/8/7P/8/7K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(2, countSubstring("candidate ", evaluator.debugSB.toString()), "Two candidates");
		assertEquals(0, countSubstring("backward ", evaluator.debugSB.toString()), "No backward");

		board.setFen("7k/p7/8/PP3ppp/8/5P1P/8/7K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(2, countSubstring("candidate ", evaluator.debugSB.toString()), "Two candidates");

		board.setFen("7k/8/3p4/1p6/2PP4/8/8/7K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(2, countSubstring("candidate ", evaluator.debugSB.toString()), "Two candidates");

		board.setFen("7k/3r4/8/3p4/8/8/8/R6K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("runner ", evaluator.debugSB.toString()), "Runner");

		board.setFen("7k/8/8/3p4/8/8/1r6/R6K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(0, countSubstring("runner ", evaluator.debugSB.toString()), "No Runner");
		assertEquals(1, countSubstring("mobile ", evaluator.debugSB.toString()), "Mobile");
		assertEquals(0, countSubstring("outside ", evaluator.debugSB.toString()), "No Outside");

		board.setFen("7k/8/8/3p4/R7/8/1r6/7K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(0, countSubstring("runner ", evaluator.debugSB.toString()), "No Runner");
		assertEquals(0, countSubstring("mobile ", evaluator.debugSB.toString()), "No Mobile");

		board.setFen("7k/5ppp/8/2p5/P7/8/5PPP/7K w - - 0 0");
		assertTrue(evaluator.evaluate(board, attacksInfo) > 10, "Outside passer superior to inside passer");

		board.setFen("7k/8/8/5P2/5P2/8/8/7K w - - 0 0");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("doubled ", evaluator.debugSB.toString()), "Doubled");
		assertEquals(0, countSubstring("connected ", evaluator.debugSB.toString()), "Not connected");
		assertEquals(1, countSubstring("passed ", evaluator.debugSB.toString()), "Only one passed");

		board.setFen("R7/3p3p/8/3P2P1/3k4/1p5p/1P1NKP1P/7q w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("backward ", evaluator.debugSB.toString()), "One backward");

		board.setFen("7k/8/8/3p1p2/1p1P1Pp1/1P2P1P1/P1P4P/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(5, countSubstring("backward ", evaluator.debugSB.toString()), "Five backward");

		board.setFen("7k/7P/8/5p2/8/8/6P1/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("backward ", evaluator.debugSB.toString()), "One backward");

		board.setFen("7k/8/Pp6/8/8/1P6/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("backward ", evaluator.debugSB.toString()), "One backward");

		board.setFen("7k/8/P1p5/8/8/1P6/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("backward ", evaluator.debugSB.toString()), "One backward");

		board.setFen("7k/2p5/P7/8/8/1P6/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("backward ", evaluator.debugSB.toString()), "One backward");

		board.setFen("7k/2p5/8/P7/8/1P6/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(0, countSubstring("backward ", evaluator.debugSB.toString()), "No backward");

		board.setFen("7k/8/8/P7/2p5/1P6/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(0, countSubstring("backward ", evaluator.debugSB.toString()), "No backward");

		board.setFen("7k/8/4p3/8/4pp2/8/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("backward ", evaluator.debugSB.toString()), "One backward");
		assertEquals(1, countSubstring("doubled ", evaluator.debugSB.toString()), "One Doubled");

		board.setFen("7k/4p3/8/5p2/4p3/8/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(0, countSubstring("backward ", evaluator.debugSB.toString()), "No backward");
		assertEquals(1, countSubstring("doubled ", evaluator.debugSB.toString()), "One Doubled");

		board.setFen("7k/4p3/8/5p2/3Pp3/8/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(1, countSubstring("backward ", evaluator.debugSB.toString()), "One backward");
		assertEquals(1, countSubstring("doubled ", evaluator.debugSB.toString()), "One Doubled");

		board.setFen("7k/2P5/pp6/1P6/8/8/8/7K w - -");
		evaluator.evaluate(board, attacksInfo);
		assertEquals(0, countSubstring("backward ", evaluator.debugSB.toString()), "No backward because it can capture");
	}

	@Test
	void testPassedPawn1() {
		assertTrue(getEval("7k/7p/P7/8/8/6p1/7P/7K w QKqk - 0 0") > 0);
	}

	@Test
	void testKnightTrapped() {
		assertTrue(getEval("NPP5/PPP5/PPP5/8/8/8/8/k6K w - - 0 0") > 0);
	}

	@Test
	void testKingSafety() {
		assertTrue(getEval("r6k/1R6/8/7p/7P/8/8/7K w QKqk - 0 0") > 0);
	}

	@Test
	void testBishopBonus() {
		compareEval("3BB2k/8/8/8/8/8/p7/7K w QKqk - 0 0", "2B1B2k/8/8/8/8/8/p7/7K w QKqk - 0 0", 40);
	}

	@Test
	void testSBDCastling() {
		compareEval("r4r2/pppbkp2/2n3p1/3Bp2p/4P2N/2P5/PP3PPP/2KR3R b q - 0 1",
				"2kr1r2/pppb1p2/2n3p1/3Bp2p/4P2N/2P5/PP3PPP/2KR3R b - - 0 1", 0);
	}

	@Test
	void testConnectedPassersVsCandidate() {
		assertTrue(getEval("8/p1p5/6pp/PPP2k2/8/4PK2/8/8 w - - 0 43") > 0);
	}
}