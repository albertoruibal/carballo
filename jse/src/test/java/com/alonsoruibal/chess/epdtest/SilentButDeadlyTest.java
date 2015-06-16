package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.SlowTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class SilentButDeadlyTest extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testSilentButDeadly() {
		processEpdFile(this.getClass().getResourceAsStream("/silentbutdeadly.epd"), 1000);
	}
}