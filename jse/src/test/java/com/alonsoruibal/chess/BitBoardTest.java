package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitBoardTest {

	@Test
	public void testHorizontalLine() {
		assertEquals((1 << 5) | (1 << 6) | (1 << 7), BitboardUtils.getHorizontalLine((1 << 5), (1 << 7)));
		assertEquals((1 << 5) | (1 << 6) | (1 << 7), BitboardUtils.getHorizontalLine((1 << 7), (1 << 5)));
	}
}
