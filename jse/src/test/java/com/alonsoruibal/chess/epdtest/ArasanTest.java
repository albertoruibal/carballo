package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.SlowTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ArasanTest extends EpdTest {

	@Test
	@Category(SlowTest.class)
	public void testArasan() {
		processEpdFile(this.getClass().getResourceAsStream("/arasan.epd"), 60000);
	}
}