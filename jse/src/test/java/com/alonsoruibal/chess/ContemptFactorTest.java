package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import junit.framework.TestCase;

/**
 * Some tests from http://membres.lycos.fr/albillo/
 */
public class ContemptFactorTest extends TestCase {

	Config config;
	SearchEngine searchEngine;

	@Override
	protected void setUp() throws Exception {
		config = new Config();
		searchEngine = new SearchEngine(config);
	}

	public void testContemp1() {
		searchEngine.getBoard().setFen("7k/7p/5P1K/8/8/8/8/8 w");
		searchEngine.go(SearchParameters.get(1000));
		searchEngine.getBoard().doMove(searchEngine.getBestMove());
		searchEngine.getTT().search(searchEngine.getBoard(), false);
		assertEquals(-Config.DEFAULT_CONTEMPT_FACTOR, searchEngine.getBestMoveScore());
	}

}
