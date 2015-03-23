package com.alonsoruibal.chess.movegen;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
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
					generateMovesFromAttacks(Move.ROOK, index, bbAttacks.getRookAttacks(index, all) & ~mines);
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Move.BISHOP, index, bbAttacks.getBishopAttacks(index, all) & ~mines);
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Move.QUEEN, index, (bbAttacks.getRookAttacks(index, all) | bbAttacks.getBishopAttacks(index, all)) & ~mines);
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Move.KING, index, bbAttacks.king[index] & ~mines);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Move.KNIGHT, index, bbAttacks.knight[index] & ~mines);
				} else if ((square & board.pawns) != 0) { // Pawns
					if ((square & board.whites) != 0) {
						if (((square << 8) & all) == 0) {
							addMoves(Move.PAWN, index, index + 8, false, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_d) != 0) && (((square << 16) & all) == 0))
								addMoves(Move.PAWN, index, index + 16, false, 0);
						}
						generatePawnCapturesFromAttacks(index, bbAttacks.pawnUpwards[index], board.getPassantSquare());
					} else {
						if (((square >>> 8) & all) == 0) {
							addMoves(Move.PAWN, index, index - 8, false, 0);
							// Two squares if it is in he first row	
							if (((square & BitboardUtils.b2_u) != 0) && (((square >>> 16) & all) == 0))
								addMoves(Move.PAWN, index, index - 16, false, 0);
						}
						generatePawnCapturesFromAttacks(index, bbAttacks.pawnDownwards[index], board.getPassantSquare());
					}
				}
			}
			square <<= 1;
			index++;
		}

		// Castling: disabled when in check or king route attacked
		if (!board.getCheck()) {
			if (board.getTurn() ? board.getWhiteKingsideCastling() : board.getBlackKingsideCastling()) {
				long rookOrigin = 1L << board.castlingKingsideRookOrigin[board.getTurn() ? 0 : 1];
				long rookDestiny = 1L << board.CASTLING_KINGSIDE_ROOK_DESTINY[board.getTurn() ? 0 : 1];
				long rookRoute = BitboardUtils.getHorizontalLine(rookDestiny, rookOrigin) & ~rookOrigin;
				long kingOrigin = board.kings & mines;
				long kingDestiny = 1L << board.CASTLING_KINGSIDE_KING_DESTINY[board.getTurn() ? 0 : 1];
				long kingRoute = BitboardUtils.getHorizontalLine(kingOrigin, kingDestiny) & ~kingOrigin;
				if ((all & (kingRoute | rookRoute) & ~rookOrigin & ~kingOrigin) == 0 //
						&& !bbAttacks.areSquaresAttacked(board, kingRoute, board.getTurn())) {
					addMoves(Move.KING, BitboardUtils.square2Index(kingOrigin), BitboardUtils.square2Index(board.chess960 ? rookOrigin : kingDestiny), false, Move.TYPE_KINGSIDE_CASTLING);
				}
			}
			if (board.getTurn() ? board.getWhiteQueensideCastling() : board.getBlackQueensideCastling()) {
				long rookOrigin = 1L << board.castlingQueensideRookOrigin[board.getTurn() ? 0 : 1];
				long rookDestiny = 1L << board.CASTLING_QUEENSIDE_ROOK_DESTINY[board.getTurn() ? 0 : 1];
				long rookRoute = BitboardUtils.getHorizontalLine(rookOrigin, rookDestiny) & ~rookOrigin;
				long kingOrigin = board.kings & mines;
				long kingDestiny = 1L << board.CASTLING_QUEENSIDE_KING_DESTINY[board.getTurn() ? 0 : 1];
				long kingRoute = BitboardUtils.getHorizontalLine(kingDestiny, kingOrigin) & ~kingOrigin;
				if ((all & (kingRoute | rookRoute) & ~rookOrigin & ~kingOrigin) == 0 //
						&& !bbAttacks.areSquaresAttacked(board, kingRoute, board.getTurn())) {
					addMoves(Move.KING, BitboardUtils.square2Index(kingOrigin), BitboardUtils.square2Index(board.chess960 ? rookOrigin : kingDestiny), false, Move.TYPE_QUEENSIDE_CASTLING);
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
				addMoves(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), true, 0);
			} else if ((to & passant) != 0) {
				addMoves(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), true, Move.TYPE_PASSANT);
			}
			attacks ^= to;
		}
	}

	/**
	 * Adds a move
	 */
	private void addMoves(int pieceMoved, int fromIndex, int toIndex, boolean capture, int moveType) {
		if (pieceMoved == Move.PAWN && (toIndex < 8 || toIndex >= 56)) {
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_QUEEN);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_KNIGHT);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_ROOK);
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, Move.TYPE_PROMOTION_BISHOP);
		} else {
			moves[moveIndex++] = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
		}
	}
}