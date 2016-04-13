package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/**
 * For efficiency Moves are int, this is a static class to threat with this
 * <p>
 * Move format (18 bits):
 * MTXCPPPFFFFFFTTTTTT
 * -------------^ To index (6 bits)
 * -------^ From index (6 bits)
 * ----^ Piece moved (3 bits)
 * ---^ Is capture (1 bit)
 * --^ Is check (1 bit)
 * ^ Move type (2 bits)
 *
 * @author Alberto Alonso Ruibal
 */
public class Move {
	// Predefined moves
	public static final int NONE = 0;
	public static final int NULL = -1;

	public static final String NONE_STRING = "none";
	public static final String NULL_STRING = "null";

	public static final String PIECE_LETTERS_LOWERCASE = " pnbrqk";
	public static final String PIECE_LETTERS_UPPERCASE = " PNBRQK";

	// Move Types
	public static final int TYPE_KINGSIDE_CASTLING = 1;
	public static final int TYPE_QUEENSIDE_CASTLING = 2;
	public static final int TYPE_PASSANT = 3;
	// Promotions must be always >= TYPE_PROMOTION_QUEEN
	public static final int TYPE_PROMOTION_QUEEN = 4;
	public static final int TYPE_PROMOTION_KNIGHT = 5;
	public static final int TYPE_PROMOTION_BISHOP = 6;
	public static final int TYPE_PROMOTION_ROOK = 7;

	public static final int CHECK_MASK = 0x1 << 16;
	public static final int CAPTURE_MASK = 0x1 << 15;

	public static int genMove(int fromIndex, int toIndex, int pieceMoved, boolean capture, boolean check, int moveType) {
		return toIndex | fromIndex << 6 | pieceMoved << 12 | (capture ? CAPTURE_MASK : 0) | (check ? CHECK_MASK : 0) | moveType << 17;
	}

	public static int genMove(int fromIndex, int toIndex, int pieceMoved, boolean capture, int moveType) {
		return toIndex | fromIndex << 6 | pieceMoved << 12 | (capture ? CAPTURE_MASK : 0) | moveType << 17;
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

	public static int getPieceMoved(int move) {
		return ((move >>> 12) & 0x7);
	}

	public static int getPieceCaptured(Board board, int move) {
		if (getMoveType(move) == TYPE_PASSANT) {
			return Piece.PAWN;
		}
		long toSquare = getToSquare(move);
		if ((toSquare & board.pawns) != 0) {
			return Piece.PAWN;
		} else if ((toSquare & board.knights) != 0) {
			return Piece.KNIGHT;
		} else if ((toSquare & board.bishops) != 0) {
			return Piece.BISHOP;
		} else if ((toSquare & board.rooks) != 0) {
			return Piece.ROOK;
		} else if ((toSquare & board.queens) != 0) {
			return Piece.QUEEN;
		}
		return 0;
	}

	public static boolean isCapture(int move) {
		return (move & CAPTURE_MASK) != 0;
	}

	public static boolean isCheck(int move) {
		return (move & CHECK_MASK) != 0;
	}

	public static boolean isCaptureOrCheck(int move) {
		return (move & (CHECK_MASK | CAPTURE_MASK)) != 0;
	}

	public static int getMoveType(int move) {
		return ((move >>> 17) & 0x7);
	}

	// Pawn push to 7 or 8th rank
	public static boolean isPawnPush(int move) {
		return Move.getPieceMoved(move) == Piece.PAWN && (Move.getToIndex(move) < 16 || Move.getToIndex(move) > 47);
	}

	// Pawn push to 6, 7 or 8th rank
	public static boolean isPawnPush678(int move) {
		return Move.getPieceMoved(move) == Piece.PAWN && (Move.getFromIndex(move) < Move.getToIndex(move) ? Move.getToIndex(move) >= 40 : Move.getToIndex(move) < 24);
	}

	// Pawn push to 5, 6, 7 or 8th rank
	public static boolean isPawnPush5678(int move) {
		return Move.getPieceMoved(move) == Piece.PAWN && (Move.getFromIndex(move) < Move.getToIndex(move) ? Move.getToIndex(move) >= 32 : Move.getToIndex(move) < 32);
	}

	/**
	 * Checks if this move is a promotion
	 */
	public static boolean isPromotion(int move) {
		return Move.getMoveType(move) >= TYPE_PROMOTION_QUEEN;
	}

	public static int getPiecePromoted(int move) {
		switch (getMoveType(move)) {
			case TYPE_PROMOTION_QUEEN:
				return Piece.QUEEN;
			case TYPE_PROMOTION_ROOK:
				return Piece.ROOK;
			case TYPE_PROMOTION_KNIGHT:
				return Piece.KNIGHT;
			case TYPE_PROMOTION_BISHOP:
				return Piece.BISHOP;
		}
		return 0;
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
		if (NULL_STRING.equals(move)) {
			return Move.NULL;
		} else if ("".equals(move) || NONE_STRING.equals(move)) {
			return Move.NONE;
		}

		int fromIndex;
		int toIndex;
		int moveType = 0;
		int pieceMoved = 0;
		boolean check = move.indexOf("+") > 0 || move.indexOf("#") > 0;
		long mines = board.getMines();
		boolean turn = board.getTurn();

		// Ignore checks, captures indicators...
		move = move.replace("+", "").replace("x", "").replace("-", "").replace("=", "").replace("#", "").replaceAll(" ", "").replaceAll("0", "o").replaceAll("O", "o");

		if ("oo".equals(move)) {
			move = BitboardUtils.SQUARE_NAMES[BitboardUtils.square2Index(board.kings & mines)] + //
					BitboardUtils.SQUARE_NAMES[BitboardUtils.square2Index(board.chess960 ? board.castlingRooks[turn ? 0 : 2] : Board.CASTLING_KING_DESTINY_SQUARE[turn ? 0 : 2])];
		} else if ("ooo".equals(move)) {
			move = BitboardUtils.SQUARE_NAMES[BitboardUtils.square2Index(board.kings & mines)] + //
					BitboardUtils.SQUARE_NAMES[BitboardUtils.square2Index(board.chess960 ? board.castlingRooks[turn ? 1 : 3] : Board.CASTLING_KING_DESTINY_SQUARE[turn ? 1 : 3])];
		} else {
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
		}

		// To is always the last 2 characters
		toIndex = BitboardUtils.algebraic2Index(move.substring(move.length() - 2, move.length()));
		long to = 0x1L << toIndex;
		long from = 0;

		BitboardAttacks bbAttacks = BitboardAttacks.getInstance();

		// Fills from with a mask of possible from values
		switch (move.charAt(0)) {
			case 'N':
				from = board.knights & mines & bbAttacks.knight[toIndex];
				break;
			case 'K':
				from = board.kings & mines & bbAttacks.king[toIndex];
				break;
			case 'R':
				from = board.rooks & mines & bbAttacks.getRookAttacks(toIndex, board.getAll());
				break;
			case 'B':
				from = board.bishops & mines & bbAttacks.getBishopAttacks(toIndex, board.getAll());
				break;
			case 'Q':
				from = board.queens & mines & (bbAttacks.getRookAttacks(toIndex, board.getAll()) | bbAttacks.getBishopAttacks(toIndex, board.getAll()));
				break;
		}
		if (from != 0) { // remove the piece char
			move = move.substring(1);
		} else { // Pawn moves
			if (move.length() == 2) {
				if (turn) {
					from = board.pawns & mines & ((to >>> 8) | (((to >>> 8) & board.getAll()) == 0 ? (to >>> 16) : 0));
				} else {
					from = board.pawns & mines & ((to << 8) | (((to << 8) & board.getAll()) == 0 ? (to << 16) : 0));
				}
			}
			if (move.length() == 3) { // Pawn capture
				from = board.pawns & mines &  bbAttacks.pawn[turn ? Color.B : Color.W][toIndex];
			}
		}
		if (move.length() == 3) { // now disambiaguate
			char disambiguate = move.charAt(0);
			int i = "abcdefgh".indexOf(disambiguate);
			if (i >= 0) {
				from &= BitboardUtils.FILE[i];
			}
			int j = "12345678".indexOf(disambiguate);
			if (j >= 0) {
				from &= BitboardUtils.RANK[j];
			}
		}
		if (move.length() == 4) { // was algebraic complete e2e4 (=UCI!)
			from = BitboardUtils.algebraic2Square(move.substring(0, 2));
		}
		if (from == 0 || (from & board.getMines()) == 0) {
			return NONE;
		}

		// Treats multiple froms, choosing the first Legal Move
		while (from != 0) {
			long myFrom = BitboardUtils.lsb(from);
			from ^= myFrom;
			fromIndex = BitboardUtils.square2Index(myFrom);

			boolean capture = false;
			if ((myFrom & board.pawns) != 0) {

				pieceMoved = Piece.PAWN;
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
				pieceMoved = Piece.BISHOP;
			} else if ((myFrom & board.knights) != 0) {
				pieceMoved = Piece.KNIGHT;
			} else if ((myFrom & board.rooks) != 0) {
				pieceMoved = Piece.ROOK;
			} else if ((myFrom & board.queens) != 0) {
				pieceMoved = Piece.QUEEN;
			} else if ((myFrom & board.kings) != 0) {
				pieceMoved = Piece.KING;
				if ((turn ? board.getWhiteKingsideCastling() : board.getBlackKingsideCastling()) && //
						(toIndex == (fromIndex - 2) || to == board.castlingRooks[turn ? 0 : 2])) {
					moveType = TYPE_KINGSIDE_CASTLING;
				}
				if ((turn ? board.getWhiteQueensideCastling() : board.getBlackQueensideCastling()) && //
						(toIndex == (fromIndex + 2) || to == board.castlingRooks[turn ? 1 : 3])) {
					moveType = TYPE_QUEENSIDE_CASTLING;
				}
			}

			// Now set captured piece flag
			if ((to & (turn ? board.blacks : board.whites)) != 0) {
				capture = true;
			}
			int moveInt = Move.genMove(fromIndex, toIndex, pieceMoved, capture, check, moveType);
			if (checkLegality) {
				if (board.doMove(moveInt, true, false)) {
					if (board.getCheck()) {
						moveInt = moveInt | CHECK_MASK; // If the move didn't has the check flag set
					}
					board.undoMove();
					return moveInt;
				}
			} else {
				return moveInt;
			}
		}
		return NONE;
	}

	/**
	 * Gets an UCI-String representation of the move
	 *
	 * @param move
	 * @return
	 */
	public static String toString(int move) {
		if (move == Move.NONE) {
			return NONE_STRING;
		} else if (move == Move.NULL) {
			return NULL_STRING;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(BitboardUtils.index2Algebraic(Move.getFromIndex(move)));
		sb.append(BitboardUtils.index2Algebraic(Move.getToIndex(move)));
		if (isPromotion(move)) {
			sb.append(PIECE_LETTERS_LOWERCASE.charAt(getPiecePromoted(move)));
		}
		return sb.toString();
	}

	public static String toStringExt(int move) {
		if (move == Move.NONE) {
			return NONE_STRING;
		} else if (move == Move.NULL) {
			return NULL_STRING;
		} else if (Move.getMoveType(move) == TYPE_KINGSIDE_CASTLING) {
			return Move.isCheck(move) ? "O-O+" : "O-O";
		} else if (Move.getMoveType(move) == TYPE_QUEENSIDE_CASTLING) {
			return Move.isCheck(move) ? "O-O-O+" : "O-O-O";
		}

		StringBuilder sb = new StringBuilder();
		if (getPieceMoved(move) != Piece.PAWN) {
			sb.append(PIECE_LETTERS_UPPERCASE.charAt(getPieceMoved(move)));
		}
		sb.append(BitboardUtils.index2Algebraic(Move.getFromIndex(move)));
		sb.append(isCapture(move) ? 'x' : '-');
		sb.append(BitboardUtils.index2Algebraic(Move.getToIndex(move)));
		if (isPromotion(move)) {
			sb.append(PIECE_LETTERS_LOWERCASE.charAt(getPiecePromoted(move)));
		}
		if (isCheck(move)) {
			sb.append("+");
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
		if (move == Move.NONE) {
			return NONE_STRING;
		} else if (move == Move.NULL) {
			return NULL_STRING;
		}
		board.generateLegalMoves();

		boolean isLegal = false;
		boolean disambiguate = false;
		boolean fileEqual = false;
		boolean rankEqual = false;
		for (int i = 0; i < board.legalMoveCount; i++) {
			int move2 = board.legalMoves[i];
			if (move == move2) {
				isLegal = true;
			} else if (getToIndex(move) == getToIndex(move2) && (getPieceMoved(move) == getPieceMoved(move2))) {
				disambiguate = true;
				if ((getFromIndex(move) % 8) == (getFromIndex(move2) % 8)) {
					fileEqual = true;
				}
				if ((getFromIndex(move) / 8) == (getFromIndex(move2) / 8)) {
					rankEqual = true;
				}
			}
		}
		if (!isLegal) {
			return Move.NONE_STRING;
		} else if (Move.getMoveType(move) == TYPE_KINGSIDE_CASTLING) {
			return Move.isCheck(move) ? "O-O+" : "O-O";
		} else if (Move.getMoveType(move) == TYPE_QUEENSIDE_CASTLING) {
			return Move.isCheck(move) ? "O-O-O+" : "O-O-O";
		}

		StringBuilder sb = new StringBuilder();
		if (getPieceMoved(move) != Piece.PAWN) {
			sb.append(PIECE_LETTERS_UPPERCASE.charAt(getPieceMoved(move)));
		}
		String fromSq = BitboardUtils.index2Algebraic(Move.getFromIndex(move));

		if (isCapture(move) && getPieceMoved(move) == Piece.PAWN) {
			disambiguate = true;
		}

		if (disambiguate) {
			if (fileEqual && rankEqual) {
				sb.append(fromSq);
			} else if (fileEqual) {
				sb.append(fromSq.charAt(1));
			} else {
				sb.append(fromSq.charAt(0));
			}
		}

		if (isCapture(move)) {
			sb.append("x");
		}
		sb.append(BitboardUtils.index2Algebraic(Move.getToIndex(move)));
		if (isPromotion(move)) {
			sb.append(PIECE_LETTERS_UPPERCASE.charAt(getPiecePromoted(move)));
		}
		if (isCheck(move)) {
			sb.append("+");
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

	public static String sanToFigurines(String in) {
		return in == null ? null : in.replace("N", "♘").replace("B", "♗").replace("R", "♖").replace("Q", "♕").replace("K", "♔");
	}
}