package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;

import junit.framework.TestCase;

import java.io.InputStream;

public class DrawDetectionTest extends TestCase {

	@Override
	protected void setUp() throws Exception {

	}

	public void test3FoldDraw() {
		PgnFile pgn = new PgnFile();
		SearchEngine se = new SearchEngine(new Config());

		InputStream is = this.getClass().getResourceAsStream("/draw.pgn");
		String pgnGame = pgn.getGameNumber(is, 0);
		pgn.setBoard(se.getBoard(), pgnGame);

		System.out.println(se.getBoard().toString());
		System.out.println("draw = " + se.getBoard().isDraw());

		assertTrue(se.getBoard().isDraw());
	}

	public void test3FoldDrawNo() {
		PgnFile pgn = new PgnFile();
		SearchEngine se = new SearchEngine(new Config());

		InputStream is = this.getClass().getResourceAsStream("/draw.pgn");
		String pgnGame = pgn.getGameNumber(is, 0);
		pgn.setBoard(se.getBoard(), pgnGame);

		se.getBoard().undoMove();

		System.out.println(se.getBoard().toString());
		System.out.println("draw = " + se.getBoard().isDraw());

		assertFalse(se.getBoard().isDraw());
	}
}