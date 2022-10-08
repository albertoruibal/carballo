package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class SilentButDeadlyTest extends EpdTest {

	@Test
	@Tag("slow")
	void testSilentButDeadly() {
		processEpdFile(this.getClass().getResourceAsStream("/silentbutdeadly.epd"), 1000);
	}
}