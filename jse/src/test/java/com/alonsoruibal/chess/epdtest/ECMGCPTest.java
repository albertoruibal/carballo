package com.alonsoruibal.chess.epdtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ECMGCPTest extends EpdTest {

	@Test
	@Tag("slow")
	void testECMGCP() {
		processEpdFile(this.getClass().getResourceAsStream("/ecmgcp.epd"), 5000);
	}
}