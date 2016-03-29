package com.alonsoruibal.chess.evaluation;


import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

public class Endgame {

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

	public static int endGameValue(Board board, int whitePawns, int blackPawns, int whiteKnights, int blackKnights, int whiteBishops, int blackBishops, int whiteRooks, int blackRooks, int whiteQueens, int blackQueens) {
		// Endgame detection
		int whiteNoPawnMaterial = whiteKnights + whiteBishops + whiteRooks + whiteQueens;
		int blackNoPawnMaterial = blackKnights + blackBishops + blackRooks + blackQueens;
		int whiteMaterial = whiteNoPawnMaterial + whitePawns;
		int blackMaterial = blackNoPawnMaterial + blackPawns;

		// Do not put here endgames already detected by Board

		// Endgames without pawns
		if (whitePawns == 0 && blackPawns == 0) {
			if ((blackMaterial == 0 && whiteMaterial == 2 && whiteKnights == 1 && whiteBishops == 1) || //
					(whiteMaterial == 0 && blackMaterial == 2 && blackKnights == 1 && blackBishops == 1)) {
				return Endgame.endgameKBNK(board, whiteMaterial > blackMaterial);
			}
			if (whiteMaterial == 1 && blackMaterial == 1 && whiteRooks == 1 && blackRooks == 1) {
				return Endgame.endgameKRKR(board); // Not always a draw
			}
		}
		if ((blackMaterial == 0 && whiteNoPawnMaterial == 0 && whitePawns == 1) || //
				(whiteMaterial == 0 && blackNoPawnMaterial == 0 && blackPawns == 1)) {
			return Endgame.endgameKPK(board, whiteMaterial > blackMaterial);
		}
		if (blackMaterial == 0 && (whiteBishops >= 2 || whiteRooks > 0 || whiteQueens > 0) || //
				whiteMaterial == 0 && (whiteBishops >= 2 || blackRooks > 0 || blackQueens > 0)) {
			return Endgame.endgameKXK(board, whiteMaterial > blackMaterial, whiteKnights + blackKnights, whiteBishops + blackBishops, whiteRooks + blackRooks, whiteQueens + blackQueens);
		}
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

		if (BitboardUtils.isBlack(board.bishops)) {
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

	private static int endgameKRKR(Board board) {
		int myKingIndex = BitboardUtils.square2Index(board.kings & board.getMines());
		int myRookIndex = BitboardUtils.square2Index(board.rooks & board.getMines());
		int otherKingIndex = BitboardUtils.square2Index(board.kings & board.getOthers());
		int otherRookIndex = BitboardUtils.square2Index(board.rooks & board.getOthers());

		// The other king is too far, or my king is near the other rook, so my rook can capture the other rook
		if ((BitboardUtils.distance(otherKingIndex, otherRookIndex) > 1 || BitboardUtils.distance(myKingIndex, otherRookIndex) == 1) &&
				(BitboardAttacks.getInstance().getRookAttacks(myRookIndex, board.getAll()) & board.rooks) != 0) {
			return Evaluator.KNOWN_WIN;
		}
		// The other rook is undefended and my king can capture it
		if (BitboardUtils.distance(otherKingIndex, otherRookIndex) > 1 &&
				BitboardUtils.distance(myKingIndex, otherRookIndex) == 1) {
			// Does the other king capture my rook just after my move?
			if (BitboardUtils.distance(otherKingIndex, myRookIndex) == 1 &&
					BitboardUtils.distance(otherRookIndex/*that's my king after after capture*/, myRookIndex) > 1) {
				return Evaluator.DRAW;
			}
			return Evaluator.KNOWN_WIN;
		}
		return Evaluator.DRAW;
	}
}