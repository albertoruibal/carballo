package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/**
 * Magic move generator
 * Pseudo-legal Moves
 *
 * @author Alberto Alonso Ruibal
 */
public class MagicMoveGenerator implements MoveGenerator {

	private int[] moves;
	private int moveIndex;
	private long all;
	private long mines;
	private long others;

	BitboardAttacks bbAttacks;

	public int generateMoves(Board board, int[] moves, int mIndex) {
		this.moves = moves;
		bbAttacks = BitboardAttacks.getInstance();

		moveIndex = mIndex;
		all = board.getAll(); // only for clearity
		mines = board.getMines();
		others = board.getOthers();

		byte index = 0;
		long square = 0x1L;
		while (square != 0) {
			if (board.getTurn() == ((square & board.whites) != 0)) {

				if ((square & board.rooks) != 0) { // Rook
					generateMovesFromAttacks(Move.ROOK, index, bbAttacks.getRookAttacks(index, all));
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Move.BISHOP, index, bbAttacks.getBishopAttacks(index, all));
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Move.QUEEN, index, bbAttacks.getRookAttacks(index, all));
					generateMovesFromAttacks(Move.QUEEN, index, bbAttacks.getBishopAttacks(index, all));
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Move.KING, index, bbAttacks.king[index]);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Move.KNIGHT, index, bbAttacks.knight[index]);
				} else if ((square & board.pawns) != 0) { // Pawns
					if ((square & board.whites) != 0) {
						if (((square << 8) & all) == 0) {
							addMoves(Move.PAWN, index, index + 8, (square << 8), false, true, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_d) != 0) && (((square << 16) & all) == 0))
								addMoves(Move.PAWN, index, index + 16, (square << 16), false, false, 0);
						}
						generatePawnCapturesFromAttacks(index, bbAttacks.pawnUpwards[index], board.getPassantSquare());
					} else {
						if (((square >>> 8) & all) == 0) {
							addMoves(Move.PAWN, index, index - 8, (square >>> 8), false, true, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_u) != 0) && (((square >>> 16) & all) == 0))
								addMoves(Move.PAWN, index, index - 16, (square >>> 16), false, false, 0);
						}
						generatePawnCapturesFromAttacks(index, bbAttacks.pawnDownwards[index], board.getPassantSquare());
					}
				}
			}
			square <<= 1;
			index++;
		}

		square = board.kings & mines; // my king
		int myKingIndex = -1;
		// Castling: disabled when in check or squares attacked
		if ((((all & (board.getTurn() ? 0x06L : 0x0600000000000000L)) == 0 &&
				(board.getTurn() ? board.getWhiteKingsideCastling() : board.getBlackKingsideCastling())))) {
			myKingIndex = BitboardUtils.square2Index(square);
			if (!board.getCheck() &&
					!bbAttacks.isIndexAttacked(board, (byte) (myKingIndex - 1), board.getTurn())
					&& !bbAttacks.isIndexAttacked(board, (byte) (myKingIndex - 2), board.getTurn()))
				addMoves(Move.KING, myKingIndex, myKingIndex - 2, 0, false, false, Move.TYPE_KINGSIDE_CASTLING);
		}
		if ((((all & (board.getTurn() ? 0x70L : 0x7000000000000000L)) == 0 &&
				(board.getTurn() ? board.getWhiteQueensideCastling() : board.getBlackQueensideCastling())))) {
			if (myKingIndex == -1) {
				myKingIndex = BitboardUtils.square2Index(square);
			}
			if (!board.getCheck() &&
					!bbAttacks.isIndexAttacked(board, (byte) (myKingIndex + 1), board.getTurn())
					&& !bbAttacks.isIndexAttacked(board, (byte) (myKingIndex + 2), board.getTurn()))
				addMoves(Move.KING, myKingIndex, myKingIndex + 2, 0, false, false, Move.TYPE_QUEENSIDE_CASTLING);
		}
		return moveIndex;
	}

	/**
	 * Generates moves from an attack mask
	 */
	private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long attacks) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			// If we collide with other piece (or other piece and cannot capture), this is blocking
			if ((to & mines) == 0) {
				// Capturing
				addMoves(pieceMoved, fromIndex, BitboardUtils.square2Index(to), to, ((to & others) != 0), true, 0);
			}
			attacks ^= to;
		}
	}

	private void generatePawnCapturesFromAttacks(int fromIndex, long attacks, long passant) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			if ((to & others) != 0) {
				addMoves(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, true, true, 0);
			} else if ((to & passant) != 0) {
				addMoves(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, true, true, Move.TYPE_PASSANT);
			}
			attacks ^= to;
		}
	}

	/**
	 * Adds an operation
	 * to onlyneeded for captures
	 */
	private void addMoves(int pieceMoved, int fromIndex, int toIndex, long to, boolean capture, boolean checkPromotion, int moveType) {

		if (checkPromotion && (pieceMoved == Move.PAWN) && ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0)) {
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_QUEEN);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_KNIGHT);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_ROOK);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_BISHOP);
		} else {
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
		}
	}
}