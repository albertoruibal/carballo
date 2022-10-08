package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ArasanTest extends EpdTest {

	@Test
	@Tag("slow")
	public void testArasan() {
		processEpdFile(this.getClass().getResourceAsStream("/arasan.epd"), 60000);
	}
}