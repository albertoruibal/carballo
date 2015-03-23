package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitBoardTest {

	@Test
	public void testHorizontalLine() {
		assertEquals((1L << 7) | (1L << 6) | (1L << 5), BitboardUtils.getHorizontalLine((1L << 7), (1L << 5)));
		assertEquals((1L << 63) | (1L << 62) | (1L << 61) | (1L << 60), BitboardUtils.getHorizontalLine((1L << 63), (1L << 60)));
	}
}
