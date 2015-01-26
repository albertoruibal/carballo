package com.alonsoruibal.chess.evaluation;


import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

public class Endgame {

	public final static int[] closerSquares = {0, 0, 100, 80, 60, 40, 20, 10};

	private final static int[] toCorners = { //
			100, 90, 80, 70, 70, 80, 90, 100, //
			90, 70, 60, 50, 50, 60, 70, 90, //
			80, 60, 40, 30, 30, 40, 60, 80, //
			70, 50, 30, 20, 20, 30, 50, 70, //
			70, 50, 30, 20, 20, 30, 50, 70, //
			80, 60, 40, 30, 30, 40, 60, 80, //
			90, 70, 60, 50, 50, 60, 70, 90, //
			100, 90, 80, 70, 70, 80, 90, 100,//
	};

	private final static int[] toColorCorners = {
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

	// One side does not have pieces, drives the king to the corners and try to approximate the kings
	public static int endgameKXK(Board board, int[] pawnMaterial, int[] material) {
		boolean whiteDominant = (material[0] == 0 ? false : true);

		int whiteKingIndex = BitboardUtils.square2Index(board.kings & board.whites);
		int blackKingIndex = BitboardUtils.square2Index(board.kings & board.blacks);
		int value = closerSquares[BitboardUtils.distance(whiteKingIndex, blackKingIndex)] +//
				(whiteDominant ? toCorners[blackKingIndex] : toCorners[whiteKingIndex]);

		if ((board.queens != 0) || (board.rooks != 0) || ((board.bishops != 0) && (board.knights) != 0) || (BitboardUtils.popCount(board.bishops) >= 2)) {
			value += Evaluator.KNOWN_WIN;
		}

		return (whiteDominant ? value : -value) + pawnMaterial[0] + material[0] - pawnMaterial[1] - material[1];
	}

	// NB vs K must drive the king to the corner of the color of the bishop
	public static int endgameKBNK(Board board, int[] pawnMaterial, int[] material) {
		boolean whiteDominant = (material[0] == 0 ? false : true);

		int whiteKingIndex = BitboardUtils.square2Index(board.kings & board.whites);
		int blackKingIndex = BitboardUtils.square2Index(board.kings & board.blacks);

		if (BitboardUtils.inBlackSquare(board.bishops)) {
			whiteKingIndex = BitboardUtils.flipHorizontal(whiteKingIndex);
			blackKingIndex = BitboardUtils.flipHorizontal(blackKingIndex);
		}

		int value = Evaluator.KNOWN_WIN + closerSquares[BitboardUtils.distance(whiteKingIndex, blackKingIndex)] + //
				(whiteDominant ? toColorCorners[blackKingIndex] : toColorCorners[whiteKingIndex]);

		return (whiteDominant ? value : -value) + pawnMaterial[0] + material[0] - pawnMaterial[1] - material[1];
	}

	public static int endgameKPK(Board board, int[] pawnMaterial) {
		if (!kpkBitbase.probe(board)) {
			return Evaluator.DRAW;
		}

		boolean whiteDominant = (board.whites & board.pawns) != 0;
		return whiteDominant ? Evaluator.KNOWN_WIN + BitboardUtils.rankOf(BitboardUtils.square2Index(board.pawns)) : //
				-Evaluator.KNOWN_WIN - (7 - BitboardUtils.rankOf(BitboardUtils.square2Index(board.pawns))) + pawnMaterial[0] - pawnMaterial[1];
	}
}