package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class WCSACTest extends EpdTest {

	@Test
	@Tag("slow")
	void testWinningChessSacrificesAndCombinations() {
		processEpdFile(this.getClass().getResourceAsStream("/wcsac.epd"), 5000);
	}
}