package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class BT2450Test extends EpdTest {

	@Test
	@Tag("slow")
	void testBT2450() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bt2450.epd"), 15 * 60000);
		double timeSeconds = time / 1000;
		double elo = 2450 - (timeSeconds / 30);
		System.out.println("BT2450 Elo = " + elo);
	}
}