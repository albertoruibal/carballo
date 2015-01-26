package com.alonsoruibal.chess.elo;

public class EloTest extends EpdTest {

	public void testBS2830() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bs2830.epd"), 15 * 60000);
		double timeMinutes = time / 60000;
		double elo = 2850 - (timeMinutes / 1.5) - (timeMinutes * timeMinutes) / (22 * 22);
		System.out.println("BS2830 Elo = " + elo);
	}

	public void testBT2450() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bt2450.epd"), 15 * 60000);
		double timeSeconds = time / 1000;
		double elo = 2450 - (timeSeconds / 30);
		System.out.println("BT2450 Elo = " + elo);
	}

	public void testBT2630() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bt2630.epd"), 15 * 60000);
		double timeSeconds = time / 1000;
		double elo = 2630 - (timeSeconds / 30);
		System.out.println("BT2630 Elo = " + elo);
	}

	public void testLCTII() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/lctii.epd"), 10 * 60000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
		System.out.println("LCTII ELO = " + (1900 + getLctPoints()));
	}

}