package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.SlowTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class LTCIITest extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testLCTII() {
		processEpdFile(this.getClass().getResourceAsStream("/lctii.epd"), 10 * 60000);
		System.out.println("LCTII ELO = " + (1900 + getLctPoints()));
	}
}