package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContemptFactorTest {

	Config config;
	SearchEngine searchEngine;

	@Before
	public void setUp() throws Exception {
		config = new Config();
		searchEngine = new SearchEngine(config);
	}

	@Test
	public void testContemp1() {
		searchEngine.getBoard().setFen("7k/7p/5P1K/8/8/8/8/8 w");
		searchEngine.go(SearchParameters.get(1000));
		searchEngine.getBoard().doMove(searchEngine.getBestMove());
		assertEquals(-SearchEngine.CONTEMPT_FACTOR, searchEngine.getBestMoveScore());
	}
}
