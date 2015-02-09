package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Estimate program ELO, from:
 * http://www.chessmaniac.com/ELORating/ELO_Chess_Rating.shtml
 */
public class BasicEloTest {

	Config config;
	SearchEngine search;

	@Before
	public void setUp() throws Exception {
		config = new Config();
		config.setBook(new FileBook("/book_small.bin"));
		search = new SearchEngine(config);
	}

	@Test
	@Category(SlowTest.class)
	public void testElo() {
		int elo1 = 1000;
		String move1 = processPosition("r1b3k1/6p1/P1n1pr1p/q1p5/1b1P4/2N2N2/PP1QBPPP/R3K2R b");
		if ("f6f3".equals(move1)) {
			elo1 = 2600;
		}
		if ("c5d4".equals(move1)) {
			elo1 = 1900;
		}
		if ("c6d4".equals(move1)) {
			elo1 = 1900;
		}
		if ("b4c3".equals(move1)) {
			elo1 = 1400;
		}
		if ("c8a6".equals(move1)) {
			elo1 = 1500;
		}
		if ("f6g6".equals(move1)) {
			elo1 = 1400;
		}
		if ("e6e5".equals(move1)) {
			elo1 = 1200;
		}
		if ("c8d7".equals(move1)) {
			elo1 = 1600;
		}
		System.out.println(move1 + " Elo1 = " + elo1);

		int elo2 = 1000;
		String move2 = processPosition("2nq1nk1/5p1p/4p1pQ/pb1pP1NP/1p1P2P1/1P4N1/P4PB1/6K1 w");
		if ("g2e4".equals(move2)) {
			elo2 = 2600;
		}
		if ("g5h7".equals(move2)) {
			elo2 = 1950;
		}
		if ("h5g6".equals(move2)) {
			elo2 = 1900;
		}
		if ("g2f1".equals(move2)) {
			elo2 = 1400;
		}
		if ("g2d5".equals(move2)) {
			elo2 = 1200;
		}
		if ("f2f4".equals(move2)) {
			elo2 = 1400;
		}
		System.out.println(move2 + " Elo2 = " + elo2);

		int elo3 = 1000;
		String move3 = processPosition("8/3r2p1/pp1Bp1p1/1kP5/1n2K3/6R1/1P3P2/8 w");
		if ("c5c6".equals(move3)) {
			elo3 = 2500;
		}
		if ("g3g6".equals(move3)) {
			elo3 = 2000;
		}
		if ("e4e5".equals(move3)) {
			elo3 = 1900;
		}
		if ("g3g5".equals(move3)) {
			elo3 = 1700;
		}
		if ("e4d4".equals(move3)) {
			elo3 = 1200;
		}
		if ("d6e5".equals(move3)) {
			elo3 = 1200;
		}
		System.out.println(move3 + " Elo3 = " + elo3);

		int elo4 = 1000;
		String move4 = processPosition("8/4kb1p/2p3pP/1pP1P1P1/1P3K2/1B6/8/8 w");
		if ("e5e6".equals(move4)) {
			elo4 = 2500;
		}
		if ("b3f7".equals(move4)) {
			elo4 = 1600;
		}
		if ("b3c2".equals(move4)) {
			elo4 = 1700;
		}
		if ("b3d1".equals(move4)) {
			elo4 = 1800;
		}
		System.out.println(move4 + " Elo4 = " + elo4);

		int elo5 = 1000;
		String move5 = processPosition("b1R2nk1/5ppp/1p3n2/5N2/1b2p3/1P2BP2/q3BQPP/6K1 w");
		if ("e3c5".equals(move5)) {
			elo5 = 2500;
		}
		if ("f5h6".equals(move5)) {
			elo5 = 2100;
		}
		if ("e3h6".equals(move5)) {
			elo5 = 1900;
		}
		if ("f5g7".equals(move5)) {
			elo5 = 1500;
		}
		if ("f2g3".equals(move5)) {
			elo5 = 1750;
		}
		if ("c8f8".equals(move5)) {
			elo5 = 1200;
		}
		if ("f2h4".equals(move5)) {
			elo5 = 1200;
		}
		if ("e3b6".equals(move5)) {
			elo5 = 1750;
		}
		if ("e2c4".equals(move5)) {
			elo5 = 1400;
		}
		System.out.println(move5 + " Elo5 = " + elo5);

		int elo6 = 1000;
		String move6 = processPosition("3rr1k1/pp3pbp/2bp1np1/q3p1B1/2B1P3/2N4P/PPPQ1PP1/3RR1K1 w");
		if ("g5f6".equals(move6)) {
			elo6 = 2500;
		}
		if ("c3d5".equals(move6)) {
			elo6 = 1700;
		}
		if ("c4b5".equals(move6)) {
			elo6 = 1900;
		}
		if ("f2f4".equals(move6)) {
			elo6 = 1700;
		}
		if ("a2a3".equals(move6)) {
			elo6 = 1200;
		}
		if ("e1e3".equals(move6)) {
			elo6 = 1200;
		}
		System.out.println(move6 + " Elo6 = " + elo6);

		int elo7 = 1000;
		String move7 = processPosition("r1b1qrk1/1ppn1pb1/p2p1npp/3Pp3/2P1P2B/2N5/PP1NBPPP/R2Q1RK1 b");
		if ("f6h7".equals(move7)) {
			elo7 = 2500;
		}
		if ("f6e4".equals(move7)) {
			elo7 = 1800;
		}
		if ("g6g5".equals(move7)) {
			elo7 = 1700;
		}
		if ("a6a5".equals(move7)) {
			elo7 = 1700;
		}
		if ("g8h7".equals(move7)) {
			elo7 = 1500;
		}
		System.out.println(move7 + " Elo7 = " + elo7);

		int elo8 = 1000;
		String move8 = processPosition("2R1r3/5k2/pBP1n2p/6p1/8/5P1P/2P3P1/7K w");
		if ("b6d8".equals(move8)) {
			elo8 = 2500;
		}
		if ("c8e8".equals(move8)) {
			elo8 = 1600;
		}
		System.out.println(move8 + " Elo8 = " + elo8);

		int elo9 = 1000;
		String move9 = processPosition("2r2rk1/1p1R1pp1/p3p2p/8/4B3/3QB1P1/q1P3KP/8 w");
		if ("e3d4".equals(move9)) {
			elo9 = 2500;
		}
		if ("e4g6".equals(move9)) {
			elo9 = 1800;
		}
		if ("e4h7".equals(move9)) {
			elo9 = 1800;
		}
		if ("e3h6".equals(move9)) {
			elo9 = 1700;
		}
		if ("d7b7".equals(move9)) {
			elo9 = 1400;
		}
		System.out.println(move9 + " Elo9 = " + elo9);

		int elo10 = 1000;
		String move10 = processPosition("r1bq1rk1/p4ppp/1pnp1n2/2p5/2PPpP2/1NP1P3/P3B1PP/R1BQ1RK1 b");
		if ("d8d7".equals(move10)) {
			elo10 = 2000;
		}
		if ("f6e8".equals(move10)) {
			elo10 = 2000;
		}
		if ("h7h5".equals(move10)) {
			elo10 = 1800;
		}
		if ("c5d4".equals(move10)) {
			elo10 = 1600;
		}
		if ("c8a6".equals(move10)) {
			elo10 = 1800;
		}
		if ("a7a5".equals(move10)) {
			elo10 = 1800;
		}
		if ("f8e8".equals(move10)) {
			elo10 = 1400;
		}
		if ("d6d5".equals(move10)) {
			elo10 = 1500;
		}
		System.out.println(move10 + " Elo10 = " + elo10);

		int elo = (elo1 + elo2 + elo3 + elo4 + elo5 + elo6 + elo7 + elo8 + elo9 + elo10) / 10;
		System.out.println("Calculated Elo = " + elo);

		assertTrue(elo > 2000);
	}

	private String processPosition(String fen) {
		search.getBoard().setFen(fen);
		search.go(SearchParameters.get(5 * 60000)); // five minutes
		String move = Move.toString(search.getBestMove());
		System.out.println("result = " + move);
		return move;
	}
}