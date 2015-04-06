package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.util.StringUtils;

/**
 * Evaluation is done in centipawns
 * <p>
 *
 * @author rui
 */
public class ExperimentalEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger("ExperimentalEvaluator");

	public final static int W = 0;
	public final static int B = 1;

	public final static int PAWN = 100;
	public final static int KNIGHT = 325;
	public final static int BISHOP = 325;
	public final static int BISHOP_PAIR = 50; // Bonus by having two bishops in different colors
	public final static int ROOK = 500;
	public final static int QUEEN = 975;

	public final static int[] PIECE_VALUES = {0, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, 9999};

	// Mobility units: this value is added for the number of destination square not occupied by one of our pieces or attacked by opposite pawns
	private final static int[][] MOBILITY = {
			{}, {},
			{oe(-40, -30), oe(-10, -8), oe(4, 3), oe(11, 8), oe(16, 12), oe(20, 15), oe(22, 17), oe(24, 18), oe(25, 19)},
			{oe(-30, -25), oe(-12, -10), oe(3, 3), oe(12, 10), oe(20, 17), oe(27, 23), oe(33, 28), oe(39, 33), oe(44, 37), oe(48, 40), oe(52, 43), oe(55, 46), oe(58, 48), oe(60, 50)},
			{oe(-13, -40), oe(-5, -15), oe(0, 0), oe(4, 12), oe(7, 21), oe(10, 29), oe(12, 37), oe(15, 44), oe(17, 50), oe(19, 56), oe(20, 61), oe(21, 64), oe(22, 67), oe(23, 69), oe(23, 70)},
			{oe(-15, -30), oe(-8, -16), oe(-3, -5), oe(0, 0), oe(3, 6), oe(6, 11), oe(8, 15), oe(9, 18), oe(11, 21), oe(12, 23), oe(12, 24), oe(13, 25), oe(13, 26), oe(14, 27), oe(14, 27), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28), oe(14, 28)}
	};

	// Knights
	private final static int KNIGHT_ATTACKS_KING = oe(4, 2);
	private final static int KNIGHT_DEFENDS_KING = oe(4, 2);
	private final static int KNIGHT_ATTACKS_PU_P = oe(3, 4);
	private final static int KNIGHT_ATTACKS_PU_B = oe(5, 5);
	private final static int KNIGHT_ATTACKS_RQ = oe(7, 10);
	private final static int KNIGHT_OUTPOST = oe(2, 3); // Adds one time if no opposite can can attack out knight and twice if it is defended by one of our pawns

	// Bishops
	private final static int BISHOP_ATTACKS_KING = oe(2, 1);
	private final static int BISHOP_DEFENDS_KING = oe(2, 1);
	private final static int BISHOP_ATTACKS_PU_P = oe(3, 4);
	private final static int BISHOP_ATTACKS_PU_K = oe(5, 5);
	private final static int BISHOP_ATTACKS_RQ = oe(7, 10);
	private final static int BISHOP_MY_PAWNS_NOT_IN_COLOR = oe(1, 1); // Bonus for each of my pawns not in the bishop color (Capablanca rule)
	private final static int BISHOP_OTHER_PAWNS_IN_COLOR = oe(-1, -1); // for each of the other pawns in my color
	private final static int BISHOP_OUTPOST = oe(1, 2); // Only if defended by pawn
	private final static int BISHOP_OUTPOST_ATT_NK_PU = oe(3, 4); // attacks squares Near King or other opposite pieces Pawn Undefended

	// Rooks
	private final static int ROOK_ATTACKS_KING = oe(3, 1);
	private final static int ROOK_DEFENDS_KING = oe(3, 1);
	private final static int ROOK_ATTACKS_PU_P = oe(2, 3); // Attacks pawn not defended by pawn (PU=Pawn Undefended)
	private final static int ROOK_ATTACKS_PU_BK = oe(4, 5); // Attacks bishop or knight not defended by pawn
	private final static int ROOK_ATTACKS_Q = oe(5, 5); // Attacks queen
	private final static int ROOK_FILE_OPEN_NO_MG = oe(20, 10); // No pawns in rook file and no minor guarded
	private final static int ROOK_FILE_OPEN_MG_P = oe(15, 5); // No pawns in rook file and minor guarded, my pawns can attack
	private final static int ROOK_FILE_OPEN_MG_NP = oe(10, 0); // No pawns in rook file and minor guarded, my pawns cannot attack
	private final static int ROOK_FILE_SEMIOPEN = oe(3, 6); // No pawns mines in file
	private final static int ROOK_FILE_SEMIOPEN_BP = oe(15, 5); // And attacks a backward pawn
	private final static int ROOK_FILE_SEMIOPEN_K = oe(3, 6); // No pawns mines in file and opposite king
	private final static int ROOK_8_KING_8 = oe(5, 10); // Rook in 8th rank and opposite king in 8th rank
	private final static int ROOK_7_KP_78 = oe(10, 30); // Rook in 7th rank and opposite king or pawn in 7/8th rank
	private final static int ROOK_6_KP_678 = oe(5, 15); // Rook in 6th rank and opposite king or pawns in 6/7/8th
	private final static int ROOK_OUTPOST = oe(1, 2); // Only if defended by pawn
	private final static int ROOK_OUTPOST_ATT_NK_PU = oe(3, 4); // Also attacks other piece not defended by pawn or a square near king

	// Queen
	private final static int QUEEN_ATTACKS_KING = oe(5, 2);
	private final static int QUEEN_DEFENDS_KING = oe(5, 2);
	private final static int QUEEN_ATTACKS_PU = oe(4, 4);
	private final static int QUEEN_7_KP_78 = oe(5, 25); // Queen in 8th rank and opposite king/pawn in 7/8th rank
	private final static int QUEEN_7_P_78_K_8_R_7 = oe(10, 15); // Queen in 7th my rook in 7th defending queen and opposite king in 8th

	// King
	private final static int KING_PAWN_SHIELD = oe(5, 0);  // Protection: sums for each pawn near king

	// Ponder kings attacks by the number of attackers (not pawns)
	private final static int[] KING_SAFETY_PONDER = {0, 1, 2, 4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};

	// Pawns
	private final static int PAWN_ATTACKS_KING = oe(1, 0); // Sums for each pawn attacking an square near the king or the king
	private final static int PAWN_ATTACKS_KNIGHT = oe(5, 7); // Sums for each pawn attacking a KNIGHT
	private final static int PAWN_ATTACKS_BISHOP = oe(5, 7);
	private final static int PAWN_ATTACKS_ROOK = oe(7, 10);
	private final static int PAWN_ATTACKS_QUEEN = oe(8, 12);

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

	private final static int HUNG_PIECES = oe(16, 25); // two or more pieces of the other side attacked by inferior pieces
	private final static int PINNED_PIECE = oe(25, 35);

	// Tempo
	public final static int TEMPO = 9; // Add to moving side score

	private final static long[] OUTPOST_MASK = {0x00007e7e7e000000L, 0x0000007e7e7e0000L};

	private final static int[] KNIGHT_OUTPOST_ATTACKS_NK_PU = { // Knight outpost attacks squares Near King or other opposite pieces Pawn Undefended
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, oe(7, 7), oe(7, 7), oe(10, 10), oe(10, 10), oe(7, 7), oe(7, 7), 0,
			0, oe(5, 5), oe(5, 5), oe(8, 8), oe(8, 8), oe(5, 5), oe(5, 5), 0,
			0, 0, oe(5, 5), oe(8, 8), oe(8, 8), oe(5, 5), 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	private final static int pawnPcsq[] = {
			oe(-21, -4), oe(-9, -6), oe(-3, -8), oe(4, -10), oe(4, -10), oe(-3, -8), oe(-9, -6), oe(-21, -4),
			oe(-24, -7), oe(-12, -9), oe(-6, -11), oe(1, -13), oe(1, -13), oe(-6, -11), oe(-12, -9), oe(-24, -7),
			oe(-23, -7), oe(-11, -9), oe(-5, -11), oe(12, -13), oe(12, -13), oe(-5, -11), oe(-11, -9), oe(-23, -7),
			oe(-22, -6), oe(-10, -8), oe(-4, -10), oe(23, -12), oe(23, -12), oe(-4, -10), oe(-10, -8), oe(-22, -6),
			oe(-20, -5), oe(-8, -7), oe(-2, -9), oe(15, -11), oe(15, -11), oe(-2, -9), oe(-8, -7), oe(-20, -5),
			oe(-19, -4), oe(-7, -6), oe(-1, -8), oe(6, -10), oe(6, -10), oe(-1, -8), oe(-7, -6), oe(-19, -4),
			oe(-18, -2), oe(-6, -4), oe(0, -6), oe(7, -8), oe(7, -8), oe(0, -6), oe(-6, -4), oe(-18, -2),
			oe(-21, -4), oe(-9, -6), oe(-3, -8), oe(4, -10), oe(4, -10), oe(-3, -8), oe(-9, -6), oe(-21, -4)
	};
	private final static int knightPcsq[] = {
			oe(-59, -22), oe(-43, -17), oe(-32, -12), oe(-28, -9), oe(-28, -9), oe(-32, -12), oe(-43, -17), oe(-59, -22),
			oe(-37, -15), oe(-21, -8), oe(-10, -4), oe(-6, -2), oe(-6, -2), oe(-10, -4), oe(-21, -8), oe(-37, -15),
			oe(-21, -10), oe(-5, -4), oe(7, 1), oe(11, 3), oe(11, 3), oe(7, 1), oe(-5, -4), oe(-21, -10),
			oe(-12, -6), oe(4, -1), oe(16, 4), oe(20, 8), oe(20, 8), oe(16, 4), oe(4, -1), oe(-12, -6),
			oe(-6, -4), oe(11, 1), oe(22, 6), oe(26, 10), oe(26, 10), oe(22, 6), oe(11, 1), oe(-6, -4),
			oe(-8, -3), oe(9, 3), oe(20, 8), oe(24, 10), oe(24, 10), oe(20, 8), oe(9, 3), oe(-8, -3),
			oe(-17, -8), oe(-1, -1), oe(11, 3), oe(15, 5), oe(15, 5), oe(11, 3), oe(-1, -1), oe(-17, -8),
			oe(-38, -15), oe(-22, -10), oe(-11, -5), oe(-7, -2), oe(-7, -2), oe(-11, -5), oe(-22, -10), oe(-38, -15)
	};
	private final static int bishopPcsq[] = {
			oe(-7, 0), oe(-9, -1), oe(-12, -2), oe(-14, -2), oe(-14, -2), oe(-12, -2), oe(-9, -1), oe(-7, 0),
			oe(-4, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-4, -1),
			oe(-7, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-7, -2),
			oe(-9, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-9, -2),
			oe(-9, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-9, -2),
			oe(-7, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-7, -2),
			oe(-4, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-4, -1),
			oe(-2, 0), oe(-4, -1), oe(-7, -2), oe(-9, -2), oe(-9, -2), oe(-7, -2), oe(-4, -1), oe(-2, 0)
	};
	private final static int rookPcsq[] = {
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1),
			oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1),
			oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1),
			oe(-5, -2), oe(-1, -2), oe(3, -2), oe(7, -2), oe(7, -2), oe(3, -2), oe(-1, -2), oe(-5, -2)
	};
	private final static int queenPcsq[] = {
			oe(-12, -15), oe(-8, -10), oe(-5, -8), oe(-3, -7), oe(-3, -7), oe(-5, -8), oe(-8, -10), oe(-12, -15),
			oe(-8, -10), oe(-2, -5), oe(0, -3), oe(2, -2), oe(2, -2), oe(0, -3), oe(-2, -5), oe(-8, -10),
			oe(-5, -8), oe(0, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(0, -3), oe(-5, -8),
			oe(-3, -7), oe(2, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(2, -2), oe(-3, -7),
			oe(-3, -7), oe(2, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(2, -2), oe(-3, -7),
			oe(-5, -8), oe(0, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(0, -3), oe(-5, -8),
			oe(-8, -10), oe(-2, -5), oe(0, -3), oe(2, -2), oe(2, -2), oe(0, -3), oe(-2, -5), oe(-8, -10),
			oe(-12, -15), oe(-8, -10), oe(-5, -8), oe(-3, -7), oe(-3, -7), oe(-5, -8), oe(-8, -10), oe(-12, -15)
	};
	private final static int kingPcsq[] = {
			oe(43, -58), oe(48, -35), oe(18, -19), oe(-2, -13), oe(-2, -13), oe(18, -19), oe(48, -35), oe(43, -58),
			oe(40, -35), oe(45, -10), oe(16, 2), oe(-4, 8), oe(-4, 8), oe(16, 2), oe(45, -10), oe(40, -35),
			oe(37, -19), oe(43, 2), oe(13, 17), oe(-7, 23), oe(-7, 23), oe(13, 17), oe(43, 2), oe(37, -19),
			oe(34, -13), oe(40, 8), oe(10, 23), oe(-10, 32), oe(-10, 32), oe(10, 23), oe(40, 8), oe(34, -13),
			oe(29, -13), oe(35, 8), oe(5, 23), oe(-15, 32), oe(-15, 32), oe(5, 23), oe(35, 8), oe(29, -13),
			oe(24, -19), oe(30, 2), oe(0, 17), oe(-20, 23), oe(-20, 23), oe(0, 17), oe(30, 2), oe(24, -19),
			oe(14, -35), oe(19, -10), oe(-10, 2), oe(-30, 8), oe(-30, 8), oe(-10, 2), oe(19, -10), oe(14, -35),
			oe(4, -58), oe(9, -35), oe(-21, -19), oe(-41, -13), oe(-41, -13), oe(-21, -19), oe(9, -35), oe(4, -58)
	};

	private Config config;

	public boolean debug = false;
	public StringBuffer debugSB;

	private int[] pawnMaterial = {0, 0};
	private int[] material = {0, 0};
	private int[] center = {0, 0};
	private int[] positional = {0, 0};
	private int[] mobility = {0, 0};
	private int[] attacks = {0, 0};
	private int[] kingAttackersCount = {0, 0};
	private int[] kingSafety = {0, 0};
	private int[] kingDefense = {0, 0};
	private int[] pawnStructure = {0, 0};
	private int[] passedPawns = {0, 0};

	private long[] superiorPieceAttacked = {0, 0};

	// Squares that pawn can attack
	private long[] pawnCanAttack = {0, 0};

	private long[] minorPiecesDefendedByPawns = {0, 0};

	// Squares surrounding King
	private long[] squaresNearKing = {0, 0};

	private long[] mobilitySquares = {0, 0};

	public ExperimentalEvaluator(Config config) {
		this.config = config;
	}

	public int evaluate(Board board, AttacksInfo ai) {
		if (debug) {
			debugSB = new StringBuffer();
			debugSB.append("\n");
			debugSB.append(board.toString());
			debugSB.append("\n");
		}

		int whitePawns = BitboardUtils.popCount(board.pawns & board.whites);
		int blackPawns = BitboardUtils.popCount(board.pawns & board.blacks);
		int whiteKnights = BitboardUtils.popCount(board.knights & board.whites);
		int blackKnights = BitboardUtils.popCount(board.knights & board.blacks);
		int whiteBishops = BitboardUtils.popCount(board.bishops & board.whites);
		int blackBishops = BitboardUtils.popCount(board.bishops & board.blacks);
		int whiteRooks = BitboardUtils.popCount(board.rooks & board.whites);
		int blackRooks = BitboardUtils.popCount(board.rooks & board.blacks);
		int whiteQueens = BitboardUtils.popCount(board.queens & board.whites);
		int blackQueens = BitboardUtils.popCount(board.queens & board.blacks);

		int endGameValue = EndgameEvaluator.endGameValue(board, whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops, whiteRooks, blackRooks, whiteQueens, blackQueens);
		if (endGameValue != Evaluator.NO_VALUE) {
			return endGameValue;
		}

		ai.build(board);

		pawnMaterial[W] = PAWN * whitePawns;
		pawnMaterial[B] = PAWN * blackPawns;
		material[W] = KNIGHT * whiteKnights + BISHOP * whiteBishops + ROOK * whiteRooks + QUEEN * whiteQueens + //
				((board.whites & board.bishops & BitboardUtils.WHITE_SQUARES) != 0 //
						&& (board.whites & board.bishops & BitboardUtils.BLACK_SQUARES) != 0 ? BISHOP_PAIR : 0);
		material[B] = KNIGHT * blackKnights + BISHOP * blackBishops + ROOK * blackRooks + QUEEN * blackQueens + //
				((board.blacks & board.bishops & BitboardUtils.WHITE_SQUARES) != 0 //
						&& (board.blacks & board.bishops & BitboardUtils.BLACK_SQUARES) != 0 ? BISHOP_PAIR : 0);

		center[W] = 0;
		center[B] = 0;
		positional[W] = 0;
		positional[B] = 0;
		mobility[W] = 0;
		mobility[B] = 0;
		kingAttackersCount[W] = 0;
		kingAttackersCount[B] = 0;
		kingSafety[W] = 0;
		kingSafety[B] = 0;
		kingDefense[W] = 0;
		kingDefense[B] = 0;
		pawnStructure[W] = 0;
		pawnStructure[B] = 0;
		passedPawns[W] = 0;
		passedPawns[B] = 0;

		superiorPieceAttacked[W] = 0;
		superiorPieceAttacked[B] = 0;

		// Squares that pawns attack or can attack by advancing
		pawnCanAttack[W] = ai.pawnAttacks[W] | ai.pawnAttacks[W] << 8 | ai.pawnAttacks[W] << 16 | ai.pawnAttacks[W] << 24 | ai.pawnAttacks[W] << 32 | ai.pawnAttacks[W] << 40;
		pawnCanAttack[B] = ai.pawnAttacks[B] | ai.pawnAttacks[B] >>> 8 | ai.pawnAttacks[B] >>> 16 | ai.pawnAttacks[B] >>> 24 | ai.pawnAttacks[B] >>> 32 | ai.pawnAttacks[B] >>> 40;

		// Initialize attacks with pawn attacks
		attacks[W] = PAWN_ATTACKS_KNIGHT * BitboardUtils.popCount(ai.pawnAttacks[W] & board.knights & board.blacks) + //
				PAWN_ATTACKS_BISHOP * BitboardUtils.popCount(ai.pawnAttacks[W] & board.bishops & board.blacks) + //
				PAWN_ATTACKS_ROOK * BitboardUtils.popCount(ai.pawnAttacks[W] & board.rooks & board.blacks) + //
				PAWN_ATTACKS_QUEEN * BitboardUtils.popCount(ai.pawnAttacks[W] & board.queens & board.blacks);
		attacks[B] = PAWN_ATTACKS_KNIGHT * BitboardUtils.popCount(ai.pawnAttacks[B] & board.knights & board.whites) + //
				PAWN_ATTACKS_BISHOP * BitboardUtils.popCount(ai.pawnAttacks[B] & board.bishops & board.whites) + //
				PAWN_ATTACKS_ROOK * BitboardUtils.popCount(ai.pawnAttacks[B] & board.rooks & board.whites) + //
				PAWN_ATTACKS_QUEEN * BitboardUtils.popCount(ai.pawnAttacks[B] & board.queens & board.whites);

		minorPiecesDefendedByPawns[W] = board.whites & (board.bishops | board.knights) & ai.pawnAttacks[W];
		minorPiecesDefendedByPawns[B] = board.blacks & (board.bishops | board.knights) & ai.pawnAttacks[B];

		// Squares surrounding King
		squaresNearKing[W] = bbAttacks.king[BitboardUtils.square2Index(board.whites & board.kings)] | board.whites & board.kings;
		squaresNearKing[B] = bbAttacks.king[BitboardUtils.square2Index(board.blacks & board.kings)] | board.blacks & board.kings;

		mobilitySquares[W] = ~board.whites & ~ai.pawnAttacks[B];
		mobilitySquares[B] = ~board.blacks & ~ai.pawnAttacks[W];

		long all = board.getAll();
		long pieceAttacks, pieceAttacksXray;
		long square = 1;
		for (int index = 0; index < 64; index++) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);
				int us = (isWhite ? W : B);
				int them = (isWhite ? B : W);
				long mines = (isWhite ? board.whites : board.blacks);
				long others = (isWhite ? board.blacks : board.whites);
				long otherPawnAttacks = (isWhite ? ai.pawnAttacks[B] : ai.pawnAttacks[W]);
				int pcsqIndex = (isWhite ? index : 63 - index);
				int rank = index >> 3;
				int file = 7 - index & 7;

				pieceAttacks = ai.attacksFromSquare[index];

				if ((square & board.pawns) != 0) {
					center[us] += pawnPcsq[pcsqIndex];

					if ((pieceAttacks & squaresNearKing[them] & ~otherPawnAttacks) != 0) {
						kingSafety[us] += PAWN_ATTACKS_KING;
					}

					superiorPieceAttacked[us] |= pieceAttacks & others & (board.knights | board.bishops | board.rooks | board.queens);

					long myPawns = board.pawns & mines;
					long otherPawns = board.pawns & others;
					long adjacentFiles = BitboardUtils.FILES_ADJACENT[file];
					long ranksForward = BitboardUtils.RANKS_FORWARD[us][rank];
					long routeToPromotion = BitboardUtils.FILE[file] & ranksForward;
					long myPawnsBesideAndBehindAdjacent = BitboardUtils.RANK_AND_BACKWARD[us][rank] & adjacentFiles & myPawns;
					long myPawnsAheadAdjacent = ranksForward & adjacentFiles & myPawns;
					long otherPawnsAheadAdjacent = ranksForward & adjacentFiles & otherPawns;

					boolean isolated = (myPawns & adjacentFiles) == 0;
					boolean supported = (square & ai.pawnAttacks[us]) != 0;
					boolean doubled = (myPawns & routeToPromotion) != 0;
					boolean opposed = (otherPawns & routeToPromotion) != 0;
					boolean passed = !doubled
							&& !opposed
							&& otherPawnsAheadAdjacent == 0;
					boolean candidate = !doubled
							&& !opposed
							&& !passed
							&& (((otherPawnsAheadAdjacent & ~pieceAttacks) == 0) || // Can become passer advancing
							(BitboardUtils.popCount(myPawnsBesideAndBehindAdjacent) >= BitboardUtils.popCount(otherPawnsAheadAdjacent))); // Has more friend pawns beside and behind than opposed pawns controlling his route to promotion
					boolean backwards = !isolated
							&& !passed
							&& !candidate
							&& myPawnsBesideAndBehindAdjacent == 0
							&& (pieceAttacks & otherPawns) == 0 // No backwards if it can capture
							&& (BitboardUtils.RANK_AND_BACKWARD[us][isWhite ? BitboardUtils.getRankLsb(myPawnsAheadAdjacent) : BitboardUtils.getRankMsb(myPawnsAheadAdjacent)] &
							routeToPromotion & (board.pawns | otherPawnAttacks)) != 0; // Other pawns stopping it from advance, opposing or capturing it before reaching my pawns

					if (debug) {
						boolean connected = ((bbAttacks.king[index] & adjacentFiles & myPawns) != 0);
						debugSB.append("PAWN " + //
										index + //
										(isWhite ? " WHITE " : " BLACK ") + //
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

					if (!supported
							&& !isolated) {
						pawnStructure[us] += PAWN_UNSUPPORTED;
					}
					if (doubled) {
						pawnStructure[us] += PAWN_DOUBLED[opposed ? 1 : 0];
					}
					if (isolated) {
						pawnStructure[us] += PAWN_ISOLATED[opposed ? 1 : 0];
					}
					if (backwards) {
						pawnStructure[us] += PAWN_BACKWARDS;
					}
					if (candidate) {
						passedPawns[us] += PAWN_CANDIDATE[(isWhite ? rank : 7 - rank)];
					}
					if (passed) {
						int relativeRank = isWhite ? rank : 7 - rank;
						long backFile = BitboardUtils.FILE[file] & BitboardUtils.RANKS_BACKWARD[us][rank];
						// If has has root/queen behind consider all the route to promotion attacked or defended
						long attackedAndNotDefendedRoute = //
								((routeToPromotion & ai.attackedSquares[them]) | ((backFile & (board.rooks | board.queens) & others) != 0 ? routeToPromotion : 0)) &
										~((routeToPromotion & ai.attackedSquares[us]) | ((backFile & (board.rooks | board.queens) & mines) != 0 ? routeToPromotion : 0));
						long pushSquare = isWhite ? square << 8 : square >>> 8;
						long pawnsLeft = BitboardUtils.FILES_LEFT[file] & board.pawns;
						long pawnsRight = BitboardUtils.FILES_RIGHT[file] & board.pawns;

						boolean connected = (bbAttacks.king[index] & adjacentFiles & myPawns) != 0;
						boolean outside = ((pawnsLeft != 0) && (pawnsRight == 0)) || ((pawnsLeft == 0) && (pawnsRight != 0));
						boolean mobile = (pushSquare & (all | attackedAndNotDefendedRoute)) == 0;
						boolean runner = mobile
								&& (routeToPromotion & all) == 0
								&& attackedAndNotDefendedRoute == 0;

						if (debug) {
							debugSB.append("        PASSER " + //
											(outside ? "outside " : "") + //
											(mobile ? "mobile " : "") + //
											(runner ? "runner " : "") + //
											"\n"
							);
						}

						passedPawns[us] += PAWN_PASSER[relativeRank];

						if (supported) {
							passedPawns[us] += PAWN_PASSER_SUPPORTED[relativeRank];
						}
						if (connected) {
							passedPawns[us] += PAWN_PASSER_CONNECTED[relativeRank];
						}
						if (outside) {
							passedPawns[us] += PAWN_PASSER_OUTSIDE[relativeRank];
						}
						if (mobile) {
							passedPawns[us] += PAWN_PASSER_MOBILE[relativeRank];
						}
						if (runner) {
							passedPawns[us] += PAWN_PASSER_RUNNER[relativeRank];
						}
					}

				} else if ((square & board.knights) != 0) {
					center[us] += knightPcsq[pcsqIndex];

					mobility[us] += MOBILITY[Piece.KNIGHT][BitboardUtils.popCount(pieceAttacks & mobilitySquares[us])];

					if ((pieceAttacks & squaresNearKing[them] & ~otherPawnAttacks) != 0) {
						kingSafety[us] += KNIGHT_ATTACKS_KING;
						kingAttackersCount[us]++;
					}
					if ((pieceAttacks & squaresNearKing[us]) != 0) {
						kingDefense[us] += KNIGHT_DEFENDS_KING;
					}

					if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0) {
						attacks[us] += KNIGHT_ATTACKS_PU_P;
					}
					if ((pieceAttacks & board.bishops & others & ~otherPawnAttacks) != 0) {
						attacks[us] += KNIGHT_ATTACKS_PU_B;
					}
					if ((pieceAttacks & (board.rooks | board.queens) & others) != 0) {
						attacks[us] += KNIGHT_ATTACKS_RQ;
					}

					superiorPieceAttacked[us] |= pieceAttacks & others & (board.rooks | board.queens);

					// Knight outpost: no opposite pawns can attack the square
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them]) != 0) {
						positional[us] += KNIGHT_OUTPOST;
						// Defended by one of our pawns
						if ((square & ai.pawnAttacks[us]) != 0) {
							positional[us] += KNIGHT_OUTPOST;
							// Attacks squares near king or other pieces pawn undefended
							if ((pieceAttacks & (squaresNearKing[them] | others) & ~otherPawnAttacks) != 0) {
								positional[us] += KNIGHT_OUTPOST_ATTACKS_NK_PU[pcsqIndex];
							}
						}
					}

				} else if ((square & board.bishops) != 0) {
					center[us] += bishopPcsq[pcsqIndex];

					mobility[us] += MOBILITY[Piece.BISHOP][BitboardUtils.popCount(pieceAttacks & mobilitySquares[us])];

					if ((pieceAttacks & squaresNearKing[them] & ~otherPawnAttacks) != 0) {
						kingSafety[us] += BISHOP_ATTACKS_KING;
						kingAttackersCount[us]++;
					}
					if ((pieceAttacks & squaresNearKing[us]) != 0) {
						kingDefense[us] += BISHOP_DEFENDS_KING;
					}

					if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0) {
						attacks[us] += BISHOP_ATTACKS_PU_P;
					}
					if ((pieceAttacks & board.knights & others & ~otherPawnAttacks) != 0) {
						attacks[us] += BISHOP_ATTACKS_PU_K;
					}
					if ((pieceAttacks & (board.rooks | board.queens) & others) != 0) {
						attacks[us] += BISHOP_ATTACKS_RQ;
					}

					superiorPieceAttacked[us] |= pieceAttacks & others & (board.rooks | board.queens);

					pieceAttacksXray = bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

					// Bishop Outpost: no opposite pawns can attack the square and defended by one of our pawns
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them] & ai.pawnAttacks[us]) != 0) {
						positional[us] += BISHOP_OUTPOST;
						// Attacks squares near king or other pieces pawn undefended
						if ((pieceAttacks & (squaresNearKing[them] | others) & ~otherPawnAttacks) != 0) {
							positional[us] += BISHOP_OUTPOST_ATT_NK_PU;
						}
					}

					long bishopColorSquares = (square & BitboardUtils.WHITE_SQUARES) != 0 ? BitboardUtils.WHITE_SQUARES : BitboardUtils.BLACK_SQUARES;
					positional[us] += BitboardUtils.popCount(mines & board.pawns & ~bishopColorSquares) * BISHOP_MY_PAWNS_NOT_IN_COLOR
							+ oeMul(BitboardUtils.popCount(others & board.pawns & bishopColorSquares), BISHOP_OTHER_PAWNS_IN_COLOR);

				} else if ((square & board.rooks) != 0) {
					center[us] += rookPcsq[pcsqIndex];

					mobility[us] += MOBILITY[Piece.ROOK][BitboardUtils.popCount(pieceAttacks & mobilitySquares[us])];

					if ((pieceAttacks & squaresNearKing[them] & ~otherPawnAttacks) != 0) {
						kingSafety[us] += ROOK_ATTACKS_KING;
						kingAttackersCount[us]++;
					}
					if ((pieceAttacks & squaresNearKing[us]) != 0) {
						kingDefense[us] += ROOK_DEFENDS_KING;
					}

					if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0) {
						attacks[us] += ROOK_ATTACKS_PU_P;
					}
					if ((pieceAttacks & (board.bishops | board.knights) & others & ~otherPawnAttacks) != 0) {
						attacks[us] += ROOK_ATTACKS_PU_BK;
					}
					if ((pieceAttacks & board.queens & others) != 0) {
						attacks[us] += ROOK_ATTACKS_Q;
					}

					superiorPieceAttacked[us] |= pieceAttacks & others & board.queens;

					pieceAttacksXray = bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

					long rank6 = isWhite ? BitboardUtils.RANK[5] : BitboardUtils.RANK[2];
					long rank7 = isWhite ? BitboardUtils.RANK[6] : BitboardUtils.RANK[B];
					long rank8 = isWhite ? BitboardUtils.RANK[7] : BitboardUtils.RANK[W];

					if ((square & rank8) != 0
							&& (others & board.kings & rank8) != 0) {
						positional[us] += ROOK_8_KING_8;
					}
					if ((square & rank7) != 0
							&& (others & (board.kings | board.pawns) & (rank7 | rank8)) != 0) {
						positional[us] += ROOK_7_KP_78;
					}
					if ((square & rank6) != 0
							&& (others & (board.kings | board.pawns) & (rank6 | rank7 | rank8)) != 0) {
						positional[us] += ROOK_6_KP_678;
					}

					long rookFile = BitboardUtils.FILE[file] & BitboardUtils.RANKS_FORWARD[us][rank];
					if ((rookFile & board.pawns & mines) == 0) {
						positional[us] += ROOK_FILE_SEMIOPEN;
						if ((rookFile & board.pawns) == 0) {
							if ((rookFile & minorPiecesDefendedByPawns[them]) == 0) {
								positional[us] += ROOK_FILE_OPEN_NO_MG;
							} else {
								if ((rookFile & minorPiecesDefendedByPawns[them] & pawnCanAttack[us]) == 0) {
									positional[us] += ROOK_FILE_OPEN_MG_NP;
								} else {
									positional[us] += ROOK_FILE_OPEN_MG_P;
								}
							}
						} else {
							// There is an opposite backward pawn
							if ((rookFile & board.pawns & others & pawnCanAttack[them]) == 0) {
								positional[us] += ROOK_FILE_SEMIOPEN_BP;
							}
						}

						if ((rookFile & board.kings & others) != 0) {
							positional[us] += ROOK_FILE_SEMIOPEN_K;
						}
					}
					// Rook Outpost: no opposite pawns can attack the square and defended by one of our pawns
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them] & ai.pawnAttacks[us]) != 0) {
						positional[us] += ROOK_OUTPOST;
						// Attacks squares near king or other pieces pawn undefended
						if ((pieceAttacks & (squaresNearKing[them] | others) & ~otherPawnAttacks) != 0) {
							positional[us] += ROOK_OUTPOST_ATT_NK_PU;
						}
					}

				} else if ((square & board.queens) != 0) {
					center[us] += queenPcsq[pcsqIndex];

					mobility[us] += MOBILITY[Piece.QUEEN][BitboardUtils.popCount(pieceAttacks & mobilitySquares[us])];

					if ((pieceAttacks & squaresNearKing[them] & ~otherPawnAttacks) != 0) {
						kingSafety[us] += QUEEN_ATTACKS_KING;
						kingAttackersCount[us]++;
					}
					if ((pieceAttacks & squaresNearKing[us]) != 0) {
						kingDefense[us] += QUEEN_DEFENDS_KING;
					}
					if ((pieceAttacks & others & ~otherPawnAttacks) != 0) {
						attacks[us] += QUEEN_ATTACKS_PU;
					}

					pieceAttacksXray = (bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) |
							bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)))
							& ~pieceAttacks;
					if ((pieceAttacksXray & board.kings & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

					long rank7 = isWhite ? BitboardUtils.RANK[6] : BitboardUtils.RANK[B];
					long rank8 = isWhite ? BitboardUtils.RANK[7] : BitboardUtils.RANK[W];

					if ((square & rank7) != 0
							&& (others & (board.kings | board.pawns) & (rank7 | rank8)) != 0) {
						attacks[us] += QUEEN_7_KP_78;
						if ((board.rooks & mines & rank7 & pieceAttacks) != 0
								&& (board.kings & others & rank8) != 0) {
							positional[us] += QUEEN_7_P_78_K_8_R_7;
						}
					}

				} else if ((square & board.kings) != 0) {
					center[us] += kingPcsq[pcsqIndex];

					// If king is in the first or second rank, we add the pawn shield
					if ((square & (isWhite ? BitboardUtils.RANK[W] | BitboardUtils.RANK[B] : BitboardUtils.RANK[6] | BitboardUtils.RANK[7])) != 0) {
						kingDefense[us] += KING_PAWN_SHIELD * BitboardUtils.popCount(pieceAttacks & mines & board.pawns);
					}
				}
			}
			square <<= 1;
		}

		// Ponder opening and Endgame value depending of the non-pawn pieces:
		// opening=> gamephase = 255 / ending => gamephase ~= 0
		int gamePhase = ((material[W] + material[B]) << 8) / 5000;
		if (gamePhase > 256) {
			gamePhase = 256; // Security
		}

		int value = 0;
		// First Material
		value += pawnMaterial[W] - pawnMaterial[B] + material[W] - material[B];
		// Tempo
		value += (board.getTurn() ? TEMPO : -TEMPO);

		int supAttWhite = BitboardUtils.popCount(superiorPieceAttacked[W]);
		int supAttBlack = BitboardUtils.popCount(superiorPieceAttacked[B]);
		int hungPieces = (supAttWhite >= 2 ? supAttWhite * HUNG_PIECES : 0) - (supAttBlack >= 2 ? supAttBlack * HUNG_PIECES : 0);

		int oe = oeMul(config.getEvalCenter(), center[W] - center[B])
				+ oeMul(config.getEvalPositional(), positional[W] - positional[B])
				+ oeMul(config.getEvalAttacks(), attacks[W] - attacks[B] + hungPieces)
				+ oeMul(config.getEvalMobility(), mobility[W] - mobility[B])
				+ oeMul(config.getEvalPawnStructure(), pawnStructure[W] - pawnStructure[B])
				+ oeMul(config.getEvalPassedPawns(), passedPawns[W] - passedPawns[B])
				+ oeMul(config.getEvalKingSafety(), kingDefense[W] - kingDefense[B]
				+ (KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B]));

		value += (gamePhase * o(oe)) / (256 * 100); // divide by 256
		value += ((256 - gamePhase) * e(oe)) / (256 * 100);

		if (debug) {
			logger.debug(debugSB);

			logger.debug("material          = " + (material[W] - material[B]));
			logger.debug("pawnMaterial      = " + (pawnMaterial[W] - pawnMaterial[B]));
			logger.debug("tempo             = " + (board.getTurn() ? TEMPO : -TEMPO));
			logger.debug("gamePhase         = " + gamePhase);
			logger.debug("                     Opening  Endgame");
			logger.debug("center            = " + formatOE(center[W] - center[B]));
			logger.debug("positional        = " + formatOE(positional[W] - positional[B]));
			logger.debug("attacks           = " + formatOE(attacks[W] - attacks[B]));
			logger.debug("mobility          = " + formatOE(mobility[W] - mobility[B]));
			logger.debug("pawnStructure     = " + formatOE(pawnStructure[W] - pawnStructure[B]));
			logger.debug("passedPawns       = " + formatOE(passedPawns[W] - passedPawns[B]));
			logger.debug("kingSafety        = " + formatOE(KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B]));
			logger.debug("kingDefense       = " + formatOE(kingDefense[W] - kingDefense[B]));
			logger.debug("value             = " + value);
		}
		assert Math.abs(value) < Evaluator.KNOWN_WIN : "Eval is outside limits";
		return value;
	}

	private String formatOE(int value) {
		return StringUtils.padLeft(String.valueOf(o(value)), 8) + " " + StringUtils.padLeft(String.valueOf(e(value)), 8);
	}
}