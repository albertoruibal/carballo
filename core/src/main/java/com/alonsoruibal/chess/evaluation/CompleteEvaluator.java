package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

/**
 * Evaluation is done in centipawns
 * <p/>
 * Material imbalances from Larry KaufMan:
 * http://home.comcast.net/~danheisman/Articles/evaluation_of_material_imbalance.htm
 * <p/>
 * Piece/square values like Fruit/Toga
 * <p/>
 * TODO: pawn races
 * TODO: pawn storm
 * TODO: pinned pieces
 *
 * @author rui
 */
public class CompleteEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger("CompleteEvaluator");

	public final static int PAWN = 100;
	public final static int KNIGHT = 325;
	public final static int BISHOP = 325;
	public final static int BISHOP_PAIR = 50; // Bonus by having two bishops in different colors
	public final static int ROOK = 500;
	public final static int QUEEN = 975;

	// Bishops
	private final static int BISHOP_M_UNITS = 6;
	private final static int BISHOP_M = oe(5, 5); // Mobility units: this value is added for each destination square not occupied by one of our pieces
	private final static int BISHOP_TRAPPED = oe(-100, -100);

	// Bishops
	private final static int KNIGHT_M_UNITS = 4;
	private final static int KNIGHT_M = oe(4, 4);
	private final static int KNIGHT_KAUF_BONUS = 7;

	// Rooks
	private final static int ROOK_M_UNITS = 7;
	private final static int ROOK_M = oe(2, 4);
	private final static int ROOK_COLUMN_OPEN = oe(25, 20); // No pawns in rook column
	private final static int ROOK_COLUMN_SEMIOPEN = oe(15, 10); // Only opposite pawns in rook column
	private final static int ROOK_CONNECT = oe(20, 10); // Rook connects with other rook TODO???
	private final static int ROOK_KAUF_BONUS = -12;

	// Queen
	private final static int QUEEN_M_UNITS = 13;
	private final static int QUEEN_M = oe(2, 4);

	private final static int KING_PAWN_NEAR = oe(5, 0); // Protection: sums for each pawn near king (opening)
	// King Safety: not in endgame!!!
	private final static int PAWN_ATTACKS_KING = oe(1, 0);
	private final static int KNIGHT_ATTACKS_KING = oe(4, 0);
	private final static int BISHOP_ATTACKS_KING = oe(2, 0);
	private final static int ROOK_ATTACKS_KING = oe(3, 0);
	private final static int QUEEN_ATTACKS_KING = oe(5, 0);

	// Pawns
	private final static int PAWN_UNSUPPORTED = oe(-2, 4);

	// Array is not opposed, opposed
	private final static int PAWN_BACKWARDS = oe(-10, -15);
	private final static int[] PAWN_ISOLATED = {oe(-15, -20), oe(-12, -16)};
	private final static int[] PAWN_DOUBLED = {oe(-2, -4), oe(-4, -8)};

	// Array by relative rank
	private final static int[] PAWN_CANDIDATE = {0, 0, 0, oe(5, 5), oe(10, 12), oe(20, 25), 0, 0}; // Candidates to pawn passer
	private final static int[] PAWN_PASSER = {0, 0, 0, oe(10, 10), oe(20, 25), oe(40, 50), oe(60, 75), 0};
	private final static int[] PAWN_PASSER_OUTSIDE = {0, 0, 0, 0, oe(2, 5), oe(5, 10), oe(10, 20), 0}; // no opposite pawns at left or at right
	private final static int[] PAWN_PASSER_CONNECTED = {0, 0, 0, 0, oe(5, 10), oe(10, 15), oe(20, 30), 0};
	private final static int[] PAWN_PASSER_SUPPORTED = {0, 0, 0, 0, oe(5, 10), oe(10, 15), oe(15, 25), 0}; // defended by pawn
	private final static int[] PAWN_PASSER_MOBILE = {0, 0, 0, oe(1, 2), oe(2, 3), oe(3, 5), oe(5, 10), 0};
	private final static int[] PAWN_PASSER_RUNNER = {0, 0, 0, 0, oe(5, 10), oe(10, 20), oe(20, 40), 0};

	// Ponder kings attacks by the number of attackers (not pawns) later divided by 8
	private final static int[] KING_SAFETY_PONDER = {0, 1, 4, 8, 16, 25, 36, 49, 50, 50, 50, 50, 50, 50, 50, 50};

	private final static int HUNG_PIECES = oe(16, 25); // two or more pieces of the other side attacked by inferior pieces
	private final static int PINNED_PIECE = oe(25, 35);

	// Tempo
	public final static int TEMPO = 10; // Add to moving side score

	private final static int[] KNIGTH_OUTPOST = {
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, oe(7, 7), oe(9, 9), oe(9, 9), oe(7, 7), 0, 0,
			0, oe(5, 5), oe(10, 10), oe(20, 20), oe(20, 20), oe(10, 10), oe(5, 5), 0,
			0, oe(5, 5), oe(10, 10), oe(20, 20), oe(20, 20), oe(10, 10), oe(5, 5), 0,
			0, 0, oe(7, 7), oe(9, 9), oe(9, 9), oe(7, 7), 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	private final static long[] BISHOP_TRAPPING = {
			0x00, 1L << 10, 0x00, 0x00, 0x00, 0x00, 1L << 13, 0x00,
			1L << 17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 22,
			1L << 25, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 30,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			1L << 33, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 38,
			1L << 41, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 46,
			0x00, 1L << 50, 0x00, 0x00, 0x00, 0x00, 1L << 53, 0x00
	};

	// The pair of values are {opening, endgame}
	private final static int PawnColumnValue = oe(5, 0);
	private final static int KnightCenterValue = oe(5, 5);
	private final static int KnightRankValue = oe(5, 0);
	private final static int KnightBackRankValue = oe(0, 0);
	private final static int KnightTrappedValue = oe(-100, 0);
	private final static int BishopCenterValue = oe(2, 3);
	private final static int BishopBackRankValue = oe(-10, 0);
	private final static int BishopDiagonalValue = oe(4, 0);
	private final static int RookColumnValue = oe(3, 0);
	private final static int QueenCenterValue = oe(0, 4);
	private final static int QueenBackRankValue = oe(-5, 0);
	private final static int KingCenterValue = oe(0, 12);
	private final static int KingColumnValue = oe(10, 0);
	private final static int KingRankValue = oe(10, 0);

	private final static int[] PawnColumn = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] KnightLine = {-4, -2, +0, +1, +1, +0, -2, -4};
	private final static int[] KnightRank = {-2, -1, +0, +1, +2, +3, +2, +1};
	private final static int[] BishopLine = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] RookColumn = {-2, -1, +0, +1, +1, +0, -1, -2};
	private final static int[] QueenLine = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] KingLine = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] KingColumn = {+3, +4, +2, +0, +0, +2, +4, +3};
	private final static int[] KingRank = {+1, +0, -2, -3, -4, -5, -6, -7};

	// Values are rotated for whites, so when white is playing is like shown in the code
	public final static int[] pawnIndexValue = new int[64];
	public final static int[] knightIndexValue = new int[64];
	public final static int[] bishopIndexValue = new int[64];
	public final static int[] rookIndexValue = new int[64];
	public final static int[] queenIndexValue = new int[64];
	public final static int[] kingIndexValue = new int[64];

	static {
		// Initialize Piece square values Fruit/Toga style 
		int i;

		for (i = 0; i < 64; i++) {
			int rank = i >> 3;
			int column = 7 - i & 7;

			pawnIndexValue[i] = PawnColumn[column] * PawnColumnValue;
			knightIndexValue[i] = KnightLine[column] * KnightCenterValue + KnightLine[rank] * KnightCenterValue + KnightRank[rank] * KnightRankValue;
			bishopIndexValue[i] = BishopLine[column] * BishopCenterValue + BishopLine[rank] * BishopCenterValue;
			rookIndexValue[i] = RookColumn[column] * RookColumnValue;
			queenIndexValue[i] = QueenLine[column] * QueenCenterValue + QueenLine[rank] * QueenCenterValue;
			kingIndexValue[i] = KingColumn[column] * KingColumnValue + KingRank[rank] * KingRankValue + KingLine[column] * KingCenterValue + KingLine[rank] * KingCenterValue;
		}

		knightIndexValue[56] += KnightTrappedValue; // H8 
		knightIndexValue[63] += KnightTrappedValue; // A8

		for (i = 0; i < 8; i++) {
			queenIndexValue[i] += QueenBackRankValue;
			knightIndexValue[i] += KnightBackRankValue;
			bishopIndexValue[i] += BishopBackRankValue;
			bishopIndexValue[(i << 3) | i] += BishopDiagonalValue;
			bishopIndexValue[((i << 3) | i) ^ 0x38] += BishopDiagonalValue;
		}

		// Pawn opening corrections
		pawnIndexValue[19] += oe(10, 0); // E3
		pawnIndexValue[20] += oe(10, 0); // D3
		pawnIndexValue[27] += oe(25, 0); // E4
		pawnIndexValue[28] += oe(25, 0); // D4
		pawnIndexValue[35] += oe(10, 0); // E5
		pawnIndexValue[36] += oe(10, 0); // D5
	}

	private Config config;

	public boolean debug = false;
	public StringBuffer debugSB;

	private int[] bishopCount = {0, 0};

	private int[] pawnMaterial = {0, 0};
	private int[] material = {0, 0};
	private int[] center = {0, 0};
	private int[] positional = {0, 0};
	private int[] mobility = {0, 0};
	private int[] attacks = {0, 0};
	private int[] kingAttackersCount = {0, 0};
	private int[] kingSafety = {0, 0};
	//	int kingDefense[] = {0,0};
	private int[] pawnStructure = {0, 0};
	private int[] passedPawns = {0, 0};

	private long[] attacksFromSquare = new long[64];
	private long[] superiorPieceAttacked = {0, 0};
	private long[] attackedSquares = {0, 0};

	// Squares attackeds by pawns
	private long[] pawnAttacks = {0, 0};

	// Squares surrounding King
	private long[] squaresNearKing = {0, 0};

	private int[] knightKaufBonus = {0, 0};
	private int[] rookKaufBonus = {0, 0};

	public CompleteEvaluator(Config config) {
		this.config = config;
	}

	public int evaluate(Board board) {
		if (debug) {
			debugSB = new StringBuffer();
			debugSB.append("\n");
			debugSB.append(board.toString());
			debugSB.append("\n");
		}

		long all = board.getAll();
		long pieceAttacks, pieceAttacksXray;
		int auxInt;

		bishopCount[0] = 0;
		bishopCount[1] = 0;

		pawnMaterial[0] = 0;
		pawnMaterial[1] = 0;
		material[0] = 0;
		material[1] = 0;

		center[0] = 0;
		center[1] = 0;
		positional[0] = 0;
		positional[1] = 0;
		mobility[0] = 0;
		mobility[1] = 0;
		kingAttackersCount[0] = 0;
		kingAttackersCount[1] = 0;
		kingSafety[0] = 0;
		kingSafety[1] = 0;
		pawnStructure[0] = 0;
		pawnStructure[1] = 0;
		passedPawns[0] = 0;
		passedPawns[1] = 0;

		superiorPieceAttacked[0] = 0;
		superiorPieceAttacked[1] = 0;
		attackedSquares[0] = 0;
		attackedSquares[1] = 0;

		// Squares attacked by pawns
		pawnAttacks[0] = ((board.pawns & board.whites & ~BitboardUtils.b_l) << 9) | ((board.pawns & board.whites & ~BitboardUtils.b_r) << 7);
		pawnAttacks[1] = ((board.pawns & board.blacks & ~BitboardUtils.b_r) >>> 9) | ((board.pawns & board.blacks & ~BitboardUtils.b_l) >>> 7);

		attacks[0] = 0;
		attacks[1] = 0;

		// Squares surrounding King
		squaresNearKing[0] = bbAttacks.king[BitboardUtils.square2Index(board.whites & board.kings)];
		squaresNearKing[1] = bbAttacks.king[BitboardUtils.square2Index(board.blacks & board.kings)];

		// From material imbalances (Larry Kaufmann):
		// A further refinement would be to raise the knight's value by 1/16 and lower the rook's value by 1/8
		// for each pawn above five of the side being valued, with the opposite adjustment for each pawn short of five
		int whitePawnsCount = BitboardUtils.popCount(board.pawns & board.whites);
		int blackPawnsCount = BitboardUtils.popCount(board.pawns & board.blacks);
		knightKaufBonus[0] = KNIGHT_KAUF_BONUS * (whitePawnsCount - 5);
		knightKaufBonus[1] = KNIGHT_KAUF_BONUS * (blackPawnsCount - 5);
		rookKaufBonus[0] = ROOK_KAUF_BONUS * (whitePawnsCount - 5);
		rookKaufBonus[1] = ROOK_KAUF_BONUS * (blackPawnsCount - 5);

		// first build attacks info
		int index;
		long square = 1;
		for (index = 0; index < 64; index++) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);
				int color = (isWhite ? 0 : 1);

				if ((square & board.pawns) != 0) {
					pieceAttacks = (isWhite ? bbAttacks.pawnUpwards[index] : bbAttacks.pawnDownwards[index]);
				} else if ((square & board.knights) != 0) {
					pieceAttacks = bbAttacks.knight[index];
				} else if ((square & board.bishops) != 0) {
					pieceAttacks = bbAttacks.getBishopAttacks(index, all);
				} else if ((square & board.rooks) != 0) {
					pieceAttacks = bbAttacks.getRookAttacks(index, all);
				} else if ((square & board.queens) != 0) {
					pieceAttacks = bbAttacks.getRookAttacks(index, all) | bbAttacks.getBishopAttacks(index, all);
				} else if ((square & board.kings) != 0) {
					pieceAttacks = bbAttacks.king[index];
				} else {
					pieceAttacks = 0;
				}
				attackedSquares[color] |= pieceAttacks;
				attacksFromSquare[index] = pieceAttacks;
			}
			square <<= 1;
		}

		square = 1;
		index = 0;
		while (square != 0) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);
				int color = (isWhite ? 0 : 1);
				long mines = (isWhite ? board.whites : board.blacks);
				long others = (isWhite ? board.blacks : board.whites);
				long otherPawnAttacks = (isWhite ? pawnAttacks[1] : pawnAttacks[0]);
				int pcsqIndex = (isWhite ? index : 63 - index);
				int rank = index >> 3;
				int column = 7 - index & 7;

				pieceAttacks = attacksFromSquare[index];

				if ((square & board.pawns) != 0) {
					pawnMaterial[color] += PAWN;
					center[color] += pawnIndexValue[pcsqIndex];

					if ((pieceAttacks & squaresNearKing[1 - color]) != 0) {
						kingSafety[color] += PAWN_ATTACKS_KING;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.knights | board.bishops | board.rooks | board.queens);

					long myPawns = board.pawns & mines;
					long otherPawns = board.pawns & others;
					long adjacentColumns = BitboardUtils.COLUMNS_ADJACENTS[column];
					long ranksForward = BitboardUtils.RANKS_FORWARD[color][rank];
					long routeToPromotion = BitboardUtils.COLUMN[column] & ranksForward;
					long myPawnsBesideAndBehindAdjacent = BitboardUtils.RANK_AND_BACKWARD[color][rank] & adjacentColumns & myPawns;
					long myPawnsAheadAdjacent = ranksForward & adjacentColumns & myPawns;
					long otherPawnsAheadAdjacent = ranksForward & adjacentColumns & otherPawns;
					long myPawnAttacks = (isWhite ? pawnAttacks[0] : pawnAttacks[1]);

					boolean isolated = (myPawns & adjacentColumns) == 0;
					boolean supported = (square & myPawnAttacks) != 0;
					boolean doubled = (myPawns & routeToPromotion) != 0;
					boolean opposed = (otherPawns & routeToPromotion) != 0;
					boolean passed = !doubled && !opposed && (otherPawnsAheadAdjacent == 0);
					boolean candidate = !doubled && !opposed && !passed &&
							(((otherPawnsAheadAdjacent & ~pieceAttacks) == 0) || // Can become passer advancing
									(BitboardUtils.popCount(myPawnsBesideAndBehindAdjacent) >= BitboardUtils.popCount(otherPawnsAheadAdjacent))); // Has more friend pawns beside and behind than opposed pawns controlling his route to promotion
					boolean backwards = !isolated && !passed && !candidate &&
							(myPawnsBesideAndBehindAdjacent == 0) &&
							((pieceAttacks & otherPawns) == 0) && // No backwards if it can capture
							((BitboardUtils.RANK_AND_BACKWARD[color][isWhite ? BitboardUtils.getRankLsb(myPawnsAheadAdjacent) : BitboardUtils.getRankMsb(myPawnsAheadAdjacent)] &
									routeToPromotion & (board.pawns | otherPawnAttacks)) != 0); // Other pawns stopping it from advance, opposing or capturing it before reaching my pawns

					if (debug) {
						boolean connected = ((bbAttacks.king[index] & adjacentColumns & myPawns) != 0);
						debugSB.append("PAWN " + //
										index + //
										(color == 0 ? " WHITE " : " BLACK ") + //
										BitboardUtils.popCount(myPawnsBesideAndBehindAdjacent) + " " + BitboardUtils.popCount(otherPawnsAheadAdjacent) + " " + //
										(isolated ? "isolated " : "") + //
										(supported ? "supported " : "") + //
										(connected ? "connected " : "") + //
										(doubled ? "doubled " : "") + //
										(opposed ? "opposed " : "") + //
										(passed ? "passed " : "") + //
										(candidate ? "candidate " : "") + //
										(backwards ? "backwards " : "") + //
										"\n"
						);
					}

					if (!supported && !isolated) {
						pawnStructure[color] += PAWN_UNSUPPORTED;
					}
					if (doubled) {
						pawnStructure[color] += PAWN_DOUBLED[opposed ? 1 : 0];
					}
					if (isolated) {
						pawnStructure[color] += PAWN_ISOLATED[opposed ? 1 : 0];
					}
					if (backwards) {
						pawnStructure[color] += PAWN_BACKWARDS;
					}
					if (candidate) {
						passedPawns[color] += PAWN_CANDIDATE[(isWhite ? rank : 7 - rank)];
					}
					if (passed) {
						int relativeRank = (isWhite ? rank : 7 - rank);
						long backColumn = BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_BACKWARD[color][rank];
						// If has has root/queen behind consider all the route to promotion attacked or defended
						long attackedAndNotDefendedRoute = //
								((routeToPromotion & attackedSquares[1 - color]) | ((backColumn & (board.rooks | board.queens) & others) != 0 ? routeToPromotion : 0)) &
										~((routeToPromotion & attackedSquares[color]) | ((backColumn & (board.rooks | board.queens) & mines) != 0 ? routeToPromotion : 0));
						long pushSquare = isWhite ? square << 8 : square >>> 8;
						long pawnsLeft = BitboardUtils.ROWS_LEFT[column] & board.pawns;
						long pawnsRight = BitboardUtils.ROWS_RIGHT[column] & board.pawns;

						boolean connected = ((bbAttacks.king[index] & adjacentColumns & myPawns) != 0);
						boolean outside = ((pawnsLeft != 0) && (pawnsRight == 0)) || ((pawnsLeft == 0) && (pawnsRight != 0));
						boolean mobile = ((pushSquare & (all | attackedAndNotDefendedRoute)) == 0);
						boolean runner = mobile && ((routeToPromotion & all) == 0) && (attackedAndNotDefendedRoute == 0);

						if (debug) {
							debugSB.append("        PASSER " + //
											(outside ? "outside " : "") + //
											(mobile ? "mobile " : "") + //
											(runner ? "runner " : "") + //
											"\n"
							);
						}

						passedPawns[color] += PAWN_PASSER[relativeRank];

						if (supported) {
							passedPawns[color] += PAWN_PASSER_SUPPORTED[relativeRank];
						}
						if (connected) {
							passedPawns[color] += PAWN_PASSER_CONNECTED[relativeRank];
						}
						if (outside) {
							passedPawns[color] += PAWN_PASSER_OUTSIDE[relativeRank];
						}
						if (mobile) {
							passedPawns[color] += PAWN_PASSER_MOBILE[relativeRank];
						}
						if (runner) {
							passedPawns[color] += PAWN_PASSER_RUNNER[relativeRank];
						}
					}

				} else if ((square & board.knights) != 0) {
					material[color] += KNIGHT + knightKaufBonus[color];
					center[color] += knightIndexValue[pcsqIndex];

					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks) - KNIGHT_M_UNITS;
					mobility[color] += KNIGHT_M * auxInt;

					if ((pieceAttacks & squaresNearKing[color]) != 0) {
						kingSafety[color] += KNIGHT_ATTACKS_KING;
						kingAttackersCount[color]++;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens);

					// Knight outpost: no opposite pawns can attack the square and is defended by one of our pawns
					if (((BitboardUtils.COLUMNS_ADJACENTS[column] &
							BitboardUtils.RANKS_FORWARD[color][rank] &
							board.pawns & others) == 0) &&
							(((isWhite ? bbAttacks.pawnDownwards[index] : bbAttacks.pawnUpwards[index]) & board.pawns & mines) != 0)) {
						positional[color] += KNIGTH_OUTPOST[pcsqIndex];
					}

				} else if ((square & board.bishops) != 0) {
					material[color] += BISHOP;
					if (++bishopCount[color] == 2) {
						material[color] += BISHOP_PAIR;
					}

					center[color] += bishopIndexValue[pcsqIndex];

					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks) - BISHOP_M_UNITS;
					mobility[color] += BISHOP_M * auxInt;

					if ((pieceAttacks & squaresNearKing[1 - color]) != 0) {
						kingSafety[color] += BISHOP_ATTACKS_KING;
						kingAttackersCount[color]++;
					}

					pieceAttacksXray = bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0) {
						attacks[color] += PINNED_PIECE;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens);

					if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0) {
						mobility[color] += BISHOP_TRAPPED;
					}

				} else if ((square & board.rooks) != 0) {
					material[color] += ROOK + rookKaufBonus[color];
					center[color] += rookIndexValue[pcsqIndex];

					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks) - ROOK_M_UNITS;
					mobility[color] += ROOK_M * auxInt;

					pieceAttacksXray = bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0) {
						attacks[color] += PINNED_PIECE;
					}

					if ((pieceAttacks & squaresNearKing[1 - color]) != 0) {
						kingSafety[color] += ROOK_ATTACKS_KING;
						kingAttackersCount[color]++;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & board.queens;

					if ((pieceAttacks & mines & (board.rooks)) != 0) {
						positional[color] += ROOK_CONNECT;
					}
					long columnAttacks = BitboardUtils.COLUMN[column];
					if ((columnAttacks & board.pawns) == 0) {
						positional[color] += ROOK_COLUMN_OPEN;
					} else if ((columnAttacks & board.pawns & mines) == 0) {
						positional[color] += ROOK_COLUMN_SEMIOPEN;
					}

				} else if ((square & board.queens) != 0) {
					center[color] += queenIndexValue[pcsqIndex];
					material[color] += QUEEN;

					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks) - QUEEN_M_UNITS;
					mobility[color] += QUEEN_M * auxInt;

					if ((pieceAttacks & squaresNearKing[1 - color]) != 0) {
						kingSafety[color] += QUEEN_ATTACKS_KING;
						kingAttackersCount[color]++;
					}

					pieceAttacksXray = (bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others)) | bbAttacks.getBishopAttacks(index, all
							& ~(pieceAttacks & others)))
							& ~pieceAttacks;
					if ((pieceAttacksXray & board.kings & others) != 0) {
						attacks[color] += PINNED_PIECE;
					}

				} else if ((square & board.kings) != 0) {
					center[color] += kingIndexValue[pcsqIndex];
					// TODO
					if ((square & (isWhite ? BitboardUtils.RANK[0] : BitboardUtils.RANK[7])) != 0) {
						positional[color] += KING_PAWN_NEAR * BitboardUtils.popCount(pieceAttacks & mines & board.pawns);
					}
				}
			}
			square <<= 1;
			index++;
		}

		// Ponder opening and Endgame value depending of the non-pawn pieces:
		// opening=> gamephase = 255 / ending => gamephase ~= 0
		int gamePhase = ((material[0] + material[1]) << 8) / 5000;
		if (gamePhase > 256) {
			gamePhase = 256; // Security
		}

		int value = 0;
		// First Material
		value += pawnMaterial[0] - pawnMaterial[1] + material[0] - material[1];
		// Tempo
		value += (board.getTurn() ? TEMPO : -TEMPO);

		int oe = config.getEvalCenter() * (center[0] - center[1])
				+ config.getEvalPositional() * (positional[0] - positional[1])
				+ config.getEvalAttacks() * (attacks[0] - attacks[1])
				+ config.getEvalMobility() * (mobility[0] - mobility[1])
				+ config.getEvalPawnStructure() * (pawnStructure[0] - pawnStructure[1])
				+ config.getEvalPassedPawns() * (passedPawns[0] - passedPawns[1])
				+ (config.getEvalKingSafety() / 8) * ((KING_SAFETY_PONDER[kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1])) // Divide by eight
				+ config.getEvalAttacks() * ((BitboardUtils.popCount(superiorPieceAttacked[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.popCount(superiorPieceAttacked[1]) >= 2 ? HUNG_PIECES : 0));

		value += (gamePhase * o(oe)) / (256 * 100); // divide by 256
		value += ((256 - gamePhase) * e(oe)) / (256 * 100);

		if (debug) {
			logger.debug(debugSB);

			logger.debug("materialValue          = " + (material[0] - material[1]));
			logger.debug("pawnMaterialValue      = " + (pawnMaterial[0] - pawnMaterial[1]));

			logger.debug("centerOpening          = " + o(center[0] - center[1]));
			logger.debug("centerEndgame          = " + e(center[0] - center[1]));

			logger.debug("positionalOpening      = " + o(positional[0] - positional[1]));
			logger.debug("positionalEndgame      = " + e(positional[0] - positional[1]));

			logger.debug("attacksO               = " + o(attacks[0] - attacks[1]));
			logger.debug("attacksE               = " + e(attacks[0] - attacks[1]));

			logger.debug("mobilityO              = " + o(mobility[0] - mobility[1]));
			logger.debug("mobilityE              = " + e(mobility[0] - mobility[1]));

			logger.debug("pawnsO                 = " + o(pawnStructure[0] - pawnStructure[1]));
			logger.debug("pawnsE                 = " + e(pawnStructure[0] - pawnStructure[1]));

			logger.debug("passedPawnsO           = " + o(passedPawns[0] - passedPawns[1]));
			logger.debug("passedPawnsE           = " + e(passedPawns[0] - passedPawns[1]));

			logger.debug("kingSafetyValueO       = " + o(KING_SAFETY_PONDER[kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]));
			logger.debug("kingSafetyValueE       = " + e(KING_SAFETY_PONDER[kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]));

			logger.debug("HungPiecesO            = " + o((BitboardUtils.popCount(superiorPieceAttacked[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.popCount(superiorPieceAttacked[1]) >= 2 ? HUNG_PIECES : 0)));
			logger.debug("HungPiecesE            = " + o((BitboardUtils.popCount(superiorPieceAttacked[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.popCount(superiorPieceAttacked[1]) >= 2 ? HUNG_PIECES : 0)));

			logger.debug("gamePhase              = " + gamePhase);
			logger.debug("tempo                  = " + (board.getTurn() ? TEMPO : -TEMPO));
			logger.debug("value                  = " + value);
		}

		return value;
	}
}