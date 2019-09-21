package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class LTCIITest extends EpdTest {

	@Test
	@Tag("slow")
	void testLCTII() {
		processEpdFile(this.getClass().getResourceAsStream("/lctii.epd"), 10 * 60000);
		System.out.println("LCTII ELO = " + (1900 + getLctPoints()));
	}
}