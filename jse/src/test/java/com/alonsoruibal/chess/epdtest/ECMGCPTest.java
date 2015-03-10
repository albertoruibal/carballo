package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.SlowTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ECMGCPTest extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testECMGCP() {
		processEpdFile(this.getClass().getResourceAsStream("/ecmgcp.epd"), 10000);
	}
}