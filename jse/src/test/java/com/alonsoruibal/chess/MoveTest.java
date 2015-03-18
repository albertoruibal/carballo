package com.alonsoruibal.chess;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoveTest {

	@Test
	public void toSanTest() {
		Board b = new Board();
		b.setFen("1r4k1/4Bppp/2p5/3p4/3Pq3/4P1Pb/4NP2/3RR1K1 b - - 0 0");
		int m = Move.getFromString(b, "Qg2#", true);
		assertTrue("Move must have the check flag", (m & Move.CHECK_MASK) != 0);
		b.doMove(m);
		assertEquals("Move must not be none", "Qg2#", b.getSanMove(b.getMoveNumber() - 1));
	}
}
