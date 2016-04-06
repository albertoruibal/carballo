package com.alonsoruibal.chess.tt;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchEngine;

import java.util.Arrays;

/**
 * Transposition table using two keys and multiprobe
 * <p/>
 * Uses part of the board's zobrist key (shifted) as the index
 *
 * @author rui
 */
public class TranspositionTable {
	private static final Logger logger = Logger.getLogger("MultiprobeTranspositionTable");

	public static final int DEPTH_QS_CHECKS = 0;
	public static final int DEPTH_QS_NO_CHECKS = -1;

	public static final int TYPE_EVAL = 0;
	public static final int TYPE_EXACT_SCORE = 1;
	public static final int TYPE_FAIL_LOW = 2;
	public static final int TYPE_FAIL_HIGH = 3;

	private static final int MAX_PROBES = 4;

	public long[] keys;
	public long[] infos;
	public short[] evals;

	private int size;
	private long info;
	private short eval;
	private int generation;
	private int entriesOccupied;

	private int score;
	private int sizeBits;

	public TranspositionTable(int sizeMb) {
		sizeBits = BitboardUtils.square2Index(sizeMb) + 16;
		size = 1 << sizeBits;
		keys = new long[size];
		infos = new long[size];
		evals = new short[size];
		entriesOccupied = 0;

		generation = 0;
		logger.debug("Created transposition table, size = " + size + " slots " + size * 18.0 / (1024 * 1024) + " MBytes");
	}

	public void clear() {
		entriesOccupied = 0;
		Arrays.fill(keys, 0);
	}

	public boolean search(Board board, int distanceToInitialPly, boolean exclusion) {
		info = 0;
		score = 0;
		int startIndex = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64 - sizeBits));
		// Verifies that it is really this board
		for (int i = startIndex; i < startIndex + MAX_PROBES && i < size; i++) {
			if (keys[i] == board.getKey2()) {
				info = infos[i];
				eval = evals[i];
				score = (short) ((info >>> 48) & 0xffff);

				// Fix mate score with the real distance to the initial PLY
				if (score >= SearchEngine.VALUE_IS_MATE) {
					score -= distanceToInitialPly;
				} else if (score <= -SearchEngine.VALUE_IS_MATE) {
					score += distanceToInitialPly;
				}
				return true;
			}
		}
		return false;
	}

	public int getBestMove() {
		return (int) (info & 0x1fffff);
	}

	public int getNodeType() {
		return (int) ((info >>> 21) & 0xf);
	}

	public int getGeneration() {
		return (int) ((info >>> 32) & 0xff);
	}

	public int getDepthAnalyzed() {
		int depthAnalyzed = (int) (info >>> 40) & 0xff;
		return depthAnalyzed == 0xff ? -1 : depthAnalyzed;
	}

	public int getScore() {
		return score;
	}

	public int getEval() {
		return eval;
	}

	public void newGeneration() {
		generation = (generation + 1) & 0xff;
	}

	public boolean isMyGeneration() {
		return getGeneration() == generation;
	}

	public void save(Board board, int distanceToInitialPly, int depthAnalyzed, int bestMove, int score, int lowerBound, int upperBound, int eval, boolean exclusion) {
		// Fix mate score with the real distance to mate from the current PLY, not from the initial PLY
		int fixedScore = score;
		if (score >= SearchEngine.VALUE_IS_MATE) {
			fixedScore += distanceToInitialPly;
		} else if (score <= -SearchEngine.VALUE_IS_MATE) {
			fixedScore -= distanceToInitialPly;
		}

		assert fixedScore >= -Evaluator.MATE && fixedScore <= Evaluator.MATE : "Fixed TT score is outside limits";
		assert Math.abs(eval) < SearchEngine.VALUE_IS_MATE || Math.abs(eval) == Evaluator.MATE || eval == Evaluator.NO_VALUE : "Storing a eval value in the TT outside limits";

		if (score <= lowerBound) {
			set(board, TYPE_FAIL_LOW, bestMove, fixedScore, depthAnalyzed, eval, exclusion);
		} else if (score >= upperBound) {
			set(board, TYPE_FAIL_HIGH, bestMove, fixedScore, depthAnalyzed, eval, exclusion);
		} else {
			set(board, TYPE_EXACT_SCORE, bestMove, fixedScore, depthAnalyzed, eval, exclusion);
		}
	}

	public void set(Board board, int nodeType, int bestMove, int score, int depthAnalyzed, int eval, boolean exclusion) {
		long key2 = board.getKey2();
		int startIndex = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64 - sizeBits));
		int replaceIndex = startIndex;
		int replaceImportance = Integer.MAX_VALUE; // A higher value, so the first entry will be the default

		for (int i = startIndex; i < startIndex + MAX_PROBES && i < size; i++) {
			info = infos[i];

			if (keys[i] == 0) { // Replace an empty TT position
				entriesOccupied++;
				replaceIndex = i;
				break;
			} else if (keys[i] == key2) { // Replace the same position
				replaceIndex = i;
				if (bestMove == Move.NONE) { // Keep previous best move
					bestMove = getBestMove();
				}
				break;
			}

			// Calculates a value with this TT entry importance
			int entryImportance = (getNodeType() == TYPE_EXACT_SCORE ? 10 : 0) // Bonus for the PV entries
					- getGenerationDelta() // The older the generation, the less importance
					+ getDepthAnalyzed(); // The more depth, the more importance

			// We will replace the less important entry
			if (entryImportance < replaceImportance) {
				replaceImportance = entryImportance;
				replaceIndex = i;
			}
		}

		keys[replaceIndex] = key2;
		info = (bestMove & 0x1fffff) | ((nodeType & 0xf) << 21) | (((long) (generation & 0xff)) << 32) | (((long) (depthAnalyzed & 0xff)) << 40)
				| (((long) (score & 0xffff)) << 48);

		infos[replaceIndex] = info;
		evals[replaceIndex] = (short) eval;
	}

	/**
	 * Returns the difference between the current generation and the entry generation (max 255)
	 */
	private int getGenerationDelta() {
		byte entryGeneration = (byte) ((info >>> 32) & 0xff);
		return (generation >= entryGeneration ? generation - entryGeneration : 256 + generation - entryGeneration);
	}

	public int getHashFull() {
		return (int) (1000L * entriesOccupied / size);
	}
}