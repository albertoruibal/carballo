package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Color;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

import java.util.ArrayList;

/**
 * Derived from Stockfish bitbase.cpp
 */
public class KPKBitbaseGenerator {
	private static final Logger logger = Logger.getLogger("KPKBitbase");

	// There are 24 possible pawn squares: the first 4 files and ranks from 2 to 7
	final static int MAX_INDEX = 2 * 24 * 64 * 64; // whiteToMove * pawnIndex * whiteKingIndex * blackKingIndex = 196608

	// Each int stores results of 32 positions, one per bit, 24Kbytes
	public int[] bitbase = new int[MAX_INDEX / 32];

	static final int RESULT_INVALID = 0;
	static final int RESULT_UNKNOWN = 1;
	static final int RESULT_DRAW = 2;
	static final int RESULT_WIN = 4;

	static final int RANK_7 = 6;
	static final int RANK_2 = 1;
	static final int DELTA_N = 8;

	// A KPK bitbase index is an integer in [0, IndexMax] range
	//
	// Information is mapped in a way that minimizes the number of iterations:
	//
	// bit  0- 5: white king index
	// bit  6-11: black king index
	// bit    12: side to move 1 WHITE
	// bit 13-14: white pawn file (from FILE_H to FILE_E)
	// bit 15-17: white pawn RANK_7 - rank (from RANK_7 - RANK_7 to RANK_7 - RANK_2)
	int index(boolean whitetoMove, int blackKingIndex, int whiteKingIndex, int pawnIndex) {
		return whiteKingIndex + (blackKingIndex << 6) + //
				((whitetoMove ? 1 : 0) << 12) + //
				(BitboardUtils.getFileOfIndex(pawnIndex) << 13) + ((RANK_7 - BitboardUtils.getRankOfIndex(pawnIndex)) << 15);
	}

	class KPKPosition {
		boolean whiteToMove;
		int blackKingIndex, whiteKingIndex, pawnIndex;
		int result;

		public KPKPosition(int idx) {
			whiteKingIndex = idx & 0x3F;
			blackKingIndex = (idx >>> 6) & 0x3F;
			whiteToMove = ((idx >>> 12) & 0x01) != 0;
			pawnIndex = 7 - ((idx >>> 13) & 0x03) + ((RANK_7 - (idx >>> 15)) << 3);
			result = RESULT_UNKNOWN;

			long blackKingSquare = BitboardUtils.index2Square((byte) blackKingIndex);
			long pawnSquare = BitboardUtils.index2Square((byte) (pawnIndex));
			long pawnAttacks = BitboardAttacks.getInstance().pawn[Color.W][pawnIndex];

			long pawnSquareNextRank = BitboardUtils.index2Square((byte) (pawnIndex + 8));
			long whiteKingAttacks = BitboardAttacks.getInstance().king[whiteKingIndex];
			long blackKingAttacks = BitboardAttacks.getInstance().king[blackKingIndex];

			// Check if two pieces are on the same square or if a king can be captured
			if (BitboardUtils.distance(whiteKingIndex, blackKingIndex) <= 1
					|| whiteKingIndex == pawnIndex
					|| blackKingIndex == pawnIndex
					|| (whiteToMove && (pawnAttacks & blackKingSquare) != 0)) {
				result = RESULT_INVALID;
			} else if (whiteToMove) {
				// Immediate win if a pawn can be promoted without getting captured
				if (BitboardUtils.getRankOfIndex(pawnIndex) == RANK_7
						&& whiteKingIndex != pawnIndex + DELTA_N
						&& ((BitboardUtils.distance(blackKingIndex, pawnIndex + DELTA_N) > 1 || (whiteKingAttacks & pawnSquareNextRank) != 0))) {
					result = RESULT_WIN;
				}
			}
			// Immediate draw if it is a stalemate or a king captures undefended pawn
			else if ((blackKingAttacks & ~(whiteKingAttacks | pawnAttacks)) == 0
					|| (blackKingAttacks & pawnSquare & ~whiteKingAttacks) != 0) {
				result = RESULT_DRAW;
			}
		}

		public int getResult() {
			return result;
		}

		int classify(ArrayList<KPKPosition> db) {
			return classify(whiteToMove, db);
		}

		int classify(boolean whiteToMove, ArrayList<KPKPosition> db) {
			// White to Move: If one move leads to a position classified as WIN, the result
			// of the current position is WIN. If all moves lead to positions classified
			// as DRAW, the current position is classified as DRAW, otherwise the current
			// position is classified as UNKNOWN.
			//
			// Black to Move: If one move leads to a position classified as DRAW, the result
			// of the current position is DRAW. If all moves lead to positions classified
			// as WIN, the position is classified as WIN, otherwise the current position is
			// classified as UNKNOWN.

			int r = RESULT_INVALID;
			long b = BitboardAttacks.getInstance().king[whiteToMove ? whiteKingIndex : blackKingIndex];

			// test king moves
			while (b != 0) {
				long b1 = BitboardUtils.lsb(b);
				b = b & ~b1;
				r |= whiteToMove ? db.get(index(false, blackKingIndex, BitboardUtils.square2Index(b1), pawnIndex)).getResult() : //
						db.get(index(true, BitboardUtils.square2Index(b1), whiteKingIndex, pawnIndex)).getResult();
			}

			if (whiteToMove && BitboardUtils.getRankOfIndex(pawnIndex) < RANK_7) {
				int s = pawnIndex + DELTA_N;
				r |= db.get(index(false, blackKingIndex, whiteKingIndex, s)).getResult(); // Single push

				if (BitboardUtils.getRankOfIndex(pawnIndex) == RANK_2 && s != whiteKingIndex && s != blackKingIndex) {
					r |= db.get(index(false, blackKingIndex, whiteKingIndex, s + DELTA_N)).getResult(); // Double push
				}
			}

			if (whiteToMove) {
				return result = (r & RESULT_WIN) != 0 ? RESULT_WIN : (r & RESULT_UNKNOWN) != 0 ? RESULT_UNKNOWN : RESULT_DRAW;
			} else {
				return result = (r & RESULT_DRAW) != 0 ? RESULT_DRAW : (r & RESULT_UNKNOWN) != 0 ? RESULT_UNKNOWN : RESULT_WIN;
			}
		}
	}

	public void init() {
		logger.debug("Generating KPK tables...");
		long time1 = System.currentTimeMillis();
		int i;
		boolean repeat = true;
		ArrayList<KPKPosition> db = new ArrayList<KPKPosition>();

		// Initialize db with known win / draw positions
		for (i = 0; i < MAX_INDEX; ++i) {
			db.add(new KPKPosition(i));
		}
		long time1b = System.currentTimeMillis();

		// Iterate through the positions until none of the unknown positions can be
		// changed to either wins or draws (15 cycles needed).
		while (repeat) {
			for (repeat = false, i = 0; i < MAX_INDEX; ++i) {
				repeat |= ((db.get(i).getResult() == RESULT_UNKNOWN) && (db.get(i).classify(db) != RESULT_UNKNOWN));
			}
		}

		// Map 32 results into one bitbase[] entry
		for (i = 0; i < MAX_INDEX; i++) {
			if (db.get(i).getResult() == RESULT_WIN) {
				bitbase[i / 32] |= (1 << (i & 0x1F));
			}
		}
		long time2 = System.currentTimeMillis();
		logger.debug("Generated KPK tables in " + (time2 - time1) + "ms, initialization = " + (time1b - time1) + "ms");
	}

	public static void main(String args[]) {
		// Prints bitbase in java array format
		KPKBitbaseGenerator kpkBitbase = new KPKBitbaseGenerator();
		kpkBitbase.init();

		for (int i = 0; i < kpkBitbase.bitbase.length; i++) {
			if (i % 50 == 0) {
				System.out.print("\n");
			}
			System.out.print("0x" + Integer.toHexString(kpkBitbase.bitbase[i]) + ", ");
		}
	}
}