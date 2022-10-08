package com.alonsoruibal.chess;

import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.pgn.PgnFile;
import com.alonsoruibal.chess.pgn.PgnImportExport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ProblemsPgnTest {
	private static final Logger logger = Logger.getLogger("ProblemsPgnTest");

	void processPgnFile(String file, int count) {

		Board board = new Board();

		for (int gameNo = 0; gameNo < count; gameNo++) {
			String problemPgn = PgnFile.getGameNumber(this.getClass().getResourceAsStream(file), gameNo);
			logger.debug("Problem\n" + problemPgn);
			PgnImportExport.setBoard(board, problemPgn);
		}
	}

	@Test
	@Tag("slow")
	void testProblems() {
		try {
			processPgnFile("/problems_easy.pgn", 311);
			processPgnFile("/problems_medium.pgn", 928);
			processPgnFile("/problems_hard.pgn", 1666);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}