package com.alonsoruibal.chess;

import com.alonsoruibal.chess.pgn.PgnFile;
import com.alonsoruibal.chess.pgn.PgnImportExport;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;


public class DrawDetectionTest {

	@Test
	public void test3FoldDraw() {
        Board b = new Board();

		InputStream is = this.getClass().getResourceAsStream("/draw.pgn");
		String pgnGame = PgnFile.getGameNumber(is, 0);
        PgnImportExport.setBoard(b, pgnGame);

        System.out.println(b.toString());
        System.out.println("draw = " + b.isDraw());

        assertTrue(b.isDraw());
    }

	@Test
	public void test3FoldDrawNo() {
        Board b = new Board();

		InputStream is = this.getClass().getResourceAsStream("/draw.pgn");
		String pgnGame = PgnFile.getGameNumber(is, 0);
        PgnImportExport.setBoard(b, pgnGame);

        b.undoMove();

        System.out.println(b.toString());
        System.out.println("draw = " + b.isDraw());

        assertFalse(b.isDraw());
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