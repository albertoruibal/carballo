package com.alonsoruibal.chess.elo;

import com.alonsoruibal.chess.elo.EpdTest;

public class ECMGCPTest extends EpdTest {

	public void testMyEpd() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/ecmgcp.epd"), 10000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
	}
}