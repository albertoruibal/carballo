package com.alonsoruibal.chess;

import com.alonsoruibal.chess.log.Logger;

public class WinAtChessTest extends EpdTest {
	private static final Logger logger = Logger.getLogger("WinAtChessTest");

	public void testWinAtChess() {

		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 5000);
		int solved1 = getSolved();

//		search.getConfig().setExtensionsPassedPawn(0);
//		search.getConfig().setExtensionsPawnPush(2);
//		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 5000);
//		int solved2 = getSolved();

//		search.getConfig().setExtensionsPassedPawn(2);
//		search.getConfig().setExtensionsPawnPush(0);
//		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 1000);
//		int solved3 = getSolved();
//
//		search.getConfig().setExtensionsPassedPawn(0);
//		search.getConfig().setExtensionsPawnPush(2);
//		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 1000);
//		int solved4 = getSolved();
//
//		search.getConfig().setExtensionsPassedPawn(2);
//		search.getConfig().setExtensionsPawnPush(2);
//		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 1000);
//		int solved5 = getSolved();

//		processEpdFile(this.getClass().getResourceAsStream("/wacnew.epd"), 5000);
//		int solved6 = getSolved();

		logger.debug("Config1 = " + solved1);
//		logger.debug("Config2 = " + solved2);
//		logger.debug("Config3 = " + solved3);
//		logger.debug("Config4 = " + solved4);
//		logger.debug("Config5 = " + solved5);
//		logger.debug("Config6 = " + solved6);
		assertTrue(solved1 >= 280);
	}
}