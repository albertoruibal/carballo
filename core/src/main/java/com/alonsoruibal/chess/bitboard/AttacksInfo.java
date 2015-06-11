package com.alonsoruibal.chess.bitboard;

import com.alonsoruibal.chess.Board;

/**
 * Holds all the possible attacks for a board
 *
 * It is used by the evaluators and the move iterator, and also to speed the SEE calculations detecting not attacked squares
 *
 * Calculates the checking pieces and the interpose squares to avoid checks
 */
public class AttacksInfo {
	public final static int W = 0;
	public final static int B = 1;

	BitboardAttacks bbAttacks;

	public long boardKey = 0;
	public long attacksFromSquare[] = new long[64];
	public long attackedSquares[] = {0, 0};
	public long pawnAttacks[] = {0, 0};
	public long knightAttacks[] = {0, 0};
	public long bishopAttacks[] = {0, 0};
	public long rookAttacks[] = {0, 0};
	public long queenAttacks[] = {0, 0};
	public long kingAttacks[] = {0, 0};
	public int kingIndex[] = {0, 0};
	//
	// Squares with possible ray attacks to the kings: used to detect check and move legality
	//
	public long bishopAttacksKing[] = {0, 0};
	public long rookAttacksKing[] = {0, 0};

	public long mayPin; // bot my pieces than can discover an attack and the opponent pieces pinned, that is any piece attacked by a slider
	public long piecesGivingCheck;
	public long interposeCheckSquares;

	public AttacksInfo() {
		this.bbAttacks = BitboardAttacks.getInstance();
	}

	/**
	 * If we already hold the attacks for this board, do nothing
	 */
	public void build(Board board) {
		if (boardKey == board.getKey()) {
			return;
		}
		boardKey = board.getKey();
		long all = board.getAll();
		long mines = board.getMines();
		long myKing = board.kings & mines;
		int us = board.getTurn() ? 0 : 1;

		kingIndex[W] = BitboardUtils.square2Index(board.kings & board.whites);
		kingIndex[B] = BitboardUtils.square2Index(board.kings & board.blacks);

		bishopAttacksKing[W] = bbAttacks.getBishopAttacks(kingIndex[W], all);
		bishopAttacksKing[B] = bbAttacks.getBishopAttacks(kingIndex[B], all);

		rookAttacksKing[W] = bbAttacks.getRookAttacks(kingIndex[W], all);
		rookAttacksKing[B] = bbAttacks.getRookAttacks(kingIndex[B], all);

		pawnAttacks[W] = 0;
		pawnAttacks[B] = 0;
		knightAttacks[W] = 0;
		knightAttacks[B] = 0;
		bishopAttacks[W] = 0;
		bishopAttacks[B] = 0;
		rookAttacks[W] = 0;
		rookAttacks[B] = 0;
		queenAttacks[W] = 0;
		queenAttacks[B] = 0;
		kingAttacks[W] = 0;
		kingAttacks[B] = 0;
		mayPin = 0;
		piecesGivingCheck = 0;
		interposeCheckSquares = 0;

		long pieceAttacks;
		int index;
		long square = 1;
		for (index = 0; index < 64; index++) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);
				int color = isWhite ? W : B;

				pieceAttacks = 0;
				if ((square & board.pawns) != 0) {
					pieceAttacks = (isWhite ? bbAttacks.pawnUpwards[index] : bbAttacks.pawnDownwards[index]);
					if ((square & mines) == 0 && (pieceAttacks & myKing) != 0) {
						piecesGivingCheck |= square;
					}
					pawnAttacks[color] |= pieceAttacks;

				} else if ((square & board.knights) != 0) {
					pieceAttacks = bbAttacks.knight[index];
					if ((square & mines) == 0 && (pieceAttacks & myKing) != 0) {
						piecesGivingCheck |= square;
					}
					knightAttacks[color] |= pieceAttacks;

				} else if ((square & board.bishops) != 0) {
					pieceAttacks = bbAttacks.getBishopAttacks(index, all);
					if ((square & mines) == 0 && (pieceAttacks & myKing) != 0) {
						piecesGivingCheck |= square;
						interposeCheckSquares |= pieceAttacks & bishopAttacksKing[us]; // And with only the diagonal attacks to the king
					}
					bishopAttacks[color] |= pieceAttacks;
					mayPin |= all & pieceAttacks;

				} else if ((square & board.rooks) != 0) {
					pieceAttacks = bbAttacks.getRookAttacks(index, all);
					if ((square & mines) == 0 && (pieceAttacks & myKing) != 0) {
						piecesGivingCheck |= square;
						interposeCheckSquares |= pieceAttacks & rookAttacksKing[us]; // And with only the rook attacks to the king
					}
					rookAttacks[color] |= pieceAttacks;
					mayPin |= all & pieceAttacks;

				} else if ((square & board.queens) != 0) {
					long bishopSliderAttacks = bbAttacks.getBishopAttacks(index, all);
					if ((square & mines) == 0 && (bishopSliderAttacks & myKing) != 0) {
						piecesGivingCheck |= square;
						interposeCheckSquares |= bishopSliderAttacks & bishopAttacksKing[us]; // And with only the diagonal attacks to the king
					}
					long rookSliderAttacks = bbAttacks.getRookAttacks(index, all);
					if ((square & mines) == 0 && (rookSliderAttacks & myKing) != 0) {
						piecesGivingCheck |= square;
						interposeCheckSquares |= rookSliderAttacks & rookAttacksKing[us]; // And with only the rook attacks to the king
					}
					pieceAttacks = rookSliderAttacks | bishopSliderAttacks;
					queenAttacks[color] |= pieceAttacks;
					mayPin |= all & pieceAttacks;

				} else if ((square & board.kings) != 0) {
					pieceAttacks = bbAttacks.king[index];
					kingAttacks[color] |= pieceAttacks;
				}

				attacksFromSquare[index] = pieceAttacks;
			} else {
				attacksFromSquare[index] = 0;
			}
			square <<= 1;
		}
		attackedSquares[W] = pawnAttacks[W] | knightAttacks[W] | bishopAttacks[W] | rookAttacks[W] | queenAttacks[W] | kingAttacks[W];
		attackedSquares[B] = pawnAttacks[B] | knightAttacks[B] | bishopAttacks[B] | rookAttacks[B] | queenAttacks[B] | kingAttacks[B];
	}
}