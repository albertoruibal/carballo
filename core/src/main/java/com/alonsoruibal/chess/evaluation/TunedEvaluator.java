package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.Square;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

public class TunedEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger(TunedEvaluator.class.getName());

	public final int[] PAWN_BACKWARDS_PENALTY = {oe(27, 14), oe(8, 8)};
	public final int[] PAWN_ISOLATED_PENALTY = {oe(36, 10), oe(12, 8)};
	public final int[] PAWN_DOUBLED_PENALTY = {oe(9, 18), oe(14, 17)};
	public int PAWN_UNSUPPORTED_PENALTY = oe(13, 0);
	public final int[] PAWN_CANDIDATE = {0, oe(-9, -6), oe(8, 9), oe(11, 16), oe(17, 25), oe(16, 77), oe(99, 112), 0};
	public final int[] PAWN_PASSER = {0, oe(-12, 13), oe(-23, 11), oe(-20, 23), oe(7, 30), oe(30, 68), oe(76, 77), 0};
	public final int[] PAWN_PASSER_OUTSIDE = {0, oe(-21, -4), oe(40, 6), oe(22, 15), oe(-4, 6), oe(26, -16), oe(68, 36), 0};
	public final int[] PAWN_PASSER_CONNECTED = {0, oe(11, -10), oe(0, 11), oe(12, 12), oe(29, 29), oe(100, 49), oe(-33, 166), 0};
	public final int[] PAWN_PASSER_SUPPORTED = {0, 0, oe(20, 0), oe(10, 8), oe(6, 26), oe(107, 3), oe(196, -12), 0};
	public final int[] PAWN_PASSER_MOBILE = {0, oe(5, 6), oe(3, 8), oe(16, 16), oe(19, 23), oe(24, 25), oe(69, 69), 0};
	public final int[] PAWN_PASSER_RUNNER = {0, oe(-12, 29), oe(-22, 26), oe(-21, 41), oe(2, 57), oe(32, 93), oe(60, 157), 0};
	public final int[] PAWN_PASSER_OTHER_KING_DISTANCE = {0, 0, oe(-5, -1), oe(0, 8), oe(-3, 16), oe(-2, 28), oe(16, 40), 0};
	public final int[] PAWN_PASSER_MY_KING_DISTANCE = {0, 0, oe(-2, 4), oe(2, 13), oe(-1, 13), oe(4, 17), oe(12, 14), 0};
	public final int[] PAWN_SHIELD_CENTER = {0, oe(58, -21), oe(50, -11), oe(14, -1), oe(34, 8), oe(-20, 35), oe(-31, 78), 0};
	public final int[] PAWN_SHIELD = {0, oe(37, -14), oe(42, -9), oe(3, 0), oe(-2, 17), oe(-48, 31), oe(16, 44), 0};
	public final int[] PAWN_STORM_CENTER = {0, 0, 0, oe(-12, -5), oe(-3, -4), oe(35, -4), 0, 0};
	public final int[] PAWN_STORM = {0, 0, 0, oe(-5, -8), oe(2, -17), oe(74, -45), 0, 0};
	public final int[] PAWN_PCSQ = {
			0, 0, 0, 0, 0, 0, 0, 0,
			oe(-37, 1), oe(-8, 5), oe(4, 5), oe(-4, 11), oe(-4, -28), oe(-28, 5), oe(-18, 10), oe(-24, 7),
			oe(-39, -6), oe(-20, -5), oe(-28, -1), oe(-3, -9), oe(-4, -7), oe(-36, 3), oe(-29, -3), oe(-31, 5),
			oe(-4, -6), oe(-9, 2), oe(6, 0), oe(-8, -5), oe(-20, -5), oe(-19, 4), oe(-28, 4), oe(-12, 11),
			oe(33, 1), oe(-3, 15), oe(5, 0), oe(-4, 3), oe(-16, -11), oe(-18, 6), oe(-7, 8), oe(-2, 21),
			oe(62, 17), oe(20, 20), oe(77, 13), oe(22, 14), oe(36, -1), oe(15, -2), oe(13, 22), oe(5, 21),
			oe(-18, -98), oe(-64, -32), oe(-41, -13), oe(51, 6), oe(55, 1), oe(-19, -8), oe(37, -20), oe(-24, -77),
			0, 0, 0, 0, 0, 0, 0, 0
	};
	public final int[] KNIGHT_OUTPOST = {oe(14, 18), oe(7, 27)};
	public final int[] KNIGHT_MOBILITY = {oe(-22, -36), oe(-10, -22), oe(-1, -9), oe(5, 4), oe(10, 12), oe(15, 16), oe(19, 18), oe(23, 19), oe(26, 19)};
	public final int[] KNIGHT_PCSQ = {
			oe(-70, -72), oe(-86, -69), oe(-16, -29), oe(-9, -36), oe(-37, -46), oe(-41, -60), oe(-46, -56), oe(-20, -98),
			oe(-10, -22), oe(-12, -46), oe(-1, -45), oe(-21, -33), oe(-1, -34), oe(-22, -40), oe(-32, -70), oe(-42, -63),
			oe(-11, -64), oe(-15, -31), oe(-9, -35), oe(9, -27), oe(0, -23), oe(-19, -39), oe(-25, -44), oe(-43, -57),
			oe(2, -34), oe(7, -33), oe(7, -48), oe(-3, -31), oe(-3, -35), oe(3, -42), oe(-14, -59), oe(-19, -58),
			oe(30, -30), oe(4, -40), oe(15, -30), oe(4, -38), oe(31, -41), oe(3, -43), oe(-20, -49), oe(5, -31),
			oe(-35, -51), oe(10, -55), oe(83, -42), oe(17, -44), oe(-15, -43), oe(5, -48), oe(-34, -40), oe(-34, -83),
			oe(-66, -56), oe(-8, -61), oe(18, -49), oe(13, -49), oe(-31, -19), oe(-34, -30), oe(-73, -22), oe(-66, -45),
			oe(-170, -71), oe(-30, -13), oe(-141, -53), oe(-19, -59), oe(-6, -43), oe(-52, -38), oe(-126, -46), oe(-154, -77)
	};
	public final int[] BISHOP_OUTPOST = {oe(21, 8), oe(32, 14)};
	public int BISHOP_MY_PAWNS_IN_COLOR_PENALTY = oe(8, 8);
	public final int[] BISHOP_TRAPPED_BY_OPPOSITE_PAWN_PENALTY = {oe(160, 26), oe(223, 5)};
	public final int[] BISHOP_MOBILITY = {oe(-16, -32), oe(-7, -10), oe(0, 0), oe(5, 6), oe(8, 11), oe(11, 16), oe(13, 19), oe(16, 20), oe(18, 20), oe(20, 21), oe(23, 21), oe(25, 21), oe(27, 21), oe(27, 21)};
	public final int[] BISHOP_PCSQ = {
			oe(-22, -56), oe(7, 1), oe(21, -4), oe(14, 8), oe(-2, -6), oe(-2, 10), oe(-26, -25), oe(12, -27),
			oe(32, 2), oe(25, -2), oe(37, 0), oe(12, 1), oe(22, 11), oe(36, -14), oe(10, 6), oe(-16, 0),
			oe(19, -18), oe(26, 21), oe(28, 2), oe(18, 19), oe(14, 3), oe(26, 18), oe(32, -9), oe(1, 5),
			oe(19, 15), oe(8, -13), oe(10, 13), oe(20, -6), oe(29, 10), oe(14, 0), oe(-2, 13), oe(-2, 4),
			oe(-18, -10), oe(18, 17), oe(6, -12), oe(13, 20), oe(17, 1), oe(2, 7), oe(-1, -1), oe(-19, 8),
			oe(8, 29), oe(-23, 0), oe(16, 14), oe(-4, 0), oe(-19, 13), oe(-31, 2), oe(-1, -2), oe(16, -1),
			oe(-42, -14), oe(-45, 17), oe(8, 6), oe(-20, 14), oe(-25, 11), oe(-31, 17), oe(-23, 3), oe(-40, 12),
			oe(16, 14), oe(-89, -6), oe(-11, 3), oe(6, -5), oe(17, 11), oe(8, -8), oe(-18, 12), oe(-28, 8)
	};
	public final int[] ROOK_OUTPOST = {oe(24, 17), oe(35, 18)};
	public final int[] ROOK_FILE = {oe(33, 5), oe(13, 0)};
	public int ROOK_7 = oe(5, 0);
	public final int[] ROOK_TRAPPED_BY_OWN_KING_PENALTY = {oe(102, -1), oe(14, 25), oe(29, 12), oe(-5, -25)};
	public final int[] ROOK_MOBILITY = {oe(4, -49), oe(5, -10), oe(1, 5), oe(3, 9), oe(2, 17), oe(5, 22), oe(6, 26), oe(9, 28), oe(16, 30), oe(17, 30), oe(15, 33), oe(29, 31), oe(41, 27), oe(52, 29), oe(129, -3)};
	public final int[] ROOK_PCSQ = {
			oe(8, 23), oe(41, 20), oe(12, 28), oe(19, 32), oe(14, 37), oe(2, 32), oe(9, 34), oe(3, 37),
			oe(13, 10), oe(5, 23), oe(15, 17), oe(-1, 30), oe(-10, 40), oe(-15, 30), oe(15, 22), oe(-25, 33),
			oe(-6, 0), oe(4, 25), oe(-7, 34), oe(-4, 37), oe(-9, 43), oe(-8, 44), oe(-8, 35), oe(-30, 40),
			oe(-21, 28), oe(-41, 26), oe(-26, 41), oe(-35, 33), oe(-22, 38), oe(2, 25), oe(-35, 39), oe(-27, 44),
			oe(12, 42), oe(-11, 33), oe(29, 31), oe(8, 34), oe(22, 33), oe(-24, 37), oe(3, 43), oe(-16, 56),
			oe(56, 37), oe(81, 21), oe(35, 29), oe(55, 38), oe(36, 34), oe(6, 36), oe(10, 30), oe(0, 53),
			oe(54, 46), oe(10, 59), oe(86, 48), oe(43, 61), oe(54, 57), oe(29, 57), oe(11, 57), oe(26, 52),
			oe(23, 49), oe(-2, 63), oe(125, 57), oe(3, 78), oe(11, 68), oe(20, 69), oe(47, 65), oe(-18, 55)
	};
	public final int[] QUEEN_MOBILITY = {oe(-16, -12), oe(1, -13), oe(13, -3), oe(17, 2), oe(14, 25), oe(16, 51), oe(21, 43), oe(17, 54), oe(17, 59), oe(23, 55), oe(24, 62), oe(29, 59), oe(20, 65), oe(33, 56), oe(61, 37), oe(61, 40), oe(73, 38), oe(79, 31), oe(88, 23), oe(95, 21), oe(82, 24), oe(85, 33), oe(33, 44), oe(20, 37), oe(7, 48), oe(28, 71), oe(33, 67), oe(35, 109)};
	public final int[] QUEEN_PCSQ = {
			oe(37, -41), oe(-19, -1), oe(8, -29), oe(14, -3), oe(11, 15), oe(4, 2), oe(23, -30), oe(31, -14),
			oe(35, -18), oe(6, 14), oe(11, 10), oe(15, 16), oe(13, 22), oe(13, 27), oe(17, 13), oe(3, 28),
			oe(13, 10), oe(8, 13), oe(8, 34), oe(13, 18), oe(8, 22), oe(6, 38), oe(-3, 32), oe(-2, 39),
			oe(16, 40), oe(18, 25), oe(12, 43), oe(20, 28), oe(16, 46), oe(18, 32), oe(-17, 47), oe(4, 6),
			oe(33, 27), oe(-9, 46), oe(24, 23), oe(24, 56), oe(5, 46), oe(1, 52), oe(-4, 45), oe(-7, 32),
			oe(47, 49), oe(58, 14), oe(91, 52), oe(44, 53), oe(32, 45), oe(13, 43), oe(-18, 64), oe(22, 44),
			oe(57, 25), oe(5, 46), oe(49, 59), oe(-6, 65), oe(41, 36), oe(-3, 63), oe(-19, 45), oe(19, 12),
			oe(29, 32), oe(29, 28), oe(-8, 41), oe(-1, 20), oe(15, 35), oe(-3, 20), oe(-24, 48), oe(-68, 44)
	};
	public final int[] KING_PCSQ = {
			oe(22, -18), oe(24, -17), oe(-21, -19), oe(58, -39), oe(-23, -35), oe(19, -27), oe(24, -25), oe(-8, -7),
			oe(49, -14), oe(22, -3), oe(30, -8), oe(40, -12), oe(39, -20), oe(49, -24), oe(64, -15), oe(80, -22),
			oe(-55, -2), oe(49, 3), oe(31, -5), oe(80, -3), oe(67, -6), oe(20, -12), oe(139, -21), oe(-9, -27),
			oe(-11, 3), oe(12, 9), oe(30, 11), oe(63, 3), oe(81, 6), oe(8, 1), oe(72, -2), oe(68, -15),
			oe(56, 30), oe(10, 29), oe(27, 24), oe(38, 7), oe(109, -2), oe(30, 12), oe(74, 18), oe(132, 5),
			oe(121, 53), oe(100, 69), oe(48, 52), oe(-1, 11), oe(0, 19), oe(101, 33), oe(62, 31), oe(5, 5),
			oe(-66, 32), oe(-34, 48), oe(31, 55), oe(-33, 25), oe(-85, 28), oe(40, 35), oe(81, 26), oe(-12, -7),
			oe(-59, -37), oe(41, 29), oe(-141, 39), oe(-146, 9), oe(-154, 40), oe(-80, 18), oe(28, 5), oe(-166, -57)
	};
	public final int[] PAWN_ATTACKS = {0, 0, oe(46, 26), oe(43, 49), oe(60, 26), oe(44, 36), 0};
	public final int[] MINOR_ATTACKS = {0, oe(2, 20), oe(31, 22), oe(27, 22), oe(53, 24), oe(40, 38), 0};
	public final int[] MAJOR_ATTACKS = {0, oe(0, 19), oe(11, 21), oe(15, 20), oe(0, 14), oe(36, 21), 0};
	public int HUNG_PIECES = oe(45, 84);
	public int PINNED_PIECE = oe(26, -24);
	public final int[] PIECE_ATTACKS_KING = {0, 0, oe(19, 0), oe(13, 0), oe(33, 0), oe(40, 0)};
	public int SPACE = oe(6, 0);
	public int TEMPO = oe(5, 0);

	// Sums for each piece attacking an square near the king
	// Ponder kings attacks by the number of attackers (not pawns)
	private static final int[] KING_SAFETY_PONDER = {0, 0, 32, 48, 56, 60, 62, 63, 64, 64, 64, 64, 64, 64, 64, 64};

	private static final long WHITE_SPACE_ZONE = (BitboardUtils.C | BitboardUtils.D | BitboardUtils.E | BitboardUtils.F) &
			(BitboardUtils.R2 | BitboardUtils.R3 | BitboardUtils.R4);
	private static final long BLACK_SPACE_ZONE = (BitboardUtils.C | BitboardUtils.D | BitboardUtils.E | BitboardUtils.F) &
			(BitboardUtils.R5 | BitboardUtils.R6 | BitboardUtils.R7);

	private static final long[] OUTPOST_MASK = {0x00007e7e7e000000L, 0x0000007e7e7e0000L};

	// Indexed by bishop position, contains the square where an opponent pawn can trap the bishop
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
	// Indexed by bishop position, contains the square where other opponent pawn defends the trapping pawn
	private static final long[] BISHOP_TRAPPING_GUARD = {
			0, 0, 0, 0, 0, 0, 0, 0,
			Square.F2, 0, 0, 0, 0, 0, 0, Square.C2,
			Square.F3, 0, 0, 0, 0, 0, 0, Square.C3,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			Square.F6, 0, 0, 0, 0, 0, 0, Square.C6,
			Square.F7, 0, 0, 0, 0, 0, 0, Square.C7,
			0, 0, 0, 0, 0, 0, 0, 0
	};
	// Indexed by own king position, contains the squares where a rook may be trapped by the king
	private static final long[] ROOK_TRAPPING = {
			0, Square.H1 | Square.H2, Square.H1 | Square.H2 | Square.G1 | Square.G2, 0,
			0, Square.A1 | Square.A2 | Square.B1 | Square.B2, Square.A1 | Square.A2, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, Square.H7 | Square.H8, Square.H7 | Square.H8 | Square.G7 | Square.G8, 0,
			0, Square.A7 | Square.A8 | Square.B7 | Square.B8, Square.A7 | Square.A8, 0,
	};

	public boolean debug = false;
	public boolean debugPawns = false;
	public StringBuffer debugSB;

	private final int[] scaleFactor = {0};

	private final int[] pawnMaterial = {0, 0};
	private final int[] nonPawnMaterial = {0, 0};
	private final int[] pcsq = {0, 0};
	private final int[] space = {0, 0};
	private final int[] positional = {0, 0};
	private final int[] mobility = {0, 0};
	private final int[] attacks = {0, 0};
	private final int[] kingAttackersCount = {0, 0};
	private final int[] kingSafety = {0, 0};
	private final int[] pawnStructure = {0, 0};
	private final int[] passedPawns = {0, 0};
	private final long[] pawnCanAttack = {0, 0};
	private final long[] mobilitySquares = {0, 0};
	private final long[] kingZone = {0, 0}; // Squares surrounding King

	public int evaluate(Board board, AttacksInfo ai) {
		if (debug) {
			debugSB = new StringBuffer();
			debugSB.append("\n");
			debugSB.append(board.toString());
			debugSB.append("\n");
		}

		int whitePawns = Long.bitCount(board.pawns & board.whites);
		int blackPawns = Long.bitCount(board.pawns & board.blacks);
		int whiteKnights = Long.bitCount(board.knights & board.whites);
		int blackKnights = Long.bitCount(board.knights & board.blacks);
		int whiteBishops = Long.bitCount(board.bishops & board.whites);
		int blackBishops = Long.bitCount(board.bishops & board.blacks);
		int whiteRooks = Long.bitCount(board.rooks & board.whites);
		int blackRooks = Long.bitCount(board.rooks & board.blacks);
		int whiteQueens = Long.bitCount(board.queens & board.whites);
		int blackQueens = Long.bitCount(board.queens & board.blacks);

		int endgameValue = Endgame.evaluateEndgame(board, scaleFactor, whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops, whiteRooks, blackRooks, whiteQueens, blackQueens);
		if (endgameValue != NO_VALUE) {
			return endgameValue;
		}

		pawnMaterial[W] = whitePawns * PIECE_VALUES_OE[Piece.PAWN];
		nonPawnMaterial[W] = whiteKnights * PIECE_VALUES_OE[Piece.KNIGHT] +
				whiteBishops * PIECE_VALUES_OE[Piece.BISHOP] +
				whiteRooks * PIECE_VALUES_OE[Piece.ROOK] +
				whiteQueens * PIECE_VALUES_OE[Piece.QUEEN] +
				((board.whites & board.bishops & Square.WHITES) != 0
						&& (board.whites & board.bishops & Square.BLACKS) != 0 ? BISHOP_PAIR : 0);
		pawnMaterial[B] = blackPawns * PIECE_VALUES_OE[Piece.PAWN];
		nonPawnMaterial[B] = blackKnights * PIECE_VALUES_OE[Piece.KNIGHT] +
				blackBishops * PIECE_VALUES_OE[Piece.BISHOP] +
				blackRooks * PIECE_VALUES_OE[Piece.ROOK] +
				blackQueens * PIECE_VALUES_OE[Piece.QUEEN] +
				((board.blacks & board.bishops & Square.WHITES) != 0
						&& (board.blacks & board.bishops & Square.BLACKS) != 0 ? BISHOP_PAIR : 0);

		int nonPawnMat = e(nonPawnMaterial[W] + nonPawnMaterial[B]);
		int gamePhase = nonPawnMat >= NON_PAWN_MATERIAL_MIDGAME_MAX ? GAME_PHASE_MIDGAME :
				nonPawnMat <= NON_PAWN_MATERIAL_ENDGAME_MIN ? GAME_PHASE_ENDGAME :
						((nonPawnMat - NON_PAWN_MATERIAL_ENDGAME_MIN) * GAME_PHASE_MIDGAME) / (NON_PAWN_MATERIAL_MIDGAME_MAX - NON_PAWN_MATERIAL_ENDGAME_MIN);

		pcsq[W] = 0;
		pcsq[B] = 0;
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

		long whitePawnsAux = board.pawns & board.whites;
		long blackPawnsAux = board.pawns & board.blacks;

		// Space evaluation
		if (gamePhase > 0) {
			long whiteSafe = WHITE_SPACE_ZONE & ~ai.pawnAttacks[B] & (~ai.attackedSquares[B] | ai.attackedSquares[W]);
			long blackSafe = BLACK_SPACE_ZONE & ~ai.pawnAttacks[W] & (~ai.attackedSquares[W] | ai.attackedSquares[B]);

			long whiteBehindPawn = ((whitePawnsAux >>> 8) | (whitePawnsAux >>> 16) | (whitePawnsAux >>> 24));
			long blackBehindPawn = ((blackPawnsAux << 8) | (blackPawnsAux << 16) | (blackPawnsAux << 24));

			space[W] = SPACE * (((Long.bitCount(whiteSafe) + Long.bitCount(whiteSafe & whiteBehindPawn)) *
					(whiteKnights + whiteBishops)) / 4);
			space[B] = SPACE * (((Long.bitCount(blackSafe) + Long.bitCount(blackSafe & blackBehindPawn)) *
					(blackKnights + blackBishops)) / 4);
		} else {
			space[W] = 0;
			space[B] = 0;
		}

		// Squares that pawns attack or can attack by advancing
		pawnCanAttack[W] = ai.pawnAttacks[W];
		pawnCanAttack[B] = ai.pawnAttacks[B];
		for (int i = 0; i < 5; i++) {
			whitePawnsAux = whitePawnsAux << 8;
			whitePawnsAux &= ~((board.pawns & board.blacks) | ai.pawnAttacks[B]); // Cannot advance because of a blocking pawn or a opposite pawn attack
			blackPawnsAux = blackPawnsAux >>> 8;
			blackPawnsAux &= ~((board.pawns & board.whites) | ai.pawnAttacks[W]); // Cannot advance because of a blocking pawn or a opposite pawn attack

			if (whitePawnsAux == 0 && blackPawnsAux == 0) {
				break;
			}
			pawnCanAttack[W] |= ((whitePawnsAux & ~BitboardUtils.b_l) << 9) | ((whitePawnsAux & ~BitboardUtils.b_r) << 7);
			pawnCanAttack[B] |= ((blackPawnsAux & ~BitboardUtils.b_r) >>> 9) | ((blackPawnsAux & ~BitboardUtils.b_l) >>> 7);
		}

		// Calculate attacks
		attacks[W] = evalAttacks(board, ai, W, board.blacks);
		attacks[B] = evalAttacks(board, ai, B, board.whites);

		// Squares surrounding King and three squares towards thew other side
		kingZone[W] = bbAttacks.king[ai.kingIndex[W]];
		kingZone[W] |= (kingZone[W] << 8);
		kingZone[B] = bbAttacks.king[ai.kingIndex[B]];
		kingZone[B] |= (kingZone[B] >>> 8);

		long all = board.getAll();
		long pieceAttacks, safeAttacks, kingAttacks;

		long square = 1;
		for (int index = 0; index < 64; index++) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);
				int us = isWhite ? W : B;
				int them = isWhite ? B : W;
				long mines = isWhite ? board.whites : board.blacks;
				long others = isWhite ? board.blacks : board.whites;
				int pcsqIndex = isWhite ? index : 63 - (index & ~0x7) - (7 - index & 0x7);
				int rank = index >> 3;
				int relativeRank = isWhite ? rank : 7 - rank;
				int file = 7 - index & 7;

				pieceAttacks = ai.attacksFromSquare[index];

				if ((square & board.pawns) != 0) {
					pcsq[us] += PAWN_PCSQ[pcsqIndex];

					long myPawns = board.pawns & mines;
					long otherPawns = board.pawns & others;
					long adjacentFiles = BitboardUtils.FILES_ADJACENT[file];
					long ranksForward = BitboardUtils.RANKS_FORWARD[us][rank];
					long pawnFile = BitboardUtils.FILE[file];
					long routeToPromotion = pawnFile & ranksForward;
					long otherPawnsAheadAdjacent = ranksForward & adjacentFiles & otherPawns;
					long pushSquare = isWhite ? square << 8 : square >>> 8;

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
								(Long.bitCount(myPawnsBesideAndBehindAdjacent) >= Long.bitCount(otherPawnsAheadAdjacent & ~pieceAttacks))); // Has more friend pawns beside and behind than opposed pawns controlling his route to promotion
						boolean backward = !isolated
								&& !candidate
								&& myPawnsBesideAndBehindAdjacent == 0
								&& (pieceAttacks & otherPawns) == 0 // No backwards if it can capture
								&& (BitboardUtils.RANK_AND_BACKWARD[us][isWhite ? BitboardUtils.getRankLsb(myPawnsAheadAdjacent) : BitboardUtils.getRankMsb(myPawnsAheadAdjacent)] &
								routeToPromotion & (board.pawns | ai.pawnAttacks[them])) != 0; // Other pawns stopping it from advance, opposing or capturing it before reaching my pawns

						if (debugPawns) {
							boolean connected = ((bbAttacks.king[index] & adjacentFiles & myPawns) != 0);
							debugSB.append("PAWN ")
									.append(BitboardUtils.SQUARE_NAMES[index])
									.append(isWhite ? " WHITE " : " BLACK ")
									.append(isolated ? "isolated " : "")
									.append(supported ? "supported " : "")
									.append(connected ? "connected " : "")
									.append(doubled ? "doubled " : "")
									.append(opposed ? "opposed " : "")
									.append(candidate ? "candidate " : "")
									.append(backward ? "backward " : "")
									.append("\n");
						}

						if (backward) {
							pawnStructure[us] -= PAWN_BACKWARDS_PENALTY[opposed ? 1 : 0];
						}
						if (isolated) {
							pawnStructure[us] -= PAWN_ISOLATED_PENALTY[opposed ? 1 : 0];
						}
						if (doubled) {
							pawnStructure[us] -= PAWN_DOUBLED_PENALTY[opposed ? 1 : 0];
						}
						if (!supported
								&& !isolated
								&& !backward) {
							pawnStructure[us] -= PAWN_UNSUPPORTED_PENALTY;
						}
						if (candidate) {
							passedPawns[us] += PAWN_CANDIDATE[relativeRank];
						}

						// Pawn Storm: It can open a file near the other king
						if (gamePhase > 0 && relativeRank > 2) {
							// Only if in kingside or queenside
							long stormedPawns = otherPawnsAheadAdjacent & ~BitboardUtils.D & ~BitboardUtils.E;
							if (stormedPawns != 0) {
								// The stormed pawn must be in the other king's adjacent files
								int otherKingFile = 7 - ai.kingIndex[them] & 7;
								if ((stormedPawns & BitboardUtils.FILE[otherKingFile]) != 0) {
									pawnStructure[us] += PAWN_STORM_CENTER[relativeRank];
								} else if ((stormedPawns & BitboardUtils.FILES_ADJACENT[otherKingFile]) != 0) {
									pawnStructure[us] += PAWN_STORM[relativeRank];
								}
							}
						}
					} else {
						//
						// Passed Pawn
						//
						// Backfile only to the first piece found
						long backFile = bbAttacks.getRookAttacks(index, all) & pawnFile & BitboardUtils.RANKS_BACKWARD[us][rank];
						// If it has a rook or queen behind consider all the route to promotion attacked or defended
						long attackedAndNotDefendedRoute =
								((routeToPromotion & ai.attackedSquares[them]) | ((backFile & (board.rooks | board.queens) & others) != 0 ? routeToPromotion : 0)) &
										~((routeToPromotion & ai.attackedSquares[us]) | ((backFile & (board.rooks | board.queens) & mines) != 0 ? routeToPromotion : 0));
						boolean connected = (bbAttacks.king[index] & adjacentFiles & myPawns) != 0;
						boolean outside = otherPawns != 0
								&& (((square & BitboardUtils.FILES_LEFT[3]) != 0 && (board.pawns & BitboardUtils.FILES_LEFT[file]) == 0)
								|| ((square & BitboardUtils.FILES_RIGHT[4]) != 0 && (board.pawns & BitboardUtils.FILES_RIGHT[file]) == 0));
						boolean mobile = (pushSquare & (all | attackedAndNotDefendedRoute)) == 0;
						boolean runner = mobile
								&& (routeToPromotion & all) == 0
								&& attackedAndNotDefendedRoute == 0;

						if (debug) {
							debugSB.append("PAWN ")
									.append(BitboardUtils.SQUARE_NAMES[index])
									.append(isWhite ? " WHITE " : " BLACK ")
									.append("passed ")
									.append(outside ? "outside " : "")
									.append(connected ? "connected " : "")
									.append(supported ? "supported " : "")
									.append(mobile ? "mobile " : "")
									.append(runner ? "runner " : "")
									.append("\n");
						}

						passedPawns[us] += PAWN_PASSER[relativeRank];

						if (relativeRank >= 2) {
							int pushIndex = isWhite ? index + 8 : index - 8;
							passedPawns[us] += BitboardUtils.DISTANCE[pushIndex][ai.kingIndex[them]] * PAWN_PASSER_OTHER_KING_DISTANCE[relativeRank]
									- BitboardUtils.DISTANCE[pushIndex][ai.kingIndex[us]] * PAWN_PASSER_MY_KING_DISTANCE[relativeRank];
						}
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
					}
					// Pawn is part of the king shield
					if (gamePhase > 0
							&& (pawnFile & ~ranksForward & kingZone[us] & ~BitboardUtils.D & ~BitboardUtils.E) != 0) { // Pawn in the kingzone
						pawnStructure[us] += (pawnFile & board.kings & mines) != 0 ?
								PAWN_SHIELD_CENTER[relativeRank] :
								PAWN_SHIELD[relativeRank];
					}

				} else if ((square & board.knights) != 0) {
					pcsq[us] += KNIGHT_PCSQ[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them];

					mobility[us] += KNIGHT_MOBILITY[Long.bitCount(safeAttacks & mobilitySquares[us])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.KNIGHT] * Long.bitCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					// Knight outpost: no opposite pawns can attack the square
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them]) != 0) {
						positional[us] += KNIGHT_OUTPOST[(square & ai.pawnAttacks[us]) != 0 ? 1 : 0];
					}

				} else if ((square & board.bishops) != 0) {
					pcsq[us] += BISHOP_PCSQ[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them];

					mobility[us] += BISHOP_MOBILITY[Long.bitCount(safeAttacks & mobilitySquares[us])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.BISHOP] * Long.bitCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					// Bishop Outpost
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them]) != 0) {
						positional[us] += BISHOP_OUTPOST[(square & ai.pawnAttacks[us]) != 0 ? 1 : 0];
					}

					positional[us] -= BISHOP_MY_PAWNS_IN_COLOR_PENALTY * Long.bitCount(board.pawns & mines & BitboardUtils.getSameColorSquares(square));

					if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0) {
						mobility[us] -= BISHOP_TRAPPED_BY_OPPOSITE_PAWN_PENALTY[(BISHOP_TRAPPING_GUARD[index] & board.pawns & others) != 0 ? 1 : 0];
					}

				} else if ((square & board.rooks) != 0) {
					pcsq[us] += ROOK_PCSQ[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them] & ~ai.knightAttacks[them] & ~ai.bishopAttacks[them];

					int mobilityCount = Long.bitCount(safeAttacks & mobilitySquares[us]);
					mobility[us] += ROOK_MOBILITY[mobilityCount];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.ROOK] * Long.bitCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them]) != 0) {
						positional[us] += ROOK_OUTPOST[(square & ai.pawnAttacks[us]) != 0 ? 1 : 0];
					}

					long rookFile = BitboardUtils.FILE[file];
					if ((rookFile & board.pawns & mines) == 0) {
						positional[us] += ROOK_FILE[(rookFile & board.pawns) == 0 ? 0 : 1];
					}

					if (relativeRank >= 4) {
						long pawnsAligned = BitboardUtils.RANK[rank] & board.pawns & others;
						if (pawnsAligned != 0) {
							positional[us] += ROOK_7 * Long.bitCount(pawnsAligned);
						}
					}

					// Rook trapped by king
					if ((square & ROOK_TRAPPING[ai.kingIndex[us]]) != 0
							&& mobilityCount < ROOK_TRAPPED_BY_OWN_KING_PENALTY.length) {
						positional[us] -= ROOK_TRAPPED_BY_OWN_KING_PENALTY[mobilityCount];
					}

				} else if ((square & board.queens) != 0) {
					pcsq[us] += QUEEN_PCSQ[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them] & ~ai.knightAttacks[them] & ~ai.bishopAttacks[them] & ~ai.rookAttacks[them];

					mobility[us] += QUEEN_MOBILITY[Long.bitCount(safeAttacks & mobilitySquares[us])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.QUEEN] * Long.bitCount(kingAttacks);
						kingAttackersCount[us]++;
					}

				} else if ((square & board.kings) != 0) {
					pcsq[us] += KING_PCSQ[pcsqIndex];
				}
			}
			square <<= 1;
		}

		int oe = (board.getTurn() ? TEMPO : -TEMPO)
				+ pawnMaterial[W] - pawnMaterial[B]
				+ nonPawnMaterial[W] - nonPawnMaterial[B]
				+ pcsq[W] - pcsq[B]
				+ space[W] - space[B]
				+ positional[W] - positional[B]
				+ attacks[W] - attacks[B]
				+ mobility[W] - mobility[B]
				+ pawnStructure[W] - pawnStructure[B]
				+ passedPawns[W] - passedPawns[B]
				+ oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B]);

		// Ponder opening and Endgame value depending of the game phase and the scale factor
		int value = (gamePhase * o(oe)
				+ (GAME_PHASE_MIDGAME - gamePhase) * e(oe) * scaleFactor[0] / Endgame.SCALE_FACTOR_DEFAULT) / GAME_PHASE_MIDGAME;

		if (debug) {
			logger.debug(debugSB);
			logger.debug("                    WOpening WEndgame BOpening BEndgame");
			logger.debug("pawnMaterial      = " + formatOE(pawnMaterial[W]) + " " + formatOE(pawnMaterial[B]));
			logger.debug("nonPawnMaterial   = " + formatOE(nonPawnMaterial[W]) + " " + formatOE(nonPawnMaterial[B]));
			logger.debug("pcsq              = " + formatOE(pcsq[W]) + " " + formatOE(pcsq[B]));
			logger.debug("space             = " + formatOE(space[W]) + " " + formatOE(space[B]));
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
			long lsb = Long.lowestOneBit(attackedByPawn);
			attacks += PAWN_ATTACKS[board.getPieceIntAt(lsb)];
			attackedByPawn &= ~lsb;
		}

		long otherWeak = ai.attackedSquares[us] & others & ~ai.pawnAttacks[1 - us];
		if (otherWeak != 0) {
			long attackedByMinor = (ai.knightAttacks[us] | ai.bishopAttacks[us]) & otherWeak;
			while (attackedByMinor != 0) {
				long lsb = Long.lowestOneBit(attackedByMinor);
				attacks += MINOR_ATTACKS[board.getPieceIntAt(lsb)];
				attackedByMinor &= ~lsb;
			}
			long attackedByMajor = (ai.rookAttacks[us] | ai.queenAttacks[us]) & otherWeak;
			while (attackedByMajor != 0) {
				long lsb = Long.lowestOneBit(attackedByMajor);
				attacks += MAJOR_ATTACKS[board.getPieceIntAt(lsb)];
				attackedByMajor &= ~lsb;
			}
		}

		long superiorAttacks = ai.pawnAttacks[us] & others & ~board.pawns
				| (ai.knightAttacks[us] | ai.bishopAttacks[us]) & others & (board.rooks | board.queens)
				| ai.rookAttacks[us] & others & board.queens;
		int superiorAttacksCount = Long.bitCount(superiorAttacks);
		if (superiorAttacksCount >= 2) {
			attacks += superiorAttacksCount * HUNG_PIECES;
		}

		long pinnedNotPawn = ai.pinnedPieces & ~board.pawns & others;
		if (pinnedNotPawn != 0) {
			attacks += PINNED_PIECE * Long.bitCount(pinnedNotPawn);
		}
		return attacks;
	}
}