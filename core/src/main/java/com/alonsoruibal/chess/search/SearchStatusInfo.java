package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.evaluation.Evaluator;

/**
 * * info
 * the engine wants to send infos to the GUI. This should be done whenever one of the info has changed.
 * The engine can send only selected infos and multiple infos can be send with one info command,
 * e.g. "info currmove e2e4 currmovenumber 1" or
 * "info depth 12 nodes 123456 nps 100000".
 * Also all infos belonging to the pv should be sent together
 * e.g. "info depth 2 score cp 214 time 1242 nodes 2124 nps 34928 pv e2e4 e7e5 g1f3"
 * I suggest to start sending "currmove", "currmovenumber", "currline" and "refutation" only after one second
 * to avoid too much traffic.
 * Additional info:
 * * depth
 * search depth in plies
 * * seldepth
 * selective search depth in plies,
 * if the engine sends seldepth there must also a "depth" be present in the same string.
 * * time
 * the time searched in ms, this should be sent together with the pv.
 * * nodes
 * x nodes searched, the engine should send this info regularly
 * * pv  ...
 * the best line found
 * * multipv
 * this for the multi pv mode.
 * for the best move/pv add "multipv 1" in the string when you send the pv.
 * in k-best mode always send all k variants in k strings together.
 * * score
 * * cp
 * the score from the engine's point of view in centipawns.
 * * mate
 * mate in y moves, not plies.
 * If the engine is getting mated use negativ values for y.
 * * lowerbound
 * the score is just a lower bound.
 * * upperbound
 * the score is just an upper bound.
 * * currmove
 * currently searching this move
 * * currmovenumber
 * currently searching move number x, for the first move x should be 1 not 0.
 * * hashfull
 * the hash is x permill full, the engine should send this info regularly
 * * nps
 * x nodes per second searched, the engine should send this info regularly
 * * tbhits
 * x positions where found in the endgame table bases
 * * cpuload
 * the cpu usage of the engine is x permill.
 * * string
 * any string str which will be displayed be the engine,
 * if there is a string command the rest of the line will be interpreted as .
 * * refutation   ...
 * move  is refuted by the line  ... , i can be any number >= 1.
 * Example: after move d1h5 is searched, the engine can send
 * "info refutation d1h5 g6h5"
 * if g6h5 is the best answer after d1h5 or if g6h5 refutes the move d1h5.
 * if there is norefutation for d1h5 found, the engine should just send
 * "info refutation d1h5"
 * The engine should only send this if the option "UCI_ShowRefutations" is set to true.
 * * currline   ...
 * this is the current line the engine is calculating.  is the number of the cpu if
 * the engine is running on more than one cpu.  = 1,2,3....
 * if the engine is just using one cpu,  can be omitted.
 * If  is greater than 1, always send all k lines in k strings together.
 * The engine should only send this if the option "UCI_ShowCurrLine" is set to true.
 */

public class SearchStatusInfo {

	int depth;
	int selDepth;
	long time = Long.MIN_VALUE;
	long nodes;
	String pv;
	int multiPv;
	int score;
	boolean lowerBound;
	boolean upperBound;
	String currMove;
	int currMoveNumber;
	int hashFull;
	long nps;
	int tbHits;
	int cpuLoad;
	String string;
	String refutation;
	String currLine;

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getSelDepth() {
		return selDepth;
	}

	public void setSelDepth(int selDepth) {
		this.selDepth = selDepth;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getNodes() {
		return nodes;
	}

	public void setNodes(long nodes) {
		this.nodes = nodes;
	}

	public String getPv() {
		return pv;
	}

	public void setPv(String pv) {
		this.pv = pv;
	}

	public int getMultiPv() {
		return multiPv;
	}

	public void setMultiPv(int multiPv) {
		this.multiPv = multiPv;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getCurrMove() {
		return currMove;
	}

	public void setCurrMove(String currMove) {
		this.currMove = currMove;
	}

	public int getCurrMoveNumber() {
		return currMoveNumber;
	}

	public void setCurrMoveNumber(int currMoveNumber) {
		this.currMoveNumber = currMoveNumber;
	}

	public int getHashFull() {
		return hashFull;
	}

	public void setHashFull(int hashFull) {
		this.hashFull = hashFull;
	}

	public long getNps() {
		return nps;
	}

	public void setNps(long nps) {
		this.nps = nps;
	}

	public int getTbHits() {
		return tbHits;
	}

	public void setTbHits(int tbHits) {
		this.tbHits = tbHits;
	}

	public int getCpuLoad() {
		return cpuLoad;
	}

	public void setCpuLoad(int cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public String getRefutation() {
		return refutation;
	}

	public void setRefutation(String refutation) {
		this.refutation = refutation;
	}

	public String getCurrLine() {
		return currLine;
	}

	public void setCurrLine(String currLine) {
		this.currLine = currLine;
	}

	public void setScore(int score, int alpha, int beta) {
		this.score = score;
		upperBound = score <= alpha;
		lowerBound = score >= beta;
	}

	public boolean isMate() {
		return (score < -SearchEngine.VALUE_IS_MATE) || (score > SearchEngine.VALUE_IS_MATE);
	}

	public int getMateIn() {
		int x = (score < 0 ? -Evaluator.MATE : Evaluator.MATE) - score;
		if ((x & 1) != 0) {
			return (x >> 1) + 1;
		} else {
			return x >> 1;
		}
	}

	/**
	 * in UCI format
	 * TODO complete
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (depth != 0) {
			sb.append("depth ");
			sb.append(depth);
		}
		if (selDepth != 0) {
			sb.append(" seldepth ");
			sb.append(selDepth);
		}
		if (isMate()) {
			sb.append(" score mate ");
			sb.append(getMateIn());
		} else {
			sb.append(" score cp ");
			sb.append(score);
		}
		if (lowerBound) {
			sb.append(" lowerbound");
		} else if (upperBound) {
			sb.append(" upperbound");
		}
		if (nodes != 0) {
			sb.append(" nodes ");
			sb.append(nodes);
		}
		if (time != Long.MIN_VALUE) {
			sb.append(" time ");
			sb.append(time);
		}
		if (hashFull != 0) {
			sb.append(" hashfull ");
			sb.append(hashFull);
		}
		if (nps != 0) {
			sb.append(" nps ");
			sb.append(nps);
		}
		if (pv != null) {
			sb.append(" pv ");
			sb.append(pv);
		}
		return sb.toString();
	}
}
