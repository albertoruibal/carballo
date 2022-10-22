package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.Square;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

public class CompleteEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger(CompleteEvaluator.class.getName());

	public static final int PAWN_OPENING = 80;
	public static final int[] PIECE_VALUES_OE = {0, oe(PAWN_OPENING, PAWN), oe(KNIGHT, KNIGHT), oe(BISHOP, BISHOP), oe(ROOK, ROOK), oe(QUEEN, QUEEN)};

	// Mobility units: this value is added for the number of destination square not occupied by one of our pieces or attacked by opposite pawns
	private static final int[] KNIGHT_MOBILITY = {oe(-12, -16), oe(2, 2), oe(5, 7), oe(7, 9), oe(8, 11), oe(10, 13), oe(11, 14), oe(11, 15), oe(12, 16)};
	private static final int[] BISHOP_MOBILITY = {oe(-16, -16), oe(-1, -1), oe(3, 3), oe(6, 6), oe(8, 8), oe(9, 9), oe(11, 11), oe(12, 12), oe(13, 13), oe(13, 13), oe(14, 14), oe(15, 15), oe(15, 15), oe(16, 16)};
	private static final int[] ROOK_MOBILITY = {oe(-14, -21), oe(-1, -2), oe(3, 4), oe(5, 7), oe(7, 10), oe(8, 12), oe(9, 13), oe(10, 15), oe(11, 16), oe(11, 17), oe(12, 18), oe(13, 19), oe(13, 20), oe(14, 20), oe(14, 21)};
	private static final int[] QUEEN_MOBILITY = {oe(-27, -27), oe(-9, -9), oe(-2, -2), oe(2, 2), oe(5, 5), oe(8, 8), oe(10, 10), oe(12, 12), oe(13, 13), oe(14, 14), oe(16, 16), oe(17, 17), oe(18, 18), oe(19, 19), oe(19, 19), oe(20, 20), oe(21, 21), oe(22, 22), oe(22, 22), oe(23, 23), oe(24, 24), oe(24, 24), oe(25, 25), oe(25, 25), oe(26, 26), oe(26, 26), oe(27, 27), oe(27, 27)};

	// Space
	private static final long WHITE_SPACE_ZONE = (BitboardUtils.C | BitboardUtils.D | BitboardUtils.E | BitboardUtils.F) &
			(BitboardUtils.R2 | BitboardUtils.R3 | BitboardUtils.R4);
	private static final long BLACK_SPACE_ZONE = (BitboardUtils.C | BitboardUtils.D | BitboardUtils.E | BitboardUtils.F) &
			(BitboardUtils.R5 | BitboardUtils.R6 | BitboardUtils.R7);
	private static final int SPACE = oe(2, 0);

	// Attacks
	private static final int[] PAWN_ATTACKS = {0, 0, oe(11, 15), oe(12, 16), oe(17, 23), oe(19, 25), 0};
	private static final int[] MINOR_ATTACKS = {0, oe(3, 5), oe(7, 9), oe(7, 9), oe(10, 14), oe(11, 15), 0}; // Minor piece attacks to pawn undefended pieces
	private static final int[] MAJOR_ATTACKS = {0, oe(2, 2), oe(3, 4), oe(3, 4), oe(5, 6), oe(5, 7), 0}; // Major piece attacks to pawn undefended pieces

	private static final int HUNG_PIECES = oe(16, 25); // Two or more pieces of the other side attacked by inferior pieces
	private static final int PINNED_PIECE = oe(7, 15);

	// Pawns
	// Those are all penalties. Array is {not opposed, opposed}: If not opposed, backwards and isolated pawns can be easily attacked
	private static final int[] PAWN_BACKWARDS_PENALTY = {oe(20, 15), oe(10, 15)}; // Not opposed is worse in the opening
	private static final int[] PAWN_ISOLATED_PENALTY = {oe(20, 20), oe(10, 20)}; // Not opposed is worse in the opening
	private static final int[] PAWN_DOUBLED_PENALTY = {oe(8, 16), oe(10, 20)}; // Not opposed is better, opening is better
	// Not backwards or isolated
	private static final int PAWN_UNSUPPORTED_PENALTY = oe(2, 4);

	// And now the bonuses. Array by relative rank
	private static final int[] PAWN_CANDIDATE = {0, oe(10, 13), oe(10, 13), oe(14, 18), oe(22, 28), oe(34, 43), oe(50, 63), 0};
	private static final int[] PAWN_PASSER = {0, oe(20, 25), oe(20, 25), oe(28, 35), oe(44, 55), oe(68, 85), oe(100, 125), 0};
	private static final int[] PAWN_PASSER_OUTSIDE = {0, 0, 0, oe(2, 3), oe(7, 9), oe(14, 18), oe(24, 30), 0};
	private static final int[] PAWN_PASSER_CONNECTED = {0, 0, 0, oe(3, 3), oe(8, 8), oe(15, 15), oe(25, 25), 0};
	private static final int[] PAWN_PASSER_SUPPORTED = {0, 0, 0, oe(6, 6), oe(17, 17), oe(33, 33), oe(55, 55), 0};
	private static final int[] PAWN_PASSER_MOBILE = {0, 0, 0, oe(2, 2), oe(6, 6), oe(12, 12), oe(20, 20), 0};
	private static final int[] PAWN_PASSER_RUNNER = {0, 0, 0, oe(6, 6), oe(18, 18), oe(36, 36), oe(60, 60), 0};

	private static final int[] PAWN_PASSER_OTHER_KING_DISTANCE = {0, 0, 0, oe(0, 2), oe(0, 6), oe(0, 12), oe(0, 20), 0};
	private static final int[] PAWN_PASSER_MY_KING_DISTANCE = {0, 0, 0, oe(0, 1), oe(0, 3), oe(0, 6), oe(0, 10), 0};

	private static final int[] PAWN_SHIELD_CENTER = {0, oe(55, 0), oe(41, 0), oe(28, 0), oe(14, 0), 0, 0, 0};
	private static final int[] PAWN_SHIELD = {0, oe(35, 0), oe(26, 0), oe(18, 0), oe(9, 0), 0, 0, 0};
	private static final int[] PAWN_STORM_CENTER = {0, 0, 0, oe(8, 0), oe(15, 0), oe(30, 0), 0, 0};
	private static final int[] PAWN_STORM = {0, 0, 0, oe(5, 0), oe(10, 0), oe(20, 0), 0, 0};

	// Penalty for pawns in [D,E] in the initial square blocked by our own pieces
	private static final int PAWN_BLOCKADE = oe(5, 0);

	// Knights
	// Array is Not defended by pawn, defended by pawn
	private static final int[] KNIGHT_OUTPOST = {oe(15, 10), oe(22, 15)};

	// Bishops
	private static final int[] BISHOP_OUTPOST = {oe(7, 4), oe(10, 7)};
	private static final int BISHOP_MY_PAWNS_IN_COLOR_PENALTY = oe(2, 4); // Penalty for each of my pawns in the bishop color (Capablanca rule)
	private static final int[] BISHOP_TRAPPED_BY_OPPOSITE_PAWN_PENALTY = {oe(40, 40), oe(80, 80)}; // By pawn not guarded / guarded
	private static final long[] BISHOP_TRAPPING = { // Indexed by bishop position, contains the square where a pawn can trap the bishop
			0, Square.F2, 0, 0, 0, 0, Square.C2, 0,
			Square.G3, 0, 0, 0, 0, 0, 0, Square.B3,
			Square.G4, 0, 0, 0, 0, 0, 0, Square.B4,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			Square.G5, 0, 0, 0, 0, 0, 0, Square.B5,
			Square.G6, 0, 0, 0, 0, 0, 0, Square.B6,
			0, Square.F7, 0, 0, 0, 0, Square.C7, 0
	};
	private static final long[] BISHOP_TRAPPING_GUARD = { // Indexed by bishop position, contains the square where other pawn defends the trapping pawn
			0, 0, 0, 0, 0, 0, 0, 0,
			Square.F2, 0, 0, 0, 0, 0, 0, Square.C2,
			Square.F3, 0, 0, 0, 0, 0, 0, Square.C3,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			Square.F6, 0, 0, 0, 0, 0, 0, Square.C6,
			Square.F7, 0, 0, 0, 0, 0, 0, Square.C7,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	// Rooks
	private static final int[] ROOK_OUTPOST = {oe(2, 1), oe(3, 2)}; // Array is Not defended by pawn, defended by pawn
	private static final int[] ROOK_FILE = {oe(15, 10), oe(7, 5)}; // Open / Semi open
	private static final int ROOK_7 = oe(7, 10); // Rook 5, 6 or 7th rank attacking a pawn in the same rank not defended by pawn
	private static final int[] ROOK_TRAPPED_BY_OWN_KING_PENALTY = {oe(40, 0), oe(30, 0), oe(20, 0), oe(10, 0)}; // Penalty by number of mobility squares
	private static final long[] ROOK_TRAPPING = { // Indexed by own king position, contains the squares where a rook may be traped by the king
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

	// King
	// Sums for each piece attacking an square near the king
	private static final int[] PIECE_ATTACKS_KING = {0, 0, oe(30, 0), oe(20, 0), oe(40, 0), oe(80, 0)};
	// Ponder kings attacks by the number of attackers (not pawns)
	private static final int[] KING_SAFETY_PONDER = {0, 0, 32, 48, 56, 60, 62, 63, 64, 64, 64, 64, 64, 64, 64, 64};

	// Tempo
	public static final int TEMPO = oe(15, 5); // Add to moving side score

	private static final long[] OUTPOST_MASK = {0x00007e7e7e000000L, 0x0000007e7e7e0000L};

	private static final int[] PAWN_PCSQ = {
			oe(-18, 4), oe(-6, 2), oe(0, 0), oe(6, -2), oe(6, -2), oe(0, 0), oe(-6, 2), oe(-18, 4),
			oe(-21, 1), oe(-9, -1), oe(-3, -3), oe(3, -5), oe(3, -5), oe(-3, -3), oe(-9, -1), oe(-21, 1),
			oe(-20, 1), oe(-8, -1), oe(-2, -3), oe(4, -5), oe(4, -5), oe(-2, -3), oe(-8, -1), oe(-20, 1),
			oe(-19, 2), oe(-7, 0), oe(-1, -2), oe(12, -4), oe(12, -4), oe(-1, -2), oe(-7, 0), oe(-19, 2),
			oe(-17, 3), oe(-5, 1), oe(1, -1), oe(10, -3), oe(10, -3), oe(1, -1), oe(-5, 1), oe(-17, 3),
			oe(-16, 4), oe(-4, 2), oe(2, 0), oe(8, -2), oe(8, -2), oe(2, 0), oe(-4, 2), oe(-16, 4),
			oe(-15, 6), oe(-3, 4), oe(3, 2), oe(9, 0), oe(9, 0), oe(3, 2), oe(-3, 4), oe(-15, 6),
			oe(-18, 4), oe(-6, 2), oe(0, 0), oe(6, -2), oe(6, -2), oe(0, 0), oe(-6, 2), oe(-18, 4)
	};
	private static final int[] KNIGHT_PCSQ = {
			oe(-27, -22), oe(-17, -17), oe(-9, -12), oe(-4, -9), oe(-4, -9), oe(-9, -12), oe(-17, -17), oe(-27, -22),
			oe(-21, -15), oe(-11, -8), oe(-3, -4), oe(2, -2), oe(2, -2), oe(-3, -4), oe(-11, -8), oe(-21, -15),
			oe(-15, -10), oe(-5, -4), oe(3, 1), oe(8, 3), oe(8, 3), oe(3, 1), oe(-5, -4), oe(-15, -10),
			oe(-9, -6), oe(1, -1), oe(9, 4), oe(14, 8), oe(14, 8), oe(9, 4), oe(1, -1), oe(-9, -6),
			oe(-5, -4), oe(5, 1), oe(13, 6), oe(18, 10), oe(18, 10), oe(13, 6), oe(5, 1), oe(-5, -4),
			oe(-6, -4), oe(4, 2), oe(12, 7), oe(17, 9), oe(17, 9), oe(12, 7), oe(4, 2), oe(-6, -4),
			oe(-10, -8), oe(0, -1), oe(8, 3), oe(13, 5), oe(13, 5), oe(8, 3), oe(0, -1), oe(-10, -8),
			oe(-20, -15), oe(-10, -10), oe(-2, -5), oe(3, -2), oe(3, -2), oe(-2, -5), oe(-10, -10), oe(-20, -15)
	};
	private static final int[] BISHOP_PCSQ = {
			oe(-7, 0), oe(-8, -1), oe(-11, -2), oe(-13, -2), oe(-13, -2), oe(-11, -2), oe(-8, -1), oe(-7, 0),
			oe(-3, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-3, -1),
			oe(-6, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-6, -2),
			oe(-8, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-8, -2),
			oe(-8, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-8, -2),
			oe(-6, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-6, -2),
			oe(-3, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-3, -1),
			oe(-2, 0), oe(-3, -1), oe(-6, -2), oe(-8, -2), oe(-8, -2), oe(-6, -2), oe(-3, -1), oe(-2, 0)
	};
	private static final int[] ROOK_PCSQ = {
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1),
			oe(-4, 3), oe(0, 3), oe(4, 3), oe(8, 3), oe(8, 3), oe(4, 3), oe(0, 3), oe(-4, 3),
			oe(-4, 5), oe(0, 5), oe(4, 5), oe(8, 5), oe(8, 5), oe(4, 5), oe(0, 5), oe(-4, 5),
			oe(-4, -2), oe(0, -2), oe(4, -2), oe(8, -2), oe(8, -2), oe(4, -2), oe(0, -2), oe(-4, -2)
	};
	private static final int[] QUEEN_PCSQ = {
			oe(-9, -15), oe(-6, -10), oe(-4, -8), oe(-2, -7), oe(-2, -7), oe(-4, -8), oe(-6, -10), oe(-9, -15),
			oe(-6, -10), oe(-1, -5), oe(1, -3), oe(3, -2), oe(3, -2), oe(1, -3), oe(-1, -5), oe(-6, -10),
			oe(-4, -8), oe(1, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(1, -3), oe(-4, -8),
			oe(-2, -7), oe(3, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(3, -2), oe(-2, -7),
			oe(-2, -7), oe(3, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(3, -2), oe(-2, -7),
			oe(-4, -8), oe(1, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(1, -3), oe(-4, -8),
			oe(-6, -10), oe(-1, -5), oe(1, -3), oe(3, -2), oe(3, -2), oe(1, -3), oe(-1, -5), oe(-6, -10),
			oe(-9, -15), oe(-6, -10), oe(-4, -8), oe(-2, -7), oe(-2, -7), oe(-4, -8), oe(-6, -10), oe(-9, -15)
	};
	private static final int[] KING_PCSQ = {
			oe(34, -58), oe(39, -35), oe(14, -19), oe(-6, -13), oe(-6, -13), oe(14, -19), oe(39, -35), oe(34, -58),
			oe(31, -35), oe(36, -10), oe(11, 2), oe(-9, 8), oe(-9, 8), oe(11, 2), oe(36, -10), oe(31, -35),
			oe(28, -19), oe(33, 2), oe(8, 17), oe(-12, 23), oe(-12, 23), oe(8, 17), oe(33, 2), oe(28, -19),
			oe(25, -13), oe(30, 8), oe(5, 23), oe(-15, 32), oe(-15, 32), oe(5, 23), oe(30, 8), oe(25, -13),
			oe(20, -13), oe(25, 8), oe(0, 23), oe(-20, 32), oe(-20, 32), oe(0, 23), oe(25, 8), oe(20, -13),
			oe(15, -19), oe(20, 2), oe(-5, 17), oe(-25, 23), oe(-25, 23), oe(-5, 17), oe(20, 2), oe(15, -19),
			oe(5, -35), oe(10, -10), oe(-15, 2), oe(-35, 8), oe(-35, 8), oe(-15, 2), oe(10, -10), oe(5, -35),
			oe(-5, -58), oe(0, -35), oe(-25, -19), oe(-45, -13), oe(-45, -13), oe(-25, -19), oe(0, -35), oe(-5, -58)
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

						if ((square & (BitboardUtils.D | BitboardUtils.E)) != 0
								&& relativeRank == 1
								&& (pushSquare & mines & ~board.pawns) != 0) {
							pawnStructure[us] -= PAWN_BLOCKADE;
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