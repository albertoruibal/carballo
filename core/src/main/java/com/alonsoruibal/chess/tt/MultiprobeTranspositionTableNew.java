package com.alonsoruibal.chess.tt;

import java.util.Arrays;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.log.Logger;

/**
 * Transposition table using two keys and multiprobe
 * 
 * Uses part of the board's zobrish key (shifted) as the index
 * 
 * @author rui
 * 
 */
public class MultiprobeTranspositionTableNew extends TranspositionTable {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("MultiprobeTranspositionTableNew");

	private final static int MAX_PROBES = 4;

	public long[] keys;
	public long[] infos;

	private int sizeBits;
	private int index;
	private int size;
	private long info;
	private byte generation;

	/**
	 * Whe must indicate the number in bits of the size example: 23 => 2^23 are
	 * 8 million entries
	 * 
	 * @param sizeBits
	 */
	public MultiprobeTranspositionTableNew(int sizeBits) {
		this.sizeBits = sizeBits;
		size = 1 << sizeBits;
		keys = new long[size];
		infos = new long[size];

		generation = 0;
		index = -1;
		logger.debug("Created Multiprobe transposition table New, size = " + size + " entries " + size * 16 / (1024 * 1024) + "MB");
	}

	public boolean search(Board board, boolean exclusion) {
		info = 0;
		int startIndex = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64 - sizeBits)) & ~0x03;
		// Verifies that is really this board
		for (index = startIndex; index < startIndex + MAX_PROBES && index < size; index++) {
			if (keys[index] == board.getKey2()) {
				info = infos[index];
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
		return (short) ((info >>> 48) & 0xffff);
	}

	/**
	 * In case of collision overwrites replace the eldest keep pv nodes
	 */
	public void set(Board board, int nodeType, int bestMove, int score, byte depthAnalyzed, boolean exclusion) {
		long key2 = board.getKey2();
		int startIndex = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64 - sizeBits)) & ~0x03;

		// Verifies that is really this board
		index = -1;
		for (int i = startIndex; i < startIndex + MAX_PROBES; i++) {
			info = infos[i];
			if (keys[i] == 0 || (keys[i] == key2)) { // Empty or replace
				if (keys[i] != 0 && getGeneration() == generation && // When
																		// replacing
																		// something
																		// my
																		// generation
						(depthAnalyzed == 0 || getDepthAnalyzed() > depthAnalyzed
						// || (getDepthAnalyzed() == depthAnalyzed &&
						// getNodeType() == TYPE_EXACT_SCORE && nodeType !=
						// TYPE_EXACT_SCORE)
						))
					return; // Never replace with eval values or lower depth or
							// exact scores with other nodetypes
				index = i;
				if (keys[i] == key2 && bestMove == 0)
					bestMove = getBestMove(); // Keep best move when replacing
												// and no move
				break;
			}

			if (getGeneration() != generation || getDepthAnalyzed() < depthAnalyzed) { // TODO
																						// <
																						// or
																						// >=
																						// ?
				index = i;
				break;
			}
		}
		if (index == -1)
			return; // No slot found

		keys[index] = key2;
		info = (bestMove & 0x1fffff) | ((nodeType & 0xf) << 21) | (((long) (generation & 0xff)) << 32) | (((long) (depthAnalyzed & 0xff)) << 40)
				| (((long) (score & 0xffff)) << 48);

		infos[index] = info;
	}

	public void newGeneration() {
		generation++;
	}

	@Override
	public boolean isMyGeneration() {
		return getGeneration() == generation;
	}

	public void clear() {
		Arrays.fill(keys, 0);
	}
}