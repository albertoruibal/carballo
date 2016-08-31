package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ContemptFactorTest extends BaseTest {

	@Test
	public void testContemp1() {
		assertTrue(getSearchScore("7k/7p/5P1K/8/8/8/8/8 w", 18) == Evaluator.DRAW);
	}
}
