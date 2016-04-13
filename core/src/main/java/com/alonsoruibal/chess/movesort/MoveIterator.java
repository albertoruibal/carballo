package com.alonsoruibal.chess.movesort;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Color;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/**
 * The Move Iterator generates moves as needed. It is separated into phases.
 * It sets the check flag on moves. It also checks if the move is legal before generating it.
 */
public class MoveIterator {
	//
	// Kind of moves to generate
	// In check evasions all moves are always generated
	public static final int GENERATE_ALL = 0;
	public static final int GENERATE_CAPTURES_PROMOS = 1; // Generates only good/equal captures and queen promotions
	public static final int GENERATE_CAPTURES_PROMOS_CHECKS = 2; // Generates only good/equal captures, queen promotions and checks
	//
	// Move generation phases
	//
	public static final int PHASE_TT = 0;
	public static final int PHASE_GEN_CAPTURES = 1;
	public static final int PHASE_GOOD_CAPTURES_AND_PROMOS = 2;
	public static final int PHASE_EQUAL_CAPTURES = 3;
	public static final int PHASE_GEN_NON_CAPTURES = 4;
	public static final int PHASE_KILLER1 = 5;
	public static final int PHASE_KILLER2 = 6;
	public static final int PHASE_KILLER3 = 7;
	public static final int PHASE_KILLER4 = 8;
	public static final int PHASE_NON_CAPTURES = 9;
	public static final int PHASE_BAD_CAPTURES = 10;
	public static final int PHASE_END = 11;

	private static final int[] VICTIM_PIECE_VALUES = {0, 100, 325, 330, 500, 975, 10000};
	private static final int[] AGGRESSOR_PIECE_VALUES = {0, 10, 32, 33, 50, 97, 99};
	private static final int SCORE_PROMOTION_QUEEN = 975;
	private static final int SCORE_UNDERPROMOTION = Integer.MIN_VALUE + 1;
	private static final int SCORE_LOWEST = Integer.MIN_VALUE;

	public static final int SEE_NOT_CALCULATED = Short.MAX_VALUE;

	private Board board;
	private AttacksInfo ai;

	private int ttMove;
	private int movesToGenerate;

	private int move;
	private int lastMoveSee;
	private boolean lastMoveIsKiller;
	private int killer1;
	private int killer2;
	private int killer3;
	private int killer4;
	private boolean foundKiller1;
	private boolean foundKiller2;
	private boolean foundKiller3;
	private boolean foundKiller4;

	public boolean checkEvasion;

	private int us;
	private int them;
	private boolean turn;
	private long all;
	private long mines;
	private long others;

	private int goodCaptureIndex;
	private int equalCaptureIndex;
	private int badCaptureIndex;
	private int nonCaptureIndex;

	private int[] goodCaptures = new int[256]; // Stores captures and queen promotions
	private int[] goodCapturesSee = new int[256];
	private int[] goodCapturesScores = new int[256];
	private int[] badCaptures = new int[256]; // Stores captures and queen promotions
	private int[] badCapturesScores = new int[256];
	private int[] equalCaptures = new int[256]; // Stores captures and queen promotions
	private int[] equalCapturesSee = new int[256];
	private int[] equalCapturesScores = new int[256];
	private int[] nonCaptures = new int[256]; // Stores non captures and underpromotions
	private int[] nonCapturesSee = new int[256];
	private int[] nonCapturesScores = new int[256];

	private int depth;
	private SortInfo sortInfo;
	private int phase;

	private BitboardAttacks bbAttacks;

	public MoveIterator(Board board, AttacksInfo ai, SortInfo sortInfo, int depth) {
		this.board = board;
		this.ai = ai;
		this.sortInfo = sortInfo;
		this.depth = depth;

		bbAttacks = BitboardAttacks.getInstance();
	}

	public int getLastMoveSee() {
		if (lastMoveSee == SEE_NOT_CALCULATED) {
			lastMoveSee = board.see(move, ai);
		}
		return lastMoveSee;
	}

	public boolean getLastMoveIsKiller() {
		return lastMoveIsKiller;
	}

	public void genMoves(int ttMove) {
		genMoves(ttMove, GENERATE_ALL);
	}

	public void genMoves(int ttMove, int movesToGenerate) {
		this.ttMove = ttMove;
		this.movesToGenerate = movesToGenerate;

		phase = PHASE_TT;
		checkEvasion = board.getCheck();
		lastMoveSee = SEE_NOT_CALCULATED;
		lastMoveIsKiller = false;
	}

	private void initMoveGen() {
		ai.build(board);

		killer1 = sortInfo.killerMove1[depth];
		killer2 = sortInfo.killerMove2[depth];
		killer3 = depth < 2 ? Move.NONE : sortInfo.killerMove1[depth - 2];
		killer4 = depth < 2 ? Move.NONE : sortInfo.killerMove2[depth - 2];

		foundKiller1 = false;
		foundKiller2 = false;
		foundKiller3 = false;
		foundKiller4 = false;

		goodCaptureIndex = 0;
		badCaptureIndex = 0;
		equalCaptureIndex = 0;
		nonCaptureIndex = 0;

		// Only for clarity
		turn = board.getTurn();
		us = turn ? Color.W : Color.B;
		them = turn ? Color.B : Color.W;
		all = board.getAll();
		mines = board.getMines();
		others = board.getOthers();
	}

	public int next() {
		switch (phase) {
			case PHASE_TT:
				phase++;
				if (ttMove != Move.NONE) {
					move = ttMove;
					if (checkEvasion //
							|| movesToGenerate == GENERATE_ALL //
							|| Move.getMoveType(move) == Move.TYPE_PROMOTION_QUEEN
							|| (movesToGenerate == GENERATE_CAPTURES_PROMOS && Move.isCapture(move) && getLastMoveSee() >= 0) //
							|| (movesToGenerate == GENERATE_CAPTURES_PROMOS_CHECKS && Move.isCaptureOrCheck(move) && getLastMoveSee() >= 0)) {
						return move;
					}
				}

			case PHASE_GEN_CAPTURES:
				initMoveGen();
				if (checkEvasion) {
					generateCheckEvasionCaptures();
				} else {
					generateCaptures();
				}
				phase++;

			case PHASE_GOOD_CAPTURES_AND_PROMOS:
				move = pickMoveFromArray(goodCaptureIndex, goodCaptures, goodCapturesScores, goodCapturesSee);
				if (move != Move.NONE) {
					return move;
				}
				phase++;

			case PHASE_EQUAL_CAPTURES:
				move = pickMoveFromArray(equalCaptureIndex, equalCaptures, equalCapturesScores, equalCapturesSee);
				if (move != Move.NONE) {
					return move;
				}
				phase++;

			case PHASE_GEN_NON_CAPTURES:
				if (checkEvasion) {
					generateCheckEvasionsNonCaptures();
				} else {
					if (movesToGenerate == GENERATE_CAPTURES_PROMOS) {
						phase = PHASE_END;
						return Move.NONE;
					}
					generateNonCaptures();
				}
				phase++;

			case PHASE_KILLER1:
				phase++;
				lastMoveIsKiller = true;
				if (foundKiller1) {
					move = killer1;
					lastMoveSee = SEE_NOT_CALCULATED;
					return move;
				}

			case PHASE_KILLER2:
				phase++;
				if (foundKiller2) {
					move = killer2;
					lastMoveSee = SEE_NOT_CALCULATED;
					return move;
				}

			case PHASE_KILLER3:
				phase++;
				if (foundKiller3) {
					move = killer3;
					lastMoveSee = SEE_NOT_CALCULATED;
					return move;
				}

			case PHASE_KILLER4:
				phase++;
				if (foundKiller4) {
					move = killer4;
					lastMoveSee = SEE_NOT_CALCULATED;
					return move;
				}

			case PHASE_NON_CAPTURES:
				lastMoveIsKiller = false;
				move = pickMoveFromArray(nonCaptureIndex, nonCaptures, nonCapturesScores, nonCapturesSee);
				if (move != Move.NONE) {
					return move;
				}
				phase++;

			case PHASE_BAD_CAPTURES:
				move = pickMoveFromArray(badCaptureIndex, badCaptures, badCapturesScores, badCapturesScores);
				if (move != Move.NONE) {
					return move;
				}
				phase = PHASE_END;
				return Move.NONE;
		}
		return Move.NONE;
	}

	private int pickMoveFromArray(int arrayLength, int arrayMoves[], int arrayScores[], int arraySee[]) {
		if (arrayLength == 0) {
			return Move.NONE;
		}
		int maxScore = SCORE_LOWEST;
		int bestIndex = -1;
		for (int i = 0; i < arrayLength; i++) {
			if (arrayScores[i] > maxScore) {
				maxScore = arrayScores[i];
				bestIndex = i;
			}
		}
		if (bestIndex != -1) {
			int move = arrayMoves[bestIndex];
			lastMoveSee = arraySee[bestIndex];
			arrayScores[bestIndex] = SCORE_LOWEST;
			return move;
		} else {
			return Move.NONE;
		}
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	/**
	 * Generates captures and good promos
	 */
	public void generateCaptures() {
		long square = 0x1L;
		for (int index = 0; index < 64; index++) {
			if ((square & mines) != 0) {
				if ((square & board.rooks) != 0) { // Rook
					generateMovesFromAttacks(Piece.ROOK, index, square, ai.attacksFromSquare[index] & others, true);
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Piece.BISHOP, index, square, ai.attacksFromSquare[index] & others, true);
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Piece.QUEEN, index, square, ai.attacksFromSquare[index] & others, true);
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Piece.KING, index, square, ai.attacksFromSquare[index] & others & ~ai.attackedSquaresAlsoPinned[them], true);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Piece.KNIGHT, index, square, ai.attacksFromSquare[index] & others, true);
				} else if ((square & board.pawns) != 0) { // Pawns
					if (turn) {
						generatePawnCapturesOrGoodPromos(index, square, //
								(ai.attacksFromSquare[index] & (others | board.getPassantSquare())) //
										| (((square & BitboardUtils.b2_u) != 0) && (((square << 8) & all) == 0) ? (square << 8) : 0), // Pushes only if promotion
								board.getPassantSquare());
					} else {
						generatePawnCapturesOrGoodPromos(index, square, //
								(ai.attacksFromSquare[index] & (others | board.getPassantSquare())) //
										| (((square & BitboardUtils.b2_d) != 0) && (((square >>> 8) & all) == 0) ? (square >>> 8) : 0), // Pushes only if promotion
								board.getPassantSquare());
					}
				}
			}
			square <<= 1;
		}
	}

	/**
	 * Generates non tactical moves
	 */
	public void generateNonCaptures() {
		long square = 0x1L;
		for (int index = 0; index < 64; index++) {
			if ((square & mines) != 0) {
				if ((square & board.rooks) != 0) { // Rook
					generateMovesFromAttacks(Piece.ROOK, index, square, ai.attacksFromSquare[index] & ~all, false);
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Piece.BISHOP, index, square, ai.attacksFromSquare[index] & ~all, false);
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Piece.QUEEN, index, square, ai.attacksFromSquare[index] & ~all, false);
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Piece.KING, index, square, ai.attacksFromSquare[index] & ~all & ~ai.attackedSquaresAlsoPinned[them], false);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Piece.KNIGHT, index, square, ai.attacksFromSquare[index] & ~all, false);
				}
				if ((square & board.pawns) != 0) { // Pawns excluding the already generated promos
					if (turn) {
						generatePawnNonCapturesAndBadPromos(index, square, (((square << 8) & all) == 0 ? (square << 8) : 0)
								| ((square & BitboardUtils.b2_d) != 0 && (((square << 8) | (square << 16)) & all) == 0 ? (square << 16) : 0));
					} else {
						generatePawnNonCapturesAndBadPromos(index, square, (((square >>> 8) & all) == 0 ? (square >>> 8) : 0)
								| ((square & BitboardUtils.b2_u) != 0 && (((square >>> 8) | (square >>> 16)) & all) == 0 ? (square >>> 16) : 0));
					}
				}
			}
			square <<= 1;
		}
		// Castling: disabled when in check or king route attacked
		if (!board.getCheck()) {
			if (turn ? board.getWhiteKingsideCastling() : board.getBlackKingsideCastling()) {
				long rookOrigin = board.castlingRooks[turn ? 0 : 2];
				long rookDestiny = Board.CASTLING_ROOK_DESTINY_SQUARE[turn ? 0 : 2];
				long rookRoute = BitboardUtils.getHorizontalLine(rookDestiny, rookOrigin) & ~rookOrigin;
				long kingOrigin = board.kings & mines;
				long kingDestiny = Board.CASTLING_KING_DESTINY_SQUARE[turn ? 0 : 2];
				long kingRoute = BitboardUtils.getHorizontalLine(kingOrigin, kingDestiny) & ~kingOrigin;

				if ((all & (kingRoute | rookRoute) & ~rookOrigin & ~kingOrigin) == 0 //
						&& (ai.attackedSquaresAlsoPinned[them] & kingRoute) == 0) {
					addMove(Piece.KING, ai.kingIndex[us], kingOrigin, board.chess960 ? rookOrigin : kingDestiny, false, Move.TYPE_KINGSIDE_CASTLING);
				}
			}
			if (turn ? board.getWhiteQueensideCastling() : board.getBlackQueensideCastling()) {
				long rookOrigin = board.castlingRooks[turn ? 1 : 3];
				long rookDestiny = Board.CASTLING_ROOK_DESTINY_SQUARE[turn ? 1 : 3];
				long rookRoute = BitboardUtils.getHorizontalLine(rookOrigin, rookDestiny) & ~rookOrigin;
				long kingOrigin = board.kings & mines;
				long kingDestiny = Board.CASTLING_KING_DESTINY_SQUARE[turn ? 1 : 3];
				long kingRoute = BitboardUtils.getHorizontalLine(kingDestiny, kingOrigin) & ~kingOrigin;

				if ((all & (kingRoute | rookRoute) & ~rookOrigin & ~kingOrigin) == 0 //
						&& (ai.attackedSquaresAlsoPinned[them] & kingRoute) == 0) {
					addMove(Piece.KING, ai.kingIndex[us], kingOrigin, board.chess960 ? rookOrigin : kingDestiny, false, Move.TYPE_QUEENSIDE_CASTLING);
				}
			}
		}
	}

	public void generateCheckEvasionCaptures() {
		// King can capture one of the checking pieces if two pieces giving check
		generateMovesFromAttacks(Piece.KING, ai.kingIndex[us], board.kings & mines, others & ai.attacksFromSquare[ai.kingIndex[us]] & ~ai.attackedSquaresAlsoPinned[them], true);

		if (BitboardUtils.popCount(ai.piecesGivingCheck) == 1) {
			long square = 1;
			for (int index = 0; index < 64; index++) {
				if ((square & mines) != 0 && (square & board.kings) == 0) {
					if ((square & board.pawns) != 0) { // Pawns
						long destinySquares = 0;
						// Good promotion interposes to the check
						if ((square & (turn ? BitboardUtils.b2_u : BitboardUtils.b2_d)) != 0) { // Pawn about to promote
							destinySquares = ai.interposeCheckSquares & (turn ? (((square << 8) & all) == 0 ? (square << 8) : 0) : (((square >>> 8) & all) == 0 ? (square >>> 8) : 0));
						}
						// Pawn captures the checking piece
						destinySquares |= (ai.attacksFromSquare[index] & ai.piecesGivingCheck);
						if (destinySquares != 0) {
							generatePawnCapturesOrGoodPromos(index, square, destinySquares, board.getPassantSquare());
						} else if (board.getPassantSquare() != 0 &&
								(ai.attacksFromSquare[index] & board.getPassantSquare()) != 0) { // This pawn can capture to the passant square
							long testPassantSquare = (turn ? ai.piecesGivingCheck << 8 : ai.piecesGivingCheck >>> 8);
							if (testPassantSquare == board.getPassantSquare() || // En-passant capture target giving check
									(board.getPassantSquare() & ai.interposeCheckSquares) != 0) { // En passant capture to interpose
								addMove(Piece.PAWN, index, square, board.getPassantSquare(), true, Move.TYPE_PASSANT);
							}
						}
					} else {
						if (((ai.attacksFromSquare[index] & ai.piecesGivingCheck)) != 0) {
							if ((square & board.rooks) != 0) { // Rook
								generateMovesFromAttacks(Piece.ROOK, index, square, ai.piecesGivingCheck, true);
							} else if ((square & board.bishops) != 0) { // Bishop
								generateMovesFromAttacks(Piece.BISHOP, index, square, ai.piecesGivingCheck, true);
							} else if ((square & board.queens) != 0) { // Queen
								generateMovesFromAttacks(Piece.QUEEN, index, square, ai.piecesGivingCheck, true);
							} else if ((square & board.knights) != 0) { // Knight
								generateMovesFromAttacks(Piece.KNIGHT, index, square, ai.piecesGivingCheck, true);
							}
						}
					}
				}
				square <<= 1;
			}
		}
	}

	public void generateCheckEvasionsNonCaptures() {
		// Moving king (without captures)
		generateMovesFromAttacks(Piece.KING, ai.kingIndex[us], board.kings & mines, ai.attacksFromSquare[ai.kingIndex[us]] & ~all & ~ai.attackedSquaresAlsoPinned[them], false);

		// Interpose: Cannot interpose with more than one piece giving check
		if (BitboardUtils.popCount(ai.piecesGivingCheck) == 1) {
			long square = 1;
			for (int index = 0; index < 64; index++) {
				if ((square & mines) != 0 && (square & board.kings) == 0) {
					if ((square & board.pawns) != 0) {
						long destinySquares;
						if (turn) {
							destinySquares = ai.interposeCheckSquares &
									((((square << 8) & all) == 0 ? (square << 8) : 0)
											| ((square & BitboardUtils.b2_d) != 0 && (((square << 8) | (square << 16)) & all) == 0 ? (square << 16) : 0));
						} else {
							destinySquares = ai.interposeCheckSquares &
									((((square >>> 8) & all) == 0 ? (square >>> 8) : 0)
											| ((square & BitboardUtils.b2_u) != 0 && (((square >>> 8) | (square >>> 16)) & all) == 0 ? (square >>> 16) : 0));
						}
						if (destinySquares != 0) {
							generatePawnNonCapturesAndBadPromos(index, square, destinySquares);
						}
					} else {
						long destinySquares = ai.attacksFromSquare[index] & ai.interposeCheckSquares & ~all;
						if (destinySquares != 0) {
							if ((square & board.rooks) != 0) { // Rook
								generateMovesFromAttacks(Piece.ROOK, index, square, destinySquares, false);
							} else if ((square & board.bishops) != 0) { // Bishop
								generateMovesFromAttacks(Piece.BISHOP, index, square, destinySquares, false);
							} else if ((square & board.queens) != 0) { // Queen
								generateMovesFromAttacks(Piece.QUEEN, index, square, destinySquares, false);
							} else if ((square & board.knights) != 0) { // Knight
								generateMovesFromAttacks(Piece.KNIGHT, index, square, destinySquares, false);
							}
						}
					}
				}
				square <<= 1;
			}
		}
	}

	/**
	 * Generates moves from an attack mask
	 */
	private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long from, long attacks, boolean capture) {
		while (attacks != 0) {
			long to = turn ? BitboardUtils.msb(attacks) : BitboardUtils.lsb(attacks);
			addMove(pieceMoved, fromIndex, from, to, capture, 0);
			attacks ^= to;
		}
	}

	private void generatePawnCapturesOrGoodPromos(int fromIndex, long from, long attacks, long passant) {
		if ((ai.pinnedPieces & from) != 0) {
			attacks &= ai.pinnedMobility[fromIndex]; // Be careful with pawn advance moves, the pawn may be pinned
		}

		while (attacks != 0) {
			long to = turn ? BitboardUtils.msb(attacks) : BitboardUtils.lsb(attacks);
			if ((to & passant) != 0) {
				addMove(Piece.PAWN, fromIndex, from, to, true, Move.TYPE_PASSANT);
			} else {
				boolean capture = (to & others) != 0;
				if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0) {
					addMove(Piece.PAWN, fromIndex, from, to, capture, Move.TYPE_PROMOTION_QUEEN);
					// If it is a capture, we must add the underpromotions
					if (capture) {
						addMove(Piece.PAWN, fromIndex, from, to, true, Move.TYPE_PROMOTION_KNIGHT);
						addMove(Piece.PAWN, fromIndex, from, to, true, Move.TYPE_PROMOTION_ROOK);
						addMove(Piece.PAWN, fromIndex, from, to, true, Move.TYPE_PROMOTION_BISHOP);
					}
				} else if (capture) {
					addMove(Piece.PAWN, fromIndex, from, to, true, 0);
				}
			}
			attacks ^= to;
		}
	}

	private void generatePawnNonCapturesAndBadPromos(int fromIndex, long from, long attacks) {
		if ((ai.pinnedPieces & from) != 0) {
			attacks &= ai.pinnedMobility[fromIndex]; // Be careful with pawn advance moves, the pawn may be pinned
		}

		while (attacks != 0) {
			long to = turn ? BitboardUtils.msb(attacks) : BitboardUtils.lsb(attacks);
			if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0) {
				addMove(Piece.PAWN, fromIndex, from, to, false, Move.TYPE_PROMOTION_KNIGHT);
				addMove(Piece.PAWN, fromIndex, from, to, false, Move.TYPE_PROMOTION_ROOK);
				addMove(Piece.PAWN, fromIndex, from, to, false, Move.TYPE_PROMOTION_BISHOP);
			} else {
				addMove(Piece.PAWN, fromIndex, from, to, false, 0);
			}
			attacks ^= to;
		}
	}

	private void addMove(int pieceMoved, int fromIndex, long from, long to, boolean capture, int moveType) {
		int toIndex = BitboardUtils.square2Index(to);

		//
		// Verify check and legality
		//
		boolean check = false;
		int newMyKingIndex;
		long rookSlidersAfterMove, allAfterMove, minesAfterMove;
		long bishopSlidersAfterMove = (board.bishops | board.queens) & ~from & ~to;
		long squaresForDiscovery = from;

		if (moveType == Move.TYPE_KINGSIDE_CASTLING || moveType == Move.TYPE_QUEENSIDE_CASTLING) {
			// {White Kingside, White Queenside, Black Kingside, Black Queenside}
			int j = (turn ? 0 : 2) + (moveType == Move.TYPE_QUEENSIDE_CASTLING ? 1 : 0);

			newMyKingIndex = Board.CASTLING_KING_DESTINY_INDEX[j];
			// Castling has a special "to" in Chess960 where the destiny square is the rook
			long kingTo = Board.CASTLING_KING_DESTINY_SQUARE[j];
			long rookTo = Board.CASTLING_ROOK_DESTINY_SQUARE[j];
			long rookMoveMask = board.castlingRooks[j] ^ rookTo;

			rookSlidersAfterMove = (board.rooks ^ rookMoveMask) | board.queens;
			allAfterMove = ((all ^ rookMoveMask) | kingTo) & ~from;
			minesAfterMove = ((mines ^ rookMoveMask) | kingTo) & ~from;

			// Direct check by rook
			check |= (rookTo & ai.rookAttacksKing[them]) != 0;
		} else {
			if (pieceMoved == Piece.KING) {
				newMyKingIndex = toIndex;
			} else {
				newMyKingIndex = ai.kingIndex[us];
			}

			rookSlidersAfterMove = (board.rooks | board.queens) & ~from & ~to;
			allAfterMove = (all | to) & ~from;
			minesAfterMove = (mines | to) & ~from;
			squaresForDiscovery = from;

			if (moveType == Move.TYPE_PASSANT) {
				squaresForDiscovery |= (turn ? to >>> 8 : to << 8);
				allAfterMove &= ~squaresForDiscovery;
			}

			// Direct checks
			if (pieceMoved == Piece.KNIGHT || moveType == Move.TYPE_PROMOTION_KNIGHT) {
				check = (to & bbAttacks.knight[ai.kingIndex[them]]) != 0;
			} else if (pieceMoved == Piece.BISHOP || moveType == Move.TYPE_PROMOTION_BISHOP) {
				check = (to & ai.bishopAttacksKing[them]) != 0;
				bishopSlidersAfterMove |= to;
			} else if (pieceMoved == Piece.ROOK || moveType == Move.TYPE_PROMOTION_ROOK) {
				check = (to & ai.rookAttacksKing[them]) != 0;
				rookSlidersAfterMove |= to;
			} else if (pieceMoved == Piece.QUEEN || moveType == Move.TYPE_PROMOTION_QUEEN) {
				check = (to & (ai.bishopAttacksKing[them] | ai.rookAttacksKing[them])) != 0;
				bishopSlidersAfterMove |= to;
				rookSlidersAfterMove |= to;
			} else if (pieceMoved == Piece.PAWN) {
				check = (to & bbAttacks.pawn[them][ai.kingIndex[them]]) != 0;
			}
		}

		/**
		 *  As AttacksInfo already excludes pinned pieces, we only must take care from en passant captures
		 *  (they can remove two pieces from 4th rank discovering a check) and king moves when the king is
		 *  in check by a slider
		 */
		if ((squaresForDiscovery & ai.mayPin[them]) != 0 && (moveType == Move.TYPE_PASSANT || ((ai.piecesGivingCheck & (board.rooks | board.bishops | board.queens)) != 0 && pieceMoved == Piece.KING))) {
			// Candidates to leave the king in check after moving
			if (((squaresForDiscovery & ai.bishopAttacksKing[us]) != 0) ||
					((ai.piecesGivingCheck & (board.bishops | board.queens)) != 0 && pieceMoved == Piece.KING)) { // Moving the king when the king is in check by a slider
				// Regenerate bishop attacks to my king
				long newBishopAttacks = bbAttacks.getBishopAttacks(newMyKingIndex, allAfterMove);
				if ((newBishopAttacks & bishopSlidersAfterMove & ~minesAfterMove) != 0) {
					return; // Illegal move
				}
			}
			if ((squaresForDiscovery & ai.rookAttacksKing[us]) != 0 ||
					((ai.piecesGivingCheck & (board.rooks | board.queens)) != 0 && pieceMoved == Piece.KING)) {
				// Regenerate rook attacks to my king
				long newRookAttacks = bbAttacks.getRookAttacks(newMyKingIndex, allAfterMove);
				if ((newRookAttacks & rookSlidersAfterMove & ~minesAfterMove) != 0) {
					return; // Illegal move
				}
			}
		}

		// After a promotion to queen or rook there are new sliders transversing the origin square, so mayPin is not valid
		if (!check && ((squaresForDiscovery & ai.mayPin[us]) != 0 || moveType == Move.TYPE_PROMOTION_QUEEN || moveType == Move.TYPE_PROMOTION_ROOK || moveType == Move.TYPE_PROMOTION_BISHOP)) {
			// Discovered checks
			if ((squaresForDiscovery & ai.bishopAttacksKing[them]) != 0) {
				// Regenerate bishop attacks to the other king
				long newBishopAttacks = bbAttacks.getBishopAttacks(ai.kingIndex[them], allAfterMove);
				if ((newBishopAttacks & bishopSlidersAfterMove & minesAfterMove) != 0) {
					check = true;
				}
			}
			if ((squaresForDiscovery & ai.rookAttacksKing[them]) != 0) {
				// Regenerate rook attacks to the other king
				long newRookAttacks = bbAttacks.getRookAttacks(ai.kingIndex[them], allAfterMove);
				if ((newRookAttacks & rookSlidersAfterMove & minesAfterMove) != 0) {
					check = true;
				}
			}
		}

		// Generating checks, if the move is not a check, skip it
		if (movesToGenerate == GENERATE_CAPTURES_PROMOS_CHECKS && !checkEvasion && !check && !capture && moveType != Move.TYPE_PROMOTION_QUEEN) {
			return;
		}

		// Now, with legality verified and the check flag, generate the move
		int move = Move.genMove(fromIndex, toIndex, pieceMoved, capture, check, moveType);
		if (move == ttMove) {
			return;
		}
		if (!capture) {
			if (move == killer1) {
				foundKiller1 = true;
				return;
			} else if (move == killer2) {
				foundKiller2 = true;
				return;
			} else if (move == killer3) {
				foundKiller3 = true;
				return;
			} else if (move == killer4) {
				foundKiller4 = true;
				return;
			}
		}

		int pieceCaptured = capture ? Move.getPieceCaptured(board, move) : 0;
		int see = SEE_NOT_CALCULATED;

		if (capture || (movesToGenerate == GENERATE_CAPTURES_PROMOS_CHECKS && check)) {
			// If there aren't pieces attacking the destiny square
			// and the piece cannot pin an attack to the see square,
			// the see will be the captured piece value
			if ((ai.attackedSquares[them] & to) == 0
					&& (ai.mayPin[them] & from) == 0) {
				see = capture ? Board.SEE_PIECE_VALUES[pieceCaptured] : 0;
			} else {
				see = board.see(fromIndex, toIndex, pieceMoved, pieceCaptured);
			}
		}

		if (movesToGenerate != GENERATE_ALL && !checkEvasion && see < 0) {
			return;
		}

		if (capture && see < 0) {
			badCaptures[badCaptureIndex] = move;
			badCapturesScores[badCaptureIndex] = see;
			badCaptureIndex++;
			return;
		}

		boolean underPromotion = moveType == Move.TYPE_PROMOTION_KNIGHT || moveType == Move.TYPE_PROMOTION_ROOK || moveType == Move.TYPE_PROMOTION_BISHOP;

		if ((capture || moveType == Move.TYPE_PROMOTION_QUEEN) && !underPromotion) {
			// Order GOOD captures by MVV/LVA (Hyatt dixit)
			int score = 0;
			if (capture) {
				score = VICTIM_PIECE_VALUES[pieceCaptured] - AGGRESSOR_PIECE_VALUES[pieceMoved];
			}
			if (moveType == Move.TYPE_PROMOTION_QUEEN) {
				score += SCORE_PROMOTION_QUEEN;
			}
			if (see > 0 || moveType == Move.TYPE_PROMOTION_QUEEN) {
				goodCaptures[goodCaptureIndex] = move;
				goodCapturesSee[goodCaptureIndex] = see;
				goodCapturesScores[goodCaptureIndex] = score;
				goodCaptureIndex++;
			} else {
				equalCaptures[equalCaptureIndex] = move;
				equalCapturesSee[equalCaptureIndex] = see;
				equalCapturesScores[equalCaptureIndex] = score;
				equalCaptureIndex++;
			}
		} else {
			nonCaptures[nonCaptureIndex] = move;
			nonCapturesSee[nonCaptureIndex] = see;
			nonCapturesScores[nonCaptureIndex] = underPromotion ? SCORE_UNDERPROMOTION : sortInfo.getMoveScore(move);
			nonCaptureIndex++;
		}
	}
}