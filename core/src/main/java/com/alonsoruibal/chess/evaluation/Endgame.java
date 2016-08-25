package com.alonsoruibal.chess.evaluation;


import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Color;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

public class Endgame {

	public static final int GAME_PHASE_MIDGAME = 1000;
	public static final int GAME_PHASE_ENDGAME = 0;

	public static final int SCALE_FACTOR_DRAW = 0;
	public static final int SCALE_FACTOR_DRAWISH = 100;
	public static final int SCALE_FACTOR_DEFAULT = 1000;

	public static final int NON_PAWN_MATERIAL_ENDGAME_MIN = Evaluator.QUEEN + Evaluator.ROOK;
	public static final int NON_PAWN_MATERIAL_MIDGAME_MAX = 2 * Evaluator.KNIGHT + 2 * Evaluator.BISHOP + 4 * Evaluator.ROOK + 2 * Evaluator.QUEEN;

	public static final int[] closerSquares = {0, 0, 100, 80, 60, 40, 20, 10};

	private static final int[] toCorners = { //
			100, 90, 80, 70, 70, 80, 90, 100, //
			90, 70, 60, 50, 50, 60, 70, 90, //
			80, 60, 40, 30, 30, 40, 60, 80, //
			70, 50, 30, 20, 20, 30, 50, 70, //
			70, 50, 30, 20, 20, 30, 50, 70, //
			80, 60, 40, 30, 30, 40, 60, 80, //
			90, 70, 60, 50, 50, 60, 70, 90, //
			100, 90, 80, 70, 70, 80, 90, 100,//
	};

	private static final int[] toColorCorners = {
			200, 190, 180, 170, 160, 150, 140, 130,
			190, 180, 170, 160, 150, 140, 130, 140,
			180, 170, 155, 140, 140, 125, 140, 150,
			170, 160, 140, 120, 110, 140, 150, 160,
			160, 150, 140, 110, 120, 140, 160, 170,
			150, 140, 125, 140, 140, 155, 170, 180,
			140, 130, 140, 150, 160, 170, 180, 190,
			130, 140, 150, 160, 170, 180, 190, 200
	};

	static KPKBitbase kpkBitbase;

	static {
		kpkBitbase = new KPKBitbase();
	}

	/**
	 * It may return a perfect knowledge value, a scaleFactor or nothing
	 */
	public static int evaluateEndgame(Board board, int[] gamePhase, int[] scaleFactor) {
		scaleFactor[0] = SCALE_FACTOR_DEFAULT;

		int whitePawns = BitboardUtils.popCount(board.pawns & board.whites);
		int blackPawns = BitboardUtils.popCount(board.pawns & board.blacks);
		int whiteKnights = BitboardUtils.popCount(board.knights & board.whites);
		int blackKnights = BitboardUtils.popCount(board.knights & board.blacks);
		int whiteBishops = BitboardUtils.popCount(board.bishops & board.whites);
		int blackBishops = BitboardUtils.popCount(board.bishops & board.blacks);
		int whiteRooks = BitboardUtils.popCount(board.rooks & board.whites);
		int blackRooks = BitboardUtils.popCount(board.rooks & board.blacks);
		int whiteQueens = BitboardUtils.popCount(board.queens & board.whites);
		int blackQueens = BitboardUtils.popCount(board.queens & board.blacks);

		// Endgame detection
		int whiteNoPawnMaterial = whiteKnights + whiteBishops + whiteRooks + whiteQueens;
		int blackNoPawnMaterial = blackKnights + blackBishops + blackRooks + blackQueens;
		int whiteMaterial = whiteNoPawnMaterial + whitePawns;
		int blackMaterial = blackNoPawnMaterial + blackPawns;

		// Do not put here draws already detected by the FIDE rules in the Board class

		if (whitePawns == 0 && blackPawns == 0) {
			//
			// Endgames without Pawns
			//

			if ((blackMaterial == 0 && whiteMaterial == 2 && whiteKnights == 1 && whiteBishops == 1) || //
					(whiteMaterial == 0 && blackMaterial == 2 && blackKnights == 1 && blackBishops == 1)) {
				return Endgame.endgameKBNK(board, whiteMaterial > blackMaterial);
			}
			if (whiteMaterial == 1 && blackMaterial == 1 && whiteRooks == 1 && blackRooks == 1) {
				scaleFactor[0] = Endgame.scaleKRKR(board);
			}

		} else if ((whitePawns == 1 && blackPawns == 0) || (whitePawns == 0 && blackPawns == 1)) {
			//
			// Single pawn endings
			//

			if (whiteNoPawnMaterial == 0 && blackNoPawnMaterial == 0) {
				return Endgame.endgameKPK(board, whiteMaterial > blackMaterial);
			}

			// ...and with a non-pawn piece by each side
			if (whiteNoPawnMaterial == 1 && blackNoPawnMaterial == 1) {
				if (whiteRooks == 1 && blackRooks == 1) {
					scaleFactor[0] = scaleKRPKR(board, whitePawns > blackPawns);
				}
				if (whiteBishops == 1 && blackBishops == 1) {
					scaleFactor[0] = scaleKBPKB(board, whitePawns > blackPawns);
				}
				if ((whiteBishops == 1 && whitePawns == 1 && blackKnights == 1) ||
						(blackBishops == 1 && blackPawns == 1 && whiteKnights == 1)) {
					scaleFactor[0] = scaleKBPKN(board, whitePawns > blackPawns);
				}
			}
		}

		//
		// Other endgames
		//
		if (blackMaterial == 0 && (whiteBishops >= 2 || whiteRooks > 0 || whiteQueens > 0) || //
				whiteMaterial == 0 && (whiteBishops >= 2 || blackRooks > 0 || blackQueens > 0)) {
			return Endgame.endgameKXK(board, whiteMaterial > blackMaterial, whiteKnights + blackKnights, whiteBishops + blackBishops, whiteRooks + blackRooks, whiteQueens + blackQueens);
		}

		if (whiteRooks == 1 && blackRooks == 1 &&
				((whitePawns == 2 && blackPawns == 1) || (whitePawns == 1 && blackPawns == 2))) {
			scaleFactor[0] = scaleKRPPKRP(board, whitePawns > blackPawns);
		}

		//
		// Interior node recognizer for draws
		//
		if (scaleFactor[0] == SCALE_FACTOR_DRAW) {
			return Evaluator.DRAW;
		}

		// Calculate gamePhase
		int nonPawnMaterial = (whiteKnights + blackKnights) * Evaluator.KNIGHT +
				(whiteBishops + blackBishops) * Evaluator.BISHOP +
				(whiteRooks + blackRooks) * Evaluator.ROOK +
				(whiteQueens + blackQueens) * Evaluator.QUEEN;

		gamePhase[0] = nonPawnMaterial >= NON_PAWN_MATERIAL_MIDGAME_MAX ? GAME_PHASE_MIDGAME :
				nonPawnMaterial <= NON_PAWN_MATERIAL_ENDGAME_MIN ? GAME_PHASE_ENDGAME :
						((nonPawnMaterial - NON_PAWN_MATERIAL_ENDGAME_MIN) * GAME_PHASE_MIDGAME) / (NON_PAWN_MATERIAL_MIDGAME_MAX - NON_PAWN_MATERIAL_ENDGAME_MIN);

		return Evaluator.NO_VALUE;
	}

	// One side does not have pieces, drives the king to the corners and try to approximate the kings
	private static int endgameKXK(Board board, boolean whiteDominant, int knights, int bishops, int rooks, int queens) {
		int whiteKingIndex = BitboardUtils.square2Index(board.kings & board.whites);
		int blackKingIndex = BitboardUtils.square2Index(board.kings & board.blacks);
		int value = Evaluator.KNOWN_WIN +
				knights * Evaluator.KNIGHT +
				bishops * Evaluator.BISHOP +
				rooks * Evaluator.ROOK +
				queens * Evaluator.QUEEN +
				closerSquares[BitboardUtils.distance(whiteKingIndex, blackKingIndex)] +//
				(whiteDominant ? toCorners[blackKingIndex] : toCorners[whiteKingIndex]);

		return (whiteDominant ? value : -value);
	}

	// NB vs K must drive the king to the corner of the color of the bishop
	private static int endgameKBNK(Board board, boolean whiteDominant) {
		int whiteKingIndex = BitboardUtils.square2Index(board.kings & board.whites);
		int blackKingIndex = BitboardUtils.square2Index(board.kings & board.blacks);

		if (BitboardUtils.isBlackSquare(board.bishops)) {
			whiteKingIndex = BitboardUtils.flipHorizontalIndex(whiteKingIndex);
			blackKingIndex = BitboardUtils.flipHorizontalIndex(blackKingIndex);
		}

		int value = Evaluator.KNOWN_WIN + closerSquares[BitboardUtils.distance(whiteKingIndex, blackKingIndex)] + //
				(whiteDominant ? toColorCorners[blackKingIndex] : toColorCorners[whiteKingIndex]);

		return (whiteDominant ? value : -value);
	}

	private static int endgameKPK(Board board, boolean whiteDominant) {
		if (!kpkBitbase.probe(board)) {
			return Evaluator.DRAW;
		}

		return whiteDominant ?
				Evaluator.KNOWN_WIN + Evaluator.PAWN + BitboardUtils.getRankOfIndex(BitboardUtils.square2Index(board.pawns)) : //
				-Evaluator.KNOWN_WIN - Evaluator.PAWN - (7 - BitboardUtils.getRankOfIndex(BitboardUtils.square2Index(board.pawns)));
	}

	private static int scaleKRKR(Board board) {
		return SCALE_FACTOR_DRAW;
	}

	private static int scaleKRPKR(Board board, boolean whiteDominant) {
		int dominantColor = whiteDominant ? Color.W : Color.B;
		long dominantRook = board.rooks & (whiteDominant ? board.whites : board.blacks);
		long otherRook = board.rooks & (whiteDominant ? board.blacks : board.whites);

		long dominantKing = board.kings & (whiteDominant ? board.whites : board.blacks);
		long otherKing = board.kings & (whiteDominant ? board.blacks : board.whites);
		int dominantKingIndex = BitboardUtils.square2Index(dominantKing);

		int rank7 = whiteDominant ? 6 : 1;
		int rank6 = whiteDominant ? 5 : 2;
		int rank2 = whiteDominant ? 1 : 6;

		long pawn = board.pawns;
		int pawnIndex = BitboardUtils.square2Index(pawn);
		int pawnFileIndex = 7 - (BitboardUtils.square2Index(pawn) & 7);
		long pawnFile = BitboardUtils.FILE[pawnFileIndex];
		long pawnFileAndAdjacents = BitboardUtils.FILE[pawnFileIndex] | BitboardUtils.FILES_ADJACENT[pawnFileIndex];

		// Philidor position
		if ((BitboardUtils.RANKS_BACKWARD[dominantColor][rank6] & pawn) != 0 // Pawn behind rank 6
				&& (BitboardUtils.RANKS_BACKWARD[dominantColor][rank6] & dominantKing) != 0 // Dominant king behind rank 6
				&& (BitboardUtils.RANKS_FORWARD[dominantColor][rank6] & pawnFileAndAdjacents & otherKing) != 0 // King defending promotion squares
				&& (BitboardUtils.RANK[rank6] & otherRook) != 0) { // Other rook in rank 6
			return SCALE_FACTOR_DRAW;
		}
		// When the pawn is advanced to 6th, check the king from behind
		if ((BitboardUtils.RANK[rank6] & pawn) != 0 // Pawn in rank 6
				&& (BitboardUtils.RANKS_FORWARD[dominantColor][rank6] & pawnFileAndAdjacents & otherKing) != 0 // King defending promotion squares
				&& ((BitboardUtils.RANK_AND_BACKWARD[dominantColor][rank2] & otherRook) != 0
				|| ((board.getTurn() != whiteDominant) && (BitboardUtils.distance(pawnIndex, dominantKingIndex) >= 2)))) { // Rook ready to check from behind
			return SCALE_FACTOR_DRAW;
		}
		// If the pawn is in advanced to 7th...
		if ((BitboardUtils.RANK[rank7] & pawn) != 0
				&& (BitboardUtils.RANKS_FORWARD[dominantColor][rank6] & pawnFile & otherKing) != 0 // King in the promotion squares
				&& (BitboardUtils.RANK_AND_BACKWARD[dominantColor][rank2] & otherRook) != 0 // Rook must be already behind
				&& ((board.getTurn() != whiteDominant) || (BitboardUtils.distance(pawnIndex, dominantKingIndex) >= 2))) {
			return SCALE_FACTOR_DRAW;
		}

		return SCALE_FACTOR_DEFAULT;
	}

	private static int scaleKBPKN(Board board, boolean whiteDominant) {
		int dominantColor = whiteDominant ? Color.W : Color.B;
		long dominantBishop = board.bishops & (whiteDominant ? board.whites : board.blacks);
		long dominantBishopSquares = BitboardUtils.getSameColorSquares(dominantBishop);

		long pawn = board.pawns;
		long pawnRoute = BitboardUtils.frontFile(pawn, dominantColor);

		long otherKing = board.kings & (whiteDominant ? board.blacks : board.whites);

		// Other king in front of the pawn in a square different than the bishop color: DRAW
		if ((pawnRoute & otherKing) != 0 && (dominantBishopSquares & otherKing) == 0) {
			return SCALE_FACTOR_DRAW;
		}
		return SCALE_FACTOR_DEFAULT;
	}

	private static int scaleKBPKB(Board board, boolean whiteDominant) {
		int dominantColor = whiteDominant ? Color.W : Color.B;
		long dominantBishop = board.bishops & (whiteDominant ? board.whites : board.blacks);
		long dominantBishopSquares = BitboardUtils.getSameColorSquares(dominantBishop);
		long otherBishop = board.bishops & (whiteDominant ? board.blacks : board.whites);

		long pawn = board.pawns;
		long pawnRoute = BitboardUtils.frontFile(pawn, dominantColor);

		long otherKing = board.kings & (whiteDominant ? board.blacks : board.whites);

		// Other king in front of the pawn in a square different than the bishop color: DRAW
		if ((pawnRoute & otherKing) != 0 && (dominantBishopSquares & otherKing) == 0) {
			return SCALE_FACTOR_DRAW;
		}

		// Different bishop colors
		long otherBishopSquares = BitboardUtils.getSameColorSquares(otherBishop);
		if (dominantBishopSquares != otherBishopSquares) {

			int otherBishopIndex = BitboardUtils.square2Index(otherBishop);
			if ((otherBishop & pawnRoute) != 0
					|| (BitboardAttacks.getInstance().bishop[otherBishopIndex] & pawnRoute) != 0) {
				return SCALE_FACTOR_DRAW;
			}
		}

		return SCALE_FACTOR_DEFAULT;
	}

	private static int scaleKRPPKRP(Board board, boolean whiteDominant) {
		int dominantColor = whiteDominant ? Color.W : Color.B;
		long dominantPawns = board.pawns & (whiteDominant ? board.whites : board.blacks);
		long p1Front = BitboardUtils.frontPawnSpan(BitboardUtils.lsb(dominantPawns), dominantColor);
		long p2Front = BitboardUtils.frontPawnSpan(BitboardUtils.msb(dominantPawns), dominantColor);
		long otherPawn = board.pawns & (whiteDominant ? board.blacks : board.whites);

		// Check for a Passed Pawn
		if ((p1Front & otherPawn) == 0 || (p2Front & otherPawn) == 0) {
			return SCALE_FACTOR_DEFAULT;
		}
		// If the other king is in front of the pawns, it is drawish
		long otherKing = board.kings & (whiteDominant ? board.blacks : board.whites);
		if ((p1Front & otherKing) != 0 && (p1Front & otherKing) != 1) {
			return SCALE_FACTOR_DRAWISH;
		}

		return SCALE_FACTOR_DEFAULT;
	}

}