package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test Opening/Ending two short values in the same integer arithmetic
 */
public class EvaluatorOpeningEndgameTest {

	@Test
	public void conversionA() {
		int value = Evaluator.oe(-89, 54);
		assertEquals("Conversion O", -89, Evaluator.o(value));
		assertEquals("Conversion E", 54, Evaluator.e(value));
	}

	@Test
	public void conversionB() {
		int value = Evaluator.oe(54, -89);
		assertEquals("Conversion O", 54, Evaluator.o(value));
		assertEquals("Conversion E", -89, Evaluator.e(value));
	}

	@Test
	public void add() {
		int value = Evaluator.oe(12, 38) + Evaluator.oe(9, 67);
		assertEquals("Add O", 21, Evaluator.o(value));
		assertEquals("Add E", 105, Evaluator.e(value));
	}

	@Test
	public void addTwoNegatives() {
		int value = Evaluator.oe(-8, -8) + Evaluator.oe(-8, -8);
		assertEquals("Sub O", -16, Evaluator.o(value));
		assertEquals("Sub E", -16, Evaluator.e(value));
	}

	@Test
	public void addThreeNegatives() {
		int value = Evaluator.oe(-8, -8) + Evaluator.oe(-8, -8) + Evaluator.oe(-8, -8);
		assertEquals("Sub O", -24, Evaluator.o(value));
		assertEquals("Sub E", -24, Evaluator.e(value));
	}

	@Test
	public void addNegativeA() {
		int value = Evaluator.oe(10, 10) + Evaluator.oe(-2, -2);
		assertEquals("Sub O", 8, Evaluator.o(value));
		assertEquals("Sub E", 8, Evaluator.e(value));
	}

	@Test
	public void addNegativeB() {
		int value = Evaluator.oe(12, 38) + Evaluator.oe(-20, -48);
		assertEquals("AddNegative O", -8, Evaluator.o(value));
		assertEquals("AddNegative E", -10, Evaluator.e(value));
	}

	@Test
	public void subNegative() {
		int value = Evaluator.oe(10, 10) - Evaluator.oe(-8, -8);
		assertEquals("Sub O", 18, Evaluator.o(value));
		assertEquals("Sub E", 18, Evaluator.e(value));
	}

	@Test
	public void mutiplyPositive() {
		int value = 5 * Evaluator.oe(4, 50);
		assertEquals("Multiply O", 20, Evaluator.o(value));
		assertEquals("Multiply E", 250, Evaluator.e(value));
	}

	@Test
	public void mutiplyNegativeA() {
		int value = Evaluator.oeMul(5, Evaluator.oe(-10, 3));
		assertEquals("Mutiply in O", -50, Evaluator.o(value));
		assertEquals("Mutiply in E", 15, Evaluator.e(value));
	}

	@Test
	public void mutiplyNegativeB() {
		int value = Evaluator.oeMul(5, Evaluator.oe(10, -3));
		assertEquals("Mutiply in O", 50, Evaluator.o(value));
		assertEquals("Mutiply in E", -15, Evaluator.e(value));
	}

	@Test
	public void mutiplyNegativeC() {
		int value = Evaluator.oeMul(-5, Evaluator.oe(5, -1));
		assertEquals("Mutiply in O", -25, Evaluator.o(value));
		assertEquals("Mutiply in E", 5, Evaluator.e(value));
	}

	@Test
	public void mutiplyNegativeD() {
		int value = Evaluator.oeMul(-5, Evaluator.oe(-5, 1));
		assertEquals("Mutiply in O", 25, Evaluator.o(value));
		assertEquals("Mutiply in E", -5, Evaluator.e(value));
	}

	@Test
	public void openingValueMustBeZero() {
		int value = Evaluator.oe(0, -5);
		assertEquals("O must be zero", 0, Evaluator.o(value));
	}

	@Test
	public void endgameValueMustBeZero() {
		int value = Evaluator.oe(-5, 0);
		assertEquals("E must be zero", 0, Evaluator.e(value));
	}
}