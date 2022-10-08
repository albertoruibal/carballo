package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class BS2830Test extends EpdTest {

	@Test
	@Tag("slow")
	void testBS2830() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bs2830.epd"), 15 * 60000);
		double timeMinutes = time / 60000;
		double elo = 2830 - (timeMinutes / 1.5) - (timeMinutes * timeMinutes) / (22 * 22);
		System.out.println("BS2830 Elo = " + elo);
	}
}