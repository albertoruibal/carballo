package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Color;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
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

	BitboardAttacks bbAttacks;

	public int generateMoves(Board board, int[] moves, int startIndex) {
		this.moves = moves;
		bbAttacks = BitboardAttacks.getInstance();

		moveIndex = startIndex;
		all = board.getAll();
		mines = board.getMines();
		others = board.getOthers();

		int index = 0;
		long square = 0x1L;
		while (square != 0) {
			if (board.getTurn() == ((square & board.whites) != 0)) {

				if ((square & board.rooks) != 0) { // Rook
					generateMovesFromAttacks(Piece.ROOK, index, bbAttacks.getRookAttacks(index, all) & ~mines);
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Piece.BISHOP, index, bbAttacks.getBishopAttacks(index, all) & ~mines);
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Piece.QUEEN, index, (bbAttacks.getRookAttacks(index, all) | bbAttacks.getBishopAttacks(index, all)) & ~mines);
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Piece.KING, index, bbAttacks.king[index] & ~mines);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Piece.KNIGHT, index, bbAttacks.knight[index] & ~mines);
				} else if ((square & board.pawns) != 0) { // Pawns
					if ((square & board.whites) != 0) {
						if (((square << 8) & all) == 0) {
							addMoves(Piece.PAWN, index, index + 8, false, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_d) != 0) && (((square << 16) & all) == 0)) {
								addMoves(Piece.PAWN, index, index + 16, false, 0);
							}
						}
						generatePawnCapturesFromAttacks(index, bbAttacks.pawn[Color.W][index], board.getPassantSquare());
					} else {
						if (((square >>> 8) & all) == 0) {
							addMoves(Piece.PAWN, index, index - 8, false, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_u) != 0) && (((square >>> 16) & all) == 0)) {
								addMoves(Piece.PAWN, index, index - 16, false, 0);
							}
						}
						generatePawnCapturesFromAttacks(index, bbAttacks.pawn[Color.B][index], board.getPassantSquare());
					}
				}
			}
			square <<= 1;
			index++;
		}

		// Castling: disabled when in check or king route attacked
		if (!board.getCheck()) {
			if (board.getTurn() ? board.getWhiteKingsideCastling() : board.getBlackKingsideCastling()) {
				long rookOrigin = board.castlingRooks[board.getTurn() ? 0 : 2];
				long rookDestiny = Board.CASTLING_ROOK_DESTINY_SQUARE[board.getTurn() ? 0 : 2];
				long rookRoute = BitboardUtils.getHorizontalLine(rookDestiny, rookOrigin) & ~rookOrigin;
				long kingOrigin = board.kings & mines;
				long kingDestiny = Board.CASTLING_KING_DESTINY_SQUARE[board.getTurn() ? 0 : 2];
				long kingRoute = BitboardUtils.getHorizontalLine(kingOrigin, kingDestiny) & ~kingOrigin;
				if ((all & (kingRoute | rookRoute) & ~rookOrigin & ~kingOrigin) == 0 //
						&& !bbAttacks.areSquaresAttacked(board, kingRoute, board.getTurn())) {
					addMoves(Piece.KING, BitboardUtils.square2Index(kingOrigin), BitboardUtils.square2Index(board.chess960 ? rookOrigin : kingDestiny), false, Move.TYPE_KINGSIDE_CASTLING);
				}
			}
			if (board.getTurn() ? board.getWhiteQueensideCastling() : board.getBlackQueensideCastling()) {
				long rookOrigin = board.castlingRooks[board.getTurn() ? 1 : 3];
				long rookDestiny = Board.CASTLING_ROOK_DESTINY_SQUARE[board.getTurn() ? 1 : 3];
				long rookRoute = BitboardUtils.getHorizontalLine(rookOrigin, rookDestiny) & ~rookOrigin;
				long kingOrigin = board.kings & mines;
				long kingDestiny = Board.CASTLING_KING_DESTINY_SQUARE[board.getTurn() ? 1 : 3];
				long kingRoute = BitboardUtils.getHorizontalLine(kingDestiny, kingOrigin) & ~kingOrigin;
				if ((all & (kingRoute | rookRoute) & ~rookOrigin & ~kingOrigin) == 0 //
						&& !bbAttacks.areSquaresAttacked(board, kingRoute, board.getTurn())) {
					addMoves(Piece.KING, BitboardUtils.square2Index(kingOrigin), BitboardUtils.square2Index(board.chess960 ? rookOrigin : kingDestiny), false, Move.TYPE_QUEENSIDE_CASTLING);
				}
			}
		}

		return moveIndex;
	}

	/**
	 * Generates moves from an attack mask
	 */
	private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long attacks) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			addMoves(pieceMoved, fromIndex, BitboardUtils.square2Index(to), ((to & others) != 0), 0);
			attacks ^= to;
		}
	}

	private void generatePawnCapturesFromAttacks(int fromIndex, long attacks, long passant) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			if ((to & others) != 0) {
				addMoves(Piece.PAWN, fromIndex, BitboardUtils.square2Index(to), true, 0);
			} else if ((to & passant) != 0) {
				addMoves(Piece.PAWN, fromIndex, BitboardUtils.square2Index(to), true, Move.TYPE_PASSANT);
			}
			attacks ^= to;
		}
	}

	/**
	 * Adds a move
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