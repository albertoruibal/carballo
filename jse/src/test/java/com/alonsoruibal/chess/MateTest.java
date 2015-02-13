package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Mate tests from http://membres.lycos.fr/albillo/
 */
public class MateTest {

	SearchEngine searchEngine;

	@Before
	public void setUp() throws Exception {
		searchEngine = new SearchEngine(new Config());
	}

	@Test
	public void testBasic1() {
		testPosition("6k1/8/6K1/8/8/8/8/R7 w", "a1a8");
	}

	@Test
	public void testBasic2() {
		testPosition("5rk1/5ppp/8/8/8/4PQ2/r1q2PPP/RR4K1 b", "c2b2");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate1() {
		// 1.- Study by Dr. Lasker and Reichhel
		// posic101
		// White to play and win:
		// 1. Ka1-b1 Ka7-b7 2. Kb1-c1
		testPosition("8/k7/3p4/p2P1p2/P2P1P2/8/8/K7 w", "a1b1");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate2() {
		// 2.- Marco vs. Maroczy. Paris, 1900
		// posic102
		// Black to play and win:
		// 1. ... Nb2-d3 2. Nc1-b3 Nd3-e1+ 3. Kc2-d1 Ke3-d3 4. Kd1xe1 Kd3xc3 
		testPosition("8/8/2p5/1p1p4/1P1P4/p1P1k3/1nK5/2N5 b", "b2d3");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate3() {
		// 3.- Ortueta vs. Sanz. Madrid, 1933
		// posic103
		// Black to play and win:
		// 1. ... Rd2xb2 2. Na4xb2 c4-c3 3. Rb7xb6 c5-c4 4. Rb6-b4 a7-a5 
		testPosition("8/pR4pk/1b6/2p5/N1p5/8/PP1r2PP/6K1 b", "d2b2");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate4() {
		// 4.- Unknown players
		// posic104 
		// White to play and win:
		// 1. Nc5-d3+ Nb4xd3 2. a5-a6 Bf7-e8 3. Nc3-d5+ Kf4-e5 4. Nd5-e7
		testPosition("8/5b2/6p1/P1N4p/1n3k2/1PN5/6P1/6K1 w", "c5d3");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate5() {
		// 5.- Sam Loyd, American Chess Nuts, 1868
		// posic105
		// White to play and mate in 5:
		// 1. Rh6 Kxh6 2. Kxf6 Kh7 3. g5 Kh8 4. g6 
		testPosition("6b1/4Kpk1/5r2/8/3B2P1/7R/8/8 w", "h3h6");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate6() {
		// 6.- Sam Loyd, Paris Tournament, 1878
		// posic106
		// White to play and mate in 5:
		// 1. Qb4 Bf6 2. Rg7 Bxg7 3. Qb5+ Ka7 4. Bc6");
		testPosition("7r/3B4/k7/8/6Qb/8/Kn6/6R1 w", "g4b4");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate7() {
		// 7.- Position taken from "Chess skills in man and machine", Fig. 3.4.
		// posic107
		// White to play and win:
		// 1. Qh4xh5 g6xh5 2. Rh3-g3+ Bf8-g7 3. Ne3-f5 Qe7-f8 4. Nf5xg7 d6xe5 
		testPosition("2r2bk1/1b1rqp1p/p2p2p1/1p2P2n/3BP2Q/P2BN2R/1PP3PP/5R1K w", "h4h5");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate8() {
		// 8.- Position taken from "How computers play chess"
		// posic108
		// White to play and win: 1. b3-b4 
		testPosition("8/2p5/3k4/1p1p1K2/8/1P1P4/2P5/8 w", "b3b4");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate9() {
		// 9.- Karpov vs Kasparov, Moscu 1984/85
		// posic109
		// White to play and win:
		// 1. a5-a6 Bd1-b3; 2. Nc5xb3 Rb4-a4; 3. Nb3-c5 Ra4-a5
		testPosition("6k1/6p1/7p/P1N5/1r3p2/7P/1b3PP1/3bR1K1 w", "a5a6");
	}

	@Test
	@Category(SlowTest.class)
	public void testMate10() {
		// 10.- Position taken from "How computers play chess"
		// posic110
		// White to play and mate in 9: 1. Qg7-b2 
		testPosition("7K/6Q1/8/8/8/3k4/8/8 w", "g7b2");
	}

	private void testPosition(String fen, String move) {
		searchEngine.getBoard().setFen(fen);
		System.out.println("Looking for " + move);
		//System.out.println(moveGenerator.generateMoves(board));
		searchEngine.go(SearchParameters.get(5000));
		String bestOperation = Move.toString(searchEngine.getBestMove());
		System.out.println();
		assertEquals(move, bestOperation);
	}
}
