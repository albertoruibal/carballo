package com.alonsoruibal.chess.tt;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchEngine;

import java.util.Arrays;

/**
 * Transposition table using two keys and multiprobe
 * <p/>
 * Uses part of the board's zobrish key (shifted) as the index
 *
 * @author rui
 */
public class TranspositionTable {
	private static final Logger logger = Logger.getLogger("MultiprobeTranspositionTable");

	public final static int TYPE_EXACT_SCORE = 1;
	public final static int TYPE_FAIL_LOW = 2;
	public final static int TYPE_FAIL_HIGH = 3;
	public final static int TYPE_EVAL = 4;

	private final static int MAX_PROBES = 4;

	public long[] keys;
	public long[] infos;
	public short[] evals;

	private int index;
	private int size;
	private long info;
	private byte generation;
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
		index = -1;
		logger.debug("Created Multiprobe transposition table, size = " + size + " slots " + size * 18.0 / (1024 * 1024) + " MBytes");
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
		for (index = startIndex; index < startIndex + MAX_PROBES && index < size; index++) {
			if (keys[index] == board.getKey2()) {
				info = infos[index];
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

	public byte getGeneration() {
		return (byte) ((info >>> 32) & 0xff);
	}

	public byte getDepthAnalyzed() {
		return (byte) ((info >>> 40) & 0xff);
	}

	public int getScore() {
		return score;
	}

	public int getEval() {
		return evals[index];
	}

	public void newGeneration() {
		generation++;
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

		assert fixedScore >= -Evaluator.VICTORY && fixedScore <= Evaluator.VICTORY;
		assert Math.abs(eval) < SearchEngine.VALUE_IS_MATE || Math.abs(eval) == Evaluator.VICTORY;

		if (score <= lowerBound) {
			set(board, TYPE_FAIL_LOW, bestMove, fixedScore, depthAnalyzed, eval, exclusion);
		} else if (score >= upperBound) {
			set(board, TYPE_FAIL_HIGH, bestMove, fixedScore, depthAnalyzed, eval, exclusion);
		} else {
			set(board, TYPE_EXACT_SCORE, bestMove, fixedScore, depthAnalyzed, eval, exclusion);
		}
	}

	/**
	 * In case of collision overwrites the eldest. It must keep PV nodes
	 */
	public void set(Board board, int nodeType, int bestMove, int score, int depthAnalyzed, int eval, boolean exclusion) {
		long key2 = board.getKey2();
		int startIndex = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64 - sizeBits));

		// Verifies that it is really this board
		int oldGenerationIndex = -1; // first index of an old generation entry
		int notPvIndex = -1; // first index of a not PV entry
		index = -1;
		for (int i = startIndex; i < startIndex + MAX_PROBES && i < size; i++) {
			info = infos[i];

			// Replace an empty TT position or the same position
			if (keys[i] == 0) {
				entriesOccupied++;
				index = i;
				break;
			} else if (keys[i] == key2) {
				index = i;
				break;
			}
			if (oldGenerationIndex == -1 && getGeneration() != generation) {
				oldGenerationIndex = i;
			}
			if (notPvIndex == -1 && getNodeType() != TYPE_EXACT_SCORE) {
				notPvIndex = i;
			}
		}
		if (index == -1 && notPvIndex != -1) {
			index = notPvIndex;
		} else if (index == -1 && oldGenerationIndex != -1) {
			index = oldGenerationIndex;
		} else if (index == -1) {
			// TT FULL
			return;
		}

		keys[index] = key2;
		info = (bestMove & 0x1fffff) | ((nodeType & 0xf) << 21) | (((long) (generation & 0xff)) << 32) | (((long) (depthAnalyzed & 0xff)) << 40)
				| (((long) (score & 0xffff)) << 48);

		infos[index] = info;
		evals[index] = (short) eval;
	}

	public int getHashFull() {
		return (int) (1000L * entriesOccupied / size);
	}
}