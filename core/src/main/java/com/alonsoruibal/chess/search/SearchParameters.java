package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.log.Logger;

public class SearchParameters {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("SearchParameters");

	// UCI parameters
	// Remaining time
	int wtime, btime;
	// Time increment per move
	int winc, binc;
	// Moves to the next time control
	int movesToGo;
	// Analize x plyes only
	int depth = Integer.MAX_VALUE;
	// Search only this number of nodes
	int nodes = Integer.MAX_VALUE;
	// Search for mate in mate moves
	int mate;
	// Search movetime milliseconds
	int moveTime = Integer.MAX_VALUE;
	// Think infinite
	boolean infinite;
	boolean ponder;

	boolean manageTime;

	public boolean isPonder() {
		return ponder;
	}

	public void setPonder(boolean ponder) {
		this.ponder = ponder;
	}

	public int getWtime() {
		return wtime;
	}

	public void setWtime(int wtime) {
		this.wtime = wtime;
	}

	public int getBtime() {
		return btime;
	}

	public void setBtime(int btime) {
		this.btime = btime;
	}

	public int getWinc() {
		return winc;
	}

	public void setWinc(int winc) {
		this.winc = winc;
	}

	public int getBinc() {
		return binc;
	}

	public void setBinc(int binc) {
		this.binc = binc;
	}

	public int getMovesToGo() {
		return movesToGo;
	}

	public void setMovesToGo(int movesToGo) {
		this.movesToGo = movesToGo;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getNodes() {
		return nodes;
	}

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	public int getMate() {
		return mate;
	}

	public void setMate(int mate) {
		this.mate = mate;
	}

	public int getMoveTime() {
		return moveTime;
	}

	public void setMoveTime(int moveTime) {
		this.moveTime = moveTime;
	}

	public boolean isInfinite() {
		return infinite;
	}

	public void setInfinite(boolean infinite) {
		this.infinite = infinite;
	}

	/**
	 * Used to detect if it can add more time in case of panic or apply other heuristics to reduce time
	 *
	 * @return true if the engine is responsible of managing the remaining time
	 */
	public boolean manageTime() {
		return manageTime;
	}

	/**
	 * Time management routine
	 * @param panicTime is set to true when the score fails low in the root node by 100
	 *
	 * @return the time to think, or Long.MAX_VALUE if it can think an infinite time
	 */
	public long calculateMoveTime(boolean engineIsWhite, long startTime, boolean panicTime) {
		manageTime = false;
		if (ponder || infinite || depth < Integer.MAX_VALUE || nodes < Integer.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		if (moveTime != Integer.MAX_VALUE) {
			return startTime + moveTime;
		}
		manageTime = true;

		int calcTime = 0;
		int timeAvailable = engineIsWhite ? wtime : btime;
		int timeInc = engineIsWhite ? winc : binc;
		if (timeAvailable > 0) {
			calcTime = timeAvailable / 25;
		}
		if (panicTime) { // x 4
			calcTime = calcTime << 2;
		}
		calcTime = Math.min(calcTime, timeAvailable >>> 3); // Never consume more than time / 8
		calcTime += timeInc;

		logger.debug("Thinking for " + calcTime + "Ms");
		return startTime + calcTime;
	}

	public static SearchParameters get(int moveTime) {
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setMoveTime(moveTime);
		return searchParameters;
	}
}
