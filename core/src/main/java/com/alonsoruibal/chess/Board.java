package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.hash.ZobristKey;
import com.alonsoruibal.chess.movegen.LegalMoveGenerator;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Stores the position and the move history
 * TODO Other chess variants like Atomic, Suicide, etc.
 *
 * @author Alberto Alonso Ruibal
 */
public class Board {
	public static final int MAX_MOVES = 1024;
	public static final String FEN_START_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	public static final String CHESS960_START_POSITIONS[] = {"QNNRKR", "NQNRKR", "NNQRKR", "NNRQKR", "NNRKQR", "NNRKRQ", "QNRNKR", "NQRNKR", "NRQNKR", "NRNQKR", "NRNKQR", "NRNKRQ", "QNRKNR", "NQRKNR", "NRQKNR", "NRKQNR", "NRKNQR", "NRKNRQ", "QNRKRN", "NQRKRN", "NRQKRN", "NRKQRN", "NRKRQN", "NRKRNQ", "QRNNKR", "RQNNKR", "RNQNKR", "RNNQKR", "RNNKQR", "RNNKRQ", "QRNKNR", "RQNKNR", "RNQKNR", "RNKQNR", "RNKNQR", "RNKNRQ", "QRNKRN", "RQNKRN", "RNQKRN", "RNKQRN", "RNKRQN", "RNKRNQ", "QRKNNR", "RQKNNR", "RKQNNR", "RKNQNR", "RKNNQR", "RKNNRQ", "QRKNRN", "RQKNRN", "RKQNRN", "RKNQRN", "RKNRQN", "RKNRNQ", "QRKRNN", "RQKRNN", "RKQRNN", "RKRQNN", "RKRNQN", "RKRNNQ"};
	public static final String CHESS960_START_POSITIONS_BISHOPS[] = {"BB------", "B--B----", "B----B--", "B------B", "-BB-----", "--BB----", "--B--B--", "--B----B", "-B--B---", "---BB---", "----BB--", "----B--B", "-B----B-", "---B--B-", "-----BB-", "------BB"};

	// Flags: must be changed only when moving
	private static final long FLAG_TURN = 0x0001L;
	private static final long FLAG_WHITE_KINGSIDE_CASTLING = 0x0002L;
	private static final long FLAG_WHITE_QUEENSIDE_CASTLING = 0x0004L;
	private static final long FLAG_BLACK_KINGSIDE_CASTLING = 0x0008L;
	private static final long FLAG_BLACK_QUEENSIDE_CASTLING = 0x0010L;
	private static final long FLAG_CHECK = 0x0020L;
	// Position on boarch in which is captured
	private static final long FLAGS_PASSANT = 0x0000ff0000ff0000L;

	// For the castlings {White Kingside, White Queenside, Black Kingside, Black Queenside}
	public static final int CASTLING_KING_DESTINY_INDEX[] = {1, 5, 57, 61};
	public static final long CASTLING_KING_DESTINY_SQUARE[] = {1L << 1, 1L << 5, 1L << 57, 1L << 61};
	public static final int CASTLING_ROOK_DESTINY_INDEX[] = {2, 4, 58, 60};
	public static final long CASTLING_ROOK_DESTINY_SQUARE[] = {1L << 2, 1L << 4, 1L << 58, 1L << 60};

	// For the SEE SWAP algorithm
	public static final int[] SEE_PIECE_VALUES = {0, 100, 325, 330, 500, 900, 9999};

	LegalMoveGenerator legalMoveGenerator = new LegalMoveGenerator();
	int[] legalMoves = new int[256];
	int legalMoveCount = -1; // if -1 then legal moves not generated
	long[] legalMovesKey = {0, 0};
	public HashMap<Integer, String> movesSan;

	// Bitboard arrays
	public long whites = 0;
	public long blacks = 0;
	public long pawns = 0;
	public long rooks = 0;
	public long queens = 0;
	public long bishops = 0;
	public long knights = 0;
	public long kings = 0;
	public long flags = 0;

	public int fiftyMovesRule = 0;
	public int initialMoveNumber = 0;
	public int moveNumber = 0;
	public int outBookMove = Integer.MAX_VALUE;
	public long[] key = {0, 0};

	public String initialFen;

	// History array indexed by moveNumber
	public long[][] keyHistory; // to detect draw by treefold
	public int[] moveHistory;
	public long[] whitesHistory;
	public long[] blacksHistory;
	public long[] pawnsHistory;
	public long[] rooksHistory;
	public long[] queensHistory;
	public long[] bishopsHistory;
	public long[] knightsHistory;
	public long[] kingsHistory;
	public long[] flagsHistory;
	public int[] fiftyMovesRuleHistory;
	public int[] seeGain;

	// Origin squares for the castling rook {White Kingside, White Queenside, Black Kingside, Black Queenside}
	public long castlingRooks[] = {0, 0, 0, 0};

	public boolean chess960; // basically decides the destiny square of the castlings

	BitboardAttacks bbAttacks;

	public Board() {
		whitesHistory = new long[MAX_MOVES];
		blacksHistory = new long[MAX_MOVES];
		pawnsHistory = new long[MAX_MOVES];
		knightsHistory = new long[MAX_MOVES];
		bishopsHistory = new long[MAX_MOVES];
		rooksHistory = new long[MAX_MOVES];
		queensHistory = new long[MAX_MOVES];
		kingsHistory = new long[MAX_MOVES];
		flagsHistory = new long[MAX_MOVES];
		keyHistory = new long[MAX_MOVES][2];
		fiftyMovesRuleHistory = new int[MAX_MOVES];

		seeGain = new int[32];

		moveHistory = new int[MAX_MOVES];

		movesSan = new HashMap<>();

		bbAttacks = BitboardAttacks.getInstance();
	}

	/**
	 * It also computes the zobrist key
	 */
	public void startPosition() {
		setFen(FEN_START_POSITION);
	}

	/**
	 * Set a Chess960 start position
	 * http://en.wikipedia.org/wiki/Chess960_numbering_scheme
	 */
	public void startPosition(int chess960Position) {
		String base = CHESS960_START_POSITIONS_BISHOPS[chess960Position & 0x0f];
		String otherPieces = CHESS960_START_POSITIONS[chess960Position >>> 4];
		StringBuilder oSB = new StringBuilder();
		int j = 0;
		for (int i = 0; i < 8; i++) {
			if (base.charAt(i) == '-') {
				oSB.append(otherPieces.charAt(j));
				j++;
			} else {
				oSB.append('B');
			}
		}

		String fen = oSB.toString().toLowerCase() + "/pppppppp/8/8/8/8/PPPPPPPP/" + oSB.toString() + " w KQkq - 0 1";
		setFen(fen);
		chess960 = true;
	}

	public long getKey() {
		return key[0] ^ key[1];
	}

	public long getExclusionKey() {
		return key[0] ^ key[1] ^ ZobristKey.exclusionKey;
	}

	/**
	 * An alternative key to avoid collisions in the TT
	 */
	public long getKey2() {
		return key[0] ^ ~key[1];
	}

	public int getMoveNumber() {
		return moveNumber;
	}

	/**
	 * @return true if white moves
	 */
	public final boolean getTurn() {
		return (flags & FLAG_TURN) == 0;
	}

	public boolean getWhiteKingsideCastling() {
		return (flags & FLAG_WHITE_KINGSIDE_CASTLING) != 0;
	}

	public boolean getWhiteQueensideCastling() {
		return (flags & FLAG_WHITE_QUEENSIDE_CASTLING) != 0;
	}

	public boolean getBlackKingsideCastling() {
		return (flags & FLAG_BLACK_KINGSIDE_CASTLING) != 0;
	}

	public boolean getBlackQueensideCastling() {
		return (flags & FLAG_BLACK_QUEENSIDE_CASTLING) != 0;
	}

	public long getPassantSquare() {
		return flags & FLAGS_PASSANT;
	}

	public boolean getCheck() {
		return (flags & FLAG_CHECK) != 0;
	}

	public long getAll() {
		return whites | blacks;
	}

	public long getMines() {
		return (flags & FLAG_TURN) == 0 ? whites : blacks;
	}

	public long getOthers() {
		return (flags & FLAG_TURN) == 0 ? blacks : whites;
	}

	public int getPieceIntAt(long square) {
		return ((pawns & square) != 0 ? Piece.PAWN : //
				((knights & square) != 0 ? Piece.KNIGHT : //
						((bishops & square) != 0 ? Piece.BISHOP : //
								((rooks & square) != 0 ? Piece.ROOK : //
										((queens & square) != 0 ? Piece.QUEEN : //
												((kings & square) != 0 ? Piece.KING : '.'))))));
	}

	public char getPieceAt(long square) {
		char p = ((pawns & square) != 0 ? 'p' : //
				((knights & square) != 0 ? 'n' : //
						((bishops & square) != 0 ? 'b' : //
								((rooks & square) != 0 ? 'r' : //
										((queens & square) != 0 ? 'q' : //
												((kings & square) != 0 ? 'k' : '.'))))));
		return ((whites & square) != 0 ? Character.toUpperCase(p) : p);
	}

	public char getPieceUnicodeAt(long square) {
		if ((whites & square) != 0) {
			return ((pawns & square) != 0 ? '♙' : //
					((knights & square) != 0 ? '♘' : //
							((bishops & square) != 0 ? '♗' : //
									((rooks & square) != 0 ? '♖' : //
											((queens & square) != 0 ? '♕' : //
													((kings & square) != 0 ? '♔' : '.'))))));

		} else if ((blacks & square) != 0) {
			return ((pawns & square) != 0 ? '♟' : //
					((knights & square) != 0 ? '♞' : //
							((bishops & square) != 0 ? '♝' : //
									((rooks & square) != 0 ? '♜' : //
											((queens & square) != 0 ? '♛' : //
													((kings & square) != 0 ? '♚' : '.'))))));
		} else {
			return '_';
		}
	}

	public void setPieceAt(long square, char piece) {
		pawns &= ~square;
		queens &= ~square;
		rooks &= ~square;
		bishops &= ~square;
		knights &= ~square;
		kings &= ~square;

		if (piece == ' ' || piece == '.') {
			whites &= ~square;
			blacks &= ~square;
			return;
		} else if (piece == Character.toLowerCase(piece)) {
			whites &= ~square;
			blacks |= square;
		} else {
			whites |= square;
			blacks &= ~square;
		}

		switch (Character.toLowerCase(piece)) {
			case 'p':
				pawns |= square;
				break;
			case 'q':
				queens |= square;
				break;
			case 'r':
				rooks |= square;
				break;
			case 'b':
				bishops |= square;
				break;
			case 'n':
				knights |= square;
				break;
			case 'k':
				kings |= square;
				break;
		}

		key = ZobristKey.getKey(this);
		setCheckFlags();
	}

	/**
	 * Converts board to its fen notation
	 */
	public String getFen() {
		StringBuilder sb = new StringBuilder();
		long i = Square.A8;
		int j = 0;
		while (i != 0) {
			char p = getPieceAt(i);
			if (p == '.') {
				j++;
			}
			if ((j != 0) && (p != '.' || ((i & BitboardUtils.b_r) != 0))) {
				sb.append(j);
				j = 0;
			}
			if (p != '.') {
				sb.append(p);
			}
			if ((i != 1) && (i & BitboardUtils.b_r) != 0) {
				sb.append("/");
			}
			i >>>= 1;
		}
		sb.append(" ");
		sb.append((getTurn() ? "w" : "b"));
		sb.append(" ");
		if (getWhiteKingsideCastling()) {
			sb.append("K");
		}
		if (getWhiteQueensideCastling()) {
			sb.append("Q");
		}
		if (getBlackKingsideCastling()) {
			sb.append("k");
		}
		if (getBlackQueensideCastling()) {
			sb.append("q");
		}
		if (!getWhiteQueensideCastling() && !getWhiteKingsideCastling() && !getBlackQueensideCastling() && !getBlackKingsideCastling()) {
			sb.append("-");
		}
		sb.append(" ");
		sb.append((getPassantSquare() != 0 ? BitboardUtils.square2Algebraic(getPassantSquare()) : "-"));
		sb.append(" ");
		sb.append(fiftyMovesRule);
		sb.append(" ");
		sb.append((moveNumber >> 1) + 1); // 0,1->1.. 2,3->2
		return sb.toString();
	}

	/**
	 * Loads board from a fen notation
	 */
	public void setFen(String fen) {
		setFenMove(fen, null);
	}

	/**
	 * Sets fen without destroying move history. If lastMove = null destroy the move history
	 */
	public void setFenMove(String fen, String lastMove) {
		long tmpWhites = 0;
		long tmpBlacks = 0;
		long tmpPawns = 0;
		long tmpRooks = 0;
		long tmpQueens = 0;
		long tmpBishops = 0;
		long tmpKnights = 0;
		long tmpKings = 0;
		long tmpFlags;
		int tmpFiftyMovesRule = 0;
		long tmpCastlingRooks[] = {0, 0, 0, 0};
		int fenMoveNumber = 0;

		int i = 0;
		long j = Square.A8;
		String[] tokens = fen.split("[ \\t\\n\\x0B\\f\\r]+");
		String board = tokens[0];

		while ((i < board.length()) && (j != 0)) {
			char p = board.charAt(i++);
			if (p != '/') {
				int number = 0;
				try {
					number = Integer.parseInt(String.valueOf(p));
				} catch (Exception ignored) {
				}

				for (int k = 0; k < (number == 0 ? 1 : number); k++) {
					tmpWhites = (tmpWhites & ~j) | ((number == 0) && (p == Character.toUpperCase(p)) ? j : 0);
					tmpBlacks = (tmpBlacks & ~j) | ((number == 0) && (p == Character.toLowerCase(p)) ? j : 0);
					tmpPawns = (tmpPawns & ~j) | (Character.toUpperCase(p) == 'P' ? j : 0);
					tmpRooks = (tmpRooks & ~j) | (Character.toUpperCase(p) == 'R' ? j : 0);
					tmpQueens = (tmpQueens & ~j) | (Character.toUpperCase(p) == 'Q' ? j : 0);
					tmpBishops = (tmpBishops & ~j) | (Character.toUpperCase(p) == 'B' ? j : 0);
					tmpKnights = (tmpKnights & ~j) | (Character.toUpperCase(p) == 'N' ? j : 0);
					tmpKings = (tmpKings & ~j) | (Character.toUpperCase(p) == 'K' ? j : 0);
					j >>>= 1;
					if (j == 0) {
						break; // security
					}
				}
			}
		}

		// Now the rest ...
		String turn = tokens[1];
		tmpFlags = 0;
		if ("b".equals(turn)) {
			tmpFlags |= FLAG_TURN;
		}
		if (tokens.length > 2) {
			// Set castling rights supporting XFEN to disambiguate positions in Chess960
			String castlings = tokens[2];

			chess960 = false;
			// Squares to the sides of the kings {White Kingside, White Queenside, Black Kingside, Black Queenside}
			long whiteKingLateralSquares[] = {
					BitboardUtils.b_d & ((tmpKings & tmpWhites) - 1), BitboardUtils.b_d & ~(((tmpKings & tmpWhites) - 1) | tmpKings & tmpWhites),
					BitboardUtils.b_u & ((tmpKings & tmpBlacks) - 1), BitboardUtils.b_u & ~(((tmpKings & tmpBlacks) - 1) | tmpKings & tmpBlacks)};

			// Squares where we can find a castling rook
			long possibleCastlingRookSquares[] = {0, 0, 0, 0};

			for (int k = 0; k < castlings.length(); k++) {
				char c = castlings.charAt(k);
				switch (c) {
					case 'K':
						possibleCastlingRookSquares[0] = whiteKingLateralSquares[0];
						break;
					case 'Q':
						possibleCastlingRookSquares[1] = whiteKingLateralSquares[1];
						break;
					case 'k':
						possibleCastlingRookSquares[2] = whiteKingLateralSquares[2];
						break;
					case 'q':
						possibleCastlingRookSquares[3] = whiteKingLateralSquares[3];
						break;
					default:
						// Shredder-FEN receives the name of the file where the castling rook is
						int whiteFile = "ABCDEFGH".indexOf(c);
						int blackFile = "abcdefgh".indexOf(c);
						if (whiteFile >= 0) {
							long rookSquare = BitboardUtils.b_d & BitboardUtils.FILE[whiteFile];
							if ((rookSquare & whiteKingLateralSquares[0]) != 0) {
								possibleCastlingRookSquares[0] = rookSquare;
							} else if ((rookSquare & whiteKingLateralSquares[1]) != 0) {
								possibleCastlingRookSquares[1] = rookSquare;
							}
						} else if (blackFile >= 0) {
							long rookSquare = BitboardUtils.b_u & BitboardUtils.FILE[blackFile];
							if ((rookSquare & whiteKingLateralSquares[2]) != 0) {
								possibleCastlingRookSquares[2] = rookSquare;
							} else if ((rookSquare & whiteKingLateralSquares[3]) != 0) {
								possibleCastlingRookSquares[3] = rookSquare;
							}
						}
				}
			}

			// Now store the squares of the castling rooks
			tmpCastlingRooks[0] = BitboardUtils.lsb(tmpRooks & tmpWhites & possibleCastlingRookSquares[0]);
			tmpCastlingRooks[1] = BitboardUtils.msb(tmpRooks & tmpWhites & possibleCastlingRookSquares[1]);
			tmpCastlingRooks[2] = BitboardUtils.lsb(tmpRooks & tmpBlacks & possibleCastlingRookSquares[2]);
			tmpCastlingRooks[3] = BitboardUtils.msb(tmpRooks & tmpBlacks & possibleCastlingRookSquares[3]);

			// Set the castling flags and detect Chess960
			if (tmpCastlingRooks[0] != 0) {
				tmpFlags |= FLAG_WHITE_KINGSIDE_CASTLING;
				if ((tmpWhites & tmpKings) != 1L << 3 || tmpCastlingRooks[0] != 1L) {
					chess960 = true;
				}
			}
			if (tmpCastlingRooks[1] != 0) {
				tmpFlags |= FLAG_WHITE_QUEENSIDE_CASTLING;
				if ((tmpWhites & tmpKings) != 1L << 3 || tmpCastlingRooks[1] != 1L << 7) {
					chess960 = true;
				}
			}
			if (tmpCastlingRooks[2] != 0) {
				tmpFlags |= FLAG_BLACK_KINGSIDE_CASTLING;
				if ((tmpBlacks & tmpKings) != 1L << 59 || tmpCastlingRooks[2] != 1L << 56) {
					chess960 = true;
				}
			}
			if (tmpCastlingRooks[3] != 0) {
				tmpFlags |= FLAG_BLACK_QUEENSIDE_CASTLING;
				if ((tmpBlacks & tmpKings) != 1L << 59 || tmpCastlingRooks[3] != 1L << 63) {
					chess960 = true;
				}
			}
			// END FEN castlings

			if (tokens.length > 3) {
				String passant = tokens[3];
				tmpFlags |= FLAGS_PASSANT & BitboardUtils.algebraic2Square(passant);
				if (tokens.length > 4) {
					try {
						tmpFiftyMovesRule = Integer.parseInt(tokens[4]);
					} catch (Exception e) {
						tmpFiftyMovesRule = 0;
					}
					if (tokens.length > 5) {
						String moveNumberString = tokens[5];
						int aux = Integer.parseInt(moveNumberString);
						fenMoveNumber = ((aux > 0 ? aux - 1 : aux) << 1) + ((tmpFlags & FLAG_TURN) == 0 ? 0 : 1);
						if (fenMoveNumber < 0) {
							fenMoveNumber = 0;
						}
					}
				}
			}
		}

		// try to apply the last move to see if we are advancing or undoing moves
		if ((moveNumber + 1) == fenMoveNumber && lastMove != null) {
			doMove(Move.getFromString(this, lastMove, true));
		} else if (fenMoveNumber < moveNumber) {
			for (int k = moveNumber; k > fenMoveNumber; k--) {
				undoMove();
			}
		}

		// Check if board changed or if we can keep the history
		if (whites != tmpWhites //
				|| blacks != tmpBlacks //
				|| pawns != tmpPawns //
				|| rooks != tmpRooks //
				|| queens != tmpQueens //
				|| bishops != tmpBishops //
				|| knights != tmpKnights //
				|| kings != tmpKings //
				|| (flags & FLAG_TURN) != (tmpFlags & FLAG_TURN)) {

			// board reset
			movesSan.clear();

			initialFen = fen;
			initialMoveNumber = fenMoveNumber;
			moveNumber = fenMoveNumber;
			outBookMove = Integer.MAX_VALUE;

			whites = tmpWhites;
			blacks = tmpBlacks;
			pawns = tmpPawns;
			rooks = tmpRooks;
			queens = tmpQueens;
			bishops = tmpBishops;
			knights = tmpKnights;
			kings = tmpKings;
			fiftyMovesRule = tmpFiftyMovesRule;

			// Flags are not completed till verify, so skip checking
			flags = tmpFlags;

			castlingRooks[0] = tmpCastlingRooks[0];
			castlingRooks[1] = tmpCastlingRooks[1];
			castlingRooks[2] = tmpCastlingRooks[2];
			castlingRooks[3] = tmpCastlingRooks[3];

			// Set zobrist key and check flags
			key = ZobristKey.getKey(this);
			setCheckFlags();

			// and save history
			resetHistory();
			saveHistory(0, false);
		} else {
			if (moveNumber < outBookMove) {
				outBookMove = Integer.MAX_VALUE;
			}
		}
	}

	/**
	 * Prints board in one string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int j = 8;
		long i = Square.A8;
		while (i != 0) {
			sb.append(getPieceUnicodeAt(i));
			sb.append(" ");
			if ((i & BitboardUtils.b_r) != 0) {
				sb.append(j--);
				if (i == Square.H1) {
					sb.append(" ");
					sb.append(getFen());
				}
				sb.append("\n");
			}
			i >>>= 1;
		}
		sb.append("a b c d e f g h   ");
		sb.append((getTurn() ? "white moves " : "black moves "));
		sb.append((getWhiteKingsideCastling() ? " W:0-0" : "") + (getWhiteQueensideCastling() ? " W:0-0-0" : "") + (getBlackKingsideCastling() ? " B:0-0" : "") + (getBlackQueensideCastling() ? " B:0-0-0" : ""));

		return sb.toString();
	}

	/**
	 * TODO is it necessary??
	 */
	private void resetHistory() {
		Arrays.fill(whitesHistory, 0);
		Arrays.fill(blacksHistory, 0);
		Arrays.fill(pawnsHistory, 0);
		Arrays.fill(knightsHistory, 0);
		Arrays.fill(bishopsHistory, 0);
		Arrays.fill(rooksHistory, 0);
		Arrays.fill(queensHistory, 0);
		Arrays.fill(kingsHistory, 0);
		Arrays.fill(flagsHistory, 0);
		for (int i = 0; i < MAX_MOVES; i++) {
			Arrays.fill(keyHistory[i], 0);
		}
		Arrays.fill(fiftyMovesRuleHistory, 0);
		Arrays.fill(moveHistory, 0);
		movesSan.clear();
	}

	private void saveHistory(int move, boolean fillSanInfo) {
		if (fillSanInfo) {
			movesSan.put(moveNumber, Move.toSan(this, move));
		}

		moveHistory[moveNumber] = move;
		whitesHistory[moveNumber] = whites;
		blacksHistory[moveNumber] = blacks;
		pawnsHistory[moveNumber] = pawns;
		knightsHistory[moveNumber] = knights;
		bishopsHistory[moveNumber] = bishops;
		rooksHistory[moveNumber] = rooks;
		queensHistory[moveNumber] = queens;
		kingsHistory[moveNumber] = kings;
		flagsHistory[moveNumber] = flags;
		keyHistory[moveNumber][0] = key[0];
		keyHistory[moveNumber][1] = key[1];
		fiftyMovesRuleHistory[moveNumber] = fiftyMovesRule;
	}

	/**
	 * This is very inefficient because it fills the San info, so it must not be called from inside search
	 */
	public boolean doMove(int move) {
		return doMove(move, true, true);
	}

	/**
	 * Moves and also updates the board's zobrist key verify legality, if not
	 * legal undo move and return false
	 */
	public boolean doMove(int move, boolean verify, boolean fillSanInfo) {
		if (move == Move.NONE) {
			return false;
		}
		// Save history
		saveHistory(move, fillSanInfo);

		// Count consecutive moves without capture or without pawn move
		fiftyMovesRule++;
		moveNumber++; // Count Ply moves

		boolean turn = getTurn();
		int color = turn ? Color.W : Color.B;

		if ((flags & FLAGS_PASSANT) != 0) {
			// Remove passant flags: from the zobrist key
			key[1 - color] ^= ZobristKey.passantFile[BitboardUtils.getFile(flags & FLAGS_PASSANT)];
			// and from the flags
			flags &= ~FLAGS_PASSANT;
		}

		if (move == Move.NULL) {
			// Change turn
			flags ^= FLAG_TURN;
			key[0] ^= ZobristKey.whiteMove;
			return true;
		}

		int fromIndex = Move.getFromIndex(move);
		long from = Move.getFromSquare(move);

		// Check if we are applying a move in the other turn
		if ((from & getMines()) == 0) {
			undoMove();
			return false;
		}

		int toIndex = Move.getToIndex(move);
		long to = Move.getToSquare(move);
		long moveMask = from | to; // Move is as easy as xor with this mask (exceptions are promotions, captures and en-passant captures)
		int moveType = Move.getMoveType(move);
		int pieceMoved = Move.getPieceMoved(move);
		boolean capture = Move.isCapture(move);

		// Is it is a capture, remove pieces in destination square
		if (capture) {
			fiftyMovesRule = 0;
			// En-passant pawn captures remove captured pawn, put the pawn in to
			int toIndexCapture = toIndex;
			if (moveType == Move.TYPE_PASSANT) {
				to = (getTurn() ? (to >>> 8) : (to << 8));
				toIndexCapture += (getTurn() ? -8 : 8);
			}
			key[1 - color] ^= ZobristKey.getKeyPieceIndex(toIndexCapture, getPieceAt(to));

			whites &= ~to;
			blacks &= ~to;
			pawns &= ~to;
			queens &= ~to;
			rooks &= ~to;
			bishops &= ~to;
			knights &= ~to;
		}

		// Pawn movements
		switch (pieceMoved) {
			case Piece.PAWN:
				fiftyMovesRule = 0;
				// Set new passant flags if pawn is advancing two squares (marks
				// the destination square where the pawn can be captured)
				// Set only passant flags when the other side can capture
				if (((from << 16) & to) != 0 && (bbAttacks.pawn[Color.W][toIndex - 8] & pawns & getOthers()) != 0) { // white
					flags |= (from << 8);
				}
				if (((from >>> 16) & to) != 0 && (bbAttacks.pawn[Color.B][toIndex + 8] & pawns & getOthers()) != 0) { // blask
					flags |= (from >>> 8);
				}
				if ((flags & FLAGS_PASSANT) != 0) {
					key[color] ^= ZobristKey.passantFile[BitboardUtils.getFile(flags & FLAGS_PASSANT)];
				}

				if (moveType == Move.TYPE_PROMOTION_QUEEN || moveType == Move.TYPE_PROMOTION_KNIGHT || moveType == Move.TYPE_PROMOTION_BISHOP
						|| moveType == Move.TYPE_PROMOTION_ROOK) { // Promotions:
					// change
					// the piece
					pawns &= ~from;
					key[color] ^= ZobristKey.pawn[color][fromIndex];
					switch (moveType) {
						case Move.TYPE_PROMOTION_QUEEN:
							queens |= to;
							key[color] ^= ZobristKey.queen[color][toIndex];
							break;
						case Move.TYPE_PROMOTION_KNIGHT:
							knights |= to;
							key[color] ^= ZobristKey.knight[color][toIndex];
							break;
						case Move.TYPE_PROMOTION_BISHOP:
							bishops |= to;
							key[color] ^= ZobristKey.bishop[color][toIndex];
							break;
						case Move.TYPE_PROMOTION_ROOK:
							rooks |= to;
							key[color] ^= ZobristKey.rook[color][toIndex];
							break;
					}
				} else {
					pawns ^= moveMask;
					key[color] ^= ZobristKey.pawn[color][fromIndex] ^ ZobristKey.pawn[color][toIndex];
				}
				break;
			case Piece.ROOK:
				rooks ^= moveMask;
				key[color] ^= ZobristKey.rook[color][fromIndex] ^ ZobristKey.rook[color][toIndex];
				break;
			case Piece.BISHOP:
				bishops ^= moveMask;
				key[color] ^= ZobristKey.bishop[color][fromIndex] ^ ZobristKey.bishop[color][toIndex];
				break;
			case Piece.KNIGHT:
				knights ^= moveMask;
				key[color] ^= ZobristKey.knight[color][fromIndex] ^ ZobristKey.knight[color][toIndex];
				break;
			case Piece.QUEEN:
				queens ^= moveMask;
				key[color] ^= ZobristKey.queen[color][fromIndex] ^ ZobristKey.queen[color][toIndex];
				break;
			case Piece.KING: // if castling, moves rooks too
				if (moveType == Move.TYPE_KINGSIDE_CASTLING || moveType == Move.TYPE_QUEENSIDE_CASTLING) {
					// {White Kingside, White Queenside, Black Kingside, Black Queenside}
					int j = (color << 1) + (moveType == Move.TYPE_QUEENSIDE_CASTLING ? 1 : 0);

					toIndex = CASTLING_KING_DESTINY_INDEX[j];
					int originRookIndex = BitboardUtils.square2Index(castlingRooks[j]);
					int destinyRookIndex = CASTLING_ROOK_DESTINY_INDEX[j];
					// Recalculate move mask for chess960 castlings
					moveMask = from ^ (1L << toIndex);
					long rookMoveMask = (1L << originRookIndex) ^ (1L << destinyRookIndex);
					key[color] ^= ZobristKey.rook[color][originRookIndex] ^ ZobristKey.rook[color][destinyRookIndex];

					if (getTurn()) {
						whites ^= rookMoveMask;
					} else {
						blacks ^= rookMoveMask;
					}
					rooks ^= rookMoveMask;
				}
				kings ^= moveMask;
				key[color] ^= ZobristKey.king[color][fromIndex] ^ ZobristKey.king[color][toIndex];
				break;
		}
		// Move pieces in colour fields
		if (getTurn()) {
			whites ^= moveMask;
		} else {
			blacks ^= moveMask;
		}

		// Tests to disable castling
		if ((flags & FLAG_WHITE_KINGSIDE_CASTLING) != 0 && //
				((turn && pieceMoved == Piece.KING) || from == castlingRooks[0] || to == castlingRooks[0])) {
			flags &= ~FLAG_WHITE_KINGSIDE_CASTLING;
			key[0] ^= ZobristKey.whiteKingSideCastling;
		}
		if ((flags & FLAG_WHITE_QUEENSIDE_CASTLING) != 0 && //
				((turn && pieceMoved == Piece.KING) || from == castlingRooks[1] || to == castlingRooks[1])) {
			flags &= ~FLAG_WHITE_QUEENSIDE_CASTLING;
			key[0] ^= ZobristKey.whiteQueenSideCastling;
		}
		if ((flags & FLAG_BLACK_KINGSIDE_CASTLING) != 0 && //
				((!turn && pieceMoved == Piece.KING) || from == castlingRooks[2] || to == castlingRooks[2])) {
			flags &= ~FLAG_BLACK_KINGSIDE_CASTLING;
			key[1] ^= ZobristKey.blackKingSideCastling;
		}
		if ((flags & FLAG_BLACK_QUEENSIDE_CASTLING) != 0 && //
				((!turn && pieceMoved == Piece.KING) || from == castlingRooks[3] || to == castlingRooks[3])) {
			flags &= ~FLAG_BLACK_QUEENSIDE_CASTLING;
			key[1] ^= ZobristKey.blackQueenSideCastling;
		}
		// Change turn
		flags ^= FLAG_TURN;
		key[0] ^= ZobristKey.whiteMove;

		if (verify) {
			if (isValid()) {
				setCheckFlags();

				if (fillSanInfo) {
					if (isMate()) { // Append # when mate
						movesSan.put(moveNumber - 1, movesSan.get(moveNumber - 1).replace("+", "#"));
					}
				}
			} else {
				undoMove();
				return false;
			}
		} else {
			// Trust move check flag
			if (Move.isCheck(move)) {
				flags |= FLAG_CHECK;
			} else {
				flags &= ~FLAG_CHECK;
			}
		}
		return true;
	}

	/**
	 * It checks if a state is valid basically, if the other king is not in check
	 */
	private boolean isValid() {
		return (!bbAttacks.isSquareAttacked(this, kings & getOthers(), !getTurn()));
	}

	/**
	 * Sets check flag if the own king is in check
	 */
	private void setCheckFlags() {
		if (bbAttacks.isSquareAttacked(this, kings & getMines(), getTurn())) {
			flags |= FLAG_CHECK;
		} else {
			flags &= ~FLAG_CHECK;
		}
	}

	public void undoMove() {
		undoMove(moveNumber - 1);
	}

	public void undoMove(int moveNumber) {
		if (moveNumber < 0 || moveNumber < initialMoveNumber) {
			return;
		}
		this.moveNumber = moveNumber;

		whites = whitesHistory[moveNumber];
		blacks = blacksHistory[moveNumber];
		pawns = pawnsHistory[moveNumber];
		knights = knightsHistory[moveNumber];
		bishops = bishopsHistory[moveNumber];
		rooks = rooksHistory[moveNumber];
		queens = queensHistory[moveNumber];
		kings = kingsHistory[moveNumber];
		flags = flagsHistory[moveNumber];
		key[0] = keyHistory[moveNumber][0];
		key[1] = keyHistory[moveNumber][1];
		fiftyMovesRule = fiftyMovesRuleHistory[moveNumber];
	}

	/**
	 * 0 no, 1 whites won, -1 blacks won, 99 draw
	 */
	public int isEndGame() {
		int endGame = 0;
		generateLegalMoves();
		if (legalMoveCount == 0) {
			if (getCheck()) {
				endGame = (getTurn() ? -1 : 1);
			} else {
				endGame = 99;
			}
		} else if (isDraw()) {
			endGame = 99;
		}
		return endGame;
	}

	public boolean isMate() {
		int endgameState = isEndGame();
		return endgameState == 1 || endgameState == -1;
	}

	/**
	 * checks draw by fifty move rule and threefold repetition
	 */
	public boolean isDraw() {
		if (fiftyMovesRule >= 100) {
			return true;
		}
		int repetitions = 0;
		for (int i = 0; i < (moveNumber - 1); i++) {
			if (keyHistory[i][0] == key[0] && keyHistory[i][1] == key[1]) {
				repetitions++;
			}
			if (repetitions >= 2) { // with the last one they are 3
				return true;
			}
		}
		// Draw by no material to mate
		// Kk, KNk, KNNk, KBK by FIDE rules, be careful: KNnk IS NOT a draw
		return (pawns == 0 && rooks == 0 && queens == 0) &&
				((bishops == 0 && knights == 0) || //
						(knights == 0 && BitboardUtils.popCount(bishops) == 1) ||
						(bishops == 0 &&
								(BitboardUtils.popCount(knights) == 1 ||
										(BitboardUtils.popCount(knights) == 2 && // KNNk, check same color
												(BitboardUtils.popCount(knights & whites) == 2 ||
														BitboardUtils.popCount(knights & ~whites) == 2))))
				);
	}

	public int see(int move) {
		return see(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), Move.isCapture(move) ? Move.getPieceCaptured(this, move) : 0);
	}

	public int see(int move, AttacksInfo attacksInfo) {
		int them = getTurn() ? 1 : 0;
		if (attacksInfo.boardKey == getKey()
				&& (attacksInfo.attackedSquares[them] & Move.getToSquare(move)) == 0
				&& (attacksInfo.mayPin[them] & Move.getFromSquare(move)) == 0) {
			return Move.isCapture(move) ? Board.SEE_PIECE_VALUES[Move.getPieceCaptured(this, move)] : 0;
		} else {
			return see(move);
		}
	}

	/**
	 * The SWAP algorithm https://chessprogramming.wikispaces.com/SEE+-+The+Swap+Algorithm
	 */
	public int see(int fromIndex, int toIndex, int pieceMoved, int targetPiece) {
		int d = 0;
		long mayXray = pawns | bishops | rooks | queens; // not kings nor knights
		long fromSquare = 0x1L << fromIndex;
		long all = getAll();
		long attacks = bbAttacks.getIndexAttacks(this, toIndex);
		long fromCandidates;

		seeGain[d] = SEE_PIECE_VALUES[targetPiece];
		do {
			long side = (d & 1) == 0 ? getOthers() : getMines();
			d++; // next depth and side speculative store, if defended
			seeGain[d] = SEE_PIECE_VALUES[pieceMoved] - seeGain[d - 1];
			attacks ^= fromSquare; // reset bit in set to traverse
			all ^= fromSquare; // reset bit in temporary occupancy (for X-Rays)
			if ((fromSquare & mayXray) != 0) {
				attacks |= bbAttacks.getXrayAttacks(this, toIndex, all);
			}

			// Gets the next attacker
			if ((fromCandidates = attacks & pawns & side) != 0) {
				pieceMoved = Piece.PAWN;
			} else if ((fromCandidates = attacks & knights & side) != 0) {
				pieceMoved = Piece.KNIGHT;
			} else if ((fromCandidates = attacks & bishops & side) != 0) {
				pieceMoved = Piece.BISHOP;
			} else if ((fromCandidates = attacks & rooks & side) != 0) {
				pieceMoved = Piece.ROOK;
			} else if ((fromCandidates = attacks & queens & side) != 0) {
				pieceMoved = Piece.QUEEN;
			} else if ((fromCandidates = attacks & kings & side) != 0) {
				pieceMoved = Piece.KING;
			}
			fromSquare = BitboardUtils.lsb(fromCandidates);

		} while (fromSquare != 0);

		while (--d != 0) {
			seeGain[d - 1] = -Math.max(-seeGain[d - 1], seeGain[d]);
		}
		return seeGain[0];
	}

	public boolean isUsingBook() {
		return outBookMove > moveNumber;
	}

	public void setOutBookMove(int outBookMove) {
		this.outBookMove = outBookMove;
	}

	/**
	 * Check if a passed pawn is in the index, useful to trigger extensions
	 */
	public boolean isPassedPawn(int index) {
		int rank = index >> 3;
		int file = 7 - index & 7;
		long square = 0x1L << index;

		if ((whites & square) != 0) {
			return ((BitboardUtils.FILE[file] | BitboardUtils.FILES_ADJACENT[file]) & BitboardUtils.RANKS_UPWARDS[rank] & pawns & blacks) == 0;
		} else if ((blacks & square) != 0) {
			return ((BitboardUtils.FILE[file] | BitboardUtils.FILES_ADJACENT[file]) & BitboardUtils.RANKS_DOWNWARDS[rank] & pawns & whites) == 0;
		}
		return false;
	}

	/**
	 * Returns true if move is legal
	 */
	public boolean isMoveLegal(int move) {
		generateLegalMoves();
		for (int i = 0; i < legalMoveCount; i++) {
			// logger.debug(Move.toStringExt(legalMoves[i]));
			if (move == legalMoves[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generates legal moves for the position when not already generated
	 */
	void generateLegalMoves() {
		if ((key[0] != legalMovesKey[0]) || (key[1] != legalMovesKey[1])) {
			legalMoveCount = legalMoveGenerator.generateMoves(this, legalMoves, 0);
			legalMovesKey[0] = key[0];
			legalMovesKey[1] = key[1];
		}
	}

	public int getLegalMoves(int moves[]) {
		generateLegalMoves();
		System.arraycopy(legalMoves, 0, moves, 0, (legalMoveCount != -1 ? legalMoveCount : 0));
		return legalMoveCount;
	}

	public String getSanMove(int moveNumber) {
		return movesSan.get(moveNumber);
	}

	public boolean getMoveTurn(int moveNumber) {
		return (flagsHistory[moveNumber] & FLAG_TURN) == 0;
	}

	public String getInitialFen() {
		return initialFen;
	}

	public String getMoves() {
		StringBuffer oSB = new StringBuffer();
		for (int i = initialMoveNumber; i < moveNumber; i++) {
			if (oSB.length() > 0) {
				oSB.append(" ");
			}
			oSB.append(Move.toString(moveHistory[i]));
		}
		return oSB.toString();
	}

	public String getMovesSan() {
		StringBuffer oSB = new StringBuffer();
		for (int i = initialMoveNumber; i < moveNumber; i++) {
			if (oSB.length() > 0) {
				oSB.append(" ");
			}
			oSB.append(movesSan.get(i));
		}
		return oSB.toString();
	}

	public String toSanNextMoves(String moves) {
		if (moves == null || "".equals(moves.trim())) {
			return "";
		}

		StringBuffer oSB = new StringBuffer();
		String movesArray[] = moves.split(" ");
		int savedMoveNumber = moveNumber;

		for (String moveString : movesArray) {
			int move = Move.getFromString(this, moveString, true);

			if (!doMove(move)) {
				undoMove(savedMoveNumber);
				return "";
			}

			if (oSB.length() > 0) {
				oSB.append(" ");
			}
			oSB.append(getLastMoveSan());
		}
		undoMove(savedMoveNumber);
		return oSB.toString();
	}

	public int getLastMove() {
		if (moveNumber == 0) {
			return Move.NONE;
		}
		return moveHistory[moveNumber - 1];
	}

	public String getLastMoveSan() {
		if (moveNumber == 0) {
			return null;
		}
		return movesSan.get(moveNumber - 1);
	}

	/**
	 * Convenience method to apply all the moves in a string separated by spaces
	 */
	public void doMoves(String moves) {
		if (moves == null || "".equals(moves.trim())) {
			return;
		}
		String movesArray[] = moves.split(" ");
		for (String moveString : movesArray) {
			int move = Move.getFromString(this, moveString, true);
			doMove(move);
		}
	}

}