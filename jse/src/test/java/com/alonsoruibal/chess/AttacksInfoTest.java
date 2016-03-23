package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AttacksInfoTest {

	@Test
	public void testPinnedBishop() {
		Board b = new Board();
		b.setFen("3k4/3r4/8/3B4/2P5/8/8/3K4 b - - 1 1");
		System.out.println(b);
		AttacksInfo ai = new AttacksInfo();
		ai.build(b);
		System.out.println(BitboardUtils.toString(ai.bishopAttacks[0]));
		assertEquals(0, ai.bishopAttacks[0]);
	}

	@Test
	public void testPinnedRook() {
		Board b = new Board();
		b.setFen("3k4/3r4/8/3R4/2P5/8/8/3K4 b - - 1 1");
		System.out.println(b);
		AttacksInfo ai = new AttacksInfo();
		ai.build(b);
		System.out.println(BitboardUtils.toString(ai.rookAttacks[0]));
		assertEquals(5, BitboardUtils.popCount(ai.rookAttacks[0]));
	}

	@Test
	public void testPinnedPawn() {
		Board b = new Board();
		b.setFen("3k4/8/2b5/3P4/8/8/8/7K b - - 1 1");
		System.out.println(b);
		AttacksInfo ai = new AttacksInfo();
		ai.build(b);
		System.out.println(BitboardUtils.toString(ai.pawnAttacks[0]));
		assertEquals(1, BitboardUtils.popCount(ai.pawnAttacks[0]));
	}
}