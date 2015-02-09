package com.alonsoruibal.chess;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MyEpdTest extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testMyEpd() {
		long time = processEpdFile(this.getClass().getResourceAsStream("/my.epd"), 10 * 1000);
		double timeSeconds = time / 1000;
		System.out.println("time in seconds = " + timeSeconds);
	}
}