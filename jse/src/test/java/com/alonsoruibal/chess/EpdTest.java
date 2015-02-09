package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Estimate ELO with BS2850, BT2450, BT2630 test suites:
 * <p/>
 * Think 15 minutes in each position, if best move is found, time is the first time best move is seen
 * If best move is changed during thinking time, and then newly found, the time to figure is the last
 * one.
 * <p/>
 * If best move is not found, time for the test is 15 minutes.
 * <p/>
 * ELO is calculated with Total Time (TT) in minutes
 * <p/>
 * ELO = 2830 - (TT / 1.5) - (TT * TT) / (22 * 22)
 * <p/>
 * 2450/2630
 * Each test suite contain 30 positions. Select each position think for 15 minutes (900 seconds).
 * If a position is solved, write down its solution time in seconds. It doesn't count as a solution if finds
 * the move and then changes its mind. If after finding a move, then changing its mind, then finding it again,
 * you should use the last time found. Any solution that is not found, score as 900 seconds.
 * add up all the times, divide by 30 and subtract the result from either 2630 or 2450.
 *
 * @author rui
 */
public class EpdTest {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("EpdTest");

	Config config;
	SearchEngine search;

	int solved;
	int fails;
	int total;
	int totalTime;
	int lctPoints;

	public int getSolved() {
		return solved;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public int getLctPoints() {
		return lctPoints;
	}

	long processEpdFile(InputStream is, int timeLimit) {
		config = new Config();
		config.setBook(new FileBook("/book_small.bin"));
		search = new SearchEngine(config);

		totalTime = 0;
		lctPoints = 0;
		solved = 0;
		total = 0;
		StringBuilder notSolved = new StringBuilder();
		// goes through all positions
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				logger.debug("Test = " + line);
				// TODO use strtok
				int i0 = line.indexOf(" am ");
				int i1 = line.indexOf(" bm ");
				if (i0 < 0 || i1 < i0) {
					i0 = i1;
				}

				int i2 = line.indexOf(";", i1 + 4);
				int timeSolved = testPosition(line.substring(0, i0), line.substring(i1 + 4, i2), timeLimit);
				totalTime += timeSolved;
				
				/* 
				 *    * 30 points, if solution is found between 0 and 9 seconds
				 *    * 25 points, if solution is found between 10 and 29 seconds
				 *    * 20 points, if solution is found between 30 and 89 seconds
				 *    * 15 points, if solution is found between 90 and 209 seconds
				 *    * 10 points, if solution is found between 210 and 389 seconds
				 *    * 5 points, if solution is found between 390 and 600 seconds
				 *    * 0 points, if not found with in 10 minutes
				 */
				if (timeSolved < timeLimit) {
					if (0 <= timeSolved && timeSolved < 10000) {
						lctPoints += 30;
					} else if (10000 <= timeSolved && timeSolved < 30000) {
						lctPoints += 25;
					} else if (30000 <= timeSolved && timeSolved < 90000) {
						lctPoints += 20;
					} else if (90000 <= timeSolved && timeSolved < 210000) {
						lctPoints += 15;
					} else if (210000 <= timeSolved && timeSolved < 390000) {
						lctPoints += 10;
					} else if (390000 <= timeSolved && timeSolved < 600000) {
						lctPoints += 5;
					}
				} else {
					notSolved.append(line);
					notSolved.append("\n");
				}

				total++;
				if (timeSolved < timeLimit) {
					solved++;
				}
				logger.debug("Status: " + solved + " positions solved of " + total + " in " + totalTime + "Ms (lctPoints=" + lctPoints + ")");
				logger.debug("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fails = total - solved;
		logger.debug("***** Positions not Solved:");
		logger.debug(notSolved.toString());
		logger.debug("***** Result:" + solved + " positions solved of " + total + " in " + totalTime + "Ms (" + fails + " fails)");
		return totalTime;
	}

	private int testPosition(String fen, String movesString, int timeLimit) {
		int time = 0;
		search.clear();
		search.getBoard().setFen(fen);
		String movesStringArray[] = movesString.split(" ");
		int moves[] = new int[movesStringArray.length];
		for (int i = 0; i < moves.length; i++) {
			moves[i] = Move.getFromString(search.getBoard(), movesStringArray[i], true);
		}
		logger.debug("Lets see if " + movesString + " are found");

		search.go(SearchParameters.get(timeLimit));

		boolean found = false;

		for (int move : moves) {
			if (move == search.getBestMove()) {
				logger.debug("Best move found in " + search.getBestMoveTime() + "Ms :D " + Move.toStringExt(move));
				time += search.getBestMoveTime();
				found = true;
			}
		}
		if (!found) {
			logger.debug("Best move not found :( " + Move.toStringExt(search.getBestMove()) + " != " + movesString);
			return timeLimit;
		}

		return time;
	}
}