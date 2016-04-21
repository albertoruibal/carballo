package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.Square;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

/**
 * Evaluation is done in centipawns
 *
 * @author rui
 */
public class ExperimentalEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger("ExperimentalEvaluator");

	// Mobility units: this value is added for the number of destination square not occupied by one of our pieces or attacked by opposite pawns
	private static final int[][] MOBILITY = {
			{}, {},
			{oe(0, 0), oe(12, 16), oe(18, 24), oe(21, 28), oe(24, 32)},
			{oe(0, 0), oe(11, 11), oe(18, 18), oe(22, 22), oe(25, 25), oe(28, 28), oe(30, 30), oe(32, 32)},
			{oe(0, 0), oe(6, 9), oe(10, 15), oe(13, 20), oe(15, 23), oe(17, 26), oe(19, 29), oe(21, 31), oe(22, 33), oe(23, 35), oe(24, 37), oe(25, 38), oe(26, 39), oe(27, 41), oe(28, 42)},
			{oe(0, 0), oe(7, 7), oe(12, 12), oe(16, 16), oe(20, 20), oe(23, 23), oe(26, 26), oe(28, 28), oe(30, 30), oe(33, 33), oe(34, 34), oe(36, 36), oe(38, 38), oe(39, 39), oe(41, 41), oe(42, 42), oe(43, 43), oe(44, 44), oe(46, 46), oe(47, 47), oe(48, 48), oe(49, 49), oe(50, 50), oe(51, 51), oe(52, 52), oe(52, 52), oe(53, 53), oe(54, 54)}
	};

	// Attacks
	private static final int[] PAWN_ATTACKS = {
			0, oe(0, 0), oe(5, 7), oe(5, 7), oe(7, 10), oe(8, 12), 0
	};
	// Minor piece attacks to pawn undefended pieces
	private static final int[] MINOR_ATTACKS = {
			0, oe(3, 4), oe(5, 5), oe(5, 5), oe(7, 10), oe(7, 10), 0
	};
	// Major piece attacks to pawn undefended pieces
	private static final int[] MAJOR_ATTACKS = {
			0, oe(2, 3), oe(4, 5), oe(4, 5), oe(5, 5), oe(5, 5), 0
	};
	private static final int HUNG_PIECES = oe(16, 25); // Two or more pieces of the other side attacked by inferior pieces
	private static final int PINNED_PIECE = oe(25, 35);

	// Pawns
	// Those are all penalties. Array is {not opposed, opposed}: If not opposed, backwards and isolated pawns can be easily attacked
	private static final int[] PAWN_BACKWARDS = {oe(20, 15), oe(10, 15)}; // Not opposed is worse in the opening
	private static final int[] PAWN_ISOLATED = {oe(20, 20), oe(10, 20)}; // Not opposed is worse in the opening
	private static final int[] PAWN_DOUBLED = {oe(8, 16), oe(10, 20)}; // Not opposed is better, opening is better
	private static final int PAWN_UNSUPPORTED = oe(2, 4); // Not backwards or isolated

	// And now the bonuses. Array by relative rank
	private static final int[] PAWN_CANDIDATE = {0, oe(5, 7), oe(5, 7), oe(7, 9), oe(10, 14), oe(14, 21), oe(20, 30), 0};
	private static final int[] PAWN_PASSER = {0, oe(10, 20), oe(10, 20), oe(13, 25), oe(19, 35), oe(28, 50), oe(40, 70), 0};
	private static final int[] PAWN_PASSER_OUTSIDE = {0, oe(2, 5), oe(2, 5), oe(3, 7), oe(4, 10), oe(7, 14), oe(10, 20), 0};
	private static final int[] PAWN_PASSER_CONNECTED = {0, 0, 0, oe(1, 2), oe(3, 5), oe(6, 9), oe(10, 15), 0};
	private static final int[] PAWN_PASSER_SUPPORTED = {0, 0, 0, oe(2, 3), oe(6, 9), oe(12, 18), oe(20, 30), 0};
	private static final int[] PAWN_PASSER_MOBILE = {0, 0, 0, oe(1, 1), oe(2, 3), oe(4, 6), oe(7, 10), 0};
	private static final int[] PAWN_PASSER_RUNNER = {0, 0, 0, oe(2, 3), oe(6, 9), oe(12, 18), oe(20, 30), 0};
	private static final int PAWN_PASSER_UNSTOPPABLE = oe(750, 750);

	private static final int[] PAWN_SHIELD = {0, oe(32, 0), oe(24, 0), oe(16, 0), oe(8, 0), 0, 0, 0};
	private static final int[] PAWN_STORM = {0, 0, 0, oe(12, 0), oe(25, 0), oe(50, 0), 0, 0};

	// Knights
	private static final int KNIGHT_OUTPOST = oe(2, 3); // Adds one time if no opposite can can attack out knight and twice if it is defended by one of our pawns
	private static final int[] KNIGHT_OUTPOST_ATTACKING = { // Knight outpost attacks squares Near King or other opposite pieces Pawn Undefended
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, oe(7, 7), oe(7, 7), oe(10, 10), oe(10, 10), oe(7, 7), oe(7, 7), 0,
			0, oe(5, 5), oe(5, 5), oe(8, 8), oe(8, 8), oe(5, 5), oe(5, 5), 0,
			0, 0, oe(5, 5), oe(8, 8), oe(8, 8), oe(5, 5), 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	// Bishops
	private static final int BISHOP_OUTPOST = oe(1, 2); // Only if defended by pawn
	private static final int BISHOP_OUTPOST_ATTACKING = oe(3, 4); // And attacks other piece not defended by pawn or a square in the king zone
	private static final int BISHOP_MY_PAWNS_IN_COLOR_PENALTY = oe(2, 4); // Penalty for each of my pawns in the bishop color (Capablanca rule)
	private static final int BISHOP_TRAPPED_PENALTY = oe(40, 40);
	private static final long[] BISHOP_TRAPPING = {
			0, Square.F2, 0, 0, 0, 0, Square.C2, 0,
			Square.G3, 0, 0, 0, 0, 0, 0, Square.B3,
			Square.G4, 0, 0, 0, 0, 0, 0, Square.B4,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			Square.G5, 0, 0, 0, 0, 0, 0, Square.B5,
			Square.G6, 0, 0, 0, 0, 0, 0, Square.B6,
			0, Square.F7, 0, 0, 0, 0, Square.C7, 0
	};

	// Rooks
	private static final int ROOK_OUTPOST = oe(1, 2); // Only if defended by pawn
	private static final int ROOK_OUTPOST_ATTACKING = oe(3, 4); // Also attacks other piece not defended by pawn or a square near king
	private static final int ROOK_FILE_OPEN_NO_MG = oe(20, 10); // No pawns in rook file and no minor guarded
	private static final int ROOK_FILE_OPEN_MG_P = oe(15, 5); // No pawns in rook file and minor guarded, my pawns can attack
	private static final int ROOK_FILE_OPEN_MG_NP = oe(10, 0); // No pawns in rook file and minor guarded, my pawns cannot attack
	private static final int ROOK_FILE_SEMIOPEN = oe(3, 6); // No pawns mines in file
	private static final int ROOK_FILE_SEMIOPEN_WEAK_PAWN = oe(15, 5); // And attacks a backward or isolated pawn
	private static final int ROOK_FILE_SEMIOPEN_K = oe(3, 6); // No pawns mines in file and opposite king
	private static final int ROOK_7 = oe(7, 10); // Rook 5, 6 or 7th rank attacking a pawn in the same rank not defended by pawn

	// King
	// Sums for each piece attacking an square near the king
	private static final int PIECE_ATTACKS_KING[] = {0, 0, oe(30, 0), oe(20, 0), oe(40, 0), oe(80, 0)};
	// Ponder kings attacks by the number of attackers (not pawns)
	private static final int[] KING_SAFETY_PONDER = {0, 0, 32, 48, 56, 60, 62, 63, 64, 64, 64, 64, 64, 64, 64, 64};

	// Tempo
	public static final int TEMPO = oe(15, 5); // Add to moving side score

	private static final long[] OUTPOST_MASK = {0x00007e7e7e000000L, 0x0000007e7e7e0000L};

	private static final int pawnPcsq[] = {
			oe(80, 96), oe(92, 94), oe(98, 92), oe(105, 90), oe(105, 90), oe(98, 92), oe(92, 94), oe(80, 96),
			oe(77, 93), oe(89, 91), oe(95, 89), oe(102, 87), oe(102, 87), oe(95, 89), oe(89, 91), oe(77, 93),
			oe(78, 93), oe(90, 91), oe(96, 89), oe(113, 87), oe(113, 87), oe(96, 89), oe(90, 91), oe(78, 93),
			oe(79, 94), oe(91, 92), oe(97, 90), oe(124, 88), oe(124, 88), oe(97, 90), oe(91, 92), oe(79, 94),
			oe(81, 95), oe(93, 93), oe(99, 91), oe(116, 89), oe(116, 89), oe(99, 91), oe(93, 93), oe(81, 95),
			oe(82, 96), oe(94, 94), oe(100, 92), oe(107, 90), oe(107, 90), oe(100, 92), oe(94, 94), oe(82, 96),
			oe(83, 98), oe(95, 96), oe(101, 94), oe(108, 92), oe(108, 92), oe(101, 94), oe(95, 96), oe(83, 98),
			oe(80, 96), oe(92, 94), oe(98, 92), oe(105, 90), oe(105, 90), oe(98, 92), oe(92, 94), oe(80, 96)
	};
	private static final int knightPcsq[] = {
			oe(267, 303), oe(283, 308), oe(294, 313), oe(298, 316), oe(298, 316), oe(294, 313), oe(283, 308), oe(267, 303),
			oe(289, 310), oe(305, 317), oe(316, 321), oe(320, 323), oe(320, 323), oe(316, 321), oe(305, 317), oe(289, 310),
			oe(305, 315), oe(321, 321), oe(332, 326), oe(336, 328), oe(336, 328), oe(332, 326), oe(321, 321), oe(305, 315),
			oe(314, 319), oe(330, 324), oe(341, 329), oe(345, 333), oe(345, 333), oe(341, 329), oe(330, 324), oe(314, 319),
			oe(320, 321), oe(336, 326), oe(347, 331), oe(351, 335), oe(351, 335), oe(347, 331), oe(336, 326), oe(320, 321),
			oe(318, 322), oe(334, 328), oe(345, 333), oe(349, 335), oe(349, 335), oe(345, 333), oe(334, 328), oe(318, 322),
			oe(309, 317), oe(325, 324), oe(336, 328), oe(340, 330), oe(340, 330), oe(336, 328), oe(325, 324), oe(309, 317),
			oe(288, 310), oe(304, 315), oe(315, 320), oe(319, 323), oe(319, 323), oe(315, 320), oe(304, 315), oe(288, 310)
	};
	private static final int bishopPcsq[] = {
			oe(318, 325), oe(317, 324), oe(314, 323), oe(312, 323), oe(312, 323), oe(314, 323), oe(317, 324), oe(318, 325),
			oe(322, 324), oe(328, 326), oe(325, 325), oe(323, 325), oe(323, 325), oe(325, 325), oe(328, 326), oe(322, 324),
			oe(319, 323), oe(325, 325), oe(332, 328), oe(331, 327), oe(331, 327), oe(332, 328), oe(325, 325), oe(319, 323),
			oe(317, 323), oe(323, 325), oe(331, 327), oe(340, 330), oe(340, 330), oe(331, 327), oe(323, 325), oe(317, 323),
			oe(317, 323), oe(323, 325), oe(331, 327), oe(340, 330), oe(340, 330), oe(331, 327), oe(323, 325), oe(317, 323),
			oe(319, 323), oe(325, 325), oe(332, 328), oe(331, 327), oe(331, 327), oe(332, 328), oe(325, 325), oe(319, 323),
			oe(322, 324), oe(328, 326), oe(325, 325), oe(323, 325), oe(323, 325), oe(325, 325), oe(328, 326), oe(322, 324),
			oe(323, 325), oe(322, 324), oe(319, 323), oe(317, 323), oe(317, 323), oe(319, 323), oe(322, 324), oe(323, 325)
	};
	private static final int rookPcsq[] = {
			oe(496, 500), oe(500, 500), oe(504, 500), oe(508, 500), oe(508, 500), oe(504, 500), oe(500, 500), oe(496, 500),
			oe(496, 500), oe(500, 500), oe(504, 500), oe(508, 500), oe(508, 500), oe(504, 500), oe(500, 500), oe(496, 500),
			oe(496, 500), oe(500, 500), oe(504, 500), oe(508, 500), oe(508, 500), oe(504, 500), oe(500, 500), oe(496, 500),
			oe(496, 500), oe(500, 500), oe(504, 500), oe(508, 500), oe(508, 500), oe(504, 500), oe(500, 500), oe(496, 500),
			oe(496, 501), oe(500, 501), oe(504, 501), oe(508, 501), oe(508, 501), oe(504, 501), oe(500, 501), oe(496, 501),
			oe(496, 501), oe(500, 501), oe(504, 501), oe(508, 501), oe(508, 501), oe(504, 501), oe(500, 501), oe(496, 501),
			oe(496, 501), oe(500, 501), oe(504, 501), oe(508, 501), oe(508, 501), oe(504, 501), oe(500, 501), oe(496, 501),
			oe(496, 498), oe(500, 498), oe(504, 498), oe(508, 498), oe(508, 498), oe(504, 498), oe(500, 498), oe(496, 498)
	};
	private static final int queenPcsq[] = {
			oe(964, 960), oe(968, 965), oe(971, 967), oe(973, 968), oe(973, 968), oe(971, 967), oe(968, 965), oe(964, 960),
			oe(968, 965), oe(974, 970), oe(976, 972), oe(978, 973), oe(978, 973), oe(976, 972), oe(974, 970), oe(968, 965),
			oe(971, 967), oe(976, 972), oe(980, 975), oe(981, 977), oe(981, 977), oe(980, 975), oe(976, 972), oe(971, 967),
			oe(973, 968), oe(978, 973), oe(981, 977), oe(984, 980), oe(984, 980), oe(981, 977), oe(978, 973), oe(973, 968),
			oe(973, 968), oe(978, 973), oe(981, 977), oe(984, 980), oe(984, 980), oe(981, 977), oe(978, 973), oe(973, 968),
			oe(971, 967), oe(976, 972), oe(980, 975), oe(981, 977), oe(981, 977), oe(980, 975), oe(976, 972), oe(971, 967),
			oe(968, 965), oe(974, 970), oe(976, 972), oe(978, 973), oe(978, 973), oe(976, 972), oe(974, 970), oe(968, 965),
			oe(964, 960), oe(968, 965), oe(971, 967), oe(973, 968), oe(973, 968), oe(971, 967), oe(968, 965), oe(964, 960)
	};
	private static final int kingPcsq[] = {
			oe(1044, 942), oe(1049, 965), oe(1019, 981), oe(999, 987), oe(999, 987), oe(1019, 981), oe(1049, 965), oe(1044, 942),
			oe(1041, 965), oe(1046, 990), oe(1016, 1002), oe(996, 1008), oe(996, 1008), oe(1016, 1002), oe(1046, 990), oe(1041, 965),
			oe(1038, 981), oe(1043, 1002), oe(1013, 1017), oe(993, 1023), oe(993, 1023), oe(1013, 1017), oe(1043, 1002), oe(1038, 981),
			oe(1035, 987), oe(1040, 1008), oe(1010, 1023), oe(990, 1032), oe(990, 1032), oe(1010, 1023), oe(1040, 1008), oe(1035, 987),
			oe(1030, 987), oe(1035, 1008), oe(1005, 1023), oe(985, 1032), oe(985, 1032), oe(1005, 1023), oe(1035, 1008), oe(1030, 987),
			oe(1025, 981), oe(1030, 1002), oe(1000, 1017), oe(980, 1023), oe(980, 1023), oe(1000, 1017), oe(1030, 1002), oe(1025, 981),
			oe(1015, 965), oe(1020, 990), oe(990, 1002), oe(970, 1008), oe(970, 1008), oe(990, 1002), oe(1020, 990), oe(1015, 965),
			oe(1005, 942), oe(1010, 965), oe(980, 981), oe(960, 987), oe(960, 987), oe(980, 981), oe(1010, 965), oe(1005, 942)
	};

	public boolean debug = false;
	public boolean debugPawns = false;
	public StringBuffer debugSB;

	private int[] pcsq = {0, 0};
	private int[] positional = {0, 0};
	private int[] mobility = {0, 0};
	private int[] attacks = {0, 0};
	private int[] kingAttackersCount = {0, 0};
	private int[] kingSafety = {0, 0};
	private int[] pawnStructure = {0, 0};
	private int[] passedPawns = {0, 0};
	private long[] pawnCanAttack = {0, 0};
	private long[] mobilitySquares = {0, 0};
	private long[] kingZone = {0, 0}; // Squares surrounding King

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

		int endGameValue = Endgame.endGameValue(board, whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops, whiteRooks, blackRooks, whiteQueens, blackQueens);
		if (endGameValue != NO_VALUE) {
			return endGameValue;
		}

		pcsq[W] = ((board.whites & board.bishops & Square.WHITES) != 0 //
				&& (board.whites & board.bishops & Square.BLACKS) != 0 ? BISHOP_PAIR : 0);
		pcsq[B] = ((board.blacks & board.bishops & Square.WHITES) != 0 //
				&& (board.blacks & board.bishops & Square.BLACKS) != 0 ? BISHOP_PAIR : 0);

		positional[W] = 0;
		positional[B] = 0;
		mobility[W] = 0;
		mobility[B] = 0;
		kingAttackersCount[W] = 0;
		kingAttackersCount[B] = 0;
		kingSafety[W] = 0;
		kingSafety[B] = 0;
		pawnStructure[W] = 0;
		pawnStructure[B] = 0;
		passedPawns[W] = 0;
		passedPawns[B] = 0;

		mobilitySquares[W] = ~board.whites;
		mobilitySquares[B] = ~board.blacks;

		ai.build(board);

		// Squares that pawns attack or can attack by advancing
		pawnCanAttack[W] = ai.pawnAttacks[W];
		pawnCanAttack[B] = ai.pawnAttacks[B];
		long whitePawnsAux = board.pawns & board.whites;
		long blackPawnsAux = board.pawns & board.blacks;
		for (int i = 0; i < 5; i++) {
			whitePawnsAux = whitePawnsAux << 8;
			whitePawnsAux &= ~((board.pawns & board.blacks) | ai.pawnAttacks[B]); // Cannot advance because of a blocking pawn or a opposite pawn attack
			blackPawnsAux = blackPawnsAux >>> 8;
			blackPawnsAux &= ~((board.pawns & board.whites) | ai.pawnAttacks[W]); // Cannot advance because of a blocking pawn or a opposite pawn attack

			if (whitePawnsAux == 0 || blackPawnsAux == 0) {
				break;
			}
			pawnCanAttack[W] |= ((whitePawnsAux & ~BitboardUtils.b_l) << 9) | ((whitePawnsAux & ~BitboardUtils.b_r) << 7);
			pawnCanAttack[B] |= ((blackPawnsAux & ~BitboardUtils.b_r) >>> 9) | ((whitePawnsAux & ~BitboardUtils.b_l) >>> 7);
		}

		// Calculate attacks
		attacks[W] = evalAttacks(board, ai, W, board.blacks);
		attacks[B] = evalAttacks(board, ai, B, board.whites);

		// Squares surrounding King and three squares towards thew other side
		kingZone[W] = bbAttacks.king[ai.kingIndex[W]];
		kingZone[W] |= (kingZone[W] << 8);
		kingZone[B] = bbAttacks.king[ai.kingIndex[B]];
		kingZone[B] |= (kingZone[B] >> 8);

		long all = board.getAll();
		long pieceAttacks, pieceAttacksXray, safeAttacks, kingAttacks;
		boolean onlyKingsAndPawns = (board.knights | board.bishops | board.rooks | board.queens) == 0;

		long square = 1;
		for (int index = 0; index < 64; index++) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);
				int us = (isWhite ? W : B);
				int them = (isWhite ? B : W);
				long mines = (isWhite ? board.whites : board.blacks);
				long others = (isWhite ? board.blacks : board.whites);
				int pcsqIndex = (isWhite ? index : 63 - index);
				int rank = index >> 3;
				int file = 7 - index & 7;

				pieceAttacks = ai.attacksFromSquare[index];

				if ((square & board.pawns) != 0) {
					pcsq[us] += pawnPcsq[pcsqIndex];

					int relativeRank = isWhite ? rank : 7 - rank;
					long myPawns = board.pawns & mines;
					long otherPawns = board.pawns & others;
					long adjacentFiles = BitboardUtils.FILES_ADJACENT[file];
					long ranksForward = BitboardUtils.RANKS_FORWARD[us][rank];
					long pawnFile = BitboardUtils.FILE[file];
					long routeToPromotion = pawnFile & ranksForward;
					long otherPawnsAheadAdjacent = ranksForward & adjacentFiles & otherPawns;

					boolean supported = (square & ai.pawnAttacks[us]) != 0;
					boolean doubled = (myPawns & routeToPromotion) != 0;
					boolean opposed = (otherPawns & routeToPromotion) != 0;
					boolean passed = !doubled
							&& !opposed
							&& otherPawnsAheadAdjacent == 0;

					if (!passed) {
						long myPawnsAheadAdjacent = ranksForward & adjacentFiles & myPawns;
						long myPawnsBesideAndBehindAdjacent = BitboardUtils.RANK_AND_BACKWARD[us][rank] & adjacentFiles & myPawns;
						boolean isolated = (myPawns & adjacentFiles) == 0;
						boolean candidate = !doubled
								&& !opposed
								&& (((otherPawnsAheadAdjacent & ~pieceAttacks) == 0) || // Can become passer advancing
								(BitboardUtils.popCount(myPawnsBesideAndBehindAdjacent) >= BitboardUtils.popCount(otherPawnsAheadAdjacent & ~pieceAttacks))); // Has more friend pawns beside and behind than opposed pawns controlling his route to promotion
						boolean backward = !isolated
								&& !candidate
								&& myPawnsBesideAndBehindAdjacent == 0
								&& (pieceAttacks & otherPawns) == 0 // No backwards if it can capture
								&& (BitboardUtils.RANK_AND_BACKWARD[us][isWhite ? BitboardUtils.getRankLsb(myPawnsAheadAdjacent) : BitboardUtils.getRankMsb(myPawnsAheadAdjacent)] &
								routeToPromotion & (board.pawns | ai.pawnAttacks[them])) != 0; // Other pawns stopping it from advance, opposing or capturing it before reaching my pawns

						if (debugPawns) {
							boolean connected = ((bbAttacks.king[index] & adjacentFiles & myPawns) != 0);
							debugSB.append("PAWN " + BitboardUtils.SQUARE_NAMES[index] +
									(isWhite ? " WHITE " : " BLACK ") +
									(isolated ? "isolated " : "") +
									(supported ? "supported " : "") +
									(connected ? "connected " : "") +
									(doubled ? "doubled " : "") +
									(opposed ? "opposed " : "") +
									(candidate ? "candidate " : "") +
									(backward ? "backward " : "") +
									"\n"
							);
						}

						if (backward) {
							pawnStructure[us] -= PAWN_BACKWARDS[opposed ? 1 : 0];
						}
						if (isolated) {
							pawnStructure[us] -= PAWN_ISOLATED[opposed ? 1 : 0];
						}
						if (doubled) {
							pawnStructure[us] -= PAWN_DOUBLED[opposed ? 1 : 0];
						}
						if (!supported
								&& !isolated
								&& !backward) {
							pawnStructure[us] -= PAWN_UNSUPPORTED;
						}
						if (candidate) {
							passedPawns[us] += PAWN_CANDIDATE[relativeRank];
						}
						// Pawn Storm: It can open a file near the king
						if ((routeToPromotion & kingZone[them]) != 0) {
							pawnStructure[us] += PAWN_STORM[relativeRank];
						}
						// There is an opposite rook attacking this weak pawn
						if ((backward || isolated) && !opposed && (routeToPromotion & board.rooks & others) != 0) {
							positional[them] += ROOK_FILE_SEMIOPEN_WEAK_PAWN;
						}
					} else {
						//
						// Passed Pawn
						//
						long backFile = BitboardUtils.FILE[file] & BitboardUtils.RANKS_BACKWARD[us][rank];
						// If it has a rook or queen behind consider all the route to promotion attacked or defended
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
							debugSB.append("PAWN " + BitboardUtils.SQUARE_NAMES[index] +
									(isWhite ? " WHITE " : " BLACK ") +
									"passed " +
									(outside ? "outside " : "") +
									(connected ? "connected " : "") +
									(supported ? "supported " : "") +
									(mobile ? "mobile " : "") +
									(runner ? "runner " : "") +
									"\n"
							);
						}

						passedPawns[us] += PAWN_PASSER[relativeRank];

						if (outside) {
							passedPawns[us] += PAWN_PASSER_OUTSIDE[relativeRank];
						}
						if (supported) {
							passedPawns[us] += PAWN_PASSER_SUPPORTED[relativeRank];
						} else if (connected) {
							passedPawns[us] += PAWN_PASSER_CONNECTED[relativeRank];
						}
						if (runner) {
							passedPawns[us] += PAWN_PASSER_RUNNER[relativeRank];
						} else if (mobile) {
							passedPawns[us] += PAWN_PASSER_MOBILE[relativeRank];
						}

						if (onlyKingsAndPawns && runner) {
							long promotionSquare = routeToPromotion & (isWhite ? BitboardUtils.RANK[7] : BitboardUtils.RANK[0]);
							if ((ai.kingAttacks[us] & promotionSquare) != 0 // The king controls the promotion square
									&& (ai.kingAttacks[us] & square) != 0) {
								passedPawns[us] += PAWN_PASSER_UNSTOPPABLE;
							} else {
								// Simple pawn square rule implementation
								int ranksToPromo = 7 - relativeRank +
										(relativeRank == 1 ? -1 : 0); // The pawn can advance two squares
								int kingToPromo = BitboardUtils.distance(BitboardUtils.square2Index(promotionSquare), ai.kingIndex[them]) +
										(isWhite != board.getTurn() ? -1 : 0); // The other king can move first
								if (kingToPromo > ranksToPromo) {
									passedPawns[us] += PAWN_PASSER_UNSTOPPABLE;
								}
							}
						}
					}
					// Pawn is part of the king shield
					if ((pawnFile & ~ranksForward & kingZone[us]) != 0) {
						pawnStructure[us] += PAWN_SHIELD[relativeRank];
					}

				} else if ((square & board.knights) != 0) {
					pcsq[us] += knightPcsq[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them];

					mobility[us] += MOBILITY[Piece.KNIGHT][BitboardUtils.popCount(safeAttacks & mobilitySquares[us] & BitboardUtils.RANKS_FORWARD[us][rank])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.KNIGHT] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					// Knight outpost: no opposite pawns can attack the square
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them]) != 0) {
						positional[us] += KNIGHT_OUTPOST;
						// Defended by one of our pawns
						if ((square & ai.pawnAttacks[us]) != 0) {
							positional[us] += KNIGHT_OUTPOST;
							// Attacks squares near king or other pieces pawn undefended
							if ((safeAttacks & (kingZone[them] | others)) != 0) {
								positional[us] += KNIGHT_OUTPOST_ATTACKING[pcsqIndex];
							}
						}
					}

				} else if ((square & board.bishops) != 0) {
					pcsq[us] += bishopPcsq[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them];

					mobility[us] += MOBILITY[Piece.BISHOP][BitboardUtils.popCount(safeAttacks & mobilitySquares[us] & BitboardUtils.RANKS_FORWARD[us][rank])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.BISHOP] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					pieceAttacksXray = bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

					// Bishop Outpost: no opposite pawns can attack the square and defended by one of our pawns
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them] & ai.pawnAttacks[us]) != 0) {
						positional[us] += BISHOP_OUTPOST;
						// Attacks squares near king or other pieces pawn undefended
						if ((safeAttacks & (kingZone[them] | others)) != 0) {
							positional[us] += BISHOP_OUTPOST_ATTACKING;
						}
					}

					positional[us] -= BISHOP_MY_PAWNS_IN_COLOR_PENALTY * BitboardUtils.popCount(board.pawns & mines & BitboardUtils.getSameColorSquares(square));

					if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0) {
						mobility[us] -= BISHOP_TRAPPED_PENALTY;
					}

				} else if ((square & board.rooks) != 0) {
					pcsq[us] += rookPcsq[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them] & ~ai.knightAttacks[them] & ~ai.bishopAttacks[them];

					mobility[us] += MOBILITY[Piece.ROOK][BitboardUtils.popCount(safeAttacks & mobilitySquares[us])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.ROOK] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					pieceAttacksXray = bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

					// Rook Outpost: no opposite pawns can attack the square and defended by one of our pawns
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them] & ai.pawnAttacks[us]) != 0) {
						positional[us] += ROOK_OUTPOST;
						// Attacks squares near king or other pieces pawn undefended
						if ((safeAttacks & (kingZone[them] | others)) != 0) {
							positional[us] += ROOK_OUTPOST_ATTACKING;
						}
					}

					int relativeRank = isWhite ? rank : 7 - rank;
					if (relativeRank >= 4) {
						long pawnsAttacked = pieceAttacks & BitboardUtils.RANK[rank] & board.pawns & others & ~ai.pawnAttacks[them];
						if (pawnsAttacked != 0) {
							positional[us] += ROOK_7 * BitboardUtils.popCount(pawnsAttacked);
						}
					}

					long rookFile = BitboardUtils.FILE[file] & BitboardUtils.RANKS_FORWARD[us][rank];
					if ((rookFile & board.pawns & mines) == 0) {
						positional[us] += ROOK_FILE_SEMIOPEN;
						if ((rookFile & board.pawns) == 0) {
							long minorPiecesDefendedByPawns = others & (board.bishops | board.knights) & ai.pawnAttacks[them];
							if ((rookFile & minorPiecesDefendedByPawns) == 0) {
								positional[us] += ROOK_FILE_OPEN_NO_MG;
							} else {
								if ((rookFile & minorPiecesDefendedByPawns & pawnCanAttack[us]) == 0) {
									positional[us] += ROOK_FILE_OPEN_MG_NP;
								} else {
									positional[us] += ROOK_FILE_OPEN_MG_P;
								}
							}
						}

						if ((rookFile & board.kings & others) != 0) {
							positional[us] += ROOK_FILE_SEMIOPEN_K;
						}
					}

				} else if ((square & board.queens) != 0) {
					pcsq[us] += queenPcsq[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them] & ~ai.knightAttacks[them] & ~ai.bishopAttacks[them] & ~ai.rookAttacks[them];

					mobility[us] += MOBILITY[Piece.QUEEN][BitboardUtils.popCount(safeAttacks & mobilitySquares[us])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.QUEEN] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					pieceAttacksXray = (bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) |
							bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)))
							& ~pieceAttacks;
					if ((pieceAttacksXray & board.kings & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

				} else if ((square & board.kings) != 0) {
					pcsq[us] += kingPcsq[pcsqIndex];
				}
			}
			square <<= 1;
		}

		int oe = (board.getTurn() ? TEMPO : -TEMPO)
				+ pcsq[W] - pcsq[B]
				+ positional[W] - positional[B]
				+ attacks[W] - attacks[B]
				+ mobility[W] - mobility[B]
				+ pawnStructure[W] - pawnStructure[B]
				+ passedPawns[W] - passedPawns[B]
				+ oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B]);

		// Ponder opening and Endgame value depending of the non-pawn pieces:
		// opening=> gamephase = 256 / ending => gamephase = 0
		int nonPawnMaterial = (whiteKnights + blackKnights) * KNIGHT +
				(whiteBishops + blackBishops) * BISHOP +
				(whiteRooks + blackRooks) * ROOK +
				(whiteQueens + blackQueens) * QUEEN;
		int gamePhase = nonPawnMaterial >= NON_PAWN_MATERIAL_MIDGAME_MAX ? 256 :
				nonPawnMaterial <= NON_PAWN_MATERIAL_ENDGAME_MIN ? 0 :
						((nonPawnMaterial - NON_PAWN_MATERIAL_ENDGAME_MIN) << 8) / (NON_PAWN_MATERIAL_MIDGAME_MAX - NON_PAWN_MATERIAL_ENDGAME_MIN);
		int value = (gamePhase * o(oe) + (256 - gamePhase) * e(oe)) >> 8; // divide by 256

		if (debug) {
			logger.debug(debugSB);
			logger.debug("                    WOpening WEndgame BOpening BEndgame");
			logger.debug("pcsq              = " + formatOE(pcsq[W]) + " " + formatOE(pcsq[B]));
			logger.debug("mobility          = " + formatOE(mobility[W]) + " " + formatOE(mobility[B]));
			logger.debug("positional        = " + formatOE(positional[W]) + " " + formatOE(positional[B]));
			logger.debug("pawnStructure     = " + formatOE(pawnStructure[W]) + " " + formatOE(pawnStructure[B]));
			logger.debug("passedPawns       = " + formatOE(passedPawns[W]) + " " + formatOE(passedPawns[B]));
			logger.debug("attacks           = " + formatOE(attacks[W]) + " " + formatOE(attacks[B]));
			logger.debug("kingSafety        = " + formatOE(oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W])) + " " + formatOE(oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B])));
			logger.debug("tempo             = " + formatOE(board.getTurn() ? TEMPO : -TEMPO));
			logger.debug("                    -----------------");
			logger.debug("TOTAL:              " + formatOE(oe));
			logger.debug("gamePhase = " + gamePhase + " => value = " + value);
		}
		assert Math.abs(value) < KNOWN_WIN : "Eval is outside limits";
		return value;
	}

	private int evalAttacks(Board board, AttacksInfo ai, int us, long others) {
		int attacks = 0;

		long attackedByPawn = ai.pawnAttacks[us] & others & ~board.pawns;
		while (attackedByPawn != 0) {
			long lsb = BitboardUtils.lsb(attackedByPawn);
			attacks += PAWN_ATTACKS[board.getPieceIntAt(lsb)];
			attackedByPawn &= ~lsb;
		}

		long otherWeak = ai.attackedSquares[us] & others & ~ai.pawnAttacks[1 - us];
		if (otherWeak != 0) {
			long attackedByMinor = (ai.knightAttacks[us] | ai.bishopAttacks[us]) & otherWeak;
			while (attackedByMinor != 0) {
				long lsb = BitboardUtils.lsb(attackedByMinor);
				attacks += MINOR_ATTACKS[board.getPieceIntAt(lsb)];
				attackedByMinor &= ~lsb;
			}
			long attackedByMajor = (ai.rookAttacks[us] | ai.queenAttacks[us]) & otherWeak;
			while (attackedByMajor != 0) {
				long lsb = BitboardUtils.lsb(attackedByMajor);
				attacks += MAJOR_ATTACKS[board.getPieceIntAt(lsb)];
				attackedByMajor &= ~lsb;
			}
		}

		long superiorAttacks = ai.pawnAttacks[us] & others & ~board.pawns
				| (ai.knightAttacks[us] | ai.bishopAttacks[us]) & others & (board.rooks | board.queens)
				| ai.rookAttacks[us] & others & board.queens;
		int superiorAttacksCount = BitboardUtils.popCount(superiorAttacks);
		if (superiorAttacksCount >= 2) {
			attacks += superiorAttacksCount * HUNG_PIECES;
		}

		return attacks;
	}
}