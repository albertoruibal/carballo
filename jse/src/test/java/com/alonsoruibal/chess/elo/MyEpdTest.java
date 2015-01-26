package com.alonsoruibal.chess.elo;

import com.alonsoruibal.chess.elo.EpdTest;

public class MyEpdTest extends EpdTest {

	public void setUp() throws Exception {
		super.setUp();
	}

	public void testMyEpd() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/my.epd"), 15 * 60 * 1000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
	}
}