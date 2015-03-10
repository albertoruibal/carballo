package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.SlowTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class WCSACTest extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testWinningChessSacrificesAndCombinations() {
		processEpdFile(this.getClass().getResourceAsStream("/wcsac.epd"), 5000);
	}
}