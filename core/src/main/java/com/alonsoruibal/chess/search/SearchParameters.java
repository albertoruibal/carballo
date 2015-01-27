package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.Board;
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
	int depth;
	// Search only this number of nodes
	int nodes;
	// seatch for mate in mate moves
	int mate;
	// search movetime seconds
	int moveTime;
	// think infinite
	boolean infinite;

	boolean ponder;

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
	 * TODO elaborate a bit
	 *
	 * @return
	 */
	public long calculateMoveTime(Board board) {
		if (infinite) {
			return 999999999;
		}
		if (moveTime != 0) {
			return moveTime;
		}

		int calctime = 0;
		if (board.getTurn()) {
			if (wtime > 0) {
				calctime = wtime / 40 + winc;
			}
		} else {
			if (btime > 0) {
				calctime = btime / 40 + binc;
			}
		}
		logger.debug("Thinking for " + calctime + "Ms");
		return calctime;
	}

	public static SearchParameters get(int moveTime) {
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setMoveTime(moveTime);
		return searchParameters;
	}
}
