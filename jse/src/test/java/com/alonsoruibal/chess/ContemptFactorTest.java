package com.alonsoruibal.chess;

import junit.framework.TestCase;

import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

/**
 * 
 * Some tests from http://membres.lycos.fr/albillo/
 * @author rui
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
		testPosition("7k/7p/5P1K/8/8/8/8/8 w", "a1a8");	
	}
	
	private void testPosition(String fen, String move) {

		searchEngine.getBoard().setFen(fen);
		System.out.println("Looking for " + move);
		//System.out.println(moveGenerator.generateMoves(board));
		searchEngine.go(SearchParameters.get(10 * 60000)); // 10 minutes max
		String bestOperation = Move.toString(searchEngine.getBestMove());
		System.out.println();
		assertEquals(move, bestOperation);
	}
}
