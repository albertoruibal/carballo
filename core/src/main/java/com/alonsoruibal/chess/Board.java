package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.hash.ZobristKey;
import com.alonsoruibal.chess.movegen.LegalMoveGenerator;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Stores the position and the move history
 * TODO FRC, Atomic, Suicide...
 *
 * @author Alberto Alonso Ruibal
 */
public class Board {
	public static final int MAX_MOVES = 1024;
	public static final String FEN_START_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

	LegalMoveGenerator legalMoveGenerator = new LegalMoveGenerator();
	int[] legalMoves = new int[256];
	int legalMoveCount = -1; // if -1 then legal moves not generated
	long[] legalMovesKey = {0, 0};
	public HashMap<Integer, String> sanMoves;

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

	// Flags: must be changed only when moving
	private static final long FLAG_TURN = 0x0001L;
	private static final long FLAG_WHITE_DISABLE_KINGSIDE_CASTLING = 0x0002L;
	private static final long FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING = 0x0004L;
	private static final long FLAG_BLACK_DISABLE_KINGSIDE_CASTLING = 0x0008L;
	private static final long FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING = 0x0010L;
	private static final long FLAG_CHECK = 0x0020L;
	// Position on boarch in which is captured
	private static final long FLAGS_PASSANT = 0x0000ff0000ff0000L;

	// For the SEE SWAP algorithm
	public static final int[] SEE_PIECE_VALUES = {0, 100, 325, 330, 500, 900, 9999};

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

		sanMoves = new HashMap<Integer, String>();

		bbAttacks = BitboardAttacks.getInstance();
	}

	/**
	 * It also computes the zobrish key
	 */
	public void startPosition() {
		setFen(FEN_START_POSITION);
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
		return (flags & FLAG_WHITE_DISABLE_KINGSIDE_CASTLING) == 0;
	}

	public boolean getWhiteQueensideCastling() {
		return (flags & FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING) == 0;
	}

	public boolean getBlackKingsideCastling() {
		return (flags & FLAG_BLACK_DISABLE_KINGSIDE_CASTLING) == 0;
	}

	public boolean getBlackQueensideCastling() {
		return (flags & FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING) == 0;
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

	public char getPieceAt(long square) {
		char p = ((pawns & square) != 0 ? 'p' : //
				((queens & square) != 0 ? 'q' : //
						((rooks & square) != 0 ? 'r' : //
								((bishops & square) != 0 ? 'b' : //
										((knights & square) != 0 ? 'n' : //
												((kings & square) != 0 ? 'k' : '.'))))));
		return ((whites & square) != 0 ? Character.toUpperCase(p) : p);
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
	}

	/**
	 * Converts board to its fen notation
	 */
	public String getFen() {
		StringBuilder sb = new StringBuilder();
		long i = BitboardUtils.A8;
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
	 *
	 * @param fen
	 */
	public void setFen(String fen) {
		setFenMove(fen, null);
	}

	/**
	 * Sets fen without destroying move history. If lastMove = null destroy the
	 * move history
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
		int fenMoveNumber = 0;

		int i = 0;
		long j = BitboardUtils.A8;
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
		tmpFlags = FLAG_WHITE_DISABLE_KINGSIDE_CASTLING | FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING | FLAG_BLACK_DISABLE_KINGSIDE_CASTLING
				| FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
		if ("b".equals(turn)) {
			tmpFlags |= FLAG_TURN;
		}
		if (tokens.length > 2) {
			String promotions = tokens[2];
			if (promotions.contains("K")) {
				tmpFlags &= ~FLAG_WHITE_DISABLE_KINGSIDE_CASTLING;
			}
			if (promotions.contains("Q")) {
				tmpFlags &= ~FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING;
			}
			if (promotions.contains("k")) {
				tmpFlags &= ~FLAG_BLACK_DISABLE_KINGSIDE_CASTLING;
			}
			if (promotions.contains("q")) {
				tmpFlags &= ~FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
			}
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
			sanMoves.clear();

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

			verify();

			// Finally set zobrish key and check flags
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
	 * Does some board verification
	 */
	private void verify() {
		// TODO Verify only one king per side

		// Verify castling
		if (getWhiteKingsideCastling() && ((whites & kings & 0x08) == 0 || (whites & rooks & 0x01) == 0)) {
			flags |= FLAG_WHITE_DISABLE_KINGSIDE_CASTLING;
		}
		if (getWhiteQueensideCastling() && ((whites & kings & 0x08) == 0 || (whites & rooks & 0x80) == 0)) {
			flags |= FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING;
		}
		if (getBlackKingsideCastling() && ((blacks & kings & 0x0800000000000000L) == 0 || (blacks & rooks & 0x0100000000000000L) == 0)) {
			flags |= FLAG_BLACK_DISABLE_KINGSIDE_CASTLING;
		}
		if (getBlackQueensideCastling() && ((blacks & kings & 0x0800000000000000L) == 0 || (blacks & rooks & 0x8000000000000000L) == 0)) {
			flags |= FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
		}
	}

	/**
	 * Prints board in one string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int j = 8;
		long i = BitboardUtils.A8;
		while (i != 0) {
			sb.append(getPieceAt(i));
			sb.append(" ");
			if ((i & BitboardUtils.b_r) != 0) {
				sb.append(j--);
				sb.append("\n");
			}
			i >>>= 1;
		}
		sb.append("a b c d e f g h  ");
		sb.append((getTurn() ? "white move\n" : "blacks move\n"));
		// sb.append(" " +getWhiteKingsideCastling()+getWhiteQueensideCastling()+getBlackKingsideCastling()+getBlackQueensideCastling());

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
		sanMoves.clear();
	}

	private void saveHistory(int move, boolean fillInfo) {
		if (fillInfo) {
			sanMoves.put(moveNumber, Move.toSan(this, move));
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

	public int getLastMove() {
		if (moveNumber == 0) {
			return 0;
		}
		return moveHistory[moveNumber - 1];
	}

	public boolean doMove(int move) {
		return doMove(move, true, true);
	}

	/**
	 * Moves and also updates the board's zobrish key verify legality, if not
	 * legal undo move and return false 0 is the null move
	 */
	public boolean doMove(int move, boolean verify, boolean fillInfo) {
		// logger.debug("Before move: \n" + toString() + "\n " +
		// Move.toStringExt(move));

		if (move == -1) {
			return false;
		}
		// Save history
		saveHistory(move, fillInfo);

		int fromIndex = Move.getFromIndex(move);
		int toIndex = Move.getToIndex(move);
		long from = Move.getFromSquare(move);
		long to = Move.getToSquare(move);
		long moveMask = from | to; // Move is as easy as xor with this mask (exceptions are promotions, captures and en-passant captures)
		int moveType = Move.getMoveType(move);
		int pieceMoved = Move.getPieceMoved(move);
		boolean capture = Move.isCapture(move);
		boolean turn = getTurn();
		int color = (turn ? 0 : 1);

		// Count consecutive moves without capture or without pawn move
		fiftyMovesRule++;
		moveNumber++; // Count Ply moves

		// Remove passant flags: from the zobrist key
		if ((flags & FLAGS_PASSANT) != 0) {
			key[1 - color] ^= ZobristKey.passantColumn[BitboardUtils.getColumn(flags & FLAGS_PASSANT)];
		}
		// and from the flags
		flags &= ~FLAGS_PASSANT;

		if (move != 0) {
			assert (from & getMines()) != 0 : "Origin square not valid";

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
				case Move.PAWN:
					fiftyMovesRule = 0;
					// Set new passant flags if pawn is advancing two squares (marks
					// the destination square where the pawn can be captured)
					// Set only passant flags when the other side can capture
					if (((from << 16) & to) != 0 && (bbAttacks.pawnUpwards[toIndex - 8] & pawns & getOthers()) != 0) { // white
						flags |= (from << 8);
					}
					if (((from >>> 16) & to) != 0 && (bbAttacks.pawnDownwards[toIndex + 8] & pawns & getOthers()) != 0) { // blask
						flags |= (from >>> 8);
					}
					if ((flags & FLAGS_PASSANT) != 0) {
						key[color] ^= ZobristKey.passantColumn[BitboardUtils.getColumn(flags & FLAGS_PASSANT)];
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
				case Move.ROOK:
					rooks ^= moveMask;
					key[color] ^= ZobristKey.rook[color][fromIndex] ^ ZobristKey.rook[color][toIndex];
					break;
				case Move.BISHOP:
					bishops ^= moveMask;
					key[color] ^= ZobristKey.bishop[color][fromIndex] ^ ZobristKey.bishop[color][toIndex];
					break;
				case Move.KNIGHT:
					knights ^= moveMask;
					key[color] ^= ZobristKey.knight[color][fromIndex] ^ ZobristKey.knight[color][toIndex];
					break;
				case Move.QUEEN:
					queens ^= moveMask;
					key[color] ^= ZobristKey.queen[color][fromIndex] ^ ZobristKey.queen[color][toIndex];
					break;
				case Move.KING: // if castling, moves rooks too
					long rookMoveMask = 0; // for the castling
					switch (moveType) {
						case Move.TYPE_KINGSIDE_CASTLING:
							rookMoveMask = (getTurn() ? 0x05L : 0x0500000000000000L);
							key[color] ^= ZobristKey.rook[color][toIndex - 1] ^ ZobristKey.rook[color][toIndex + 1];
							break;
						case Move.TYPE_QUEENSIDE_CASTLING:
							rookMoveMask = (getTurn() ? 0x90L : 0x9000000000000000L);
							key[color] ^= ZobristKey.rook[color][toIndex - 1] ^ ZobristKey.rook[color][toIndex + 2];
							break;
					}
					if (rookMoveMask != 0) {
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
			if ((moveMask & 0x0000000000000009L) != 0 && (flags & FLAG_WHITE_DISABLE_KINGSIDE_CASTLING) == 0) {
				flags |= FLAG_WHITE_DISABLE_KINGSIDE_CASTLING;
				key[0] ^= ZobristKey.whiteKingSideCastling;
			}
			if ((moveMask & 0x0000000000000088L) != 0 && (flags & FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING) == 0) {
				flags |= FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING;
				key[0] ^= ZobristKey.whiteQueenSideCastling;
			}
			if ((moveMask & 0x0900000000000000L) != 0 && (flags & FLAG_BLACK_DISABLE_KINGSIDE_CASTLING) == 0) {
				flags |= FLAG_BLACK_DISABLE_KINGSIDE_CASTLING;
				key[1] ^= ZobristKey.blackKingSideCastling;
			}
			if ((moveMask & 0x8800000000000000L) != 0 && (flags & FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING) == 0) {
				flags |= FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
				key[1] ^= ZobristKey.blackQueenSideCastling;
			}
		}
		// Change turn
		flags ^= FLAG_TURN;
		key[0] ^= ZobristKey.whiteMove;

		if (verify) {
			if (isValid()) {
				setCheckFlags();

				if (fillInfo) {
					generateLegalMoves();
					if (isMate()) { // Append # when mate
						sanMoves.put(moveNumber - 1, sanMoves.get(moveNumber - 1) + "#");
					} else if (getCheck()) { // Append + when check
						sanMoves.put(moveNumber - 1, sanMoves.get(moveNumber - 1) + "+");
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
	 * checks draw by fiftymoves rule and threefold repetition
	 */
	public boolean isDraw() {
		if (fiftyMovesRule >= 100) {
			return true;
		}
		int repetitions = 0;
		// logger.debug("My keys key0=" + key[0] + " " + " key1=" + key[1]);
		for (int i = 0; i < (moveNumber - 1); i++) {
			if (keyHistory[i][0] == key[0] && keyHistory[i][1] == key[1]) {
				repetitions++;
			}
			if (repetitions >= 2) { // with the last one they are 3
				return true;
			}
		}
		// Draw by no material to mate
		return (pawns == 0 && rooks == 0 && queens == 0) &&
				((bishops == 0 && knights == 0) || //
						(bishops == 0 && BitboardUtils.popCount(knights) == 1) || //
						(knights == 0 && BitboardUtils.popCount(bishops) == 1));
	}

	public int see(int move) {
		return see(Move.getFromIndex(move), Move.getToIndex(move), Move.getPieceMoved(move), Move.getPieceCaptured(this, move)) //
				+ SEE_PIECE_VALUES[Move.getPiecePromoted(move)]; // Fix SEE for promotions
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
			d++; // next depth and side
			// speculative store, if defended
			seeGain[d] = SEE_PIECE_VALUES[pieceMoved] - seeGain[d - 1];
			attacks ^= fromSquare; // reset bit in set to traverse
			all ^= fromSquare; // reset bit in temporary occupancy (for X-Rays)
			if ((fromSquare & mayXray) != 0) {
				attacks |= bbAttacks.getXrayAttacks(this, toIndex, all);
			}

			// Gets the next attacker
			if ((fromCandidates = attacks & pawns & side) != 0) {
				pieceMoved = Move.PAWN;
			} else if ((fromCandidates = attacks & knights & side) != 0) {
				pieceMoved = Move.KNIGHT;
			} else if ((fromCandidates = attacks & bishops & side) != 0) {
				pieceMoved = Move.BISHOP;
			} else if ((fromCandidates = attacks & rooks & side) != 0) {
				pieceMoved = Move.ROOK;
			} else if ((fromCandidates = attacks & queens & side) != 0) {
				pieceMoved = Move.QUEEN;
			} else if ((fromCandidates = attacks & kings & side) != 0) {
				pieceMoved = Move.KING;
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
	 * Check if a passed pawn is in the square, useful to trigger extensions
	 *
	 * @param index
	 * @return
	 */
	public boolean isPassedPawn(int index) {
		int rank = index >> 3;
		int column = 7 - index & 7;
		long square = 0x1L << index;

		if ((whites & square) != 0) {
			return ((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & BitboardUtils.RANKS_UPWARDS[rank] & pawns & blacks) == 0;
		} else if ((blacks & square) != 0) {
			return ((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & BitboardUtils.RANKS_DOWNWARDS[rank] & pawns & whites) == 0;
		}
		return false;
	}

	/**
	 * Returns true if move is legal
	 *
	 * @param move
	 * @return
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
		System.arraycopy(legalMoves, 0, moves, 0, legalMoveCount);
		return legalMoveCount;
	}

	public String getSanMove(int moveNumber) {
		return sanMoves.get(moveNumber);
	}

	public boolean getMoveTurn(int moveNumber) {
		return (flagsHistory[moveNumber] & FLAG_TURN) == 0;
	}
}