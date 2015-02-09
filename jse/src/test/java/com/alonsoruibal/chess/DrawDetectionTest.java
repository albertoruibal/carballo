package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DrawDetectionTest {

	@Test
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

	@Test
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