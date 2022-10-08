package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContemptFactorTest extends BaseTest {

	@Test
	void testContemp1() {
		assertEquals(getSearchScore("7k/7p/5P1K/8/8/8/8/8 w", 18), Evaluator.DRAW);
	}
}
