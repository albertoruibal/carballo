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

	public long mayPin; // bot my pieces than can discover an attack and the opponent pieces pinned, that is any piece attacked by a slider
	public long piecesGivingCheck;
	public long interposeCheckSquares;

	public int myKingIndex;
	public int otherKingIndex;

	//
	// Squares with possible ray attacks to the kings: used to detect check and move legality
	//
	public long bishopAttacksMyKing;
	public long rookAttacksMyKing;
	public long bishopAttacksOtherKing;
	public long rookAttacksOtherKing;

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

		myKingIndex = BitboardUtils.square2Index(myKing);
		otherKingIndex = BitboardUtils.square2Index(board.kings & ~mines);

		bishopAttacksMyKing = bbAttacks.getBishopAttacks(myKingIndex, all);
		rookAttacksMyKing = bbAttacks.getRookAttacks(myKingIndex, all);

		bishopAttacksOtherKing = bbAttacks.getBishopAttacks(otherKingIndex, all);
		rookAttacksOtherKing = bbAttacks.getRookAttacks(otherKingIndex, all);

		attackedSquares[0] = 0;
		attackedSquares[1] = 0;
		mayPin = 0;
		piecesGivingCheck = 0;
		interposeCheckSquares = 0;

		long pieceAttacks;
		int index;
		long square = 1;
		for (index = 0; index < 64; index++) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);

				pieceAttacks = 0;
				if ((square & board.pawns) != 0) {
					pieceAttacks = (isWhite ? bbAttacks.pawnUpwards[index] : bbAttacks.pawnDownwards[index]);
				} else if ((square & board.knights) != 0) {
					pieceAttacks = bbAttacks.knight[index];
				} else if ((square & board.kings) != 0) {
					pieceAttacks = bbAttacks.king[index];
				} else { // It is a slider
					if ((square & (board.bishops | board.queens)) != 0) {
						long sliderAttacks = bbAttacks.getBishopAttacks(index, all);
						if ((square & mines) == 0 && (sliderAttacks & myKing) != 0) {
							interposeCheckSquares |= sliderAttacks & bishopAttacksMyKing; // And with only the diagonal attacks to the king
						}
						pieceAttacks |= sliderAttacks;
					}
					if ((square & (board.rooks | board.queens)) != 0) {
						long sliderAttacks = bbAttacks.getRookAttacks(index, all);
						if ((square & mines) == 0 && (sliderAttacks & myKing) != 0) {
							interposeCheckSquares |= sliderAttacks & rookAttacksMyKing; // And with only the rook attacks to the king
						}
						pieceAttacks |= sliderAttacks;
					}
					mayPin |= all & pieceAttacks;
				}

				attackedSquares[isWhite ? W : B] |= pieceAttacks;
				attacksFromSquare[index] = pieceAttacks;

				if ((square & mines) == 0 && (pieceAttacks & myKing) != 0) {
					piecesGivingCheck |= square;
				}
			} else {
				attacksFromSquare[index] = 0;
			}
			square <<= 1;
		}
	}
}