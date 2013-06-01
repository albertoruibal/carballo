package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

import java.util.Arrays;

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
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("CompleteEvaluator");

	public final static int PAWN = 100;
	public final static int KNIGHT = 325;
	public final static int BISHOP = 325;
	public final static int BISHOP_PAIR = 50; // Bonus by having two bishops
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
	private final static int PAWN_ISOLATED = oe(-10, -20);
	private final static int PAWN_DOUBLED = oe(-10, -20); // Penalty for each pawn in a doubled rank
	//	private final static int PAWN_BACKWARD         = oe(-8,-10);
//	private final static int PAWN_BLOCKED          = oe(0,0); //-20; // Pawn blocked by opposite pawn
	private final static int PAWN_WEAK = oe(-10, -15); // Weak pawn

	private final static int[] PAWN_PASSER = {0, oe(5, 10), oe(10, 20), oe(20, 40), oe(30, 60), oe(50, 100), oe(75, 150), 0}; // Depends of the rank
	private final static int[] PAWN_PASSER_SUPPORT = {0, 0, oe(5, 10), oe(10, 20), oe(15, 30), oe(25, 50), oe(37, 75), 0}; // Depends of the rank
	private final static int[] PAWN_PASSER_KING_D = {0, 0, oe(1, 2), oe(2, 4), oe(3, 6), oe(5, 10), oe(7, 15), 0}; // Sums by each square away of the other opposite king

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
	private final static int[] pawnIndexValue = new int[64];
	private final static int[] knightIndexValue = new int[64];
	private final static int[] bishopIndexValue = new int[64];
	private final static int[] rookIndexValue = new int[64];
	private final static int[] queenIndexValue = new int[64];
	private final static int[] kingIndexValue = new int[64];

	Config config;

	public boolean debug = false;

	public CompleteEvaluator(Config config) {
		this.config = config;
	}

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
			bishopIndexValue[((i << 3) | i) ^ 070] += BishopDiagonalValue;
		}

		// Pawn opening corrections
		pawnIndexValue[19] += oe(10, 0); // E3
		pawnIndexValue[20] += oe(10, 0); // D3
		pawnIndexValue[27] += oe(25, 0); // E4
		pawnIndexValue[28] += oe(25, 0); // D4
		pawnIndexValue[35] += oe(10, 0); // E5
		pawnIndexValue[36] += oe(10, 0); // D5

//		logger.debug("***PAWN");
//		printPcsq(pawnIndexValue);
//		logger.debug("***KNIGHT");
//		printPcsq(knightIndexValue);
//		logger.debug("***BISHOP");
//		printPcsq(bishopIndexValue);
//		logger.debug("***ROOK");
//		printPcsq(rookIndexValue);
//		logger.debug("***QUEEN");
//		printPcsq(queenIndexValue);
//		logger.debug("***KING");
//		printPcsq(kingIndexValue);
//		logger.debug("PCSQ tables generated");
	}

//	private static void printPcsq(int pcsq[][]) {
//		StringBuffer sb = new StringBuffer();
//		for (int k=0; k<2; k++) {
//			if (k==0) sb.append("Opening:\n");
//			else sb.append("Endgame:\n");
//			for (int i = 0; i<64; i++) {
//				String aux = "     " + pcsq[i][k];
//				aux = aux.substring(aux.length()-5);
//				sb.append(aux);
//				if (i%8 != 7) {
//					sb.append(",");
//				} else {
//					sb.append("\n");
//				}
//			}
//		}
//		logger.debug(sb.toString());
//	}


	private int[] bishopCount = {0, 0};
	private long[] superiorPieceAttacked = {0, 0};

	private int[] material = {0, 0};
	private int[] pawnMaterial = {0, 0};
	private int[] mobility = {0, 0};
	private int[] attacks = {0, 0};
	private int[] center = {0, 0};
	private int[] positional = {0, 0};
	private int[] kingAttackersCount = {0, 0};
	private int[] kingSafety = {0, 0};
	//	int kingDefense[] = {0,0};
	private int[] pawnStructure = {0, 0};
	private int[] passedPawns = {0, 0};

	// Squares attackeds by pawns
	private long[] pawnAttacks = {0, 0};

	// Squares surrounding King
	private long[] squaresNearKing = {0, 0};

	private int[] knightKaufBonus = {0, 0};
	private int[] rookKaufBonus = {0, 0};

	private long all, pieceAttacks, pieceAttacksXray, mines, others, square;
	private int auxInt, pcsqIndex, color, index;
	private boolean isWhite;

	public int evaluateBoard(Board board, int alpha, int beta) {
		all = board.getAll();

		Arrays.fill(bishopCount, 0);
		Arrays.fill(superiorPieceAttacked, 0);
		Arrays.fill(material, 0);
		Arrays.fill(pawnMaterial, 0);
		Arrays.fill(mobility, 0);
		Arrays.fill(attacks, 0);
		Arrays.fill(center, 0);
		Arrays.fill(positional, 0);
		Arrays.fill(kingAttackersCount, 0);
		Arrays.fill(kingSafety, 0);
		Arrays.fill(pawnStructure, 0);
		Arrays.fill(passedPawns, 0);

		// Squares attackeds by pawns
		pawnAttacks[0] = ((board.pawns & board.whites & ~BitboardUtils.b_l) << 9) | ((board.pawns & board.whites & ~BitboardUtils.b_r) << 7);
		pawnAttacks[1] = ((board.pawns & board.blacks & ~BitboardUtils.b_r) >>> 9) | ((board.pawns & board.blacks & ~BitboardUtils.b_l) >>> 7);

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

		square = 1;
		index = 0;
		while (square != 0) {
			isWhite = ((board.whites & square) != 0);
			color = (isWhite ? 0 : 1);
			mines = (isWhite ? board.whites : board.blacks);
			others = (isWhite ? board.blacks : board.whites);
			pcsqIndex = (isWhite ? index : 63 - index);

			if ((square & all) != 0) {
				int rank = index >> 3;
				int column = 7 - index & 7;

				if ((square & board.pawns) != 0) {
					pawnMaterial[color] += PAWN;
					center[color] += pawnIndexValue[pcsqIndex];

					pieceAttacks = (isWhite ? bbAttacks.pawnUpwards[index] : bbAttacks.pawnDownwards[index]);

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.knights | board.bishops | board.rooks | board.queens);

					if ((pieceAttacks & squaresNearKing[1 - color]) != 0) {
						kingSafety[color] += PAWN_ATTACKS_KING;
					}

					// Doubled pawn detection
					if ((BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color][rank] & board.pawns & mines) != square)
						pawnStructure[color] += PAWN_DOUBLED;

					// Blocked Pawn
//					boolean blocked = ((isWhite ? (square<< 8)  : (square >>> 8)) & others) != 0;
//					if (blocked) pawnStructure[color] += PAWN_BLOCKED;

					// Backwards Pawn
//					if (((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & ~BitboardUtils.RANKS_FORWARD[color][rank] & board.pawns & mines) == 0)
//						pawnStructure[color] += PAWN_BACKWARD;

					// Passed Pawn
					if (((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) &
							(isWhite ? BitboardUtils.RANKS_UPWARDS[rank] : BitboardUtils.RANKS_DOWNWARDS[rank])
							& board.pawns & others) == 0) {
						passedPawns[color] += PAWN_PASSER[(isWhite ? rank : 7 - rank)];
						if ((square & pawnAttacks[color]) != 0)
							passedPawns[color] += PAWN_PASSER_SUPPORT[(isWhite ? rank : 7 - rank)];
						passedPawns[color] += PAWN_PASSER_KING_D[(isWhite ? rank : 7 - rank)] * BitboardUtils.distance(index, BitboardUtils.square2Index(board.kings & others));
					}

					// Isolated pawn
					boolean isolated = (BitboardUtils.COLUMNS_ADJACENTS[column] & board.pawns & mines) == 0;
					if (isolated) pawnStructure[color] += PAWN_ISOLATED;

					long auxLong, auxLong2;
					boolean weak = !isolated && (pawnAttacks[color] & square) == 0
//						&& pcsqIndex >= 24
							; // not defended is weak and only if over rank 2
					if (weak) {
						// Can be defended advancing one square
						auxLong = (isWhite ? bbAttacks.pawnDownwards[color] : bbAttacks.pawnUpwards[color]) & ~pawnAttacks[1 - color] & ~all;
						while (auxLong != 0) { // Not attacked by other pawn and empty
							auxLong2 = BitboardUtils.lsb(auxLong);
							auxLong &= ~auxLong2;
							auxLong2 = isWhite ? auxLong2 >>> 8 : auxLong2 << 8;
							if ((auxLong2 & mines & board.pawns) != 0) {
								weak = false;
							} else { // Defended advancing one pawn two squares
								if ((auxLong2 & all) == 0) { // empty square								
									auxLong2 = (isWhite ? auxLong2 >>> 8 : auxLong2 << 8);
									if (((isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[6]) & auxLong2 & board.pawns & mines) != 0) {
										weak = false;
									}
								}
							}
						}

						if (weak) {
							// Can advance to be supported
							auxLong = (isWhite ? square << 8 : square >>> 8) & ~pawnAttacks[1 - color] & ~all;
							if (auxLong != 0) {
								if ((auxLong & pawnAttacks[color]) != 0) {
									weak = false;
								} else {
									// Checks advancing two squares if in initial position
									if (((isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[6]) & square) != 0) {
										auxLong = (isWhite ? square << 16 : square >>> 16) & ~pawnAttacks[1 - color] & ~all;
										if ((auxLong & pawnAttacks[color]) != 0) weak = false;
									}
								}
							}
						}
					}
					if (weak) pawnStructure[color] += PAWN_WEAK;
//					if (weak) {
//						System.out.println("weak pawn: \n" + board.toString());
//						System.out.println("square: \n" + BitboardUtils.toString(square));
//					}


				} else if ((square & board.knights) != 0) {
					material[color] += KNIGHT + knightKaufBonus[color];
					center[color] += knightIndexValue[pcsqIndex];

					pieceAttacks = bbAttacks.knight[index];
					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) - KNIGHT_M_UNITS;
					mobility[color] += KNIGHT_M * auxInt;

					if ((pieceAttacks & squaresNearKing[color]) != 0) {
						kingSafety[color] += KNIGHT_ATTACKS_KING;
						kingAttackersCount[color]++;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens);

					// Knight outpost: no opposite pawns can attack the square and is defended by one of our pawns
					if (((BitboardUtils.COLUMNS_ADJACENTS[column] &
							(isWhite ? BitboardUtils.RANKS_UPWARDS[rank] : BitboardUtils.RANKS_DOWNWARDS[rank])
							& board.pawns & others) == 0) &&
							(((isWhite ? bbAttacks.pawnDownwards[index] : bbAttacks.pawnUpwards[index]) & board.pawns & mines) != 0))
						positional[color] += KNIGTH_OUTPOST[pcsqIndex];

				} else if ((square & board.bishops) != 0) {
					material[color] += BISHOP;
					if (bishopCount[color]++ == 2) material[color] += BISHOP_PAIR;

					center[color] += bishopIndexValue[pcsqIndex];

					pieceAttacks = bbAttacks.getBishopAttacks(index, all);
					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) - BISHOP_M_UNITS;
					mobility[color] += BISHOP_M * auxInt;

					if ((pieceAttacks & squaresNearKing[1 - color]) != 0) {
						kingSafety[color] += BISHOP_ATTACKS_KING;
						kingAttackersCount[color]++;
					}

					pieceAttacksXray = bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0)
						attacks[color] += PINNED_PIECE;

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens);

					if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0)
						mobility[color] += BISHOP_TRAPPED;

				} else if ((square & board.rooks) != 0) {
					material[color] += ROOK + rookKaufBonus[color];
					center[color] += rookIndexValue[pcsqIndex];

					pieceAttacks = bbAttacks.getRookAttacks(index, all);
					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) - ROOK_M_UNITS;
					mobility[color] += ROOK_M * auxInt;

					pieceAttacksXray = bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0)
						attacks[color] += PINNED_PIECE;

					if ((pieceAttacks & squaresNearKing[1 - color]) != 0) {
						kingSafety[color] += ROOK_ATTACKS_KING;
						kingAttackersCount[color]++;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & board.queens;

					if ((pieceAttacks & mines & (board.rooks)) != 0) {
						positional[color] += ROOK_CONNECT;
					}
					pieceAttacks = BitboardUtils.COLUMN[column];
					if ((pieceAttacks & board.pawns) == 0)
						positional[color] += ROOK_COLUMN_OPEN;
					else if ((pieceAttacks & board.pawns & mines) == 0)
						positional[color] += ROOK_COLUMN_SEMIOPEN;

				} else if ((square & board.queens) != 0) {
					center[color] += queenIndexValue[pcsqIndex];
					material[color] += QUEEN;

					pieceAttacks = bbAttacks.getRookAttacks(index, all) | bbAttacks.getBishopAttacks(index, all);
					auxInt = BitboardUtils.popCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) - QUEEN_M_UNITS;
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
					pieceAttacks = bbAttacks.king[index];
					center[color] += kingIndexValue[pcsqIndex];
					// TODO
					if ((square & (isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[7])) != 0) {
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
		if (gamePhase > 256) gamePhase = 256; // Security		

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
			logger.debug("\n" + board.toString());
			logger.debug(board.getFen());

			logger.debug("materialValue          = " + (material[0] - material[1]));
			logger.debug("pawnMaterialValue      = " + (pawnMaterial[0] - pawnMaterial[1]));

			logger.debug("centerOpening          = " + o(center[0] - center[1]));
			logger.debug("centerEndgame          = " + e(center[0] - center[1]));

			logger.debug("positionalOpening      = " + o(positional[0] - positional[1]));
			logger.debug("positionalEndgame      = " + e(positional[0] - positional[1]));

			logger.debug("attacksO 				 = " + o(attacks[0] - attacks[1]));
			logger.debug("attacksE 				 = " + e(attacks[0] - attacks[1]));

			logger.debug("mobilityO              = " + o(mobility[0] - mobility[1]));
			logger.debug("mobilityE              = " + e(mobility[0] - mobility[1]));

			logger.debug("pawnsO                 = " + o(pawnStructure[0] - pawnStructure[1]));
			logger.debug("pawnsE                 = " + e(pawnStructure[0] - pawnStructure[1]));

			logger.debug("passedPawnsO           = " + o(passedPawns[0] - passedPawns[1]));
			logger.debug("passedPawnsE           = " + e(passedPawns[0] - passedPawns[1]));

			logger.debug("kingSafetyValueO       = " + o(KING_SAFETY_PONDER[kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]));
			logger.debug("kingSafetyValueE       = " + e(KING_SAFETY_PONDER[kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]));

			logger.debug("HungPiecesO 		     = " + o((BitboardUtils.popCount(superiorPieceAttacked[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.popCount(superiorPieceAttacked[1]) >= 2 ? HUNG_PIECES : 0)));
			logger.debug("HungPiecesE 		     = " + o((BitboardUtils.popCount(superiorPieceAttacked[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.popCount(superiorPieceAttacked[1]) >= 2 ? HUNG_PIECES : 0)));

			logger.debug("gamePhase              = " + gamePhase);
			logger.debug("tempo                  = " + (board.getTurn() ? TEMPO : -TEMPO));
			logger.debug("value                  = " + value);
		}

		return value;
	}
}