package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class MyEpdTest extends EpdTest {

	@Test
	@Tag("slow")
	void testMyEpd() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/my.epd"), 15 * 60 * 1000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
	}
}