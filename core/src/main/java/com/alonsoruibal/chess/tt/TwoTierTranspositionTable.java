 package com.alonsoruibal.chess.tt;

import java.util.Arrays;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.log.Logger;


/**
 * Two tier Transposition table using two keys
 * 
 * odd positions are conserved the greatest depth first
 * even positions are allways replaced
 * 
 * Uses part of the board's zobrish key (shifted) as the index 
 * 
 * @author rui
 * 
 */
public class TwoTierTranspositionTable extends TranspositionTable {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("TwoTierTranspositionTable");
	
	public long[] keys;
	public long[] infos;

	private int sizeBits;
	private int index;
	private int size;
	private long info;
	private byte generation;

	/**
	 * Whe must indicate the number in bits of the size
	 * example: 23 => 2^23 are 8 million entries
	 * @param sizeBits
	 */
	public TwoTierTranspositionTable(int sizeBits) {
		this.sizeBits = sizeBits;
		size = 1 << sizeBits;
		keys = new long[size];
		infos = new long[size];

		generation = 0;
		index = -1;
		logger.debug("Created Two-Tier transposition table, size = " + size + " entries " + size * 16 /(1024*1024) + "MB");
	}

	/**
	 * Returns true if key matches with key stored
	 */
	public boolean search(Board board, boolean exclusion) {
		info = 0;
		index = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64-sizeBits)) & ~0x01; // Get the first odd index
		long key2 = board.getKey2();
		// Verifies that is really this board
		if (keys[index] == key2 || keys[++index] == key2) { // Already returns the correct index
			info = infos[index];
			return true;
		}
		return false;
	}

	public int getBestMove() {
		return (int)(info & 0x1fffff);
	}

	public int getNodeType() {
		return (int)((info >>> 21) & 0xf);
	}

	public byte getGeneration() {
		return (byte)((info >>> 32) & 0xff);
	}
	
	public byte getDepthAnalyzed() {
		return (byte)((info >>> 40) & 0xff);
	}
	
	public int getScore() {
		return (short)((info >>> 48) & 0xffff);
	}	
	
	public void set(Board board, int nodeType, int bestMove, int score, byte depthAnalyzed, boolean exclusion) {
		long key2 =  board.getKey2();
		index = (int) ((exclusion ? board.getExclusionKey() : board.getKey()) >>> (64-sizeBits)) & ~0x01; // Get the first odd index
		
		info = infos[index];
		if (keys[index] == 0 ||
			getDepthAnalyzed() <= depthAnalyzed || 
			getGeneration() != generation) {
			// Replace odd entry
		} else {
			// Replace even entry
			index++;
		}
		
		keys[index] = key2;
		info = (bestMove & 0x1fffff) |
		       ((nodeType & 0xf) << 21) |
		       (((long)(generation & 0xff)) << 32) |
			   (((long)(depthAnalyzed & 0xff)) << 40) |
			   (((long)(score & 0xffff)) << 48);

		infos[index] = info;
	}

	// called at the start of each search
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