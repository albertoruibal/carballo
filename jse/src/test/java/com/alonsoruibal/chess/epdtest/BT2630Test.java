package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.SlowTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class BT2630Test extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testBT2630() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bt2630.epd"), 15 * 60000);
		double timeSeconds = time / 1000;
		double elo = 2630 - (timeSeconds / 30);
		System.out.println("BT2630 Elo = " + elo);
	}
}