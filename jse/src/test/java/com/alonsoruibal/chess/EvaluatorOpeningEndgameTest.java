package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test Opening/Ending two short values in the same integer arithmetic
 */
class EvaluatorOpeningEndgameTest {

	@Test
	void conversionA() {
		int value = Evaluator.oe(-89, 54);
		assertEquals(-89, Evaluator.o(value), "Conversion O");
		assertEquals(54, Evaluator.e(value), "Conversion E");
	}

	@Test
	void conversionB() {
		int value = Evaluator.oe(54, -89);
		assertEquals(54, Evaluator.o(value), "Conversion O");
		assertEquals(-89, Evaluator.e(value), "Conversion E");
	}

	@Test
	void add() {
		int value = Evaluator.oe(12, 38) + Evaluator.oe(9, 67);
		assertEquals(21, Evaluator.o(value), "Add O");
		assertEquals(105, Evaluator.e(value), "Add E");
	}

	@Test
	void subFromNegative() {
		int value = Evaluator.oe(-8, -8) - Evaluator.oe(8, 8);
		assertEquals(-16, Evaluator.o(value), "Sub O");
		assertEquals(-16, Evaluator.e(value), "Sub E");
	}

	@Test
	void subThreeNegatives() {
		int value = Evaluator.oe(-8, -8) - Evaluator.oe(8, 8) - Evaluator.oe(8, 8);
		assertEquals(-24, Evaluator.o(value), "Sub O");
		assertEquals(-24, Evaluator.e(value), "Sub E");
	}

	@Test
	void subSwitchToNegativeA() {
		int value = Evaluator.oe(10, 10) - Evaluator.oe(12, 12);
		assertEquals(-2, Evaluator.o(value), "Sub O");
		assertEquals(-2, Evaluator.e(value), "Sub E");
	}

	@Test
	void mutiplyPositive() {
		int value = 5 * Evaluator.oe(4, 50);
		assertEquals(20, Evaluator.o(value), "Multiply O");
		assertEquals(250, Evaluator.e(value), "Multiply E");
	}

	@Test
	void openingValueMustBeZero() {
		int value = Evaluator.oe(0, -5);
		assertEquals(0, Evaluator.o(value), "O must be zero");
	}

	@Test
	void endgameValueMustBeZero() {
		int value = Evaluator.oe(-5, 0);
		assertEquals(0, Evaluator.e(value), "E must be zero");
	}
}