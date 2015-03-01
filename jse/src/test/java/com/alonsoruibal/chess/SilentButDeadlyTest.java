package com.alonsoruibal.chess;

import com.alonsoruibal.chess.log.Logger;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class SilentButDeadlyTest extends EpdTest {
	private static final Logger logger = Logger.getLogger("WinAtChessTest");

	@Test
	@Category(SlowTest.class)
	public void testSilentButDeadly() {
		processEpdFile(this.getClass().getResourceAsStream("/silentbutdeadly.epd"), 5000);
	}
}