package com.alonsoruibal.chess;

import junit.framework.TestCase;

import com.alonsoruibal.chess.log.Logger;

/**
 * @author rui
 */
public class ProblemsPgnTest extends TestCase {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("ProblemsPgnTest");	
		
   public void processPgnFile(String file, int count) {
	   
	   Pgn pgn = new Pgn();
	   Board board = new Board();
	   
	   for (int gameNo = 0; gameNo < count; gameNo++) {  
		   String problemPgn = pgn.getGameNumber(this.getClass().getResourceAsStream(file), gameNo);
		   logger.debug("Problem\n" + problemPgn);
		   pgn.setBoard(board, problemPgn);
	   }
   }
   
   public void testProblems() {
	   try {
		   processPgnFile("/problems_easy.pgn", 311);
		   processPgnFile("/problems_medium.pgn", 928);
		   processPgnFile("/problems_hard.pgn", 1666);
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
   }
}