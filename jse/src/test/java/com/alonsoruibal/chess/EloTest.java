package com.alonsoruibal.chess;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class EloTest extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testBS2830() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bs2830.epd"), 15 * 60000);
		double timeMinutes = time / 60000;
		double elo = 2850 - (timeMinutes / 1.5) - (timeMinutes * timeMinutes) / (22 * 22);
		System.out.println("BS2830 Elo = " + elo);
	}

	@Test
	@Category(SlowTest.class)
	public void testBT2450() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bt2450.epd"), 15 * 60000);
		double timeSeconds = time / 1000;
		double elo = 2450 - (timeSeconds / 30);
		System.out.println("BT2450 Elo = " + elo);
	}

	@Test
	@Category(SlowTest.class)
	public void testLCTII() {
		processEpdFile(this.getClass().getResourceAsStream("/lctii.epd"), 10 * 60000);
		System.out.println("LCTII ELO = " + (1900 + getLctPoints()));
	}

	@Test
	@Category(SlowTest.class)
	public void testWinningChessSacrificesAndCombinations() {
		processEpdFile(this.getClass().getResourceAsStream("/wcsac.epd"), 5000);
	}

	@Test
	@Category(SlowTest.class)
	public void testECMGCP() {
		processEpdFile(this.getClass().getResourceAsStream("/ecmgcp.epd"), 10000);
	}
}