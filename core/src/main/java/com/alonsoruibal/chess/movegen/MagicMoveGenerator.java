package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Color;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/**
 * Magic move generator
 * Generate pseudo-legal moves because can leave the king in check.
 * It does not set the check flag.
 *
 * @author Alberto Alonso Ruibal
 */
public class MagicMoveGenerator implements MoveGenerator {

	private int[] moves;
	private int moveIndex;
	private long all;
	private long mines;
	private long others;

	private final AttacksInfo ai = new AttacksInfo();

	public int generateMoves(Board board, int[] moves, int startIndex) {
		this.moves = moves;
		ai.build(board);

		moveIndex = startIndex;
		all = board.getAll();
		mines = board.getMines();
		others = board.getOthers();

		int index = 0;
		long square = 0x1L;
		while (square != 0) {
			if (board.getTurn() == ((square & board.whites) != 0)) {
				if ((square & board.rooks) != 0) { // Rook
					generateMovesFromAttacks(Piece.ROOK, index, ai.attacksFromSquare[index] & ~mines);
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Piece.BISHOP, index, ai.attacksFromSquare[index] & ~mines);
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Piece.QUEEN, index, ai.attacksFromSquare[index] & ~mines);
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Piece.KING, index, ai.attacksFromSquare[index] & ~mines);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Piece.KNIGHT, index, ai.attacksFromSquare[index] & ~mines);
				} else if ((square & board.pawns) != 0) { // Pawns
					if ((square & board.whites) != 0) {
						if (((square << 8) & all) == 0) {
							addMoves(Piece.PAWN, index, index + 8, false, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_d) != 0) && (((square << 16) & all) == 0)) {
								addMoves(Piece.PAWN, index, index + 16, false, 0);
							}
						}
						generatePawnCapturesFromAttacks(index, ai.attacksFromSquare[index], board.getPassantSquare());
					} else {
						if (((square >>> 8) & all) == 0) {
							addMoves(Piece.PAWN, index, index - 8, false, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_u) != 0) && (((square >>> 16) & all) == 0)) {
								addMoves(Piece.PAWN, index, index - 16, false, 0);
							}
						}
						generatePawnCapturesFromAttacks(index, ai.attacksFromSquare[index], board.getPassantSquare());
					}
				}
			}
			square <<= 1;
			index++;
		}

		// Castling: disabled when in check or king route attacked
		if (!board.getCheck()) {
			int us = board.getTurn() ? Color.W : Color.B;

			long kingCastlingDestination = board.canCastleKingSide(us, ai);
			if (kingCastlingDestination != 0) {
				addMoves(Piece.KING, ai.kingIndex[us], BitboardUtils.square2Index(kingCastlingDestination), false, Move.TYPE_KINGSIDE_CASTLING);
			}
			long queenCastlingDestination = board.canCastleQueenSide(us, ai);
			if (queenCastlingDestination != 0) {
				addMoves(Piece.KING, ai.kingIndex[us], BitboardUtils.square2Index(queenCastlingDestination), false, Move.TYPE_QUEENSIDE_CASTLING);
			}
		}

		return moveIndex;
	}

	/**
	 * Generates moves from an attack mask
	 */
	private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long attacks) {
		while (attacks != 0) {
			long to = Long.lowestOneBit(attacks);
			addMoves(pieceMoved, fromIndex, BitboardUtils.square2Index(to), ((to & others) != 0), 0);
			attacks ^= to;
		}
	}

	private void generatePawnCapturesFromAttacks(int fromIndex, long attacks, long passant) {
		while (attacks != 0) {
			long to = Long.lowestOneBit(attacks);
			if ((to & others) != 0) {
				addMoves(Piece.PAWN, fromIndex, BitboardUtils.square2Index(to), true, 0);
			} else if ((to & passant) != 0) {
				addMoves(Piece.PAWN, fromIndex, BitboardUtils.square2Index(to), true, Move.TYPE_PASSANT);
			}
			attacks ^= to;
		}
	}

	/**
	 * Adds a move (it can add a non legal move)
	 */
	private void addMoves(int pieceMoved, int fromIndex, int toIndex, boolean capture, int moveType) {
		if (pieceMoved == Piece.PAWN && (toIndex < 8 || toIndex >= 56)) {
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_QUEEN);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_KNIGHT);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_ROOK);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_BISHOP);
		} else {
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
		}
	}
}