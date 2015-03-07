package com.alonsoruibal.chess.movesort;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/**
 * This iterator sets the check flag on moves. It also checks if the move is legal before returning it
 * <p/>
 * Sort Moves based on heuristics
 * Sort first GOOD captures (a piece of less value captures other of more value)
 * <p/>
 * SEE captures, and move captures with SEE<0 to the end
 */
public class MoveIterator {
	//	private static final Logger logger = Logger.getLogger(MoveIterator.class);
	public final static int PHASE_TT = 0;
	public final static int PHASE_GEN_CAPTURES = 1;
	public final static int PHASE_GOOD_CAPTURES_AND_PROMOS = 2;
	public final static int PHASE_EQUAL_CAPTURES = 3;
	public final static int PHASE_GEN_NON_CAPTURES = 4;
	public final static int PHASE_KILLER1 = 5;
	public final static int PHASE_KILLER2 = 6;
	public final static int PHASE_NON_CAPTURES = 7;
	public final static int PHASE_BAD_CAPTURES = 8;
	public final static int PHASE_END = 9;

	private static final int[] VICTIM_PIECE_VALUES = {0, 100, 325, 330, 500, 975, 10000};
	private static final int[] AGGRESSOR_PIECE_VALUES = {0, 10, 32, 33, 50, 97, 99};
	private static final int SCORE_PROMOTION_QUEEN = 975;
	private static final int SCORE_UNDERPROMOTION = Integer.MIN_VALUE + 1;
	private static final int SCORE_LOWEST = Integer.MIN_VALUE;

	private Board board;
	private AttacksInfo attacksInfo;

	private int ttMove;
	private int lastMoveSee;
	private int killer1;
	private int killer2;
	private boolean foundKiller1;
	private boolean foundKiller2;
	private boolean quiescence;
	private boolean generateChecks;
	private boolean checkEvasion;

	private int nonCaptureIndex;
	private int goodCaptureIndex;
	private int equalCaptureIndex;
	private int badCaptureIndex;

	private boolean turn;
	private long all;
	private long mines;
	private long others;

	public int[] goodCaptures = new int[256]; // Stores captures and queen promotions
	public int[] goodCapturesSee = new int[256];
	public int[] goodCapturesScores = new int[256];
	public int[] badCaptures = new int[256]; // Stores captures and queen promotions
	public int[] badCapturesSee = new int[256];
	public int[] badCapturesScores = new int[256];
	public int[] equalCaptures = new int[256]; // Stores captures and queen promotions
	public int[] equalCapturesSee = new int[256];
	public int[] equalCapturesScores = new int[256];
	public int[] nonCaptures = new int[256]; // Stores non captures and underpromotions
	public int[] nonCapturesScores = new int[256];

	private int depth;
	SortInfo sortInfo;
	int phase;

	BitboardAttacks bbAttacks;

	public int getPhase() {
		return phase;
	}

	public MoveIterator(Board board, AttacksInfo attacksInfo, SortInfo sortInfo, int depth) {
		this.board = board;
		this.attacksInfo = attacksInfo;
		this.sortInfo = sortInfo;
		this.depth = depth;

		bbAttacks = BitboardAttacks.getInstance();
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
					generateMovesFromAttacks(Move.ROOK, index, attacksInfo.attacksFromSquare[index] & others, true);
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Move.BISHOP, index, attacksInfo.attacksFromSquare[index] & others, true);
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Move.QUEEN, index, attacksInfo.attacksFromSquare[index] & others, true);
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Move.KING, index, attacksInfo.attacksFromSquare[index] & others & ~attacksInfo.attackedSquares[turn ? 1 : 0], true);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Move.KNIGHT, index, attacksInfo.attacksFromSquare[index] & others, true);
				} else if ((square & board.pawns) != 0) { // Pawns
					if (turn) {
						generatePawnCapturesOrGoodPromos(index, //
								(attacksInfo.attacksFromSquare[index] & (others | board.getPassantSquare())) //
										| (((square & BitboardUtils.b2_u) != 0) && (((square << 8) & all) == 0) ? (square << 8) : 0), // Pushes only if promotion
								board.getPassantSquare());
					} else {
						generatePawnCapturesOrGoodPromos(index, //
								(attacksInfo.attacksFromSquare[index] & (others | board.getPassantSquare())) //
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
					generateMovesFromAttacks(Move.ROOK, index, attacksInfo.attacksFromSquare[index] & ~all, false);
				} else if ((square & board.bishops) != 0) { // Bishop
					generateMovesFromAttacks(Move.BISHOP, index, attacksInfo.attacksFromSquare[index] & ~all, false);
				} else if ((square & board.queens) != 0) { // Queen
					generateMovesFromAttacks(Move.QUEEN, index, attacksInfo.attacksFromSquare[index] & ~all, false);
				} else if ((square & board.kings) != 0) { // King
					generateMovesFromAttacks(Move.KING, index, attacksInfo.attacksFromSquare[index] & ~all & ~attacksInfo.attackedSquares[turn ? 1 : 0], false);
				} else if ((square & board.knights) != 0) { // Knight
					generateMovesFromAttacks(Move.KNIGHT, index, attacksInfo.attacksFromSquare[index] & ~all, false);
				}
				if ((square & board.pawns) != 0) { // Pawns excluding the already generated promos
					if (turn) {
						generatePawnNonCapturesAndBadPromos(index, (((square << 8) & all) == 0 ? (square << 8) : 0)
								| ((square & BitboardUtils.b2_d) != 0 && (((square << 8) | (square << 16)) & all) == 0 ? (square << 16) : 0));
					} else {
						generatePawnNonCapturesAndBadPromos(index, (((square >>> 8) & all) == 0 ? (square >>> 8) : 0)
								| ((square & BitboardUtils.b2_u) != 0 && (((square >>> 8) | (square >>> 16)) & all) == 0 ? (square >>> 16) : 0));
					}
				}
			}
			square <<= 1;
		}
		// Castling: disabled when in check or squares attacked
		if ((((all & (turn ? 0x06L : 0x0600000000000000L)) == 0 //
				&& (turn ? board.getWhiteKingsideCastling() : board.getBlackKingsideCastling()))) //
				&& ((attacksInfo.attackedSquares[turn ? 1 : 0] & (turn ? 0x0EL : 0x0E00000000000000L)) == 0)) {
			addMove(Move.KING, attacksInfo.myKingIndex, attacksInfo.myKingIndex - 2, 0, false, Move.TYPE_KINGSIDE_CASTLING);
		}
		if ((((all & (turn ? 0x70L : 0x7000000000000000L)) == 0 //
				&& (turn ? board.getWhiteQueensideCastling() : board.getBlackQueensideCastling())))
				&& ((attacksInfo.attackedSquares[turn ? 1 : 0] & (turn ? 0x34L : 0x3400000000000000L)) == 0)) {
			addMove(Move.KING, attacksInfo.myKingIndex, attacksInfo.myKingIndex + 2, 0, false, Move.TYPE_QUEENSIDE_CASTLING);
		}
	}

	public void generateCheckEvasionCaptures() {
		// King can capture one of the checking pieces if two pieces giving check
		generateMovesFromAttacks(Move.KING, attacksInfo.myKingIndex, others & attacksInfo.attacksFromSquare[attacksInfo.myKingIndex] & ~attacksInfo.attackedSquares[turn ? 1 : 0], true);

		if (BitboardUtils.popCount(attacksInfo.piecesGivingCheck) == 1) {
			long square = 1;
			for (int index = 0; index < 64; index++) {
				if ((square & mines) != 0 && (square & board.kings) == 0) {
					if ((square & board.pawns) != 0) { // Pawns
						long destinySquares = 0;
						// Good promotion interposes to the check
						if ((square & (turn ? BitboardUtils.b2_u : BitboardUtils.b2_d)) != 0) { // Pawn about to promote
							destinySquares = attacksInfo.interposeCheckSquares & (turn ? (((square << 8) & all) == 0 ? (square << 8) : 0) : (((square >>> 8) & all) == 0 ? (square >>> 8) : 0));
						}
						// Pawn captures the checking piece
						destinySquares |= (attacksInfo.attacksFromSquare[index] & attacksInfo.piecesGivingCheck);
						if (destinySquares != 0) {
							generatePawnCapturesOrGoodPromos(index, destinySquares, board.getPassantSquare());
						} else if (board.getPassantSquare() != 0 &&
								(attacksInfo.attacksFromSquare[index] & board.getPassantSquare()) != 0) { // This pawn can capture to the passant square
							long testPassantSquare = (turn ? attacksInfo.piecesGivingCheck << 8 : attacksInfo.piecesGivingCheck >>> 8);
							if (testPassantSquare == board.getPassantSquare() || // En-passant capture target giving check
									(board.getPassantSquare() & attacksInfo.interposeCheckSquares) != 0) { // En passant capture to interpose
								addMove(Move.PAWN, index, BitboardUtils.square2Index(board.getPassantSquare()), board.getPassantSquare(), true, Move.TYPE_PASSANT);
							}
						}
					} else {
						if (((attacksInfo.attacksFromSquare[index] & attacksInfo.piecesGivingCheck)) != 0) {
							if ((square & board.rooks) != 0) { // Rook
								generateMovesFromAttacks(Move.ROOK, index, attacksInfo.piecesGivingCheck, true);
							} else if ((square & board.bishops) != 0) { // Bishop
								generateMovesFromAttacks(Move.BISHOP, index, attacksInfo.piecesGivingCheck, true);
							} else if ((square & board.queens) != 0) { // Queen
								generateMovesFromAttacks(Move.QUEEN, index, attacksInfo.piecesGivingCheck, true);
							} else if ((square & board.knights) != 0) { // Knight
								generateMovesFromAttacks(Move.KNIGHT, index, attacksInfo.piecesGivingCheck, true);
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
		generateMovesFromAttacks(Move.KING, attacksInfo.myKingIndex, attacksInfo.attacksFromSquare[attacksInfo.myKingIndex] & ~all & ~attacksInfo.attackedSquares[turn ? 1 : 0], false);

		// Interpose: Cannot interpose with more than one piece giving check
		if (BitboardUtils.popCount(attacksInfo.piecesGivingCheck) == 1) {
			long square = 1;
			for (int index = 0; index < 64; index++) {
				if ((square & mines) != 0 && (square & board.kings) == 0) {
					if ((square & board.pawns) != 0) {
						long destinySquares;
						if (turn) {
							destinySquares = attacksInfo.interposeCheckSquares &
									((((square << 8) & all) == 0 ? (square << 8) : 0)
											| ((square & BitboardUtils.b2_d) != 0 && (((square << 8) | (square << 16)) & all) == 0 ? (square << 16) : 0));
						} else {
							destinySquares = attacksInfo.interposeCheckSquares &
									((((square >>> 8) & all) == 0 ? (square >>> 8) : 0)
											| ((square & BitboardUtils.b2_u) != 0 && (((square >>> 8) | (square >>> 16)) & all) == 0 ? (square >>> 16) : 0));
						}
						if (destinySquares != 0) {
							generatePawnNonCapturesAndBadPromos(index, destinySquares);
						}
					} else {
						long destinySquares = attacksInfo.attacksFromSquare[index] & attacksInfo.interposeCheckSquares & ~all;
						if (destinySquares != 0) {
							if ((square & board.rooks) != 0) { // Rook
								generateMovesFromAttacks(Move.ROOK, index, destinySquares, false);
							} else if ((square & board.bishops) != 0) { // Bishop
								generateMovesFromAttacks(Move.BISHOP, index, destinySquares, false);
							} else if ((square & board.queens) != 0) { // Queen
								generateMovesFromAttacks(Move.QUEEN, index, destinySquares, false);
							} else if ((square & board.knights) != 0) { // Knight
								generateMovesFromAttacks(Move.KNIGHT, index, destinySquares, false);
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
	private void generateMovesFromAttacks(int pieceMoved, int fromIndex, long attacks, boolean capture) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			addMove(pieceMoved, fromIndex, BitboardUtils.square2Index(to), to, capture, 0);
			attacks ^= to;
		}
	}

	private void generatePawnCapturesOrGoodPromos(int fromIndex, long attacks, long passant) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			if ((to & passant) != 0) {
				addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, true, Move.TYPE_PASSANT);
			} else {
				boolean capture = (to & others) != 0;
				if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0) {
					addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_QUEEN);
					// If it is a capture, we must add the underpromotions
					if (capture) {
						addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_KNIGHT);
						addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_ROOK);
						addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_BISHOP);
					}
				} else if (capture) {
					addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, 0);
				}
			}
			attacks ^= to;
		}
	}

	private void generatePawnNonCapturesAndBadPromos(int fromIndex, long attacks) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0) {
				addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, false, Move.TYPE_PROMOTION_KNIGHT);
				addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, false, Move.TYPE_PROMOTION_ROOK);
				addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, false, Move.TYPE_PROMOTION_BISHOP);
			} else {
				addMove(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, false, 0);
			}
			attacks ^= to;
		}
	}

	private void addMove(int pieceMoved, int fromIndex, int toIndex, long squareCaptured, boolean capture, int moveType) {
		int move = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
		if (move != ttMove) {

			boolean underPromotion = (moveType == Move.TYPE_PROMOTION_KNIGHT || moveType == Move.TYPE_PROMOTION_ROOK || moveType == Move.TYPE_PROMOTION_BISHOP);

			if (capture || (moveType == Move.TYPE_PROMOTION_QUEEN)) {
				int see = 0;
				int score = 0;

				if (capture) {
					// Score captures
					int pieceCaptured = 0;

					if ((squareCaptured & board.knights) != 0) {
						pieceCaptured = Move.KNIGHT;
					} else if ((squareCaptured & board.bishops) != 0) {
						pieceCaptured = Move.BISHOP;
					} else if ((squareCaptured & board.rooks) != 0) {
						pieceCaptured = Move.ROOK;
					} else if ((squareCaptured & board.queens) != 0) {
						pieceCaptured = Move.QUEEN;
					} else if (capture) {
						pieceCaptured = Move.PAWN;
					}

					see = board.see(fromIndex, toIndex, pieceMoved, pieceCaptured);
					// Order GOOD captures by MVV/LVA (Hyatt dixit)
					score = VICTIM_PIECE_VALUES[pieceCaptured] - AGGRESSOR_PIECE_VALUES[pieceMoved];
				}

				if (moveType == Move.TYPE_PROMOTION_QUEEN) {
					score += SCORE_PROMOTION_QUEEN;
				}

				if (see >= 0 && !underPromotion) {
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
					badCaptures[badCaptureIndex] = move;
					badCapturesSee[badCaptureIndex] = see;
					badCapturesScores[badCaptureIndex] = see; // <---- !!!
					badCaptureIndex++;
				}
			} else {
				if (move == killer1) {
					foundKiller1 = true;
				} else if (move == killer2) {
					foundKiller2 = true;
				} else {
					// Score non captures
					int score;
					if (underPromotion) {
						score = SCORE_UNDERPROMOTION;
					} else {
						score = sortInfo.getMoveScore(move);
					}

					nonCaptures[nonCaptureIndex] = move;
					nonCapturesScores[nonCaptureIndex] = score;
					nonCaptureIndex++;
				}
			}
		}
	}

	public void genMoves(int ttMove) {
		genMoves(ttMove, false, true);
	}

	public void genMoves(int ttMove, boolean quiescence, boolean generateChecks) {
		this.ttMove = ttMove & ~Move.CHECK_MASK; // Remove check flag from killers and TT move
		killer1 = sortInfo.killerMove1[depth] & ~Move.CHECK_MASK;
		killer2 = sortInfo.killerMove2[depth] & ~Move.CHECK_MASK;

		attacksInfo.build(board);

		foundKiller1 = false;
		foundKiller2 = false;

		this.quiescence = quiescence;
		this.generateChecks = generateChecks;

		checkEvasion = board.getCheck();
		phase = PHASE_TT;
		lastMoveSee = 0;
		goodCaptureIndex = 0;
		badCaptureIndex = 0;
		equalCaptureIndex = 0;
		nonCaptureIndex = 0;

		// Only for clarity
		turn = board.getTurn();
		all = board.getAll();
		mines = board.getMines();
		others = board.getOthers();
	}

	/**
	 * Sets the check flag and verify move legality
	 *
	 * @returns Move.MOVE_NONE if the move is not legal
	 */
	public int setCheckAndLegality(int move) {
		if (move == Move.NONE) {
			return move;
		}

		boolean check = false;
		long from = Move.getFromSquare(move);
		long to = Move.getToSquare(move);
		int pieceMoved = Move.getPieceMoved(move);
		int moveType = Move.getMoveType(move);
		long bishopSlidersAftermove = (board.bishops | board.queens) & ~from & ~to;
		long rookSlidersAftermove = (board.rooks | board.queens) & ~from & ~to;
		long allAfterMove = (all | to) & ~from;
		long minesAfterMove = (mines | to) & ~from;

		// Direct checks
		if (pieceMoved == Move.KNIGHT || moveType == Move.TYPE_PROMOTION_KNIGHT) {
			check = (to & bbAttacks.knight[attacksInfo.otherKingIndex]) != 0;
		} else if (pieceMoved == Move.BISHOP || moveType == Move.TYPE_PROMOTION_BISHOP) {
			check = (to & attacksInfo.bishopAttacksOtherking) != 0;
			bishopSlidersAftermove |= to;
		} else if (pieceMoved == Move.ROOK || moveType == Move.TYPE_PROMOTION_ROOK) {
			check = (to & attacksInfo.rookAttacksOtherking) != 0;
			rookSlidersAftermove |= to;
		} else if (pieceMoved == Move.QUEEN || moveType == Move.TYPE_PROMOTION_QUEEN) {
			check = (to & (attacksInfo.bishopAttacksOtherking | attacksInfo.rookAttacksOtherking)) != 0;
			bishopSlidersAftermove |= to;
			rookSlidersAftermove |= to;
		} else if (pieceMoved == Move.PAWN) {
			check = (to & (turn ? bbAttacks.pawnDownwards[attacksInfo.otherKingIndex] : bbAttacks.pawnUpwards[attacksInfo.otherKingIndex])) != 0;
		}

		long squaresForDiscovery = from;
		if (moveType == Move.TYPE_PASSANT) {
			squaresForDiscovery |= (turn ? to >>> 8 : to << 8);
			allAfterMove &= ~squaresForDiscovery;

		} else if (moveType == Move.TYPE_KINGSIDE_CASTLING) {
			long rookMoveMask = (turn ? 0x05L : 0x0500000000000000L);
			squaresForDiscovery |= rookMoveMask;
			allAfterMove ^= rookMoveMask;
			minesAfterMove ^= rookMoveMask;
			rookSlidersAftermove ^= rookMoveMask;

		} else if (moveType == Move.TYPE_QUEENSIDE_CASTLING) {
			long rookMoveMask = (turn ? 0x90L : 0x9000000000000000L);
			squaresForDiscovery |= rookMoveMask;
			allAfterMove ^= rookMoveMask;
			minesAfterMove ^= rookMoveMask;
			rookSlidersAftermove ^= rookMoveMask;
		}

		int newMyKingIndex = attacksInfo.myKingIndex;
		if (pieceMoved == Move.KING) {
			newMyKingIndex = Move.getToIndex(move);
		}

		// Candidates to left the king in check after moving
		if (((squaresForDiscovery & attacksInfo.bishopAttacksMyking) != 0) ||
				((attacksInfo.piecesGivingCheck & (board.bishops | board.queens)) != 0 && pieceMoved == Move.KING)) { // Moving the king when the king is in check by a slider
			// Regenerate bishop attacks to my king
			long newBishopAttacks = bbAttacks.getBishopAttacks(newMyKingIndex, allAfterMove);
			if ((newBishopAttacks & bishopSlidersAftermove & ~minesAfterMove) != 0) {
				return Move.NONE;
			}
		}
		if (((squaresForDiscovery & attacksInfo.rookAttacksMyking) != 0) ||
				((attacksInfo.piecesGivingCheck & (board.rooks | board.queens)) != 0 && pieceMoved == Move.KING)) {
			// Regenerate rook attacks to my king
			long newRookAttacks = bbAttacks.getRookAttacks(newMyKingIndex, allAfterMove);
			if ((newRookAttacks & rookSlidersAftermove & ~minesAfterMove) != 0) {
				return Move.NONE;
			}
		}

		// Discovered checks
		if (!check && ((squaresForDiscovery & attacksInfo.bishopAttacksOtherking) != 0)) {
			// Regenerate bishop attacks to the other king
			long newBishopAttacks = bbAttacks.getBishopAttacks(attacksInfo.otherKingIndex, allAfterMove);
			if ((newBishopAttacks & bishopSlidersAftermove & minesAfterMove) != 0) {
				check = true;
			}
		}
		if (!check && ((squaresForDiscovery & attacksInfo.rookAttacksOtherking) != 0)) {
			// Regenerate rook attacks to the other king
			long newRookAttacks = bbAttacks.getRookAttacks(attacksInfo.otherKingIndex, allAfterMove);
			if ((newRookAttacks & rookSlidersAftermove & minesAfterMove) != 0) {
				check = true;
			}
		}

		return move | (check ? Move.CHECK_MASK : 0);
	}

	public int next() {
		int move;
		switch (phase) {
			case PHASE_TT:
				phase++;
				int ttMoveWithCheckFlag = setCheckAndLegality(ttMove);
				if (ttMoveWithCheckFlag != Move.NONE) {
					if (Move.isCapture(ttMove)) {
						lastMoveSee = board.see(ttMoveWithCheckFlag);
					}
					return ttMoveWithCheckFlag;
				}
			case PHASE_GEN_CAPTURES:
				phase++;
				if (checkEvasion) {
					generateCheckEvasionCaptures();
				} else {
					generateCaptures();
				}

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
				phase++;

				if (quiescence && !generateChecks && !checkEvasion) {
					phase = PHASE_END;
					return Move.NONE;
				}

				lastMoveSee = 0;
				if (checkEvasion) {
					generateCheckEvasionsNonCaptures();
				} else {
					generateNonCaptures();
				}

			case PHASE_KILLER1:
				phase++;
				if (foundKiller1) {
					killer1 = setCheckAndLegality(killer1);
					if (killer1 != Move.NONE) {
						return killer1;
					}
				}

			case PHASE_KILLER2:
				phase++;
				if (foundKiller2) {
					killer2 = setCheckAndLegality(killer2);
					if (killer2 != Move.NONE) {
						return killer2;
					}
				}

			case PHASE_NON_CAPTURES:
				move = pickMoveFromArray(nonCaptureIndex, nonCaptures, nonCapturesScores, null);
				if (move != Move.NONE) {
					return move;
				}
				phase++;

			case PHASE_BAD_CAPTURES:
				move = pickMoveFromArray(badCaptureIndex, badCaptures, badCapturesScores, badCapturesSee);
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
		while (true) {
			int maxScore = SCORE_LOWEST;
			int bestIndex = -1;
			for (int i = 0; i < arrayLength; i++) {
				if (arrayScores[i] > maxScore) {
					maxScore = arrayScores[i];
					bestIndex = i;
				}
			}
			if (bestIndex != -1) {
				arrayScores[bestIndex] = SCORE_LOWEST;
				int move = setCheckAndLegality(arrayMoves[bestIndex]);
				if (move != Move.NONE) {
					if (arraySee != null) {
						lastMoveSee = arraySee[bestIndex];
					}
					return move;
				}
			} else {
				return Move.NONE;
			}
		}
	}

	public int getLastMoveSee() {
		return lastMoveSee;
	}
}