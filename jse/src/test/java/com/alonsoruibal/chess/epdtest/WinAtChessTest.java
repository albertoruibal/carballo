package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WinAtChessTest extends EpdTest {
	@Test
	@Tag("slow")
	void testWinAtChess() {
		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 1000);
		assertTrue(fails <= 16);
	}
}