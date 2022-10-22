package com.alonsoruibal.chess.tuning;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.TunedEvaluator;
import com.alonsoruibal.chess.pgn.*;
import com.alonsoruibal.chess.search.SearchEngine;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

class PositionsGenerator {
	int gameCount = 0;

	private AttacksInfo attacksInfo = new AttacksInfo();
	private SearchEngine engine = new SearchEngine(new Config());

	void processPgnFile(String fileIn, String fileOut) throws Exception {
		System.out.println("Loading positions from games...");

		FileInputStream fis = new FileInputStream(fileIn);
		FileWriter fw = new FileWriter(fileOut);

		PgnFile.readGames(fis, pgnString -> {
			gameCount++;
			if ((gameCount % 1000) == 0) {
				System.out.println("games = " + gameCount);
				try {
					fw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Game game = PgnParser.parsePgn(pgnString);
			if (game == null) {
				return false;
			}

			engine.getBoard().startPosition();
			GameNodeVariation pv = game.getPv();

			double Ri = "1-0".equals(game.getResult()) ? 1 :
					"0-1".equals(game.getResult()) ? 0 :
							0.5;

			for (int i = 0; i < pv.size(); i++) {
				if (!(pv.get(i) instanceof GameNodeMove)) {
					continue;
				}
				String moveStr = ((GameNodeMove) pv.get(i)).move;
				int move = Move.getFromString(engine.getBoard(), moveStr, true);
				engine.getBoard().doMove(move);

				// Discard book moves and moves when a mate is found
				String comment = "";
				if (i + 1 < pv.size() && pv.get(i + 1) instanceof GameNodeComment) {
					comment = ((GameNodeComment) pv.get(i + 1)).comment;
				}
				if ("book".equals(comment) || comment.startsWith("+M") || comment.startsWith("-M")) {
					continue;
				}

				if (engine.getBoard().getCheck()) {
					continue;
				}

				engine.getTT().clear();
				engine.setInitialPly();
				int qsScore = (engine.getBoard().getTurn() ? 1 : -1) * engine.quiescentSearch(0, -TunedEvaluator.KNOWN_WIN, TunedEvaluator.KNOWN_WIN);
				int evalScore = engine.getEvaluator().evaluate(engine.getBoard(), attacksInfo);
				if (qsScore != evalScore) {
					continue;
				}

				try {
					fw.write(engine.getBoard().getFen() + ";" + Ri + "\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		});
		fw.close();
	}


	@Test
	@Tag("slow")
	void createFileWithPositions() throws Exception {
		processPgnFile(TuningConstants.GAMES_FILE, TuningConstants.POSITIONS_FILE);
	}
}