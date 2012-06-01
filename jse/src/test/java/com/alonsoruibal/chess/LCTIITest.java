package com.alonsoruibal.chess;

/**
 * TODO
 * 
 * Points per position
 *
 *    * 30 points, if solution is found between 0 and 9 seconds
 *    * 25 points, if solution is found between 10 and 29 seconds
 *    * 20 points, if solution is found between 30 and 89 seconds
 *    * 15 points, if solution is found between 90 and 209 seconds
 *    * 10 points, if solution is found between 210 and 389 seconds
 *    * 5 points, if solution is found between 390 and 600 seconds
 *    * 0 points, if not found with in 10 minutes
 *    * Minimum = 1900
 *    * Maximum = 1900 + 30*35 = 2950
 * 
 * @author rui
 */
public class LCTIITest extends EpdTest {

	public void testMyEpd() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/lctii.epd"), 10 * 60000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
		System.out.println("LCTII ELO = " + (1900 + getLctPoints()));
	}
}