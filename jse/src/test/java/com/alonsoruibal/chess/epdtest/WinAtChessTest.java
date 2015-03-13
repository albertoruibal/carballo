package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.SlowTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

public class WinAtChessTest extends EpdTest {
	@Test
	@Category(SlowTest.class)
	public void testWinAtChess() {
		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 1000);
		assertTrue(fails <= 16);
	}
}