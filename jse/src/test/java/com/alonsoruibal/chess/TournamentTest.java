package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;

/**
 * Test tournament using the Noomen Test Suite
 */
public class TournamentTest extends EpdTest implements SearchObserver {

	//	static final int GAME_TIME_PER_PLAYER = 30 * 1000; // in milliseconds
//	static final int GAME_TIME_PER_PLAYER = 1 * 60 * 1000; // in milliseconds
//	static final int GAME_TIME_PER_PLAYER = 5 * 1000; // in milliseconds
//	static final int GAME_TIME_PER_PLAYER = 3 * 1000; // in milliseconds
	static final int GAME_TIME_PER_PLAYER = 1000; // in milliseconds
	static final int MOVE_TIME_INC = 0; // in milliseconds
	static final int SLEEP = 0; //1000; // Do not touch
	static final int SLEEP2 = 0;
	static final int GAMES = 20 * 60; // Test suite is based on 30 games and they are played with whites and blacks, so we make it four times

	SearchEngine engine1;
	SearchEngine engine2;
	Board b;
	boolean engine1Whites;
	int endGame;
	double[] wins;
	int wtime, btime;
	SearchParameters params;
	long lastTime;

	public void testTournament() {
		Config config1 = new Config();
		config1.setBook(new FileBook("/book_small.bin"));
		config1.setElo(2100);
//		config1.setEvaluator("experimentalnew");
//		config1.setExtensionsSingular(0);
//		setElo(config1, 1800);
		//config1.setAspirationWindowSizes("10");
		//config1.setExtensionsRecapture(0);
		//config.setExtensionsSingular(0);

		Config config2 = new Config();
		config2.setBook(new FileBook("/book_small.bin"));
		config2.setElo(2000);

//		config2.setExtensionsMateThreat(1);
//		config2.setExtensionsRecapture(0);
//		config2.setExtensionsSingular(0);
//		config2.setNullMoveMargin(300);
//		config2.setFutilityMargin(300);
//		config2.setAggressiveFutilityMargin(500);
//		config2.setRazoringMargin(900);

//		config2.setEvaluator("complete");
//		config2.setStaticNullMove(false);
//		config2.setExtensionsSingular(2);
//		setElo(config2, 1700);
		//config2.setContemptFactor(150);
		//config2.setExtensionsSingular(2);
//		config2.setFutility(false);
//		config2.setAggressiveFutility(false);
//		config2.setRazoring(false);
//		config2.setRand(50);
//		config2.setExtensionsCheck(0);
//		config2.setExtensionsMateThreat(0);
//		config2.setEvalAttacks(0);
//		config2.setEvalCenter(0);
//		config2.setEvalKingSafety(0);
//		config2.setEvalMobility(0);
//		config2.setEvalPassedPawns(0);
//		config2.setEvalPawnStructure(0);
//		config2.setEvalPositional(0);

		//config1.setNullMoveMargin(500);
		//config2.setNullMoveMargin(300);

		engine1 = new SearchEngine(config1);
		engine2 = new SearchEngine(config2);

		PgnFile pgn = new PgnFile();
		int pgnGameNumber = 0;

		// wins[0] = draws
		// wins[1] = engine 1 wins
		// wins[2] = engine 2 wins
		wins = new double[3];
		params = new SearchParameters();

		Logger.noLog = true;

		for (int i = 0; i < GAMES; i++) {
			engine1.init();
			engine2.init();

			engine1Whites = (i & 1) == 0;

			b = new Board();

			// Each position is played two times alternating color
			String positionPgn = pgn.getGameNumber(this.getClass().getResourceAsStream("/NoomenTestsuite2008.pgn"), pgnGameNumber >>> 1);
			if (positionPgn == null) {
				pgnGameNumber = 0;
				positionPgn = pgn.getGameNumber(this.getClass().getResourceAsStream("/NoomenTestsuite2008.pgn"), pgnGameNumber >>> 1);
			} else {
				pgnGameNumber++;
			}

//			System.out.println(engine1Whites + " Using pgn:\n" + positionPgn);

			pgn.setBoard(b, positionPgn);
			pgn.setBoard(engine1.getBoard(), positionPgn);
			pgn.setBoard(engine2.getBoard(), positionPgn);

			//b.startPosition();
			//engine1.getBoard().startPosition();
			//engine2.getBoard().startPosition();

			engine1.setObserver(this);
			engine2.setObserver(this);

			wtime = GAME_TIME_PER_PLAYER;
			btime = GAME_TIME_PER_PLAYER;

			endGame = 0;
			go();

			//System.out.println("move "+(j+1)+": " + Move.toStringExt(bestMove));

			while (endGame == 0) {
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			double score = wins[1] + wins[0] / 2;
			double total = wins[1] + wins[0] + wins[2];
			double percentage = (score / total);
			double eloDifference = -400 * Math.log(1 / percentage - 1) / Math.log(10);

			System.out.println("At: " + (i + 1) + " draws: " + wins[0] + "  engine1: " + wins[1] + " engine2: " + wins[2] + " elodif: " + eloDifference + " pointspercentage=" + (percentage * 100));
		}
	}

	private void go() {
		endGame = b.isEndGame();

		// if time is up
		if (wtime < 0) {
			System.out.println("White losses by time");
			endGame = -1;
		} else if (btime < 0) {
			System.out.println("Black losses by time");
			endGame = 1;
		}

		if (endGame == 1) {
			if (engine1Whites) wins[1]++;
			else wins[2]++;
		} else if (endGame == -1) {
			if (engine1Whites) wins[2]++;
			else wins[1]++;
		} else if (endGame == 99) {
			wins[0]++;
		} else {
			params.setWtime(wtime);
			params.setWinc(MOVE_TIME_INC);
			params.setBtime(btime);
			params.setBinc(MOVE_TIME_INC);

			// Delay
			try {
				Thread.sleep(SLEEP2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			lastTime = System.currentTimeMillis();

			if (engine1Whites == b.getTurn()) {
				engine1.go(params);
			} else {
				engine2.go(params);
			}
		}
	}

	public void bestMove(int bestMove, int ponder) {
		long timeThinked = System.currentTimeMillis() - lastTime;
		if (b.getTurn()) {
			wtime -= timeThinked + MOVE_TIME_INC;
		} else {
			btime -= timeThinked + MOVE_TIME_INC;
		}
		b.doMove(bestMove);
		engine1.getBoard().doMove(bestMove);
		engine2.getBoard().doMove(bestMove);
		go();
	}

	public void info(SearchStatusInfo info) {
	}
}