package com.alonsoruibal.chess;

import com.alonsoruibal.chess.log.Logger;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ArasanTest extends EpdTest {
	private static final Logger logger = Logger.getLogger("WinAtChessTest");

	@Test
	@Category(SlowTest.class)
	public void testArasan() {
		processEpdFile(this.getClass().getResourceAsStream("/arasan.epd"), 5000);
	}
}