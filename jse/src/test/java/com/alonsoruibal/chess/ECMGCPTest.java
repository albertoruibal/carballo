package com.alonsoruibal.chess;

/**

 * @author rui
 */
public class ECMGCPTest extends EpdTest {

	public void testMyEpd() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/ecmgcp.epd"), 10000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
	}
}