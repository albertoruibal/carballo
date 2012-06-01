package com.alonsoruibal.chess;

public class Bt2450Test extends EpdTest {

	public void testBT2450() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/bt2450.epd"), 15 * 60000);
		double timeSeconds = time / 1000;
		double elo = 2450 - (timeSeconds / 30);
		System.out.println("BT2450 Elo = " + elo);
	}
}