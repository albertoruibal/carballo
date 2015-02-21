package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/**
 * For efficience Moves are int, this is a static class to threat with this
 * moves score is limited to 10 bits (positive) 1024 values
 *
 * @author Alberto Alonso Ruibal
 */
public class Move {

	// Move pieces ordered by value
	public static final int PAWN = 1;
	public static final int KNIGHT = 2;
	public static final int BISHOP = 3;
	public static final int ROOK = 4;
	public static final int QUEEN = 5;
	public static final int KING = 6;

	// Move Types
	public static final int TYPE_KINGSIDE_CASTLING = 1;
	public static final int TYPE_QUEENSIDE_CASTLING = 2;
	public static final int TYPE_PASSANT = 3;
	// Promotions must be always >= TYPE_PROMOTION_QUEEN
	public static final int TYPE_PROMOTION_QUEEN = 4;
	public static final int TYPE_PROMOTION_KNIGHT = 5;
	public static final int TYPE_PROMOTION_BISHOP = 6;
	public static final int TYPE_PROMOTION_ROOK = 7;

	public static int genMove(int fromIndex, int toIndex, int pieceMoved, boolean capture, int moveType) {
		return toIndex | fromIndex << 6 | pieceMoved << 12 | (capture ? 1 << 15 : 0) | moveType << 16;
	}

	public static int getToIndex(int move) {
		return move & 0x3f;
	}

	public static long getToSquare(int move) {
		return 0x1L << (move & 0x3f);
	}

	public static int getFromIndex(int move) {
		return ((move >>> 6) & 0x3f);
	}

	public static long getFromSquare(int move) {
		return 0x1L << ((move >>> 6) & 0x3f);
	}

	/**
	 * square index in a 64*64 array (12 bits)
	 */
	public static int getFromToIndex(int move) {
		return move & 0xfff;
	}

	public static int getPieceMoved(int move) {
		return ((move >>> 12) & 0x7);
	}

	public static boolean getCapture(int move) {
		return ((move >>> 15) & 0x1) != 0;
	}

	public static int getMoveType(int move) {
		return ((move >>> 16) & 0x7);
	}

	public static boolean isCapture(int move) {
		return (move & (0x1 << 15)) != 0;
	}

	// Pawn push to 7 or 8th rank
	public static boolean isPawnPush(int move) {
		return Move.getPieceMoved(move) == PAWN && (Move.getToIndex(move) < 16 || Move.getToIndex(move) > 47);
	}

	// Pawn push to 6, 7 or 8th rank
	public static boolean isPawnPush678(int move) {
		return Move.getPieceMoved(move) == PAWN && (Move.getFromIndex(move) < Move.getToIndex(move) ? Move.getToIndex(move) >= 40 : Move.getToIndex(move) < 24);
	}

	// Pawn push to 5, 6, 7 or 8th rank
	public static boolean isPawnPush5678(int move) {
		return Move.getPieceMoved(move) == PAWN && (Move.getFromIndex(move) < Move.getToIndex(move) ? Move.getToIndex(move) >= 32 : Move.getToIndex(move) < 32);
	}

	/**
	 * Checks if this move is a promotion
	 */
	public static boolean isPromotion(int move) {
		return Move.getMoveType(move) >= TYPE_PROMOTION_QUEEN;
	}

	/**
	 * Is capture or promotion
	 *
	 * @param move
	 * @return
	 */
	public static boolean isTactical(int move) {
		return (Move.isCapture(move) || Move.isPromotion(move));
	}

	public static boolean isCastling(int move) {
		return Move.getMoveType(move) == TYPE_KINGSIDE_CASTLING || Move.getMoveType(move) == TYPE_QUEENSIDE_CASTLING;
	}

	/**
	 * Given a board creates a move from a String in uci format or short
	 * algebraic form. Checklegality true is mandatory if using sort algebraic
	 *
	 * @param board
	 * @param move
	 */
	public static int getFromString(Board board, String move, boolean checkLegality) {
		int fromIndex;
		int toIndex;
		int moveType = 0;
		int pieceMoved = 0;

		// Ignore checks, captures indicators...
		move = move.replace("+", "").replace("x", "").replace("-", "").replace("=", "").replace("#", "").replaceAll(" ", "").replaceAll("0", "o")
				.replaceAll("O", "o");
		if ("ooo".equals(move)) {
			if (board.getTurn()) {
				move = "e1c1";
			} else {
				move = "e8c8";
			}
		} else if ("oo".equals(move)) {
			if (board.getTurn()) {
				move = "e1g1";
			} else {
				move = "e8g8";
			}
		}
		char promo = move.charAt(move.length() - 1);
		switch (Character.toLowerCase(promo)) {
			case 'q':
				moveType = TYPE_PROMOTION_QUEEN;
				break;
			case 'n':
				moveType = TYPE_PROMOTION_KNIGHT;
				break;
			case 'b':
				moveType = TYPE_PROMOTION_BISHOP;
				break;
			case 'r':
				moveType = TYPE_PROMOTION_ROOK;
				break;
		}
		// If promotion, remove the last char
		if (moveType != 0) {
			move = move.substring(0, move.length() - 1);
		}

		// To is always the last 2 characters
		toIndex = BitboardUtils.algebraic2Index(move.substring(move.length() - 2, move.length()));
		long to = 0x1L << toIndex;
		long from = 0;

		BitboardAttacks bbAttacks = BitboardAttacks.getInstance();

		// Fills from with a mask of possible from values
		switch (move.charAt(0)) {
			case 'N':
				from = board.knights & board.getMines() & bbAttacks.knight[toIndex];
				break;
			case 'K':
				from = board.kings & board.getMines() & bbAttacks.king[toIndex];
				break;
			case 'R':
				from = board.rooks & board.getMines() & bbAttacks.getRookAttacks(toIndex, board.getAll());
				break;
			case 'B':
				from = board.bishops & board.getMines() & bbAttacks.getBishopAttacks(toIndex, board.getAll());
				break;
			case 'Q':
				from = board.queens & board.getMines()
						& (bbAttacks.getRookAttacks(toIndex, board.getAll()) | bbAttacks.getBishopAttacks(toIndex, board.getAll()));
				break;
		}
		if (from != 0) { // remove the piece char
			move = move.substring(1);
		} else { // Pawn moves
			if (move.length() == 2) {
				if (board.getTurn()) {
					from = board.pawns & board.getMines() & ((to >>> 8) | (((to >>> 8) & board.getAll()) == 0 ? (to >>> 16) : 0));
				} else {
					from = board.pawns & board.getMines() & ((to << 8) | (((to << 8) & board.getAll()) == 0 ? (to << 16) : 0));
				}
			}
			if (move.length() == 3) { // Pawn capture
				from = board.pawns & board.getMines() & (board.getTurn() ? bbAttacks.pawnDownwards[toIndex] : bbAttacks.pawnUpwards[toIndex]);
			}
		}
		if (move.length() == 3) { // now disambiaguate
			char disambiguate = move.charAt(0);
			int i = "abcdefgh".indexOf(disambiguate);
			if (i >= 0) {
				from &= BitboardUtils.COLUMN[i];
			}
			int j = "12345678".indexOf(disambiguate);
			if (j >= 0) {
				from &= BitboardUtils.RANK[j];
			}
		}
		if (move.length() == 4) { // was algebraic complete e2e4 (=UCI!)
			from = BitboardUtils.algebraic2Square(move.substring(0, 2));
		}
		if (from == 0) {
			return -1;
		}

		// Treats multiple froms, choosing the first Legal Move
		while (from != 0) {
			long myFrom = BitboardUtils.lsb(from);
			from ^= myFrom;
			fromIndex = BitboardUtils.square2Index(myFrom);

			boolean capture = false;
			if ((myFrom & board.pawns) != 0) {

				pieceMoved = PAWN;
				// for passant captures
				if ((toIndex != (fromIndex - 8)) && (toIndex != (fromIndex + 8)) && (toIndex != (fromIndex - 16)) && (toIndex != (fromIndex + 16))) {
					if ((to & board.getAll()) == 0) {
						moveType = TYPE_PASSANT;
						capture = true; // later is changed if it was not a pawn
					}
				}
				// Default promotion to queen if not specified
				if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0 && (moveType < TYPE_PROMOTION_QUEEN)) {
					moveType = TYPE_PROMOTION_QUEEN;
				}
			}
			if ((myFrom & board.bishops) != 0) {
				pieceMoved = BISHOP;
			} else if ((myFrom & board.knights) != 0) {
				pieceMoved = KNIGHT;
			} else if ((myFrom & board.rooks) != 0) {
				pieceMoved = ROOK;
			} else if ((myFrom & board.queens) != 0) {
				pieceMoved = QUEEN;
			} else if ((myFrom & board.kings) != 0) {
				pieceMoved = KING;
				// Only if origin square is king's initial square TODO FRC
				if (fromIndex == 3 || fromIndex == 3 + (8 * 7)) {
					if (toIndex == (fromIndex + 2)) {
						moveType = TYPE_QUEENSIDE_CASTLING;
					}
					if (toIndex == (fromIndex - 2)) {
						moveType = TYPE_KINGSIDE_CASTLING;
					}
				}
			}

			// Now set captured piece flag
			if ((to & (board.whites | board.blacks)) != 0) {
				capture = true;
			}
			int moveInt = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
			if (checkLegality) {
				if (board.doMove(moveInt, false)) {
					board.undoMove();
					return moveInt;
				}
			} else {
				return moveInt;
			}
		}
		return -1;
	}

	/**
	 * Gets an UCI-String representation of the move
	 *
	 * @param move
	 * @return
	 */
	public static String toString(int move) {
		if (move == 0 || move == -1) {
			return "none";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(BitboardUtils.index2Algebraic(Move.getFromIndex(move)));
		sb.append(BitboardUtils.index2Algebraic(Move.getToIndex(move)));
		switch (Move.getMoveType(move)) {
			case TYPE_PROMOTION_QUEEN:
				sb.append("q");
				break;
			case TYPE_PROMOTION_KNIGHT:
				sb.append("n");
				break;
			case TYPE_PROMOTION_BISHOP:
				sb.append("b");
				break;
			case TYPE_PROMOTION_ROOK:
				sb.append("r");
				break;
		}
		return sb.toString();
	}

	public static String toStringExt(int move) {
		if (move == 0 || move == -1) {
			return "none";
		} else if (Move.getMoveType(move) == TYPE_KINGSIDE_CASTLING) {
			return "O-O";
		} else if (Move.getMoveType(move) == TYPE_QUEENSIDE_CASTLING) {
			return "O-O-O";
		}

		StringBuilder sb = new StringBuilder();
		if (getPieceMoved(move) != Move.PAWN) {
			sb.append(" PNBRQK".charAt(getPieceMoved(move)));
		}
		sb.append(BitboardUtils.index2Algebraic(Move.getFromIndex(move)));
		sb.append(getCapture(move) ? 'x' : '-');
		sb.append(BitboardUtils.index2Algebraic(Move.getToIndex(move)));
		switch (Move.getMoveType(move)) {
			case TYPE_PROMOTION_QUEEN:
				sb.append("q");
				break;
			case TYPE_PROMOTION_KNIGHT:
				sb.append("n");
				break;
			case TYPE_PROMOTION_BISHOP:
				sb.append("b");
				break;
			case TYPE_PROMOTION_ROOK:
				sb.append("r");
				break;
		}
		return sb.toString();
	}

	/**
	 * It does not append + or #
	 *
	 * @param board
	 * @param move
	 * @return
	 */
	public static String toSan(Board board, int move) {
		board.generateLegalMoves();

		boolean isLegal = false;
		boolean disambiguate = false;
		boolean colEqual = false;
		boolean rowEqual = false;
		for (int i = 0; i < board.legalMoveCount; i++) {
			int move2 = board.legalMoves[i];
			if (move == move2) {
				isLegal = true;
			} else if (getToIndex(move) == getToIndex(move2) && (getPieceMoved(move) == getPieceMoved(move2))) {
				disambiguate = true;
				if ((getFromIndex(move) % 8) == (getFromIndex(move2) % 8)) {
					colEqual = true;
				}
				if ((getFromIndex(move) / 8) == (getFromIndex(move2) / 8)) {
					rowEqual = true;
				}
			}
		}
		if (move == 0 || move == -1 || !isLegal) {
			return "none";
		} else if (Move.getMoveType(move) == TYPE_KINGSIDE_CASTLING) {
			return "O-O";
		} else if (Move.getMoveType(move) == TYPE_QUEENSIDE_CASTLING) {
			return "O-O-O";
		}

		StringBuilder sb = new StringBuilder();
		if (getPieceMoved(move) != Move.PAWN) {
			sb.append(" PNBRQK".charAt(getPieceMoved(move)));
		}
		String fromSq = BitboardUtils.index2Algebraic(Move.getFromIndex(move));

		if (getCapture(move) && getPieceMoved(move) == Move.PAWN) {
			disambiguate = true;
		}

		if (disambiguate) {
			if (colEqual && rowEqual) {
				sb.append(fromSq);
			} else if (colEqual && !rowEqual) {
				sb.append(fromSq.charAt(1));
			} else {
				sb.append(fromSq.charAt(0));
			}
		}

		if (getCapture(move)) {
			sb.append("x");
		}
		sb.append(BitboardUtils.index2Algebraic(Move.getToIndex(move)));
		switch (Move.getMoveType(move)) {
			case TYPE_PROMOTION_QUEEN:
				sb.append("Q");
				break;
			case TYPE_PROMOTION_KNIGHT:
				sb.append("N");
				break;
			case TYPE_PROMOTION_BISHOP:
				sb.append("B");
				break;
			case TYPE_PROMOTION_ROOK:
				sb.append("R");
				break;
		}
		return sb.toString();
	}

	public static void printMoves(int moves[], int from, int to) {
		for (int i = from; i < to; i++) {
			System.out.print(Move.toStringExt(moves[i]));
			System.out.print(" ");
		}
		System.out.println();
	}
}