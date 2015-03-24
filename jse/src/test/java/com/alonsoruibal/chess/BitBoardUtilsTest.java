package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitBoardUtilsTest {

	@Test
	public void testHorizontalLine() {
		assertEquals((1L << 7) | (1L << 6) | (1L << 5), BitboardUtils.getHorizontalLine((1L << 7), (1L << 5)));
		assertEquals((1L << 63) | (1L << 62) | (1L << 61) | (1L << 60), BitboardUtils.getHorizontalLine((1L << 63), (1L << 60)));
	}

	@Test
	public void testLsb() {
		assertEquals((1L), BitboardUtils.lsb(1L));
		assertEquals((1L << 63), BitboardUtils.lsb(1L << 63));
		assertEquals((1L << 32), BitboardUtils.lsb((1L << 63) | 1L << 32));
		assertEquals(1L, BitboardUtils.lsb((1L << 32) | 1L));
		assertEquals(0, BitboardUtils.lsb(0));
	}

	@Test
	public void testMsb() {
		assertEquals((1L), BitboardUtils.msb(1L));
		assertEquals((1L << 63), BitboardUtils.msb(1L << 63));
		assertEquals((1L << 63), BitboardUtils.msb((1L << 63) | 1L << 32));
		assertEquals((1L << 32), BitboardUtils.msb((1L << 32) | 1L));
		assertEquals(0, BitboardUtils.msb(0));
	}
}
