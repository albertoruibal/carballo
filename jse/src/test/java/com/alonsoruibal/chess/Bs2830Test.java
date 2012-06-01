package com.alonsoruibal.chess;

public class Bs2830Test extends EpdTest {

	public void testBS2830() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bs2830.epd"), 15 * 60000);
		double timeMinutes = time / 60000;
		double elo = 2850 - (timeMinutes / 1.5) - (timeMinutes * timeMinutes) / (22 * 22);
		System.out.println("BS2830 Elo = " + elo);
	}
}