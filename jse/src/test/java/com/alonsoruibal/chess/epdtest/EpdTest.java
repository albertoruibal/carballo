package com.alonsoruibal.chess.epdtest;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.TestColors;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;
import com.alonsoruibal.chess.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
public class EpdTest implements SearchObserver {
	private static final Logger logger = Logger.getLogger("EpdTest");

	SearchEngine search;

	int solved;
	int fails;
	int total;
	int totalTime;
	long totalNodes;
	int lctPoints;

	int avoidMoves[];
	int bestMoves[];
	boolean solutionFound;

	int bestMove;
	int solutionTime;
	long solutionNodes;

	ArrayList<Integer> allSolutionTimes;
	ArrayList<Long> allSolutionNodes;

	public int getSolved() {
		return solved;
	}

	public int getLctPoints() {
		return lctPoints;
	}

	long processEpdFile(InputStream is, int timeLimit) {
		Config config = new Config();
		return processEpdFile(config, is, timeLimit);
	}

	long processEpdFile(Config config, InputStream is, int timeLimit) {
		logger.debug(config);
		search = new SearchEngine(config);
		search.debug = true;
		search.setObserver(this);

		allSolutionTimes = new ArrayList<Integer>();
		allSolutionNodes = new ArrayList<Long>();

		totalTime = 0;
		totalNodes = 0;
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
				String avoidMovesString = null;
				int i0 = line.indexOf(" am ");
				if (i0 >= 0) {
					int i2 = line.indexOf(";", i0 + 4);
					avoidMovesString = line.substring(i0 + 4, i2);
				}
				String bestMovesString = null;
				int i1 = line.indexOf(" bm ");
				if (i1 >= 0) {
					int i2 = line.indexOf(";", i1 + 4);
					bestMovesString = line.substring(i1 + 4, i2);
				}

				int timeSolved = testPosition(line.substring(0, (i0 != -1 ? i0 : i1)), avoidMovesString, bestMovesString, timeLimit);
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
				logger.debug("Status: " + solved + " positions solved of " + total + " in " + totalTime + "Ms and " + totalNodes + " nodes (lctPoints=" + lctPoints + ")");
				logger.debug("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		fails = total - solved;

		logger.debug("TEST    TIME       NODES");
		for (int i = 0; i < allSolutionTimes.size(); i++) {
			logger.debug(StringUtils.padRight(String.valueOf(i + 1), 4) + StringUtils.padLeft(String.valueOf(allSolutionTimes.get(i)), 8) + StringUtils.padLeft(String.valueOf(allSolutionNodes.get(i)), 12));
		}
		logger.debug("***** Positions not Solved:");
		logger.debug(notSolved.toString());
		logger.debug("***** Result:" + solved + " positions solved of " + total + " in " + totalTime + "Ms and " + totalNodes + " nodes (" + fails + " fails)");

		return totalTime;
	}

	private int[] parseMoves(String movesString) {
		if (movesString == null) {
			return new int[0];
		}
		String movesStringArray[] = movesString.split(" ");
		int moves[] = new int[movesStringArray.length];
		for (int i = 0; i < moves.length; i++) {
			moves[i] = Move.getFromString(search.getBoard(), movesStringArray[i], true);
		}
		return moves;
	}

	private int testPosition(String fen, String avoidMovesString, String bestMovesString, int timeLimit) {
		bestMove = 0;
		solutionFound = false;

		search.clear();
		search.getBoard().setFen(fen);
		avoidMoves = parseMoves(avoidMovesString);
		if (avoidMovesString != null) {
			logger.debug("Lets see if " + avoidMovesString + (avoidMoves.length > 1 ? " are " : " is ") + "avoided");
		}
		bestMoves = parseMoves(bestMovesString);
		if (bestMovesString != null) {
			logger.debug("Lets see if " + bestMovesString + (bestMoves.length > 1 ? " are " : " is ") + "found");
		}

		search.go(SearchParameters.get(timeLimit));

		if (solutionFound) {
			logger.debug("Solution found in " + solutionTime + "Ms and " + solutionNodes + " nodes :D " + Move.toStringExt(bestMove));
			totalNodes += solutionNodes;
			allSolutionNodes.add(solutionNodes);
			allSolutionTimes.add(solutionTime);
			return solutionTime;
		} else {
			logger.debug("Solution not found, instead played: " + Move.toStringExt(search.getBestMove()));
			allSolutionNodes.add(search.getNodes());
			allSolutionTimes.add(timeLimit);
			return timeLimit;
		}
	}

	@Override
	public void info(SearchStatusInfo info) {
		if (bestMove != search.getBestMove()) {
			bestMove = search.getBestMove();
			solutionTime = (int) info.getTime();
			solutionNodes = info.getNodes();
		}

		boolean found = bestMoves.length <= 0;
		for (int move : bestMoves) {
			if (move == search.getBestMove()) {
				found = true;
				break;
			}
		}
		for (int move : avoidMoves) {
			if (move == search.getBestMove()) {
				found = false;
				break;
			}
		}
		solutionFound = found;

		if (found) {
			logger.debug(TestColors.ANSI_GREEN + info.toString() + TestColors.ANSI_RESET);
		} else {
			logger.debug(TestColors.ANSI_RED + info.toString() + TestColors.ANSI_RESET);
		}
	}

	@Override
	public void bestMove(int bestMove, int ponder) {

	}
}