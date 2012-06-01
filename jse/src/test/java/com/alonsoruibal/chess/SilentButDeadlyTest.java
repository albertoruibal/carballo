package com.alonsoruibal.chess;

/**

 * @author rui
 */
public class SilentButDeadlyTest extends EpdTest {

	public void testMyEpd() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/silentbutdeadly.epd"), 5000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
	}
}