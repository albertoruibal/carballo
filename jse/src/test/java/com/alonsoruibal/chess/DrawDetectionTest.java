package com.alonsoruibal.chess;

import com.alonsoruibal.chess.search.SearchEngine;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
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

	@Test
	public void testDrawDetection() {
		Board b = new Board();
		b.setFen("7k/8/8/8/8/8/8/7K w - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/8/8/8/8/8/6BK b - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/8/8/8/8/8/6NK b - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/nn6/8/8/8/8/8K b - - 0 0");
		assertEquals(b.isDraw(), true);
		b.setFen("7k/8/Nn6/8/8/8/8/8K b - - 0 0");
		assertEquals(b.isDraw(), false);
		b.setFen("7k/7p/8/8/8/8/8/6NK b - - 0 0");
		assertEquals(b.isDraw(), false);
	}

	@Test
	public void testKBbkDraw() {
		Board b = new Board();
		// Different bishop color is NOT draw
		b.setFen("6bk/8/8/8/8/8/8/6BK b - - 0 0");
		assertEquals(b.isDraw(), false);
		// Both bishops in the same color is draw
		b.setFen("6bk/8/8/8/8/8/8/5B1K b - - 0 0");
		assertEquals(b.isDraw(), true);
	}
}