package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

/**
 * Evaluation is done in centipawns
 *
 * @author rui
 */
public class CompleteEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger("CompleteEvaluator");

	// Mobility units: this value is added for the number of destination square not occupied by one of our pieces or attacked by opposite pawns
	private final static int[][] MOBILITY = {
			{}, {},
			{oe(0, 0), oe(6, 8), oe(12, 16), oe(18, 24), oe(24, 32), oe(30, 40), oe(36, 48), oe(42, 56), oe(48, 64)},
			{oe(0, 0), oe(5, 5), oe(10, 10), oe(15, 15), oe(20, 20), oe(25, 25), oe(30, 30), oe(35, 35), oe(40, 40), oe(45, 45), oe(50, 50), oe(55, 55), oe(60, 60), oe(65, 65)},
			{oe(0, 0), oe(2, 3), oe(4, 6), oe(6, 9), oe(8, 12), oe(10, 15), oe(12, 18), oe(14, 21), oe(16, 24), oe(18, 27), oe(20, 30), oe(22, 33), oe(24, 36), oe(26, 39), oe(28, 42)},
			{oe(0, 0), oe(2, 2), oe(4, 4), oe(6, 6), oe(8, 8), oe(10, 10), oe(12, 12), oe(14, 14), oe(16, 16), oe(18, 18), oe(20, 20), oe(22, 22), oe(24, 24), oe(26, 26), oe(28, 28), oe(30, 30), oe(32, 32), oe(34, 34), oe(36, 36), oe(38, 38), oe(40, 40), oe(42, 42), oe(44, 44), oe(46, 46), oe(48, 48), oe(50, 50), oe(52, 52), oe(54, 54)}
	};

	// Attacks
	private final static int[] PAWN_ATTACKS = {
			0, oe(0, 0), oe(5, 7), oe(5, 7), oe(7, 10), oe(8, 12), 0
	};
	// Minor piece attacks to pawn undefended pieces
	private final static int[] MINOR_ATTACKS = {
			0, oe(3, 4), oe(5, 5), oe(5, 5), oe(7, 10), oe(7, 10), 0
	};
	// Major piece attacks to pawn undefended pieces
	private final static int[] MAJOR_ATTACKS = {
			0, oe(2, 3), oe(4, 5), oe(4, 5), oe(5, 5), oe(5, 5), 0
	};
	private final static int HUNG_PIECES = oe(16, 25); // two or more pieces of the other side attacked by inferior pieces
	private final static int PINNED_PIECE = oe(5, 10);

	// Pawns
	private final static int PAWN_UNSUPPORTED = oe(-1, -2);
	// Array is not opposed, opposed
	// If not opposed, it can be easily attacked but if opposed, it is easily stopped
	private final static int[] PAWN_BACKWARDS = {oe(-10, -15), oe(-10, -15)};
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
	private final static int[] PAWN_SHIELD = {0, oe(15, 0), oe(7, 0), oe(0, 0), 0, 0, 0, 0};
	private final static int[] PAWN_STORM = {0, 0, 0, oe(10, 0), oe(25, 0), oe(50, 0), 0, 0};

	// Knights
	private final static int[] KNIGHT_OUTPOST = {oe(10, 15), oe(20, 30)}; // Array is Not defended by pawn, defended by pawn

	// Bishops
	private final static int BISHOP_MY_PAWNS_IN_COLOR_PENALTY = oe(2, 4); // Penalty for each of my pawns in the bishop color (Capablanca rule)
	private final static int[] BISHOP_OUTPOST = {oe(5, 7), oe(10, 15)};
	private final static int BISHOP_TRAPPED = oe(-40, -40);
	private final static long[] BISHOP_TRAPPING = {
			0, 1L << 10, 0, 0, 0, 0, 1L << 13, 0,
			1L << 17, 0, 0, 0, 0, 0, 0, 1L << 22,
			1L << 25, 0, 0, 0, 0, 0, 0, 1L << 30,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			1L << 33, 0, 0, 0, 0, 0, 0, 1L << 38,
			1L << 41, 0, 0, 0, 0, 0, 0, 1L << 46,
			0, 1L << 50, 0, 0, 0, 0, 1L << 53, 0
	};

	// Rooks
	private final static int ROOK_FILE_OPEN = oe(10, 10); // No pawns in rook file
	private final static int ROOK_FILE_SEMIOPEN = oe(7, 5); // Only opposite pawns in rook file
	private final static int ROOK_7 = oe(10, 20); // Rook 5, 6, 7th rank and pawn in the same file

	// Queen
	private final static int QUEEN_7 = oe(5, 10); // Queen in 5, 6, 7th rank and pawn in the same file

	// King
	// Sums for each piece attacking an square near the king
	private final static int PIECE_ATTACKS_KING[] = {0, 0, oe(30, 0), oe(20, 0), oe(40, 0), oe(80, 0)};
	// Ponder kings attacks by the number of attackers (not pawns)
	private final static int[] KING_SAFETY_PONDER = {0, 0, 32, 48, 56, 60, 62, 63, 64, 64, 64, 64, 64, 64, 64, 64};

	// Tempo
	public final static int TEMPO = 9; // Add to moving side score

	private final static long[] OUTPOST_MASK = {0x00007e7e7e000000L, 0x0000007e7e7e0000L};

	private final static int pawnPcsq[] = {
			oe(-20, -4), oe(-8, -6), oe(-2, -8), oe(5, -10), oe(5, -10), oe(-2, -8), oe(-8, -6), oe(-20, -4),
			oe(-23, -7), oe(-11, -9), oe(-5, -11), oe(2, -13), oe(2, -13), oe(-5, -11), oe(-11, -9), oe(-23, -7),
			oe(-22, -7), oe(-10, -9), oe(-4, -11), oe(13, -13), oe(13, -13), oe(-4, -11), oe(-10, -9), oe(-22, -7),
			oe(-21, -6), oe(-9, -8), oe(-3, -10), oe(24, -12), oe(24, -12), oe(-3, -10), oe(-9, -8), oe(-21, -6),
			oe(-19, -5), oe(-7, -7), oe(-1, -9), oe(16, -11), oe(16, -11), oe(-1, -9), oe(-7, -7), oe(-19, -5),
			oe(-18, -4), oe(-6, -6), oe(0, -8), oe(7, -10), oe(7, -10), oe(0, -8), oe(-6, -6), oe(-18, -4),
			oe(-17, -2), oe(-5, -4), oe(1, -6), oe(8, -8), oe(8, -8), oe(1, -6), oe(-5, -4), oe(-17, -2),
			oe(-20, -4), oe(-8, -6), oe(-2, -8), oe(5, -10), oe(5, -10), oe(-2, -8), oe(-8, -6), oe(-20, -4)
	};
	private final static int knightPcsq[] = {
			oe(-58, -22), oe(-42, -17), oe(-31, -12), oe(-27, -9), oe(-27, -9), oe(-31, -12), oe(-42, -17), oe(-58, -22),
			oe(-36, -15), oe(-20, -8), oe(-9, -4), oe(-5, -2), oe(-5, -2), oe(-9, -4), oe(-20, -8), oe(-36, -15),
			oe(-20, -10), oe(-4, -4), oe(7, 1), oe(11, 3), oe(11, 3), oe(7, 1), oe(-4, -4), oe(-20, -10),
			oe(-11, -6), oe(5, -1), oe(16, 4), oe(20, 8), oe(20, 8), oe(16, 4), oe(5, -1), oe(-11, -6),
			oe(-5, -4), oe(11, 1), oe(22, 6), oe(26, 10), oe(26, 10), oe(22, 6), oe(11, 1), oe(-5, -4),
			oe(-7, -3), oe(9, 3), oe(20, 8), oe(24, 10), oe(24, 10), oe(20, 8), oe(9, 3), oe(-7, -3),
			oe(-16, -8), oe(0, -1), oe(11, 3), oe(15, 5), oe(15, 5), oe(11, 3), oe(0, -1), oe(-16, -8),
			oe(-37, -15), oe(-21, -10), oe(-10, -5), oe(-6, -2), oe(-6, -2), oe(-10, -5), oe(-21, -10), oe(-37, -15)
	};
	private final static int bishopPcsq[] = {
			oe(-7, 0), oe(-8, -1), oe(-11, -2), oe(-13, -2), oe(-13, -2), oe(-11, -2), oe(-8, -1), oe(-7, 0),
			oe(-3, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-3, -1),
			oe(-6, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-6, -2),
			oe(-8, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-8, -2),
			oe(-8, -2), oe(-2, 0), oe(6, 2), oe(15, 5), oe(15, 5), oe(6, 2), oe(-2, 0), oe(-8, -2),
			oe(-6, -2), oe(0, 0), oe(7, 3), oe(6, 2), oe(6, 2), oe(7, 3), oe(0, 0), oe(-6, -2),
			oe(-3, -1), oe(3, 1), oe(0, 0), oe(-2, 0), oe(-2, 0), oe(0, 0), oe(3, 1), oe(-3, -1),
			oe(-2, 0), oe(-3, -1), oe(-6, -2), oe(-8, -2), oe(-8, -2), oe(-6, -2), oe(-3, -1), oe(-2, 0)
	};
	private final static int rookPcsq[] = {
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 0), oe(0, 0), oe(4, 0), oe(8, 0), oe(8, 0), oe(4, 0), oe(0, 0), oe(-4, 0),
			oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1),
			oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1),
			oe(-4, 1), oe(0, 1), oe(4, 1), oe(8, 1), oe(8, 1), oe(4, 1), oe(0, 1), oe(-4, 1),
			oe(-4, -2), oe(0, -2), oe(4, -2), oe(8, -2), oe(8, -2), oe(4, -2), oe(0, -2), oe(-4, -2)
	};
	private final static int queenPcsq[] = {
			oe(-11, -15), oe(-7, -10), oe(-4, -8), oe(-2, -7), oe(-2, -7), oe(-4, -8), oe(-7, -10), oe(-11, -15),
			oe(-7, -10), oe(-1, -5), oe(1, -3), oe(3, -2), oe(3, -2), oe(1, -3), oe(-1, -5), oe(-7, -10),
			oe(-4, -8), oe(1, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(1, -3), oe(-4, -8),
			oe(-2, -7), oe(3, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(3, -2), oe(-2, -7),
			oe(-2, -7), oe(3, -2), oe(6, 2), oe(9, 5), oe(9, 5), oe(6, 2), oe(3, -2), oe(-2, -7),
			oe(-4, -8), oe(1, -3), oe(5, 0), oe(6, 2), oe(6, 2), oe(5, 0), oe(1, -3), oe(-4, -8),
			oe(-7, -10), oe(-1, -5), oe(1, -3), oe(3, -2), oe(3, -2), oe(1, -3), oe(-1, -5), oe(-7, -10),
			oe(-11, -15), oe(-7, -10), oe(-4, -8), oe(-2, -7), oe(-2, -7), oe(-4, -8), oe(-7, -10), oe(-11, -15)
	};
	private final static int kingPcsq[] = {
			oe(44, -58), oe(49, -35), oe(19, -19), oe(-1, -13), oe(-1, -13), oe(19, -19), oe(49, -35), oe(44, -58),
			oe(41, -35), oe(46, -10), oe(16, 2), oe(-4, 8), oe(-4, 8), oe(16, 2), oe(46, -10), oe(41, -35),
			oe(38, -19), oe(43, 2), oe(13, 17), oe(-7, 23), oe(-7, 23), oe(13, 17), oe(43, 2), oe(38, -19),
			oe(35, -13), oe(40, 8), oe(10, 23), oe(-10, 32), oe(-10, 32), oe(10, 23), oe(40, 8), oe(35, -13),
			oe(30, -13), oe(35, 8), oe(5, 23), oe(-15, 32), oe(-15, 32), oe(5, 23), oe(35, 8), oe(30, -13),
			oe(25, -19), oe(30, 2), oe(0, 17), oe(-20, 23), oe(-20, 23), oe(0, 17), oe(30, 2), oe(25, -19),
			oe(15, -35), oe(20, -10), oe(-10, 2), oe(-30, 8), oe(-30, 8), oe(-10, 2), oe(20, -10), oe(15, -35),
			oe(5, -58), oe(10, -35), oe(-20, -19), oe(-40, -13), oe(-40, -13), oe(-20, -19), oe(10, -35), oe(5, -58)
	};

	public boolean debug = false;
	public boolean debugPawns = false;
	public StringBuffer debugSB;

	private int[] pawnMaterial = {0, 0};
	private int[] material = {0, 0};
	private int[] center = {0, 0};
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

		int endGameValue = EndgameEvaluator.endGameValue(board, whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops, whiteRooks, blackRooks, whiteQueens, blackQueens);
		if (endGameValue != NO_VALUE) {
			return endGameValue;
		}

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
		long pieceAttacks, safeAttacks, kingAttacks;
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
					center[us] += pawnPcsq[pcsqIndex];

					int relativeRank = isWhite ? rank : 7 - rank;
					long myPawns = board.pawns & mines;
					long otherPawns = board.pawns & others;
					long adjacentFiles = BitboardUtils.FILES_ADJACENT[file];
					long ranksForward = BitboardUtils.RANKS_FORWARD[us][rank];
					long pawnFile = BitboardUtils.FILE[file];
					long routeToPromotion = pawnFile & ranksForward;
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
							(BitboardUtils.popCount(myPawnsBesideAndBehindAdjacent) >= BitboardUtils.popCount(otherPawnsAheadAdjacent & ~pieceAttacks))); // Has more friend pawns beside and behind than opposed pawns controlling his route to promotion
					boolean backwards = !isolated
							&& !passed
							&& !candidate
							&& myPawnsBesideAndBehindAdjacent == 0
							&& (pieceAttacks & otherPawns) == 0 // No backwards if it can capture
							&& (BitboardUtils.RANK_AND_BACKWARD[us][isWhite ? BitboardUtils.getRankLsb(myPawnsAheadAdjacent) : BitboardUtils.getRankMsb(myPawnsAheadAdjacent)] &
							routeToPromotion & (board.pawns | ai.pawnAttacks[them])) != 0; // Other pawns stopping it from advance, opposing or capturing it before reaching my pawns

					if (debugPawns) {
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
						pawnStructure[us] += PAWN_BACKWARDS[opposed ? 1 : 0];
					}
					// Pawn is part of the king shield
					if ((pawnFile & kingZone[us]) != 0) {
						pawnStructure[us] += PAWN_SHIELD[relativeRank];
					}
					// Pawn Storm
					if ((pawnFile & kingZone[them]) != 0) {
						pawnStructure[us] += PAWN_STORM[relativeRank];
					}
					if (candidate) {
						passedPawns[us] += PAWN_CANDIDATE[relativeRank];
					}
					if (passed) {
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

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them];

					mobility[us] += MOBILITY[Piece.KNIGHT][BitboardUtils.popCount(safeAttacks & mobilitySquares[us] & BitboardUtils.RANKS_FORWARD[us][rank])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.KNIGHT] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					// Knight outpost: no opposite pawns can attack the square
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them]) != 0) {
						positional[us] += KNIGHT_OUTPOST[(square & ai.pawnAttacks[us]) != 0 ? 1 : 0];
					}

				} else if ((square & board.bishops) != 0) {
					center[us] += bishopPcsq[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them];

					mobility[us] += MOBILITY[Piece.BISHOP][BitboardUtils.popCount(safeAttacks & mobilitySquares[us] & BitboardUtils.RANKS_FORWARD[us][rank])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.BISHOP] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					// Bishop Outpost
					if ((square & OUTPOST_MASK[us] & ~pawnCanAttack[them]) != 0) {
						positional[us] += BISHOP_OUTPOST[(square & ai.pawnAttacks[us]) != 0 ? 1 : 0];
					}

					positional[us] -= BISHOP_MY_PAWNS_IN_COLOR_PENALTY * BitboardUtils.popCount(board.pawns & mines & BitboardUtils.getSameColorSquares(square));

					if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0) {
						mobility[us] += BISHOP_TRAPPED;
					}

				} else if ((square & board.rooks) != 0) {
					center[us] += rookPcsq[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them] & ~ai.knightAttacks[them] & ~ai.bishopAttacks[them];

					mobility[us] += MOBILITY[Piece.ROOK][BitboardUtils.popCount(safeAttacks & mobilitySquares[us])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.ROOK] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					long rookFile = BitboardUtils.FILE[file];
					if ((rookFile & board.pawns) == 0) {
						positional[us] += ROOK_FILE_OPEN;
					} else if ((rookFile & board.pawns & mines) == 0) {
						positional[us] += ROOK_FILE_SEMIOPEN;
					}

					long ranks567 = isWhite ? BitboardUtils.RANK[4] | BitboardUtils.RANK[5] | BitboardUtils.RANK[6] :
							BitboardUtils.RANK[1] | BitboardUtils.RANK[2] | BitboardUtils.RANK[3];
					if ((square & ranks567) != 0 && (BitboardUtils.RANK[rank] & board.pawns & others) != 0) {
						positional[us] += ROOK_7;
					}

				} else if ((square & board.queens) != 0) {
					center[us] += queenPcsq[pcsqIndex];

					safeAttacks = pieceAttacks & ~ai.pawnAttacks[them] & ~ai.knightAttacks[them] & ~ai.bishopAttacks[them] & ~ai.rookAttacks[them];

					mobility[us] += MOBILITY[Piece.QUEEN][BitboardUtils.popCount(safeAttacks & mobilitySquares[us])];

					kingAttacks = safeAttacks & kingZone[them];
					if (kingAttacks != 0) {
						kingSafety[us] += PIECE_ATTACKS_KING[Piece.QUEEN] * BitboardUtils.popCount(kingAttacks);
						kingAttackersCount[us]++;
					}

					long ranks567 = isWhite ? BitboardUtils.RANK[4] | BitboardUtils.RANK[5] | BitboardUtils.RANK[6] :
							BitboardUtils.RANK[1] | BitboardUtils.RANK[2] | BitboardUtils.RANK[3];
					if ((square & ranks567) != 0 && (BitboardUtils.RANK[rank] & board.pawns & others) != 0) {
						positional[us] += QUEEN_7;
					}

				} else if ((square & board.kings) != 0) {
					center[us] += kingPcsq[pcsqIndex];
				}
			}
			square <<= 1;
		}

		// Ponder opening and Endgame value depending of the non-pawn pieces:
		// opening=> gamephase = 256 / ending => gamephase = 0
		int nonPawnMaterial = material[W] + material[B];
		int gamePhase = nonPawnMaterial >= NON_PAWN_MATERIAL_MIDGAME_MAX ? 256 :
				nonPawnMaterial <= NON_PAWN_MATERIAL_ENDGAME_MIN ? 0 :
						((nonPawnMaterial - NON_PAWN_MATERIAL_ENDGAME_MIN) << 8) / (NON_PAWN_MATERIAL_MIDGAME_MAX - NON_PAWN_MATERIAL_ENDGAME_MIN);

		int value = 0;
		// First Material
		value += pawnMaterial[W] - pawnMaterial[B] + material[W] - material[B];
		// Tempo
		value += (board.getTurn() ? TEMPO : -TEMPO);

		int oe = center[W] - center[B]
				+ positional[W] - positional[B]
				+ attacks[W] - attacks[B]
				+ mobility[W] - mobility[B]
				+ pawnStructure[W] - pawnStructure[B]
				+ passedPawns[W] - passedPawns[B]
				+ oeShr(6, KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B]);

		value += (gamePhase * o(oe)) >> 8; // divide by 256
		value += ((256 - gamePhase) * e(oe)) >> 8;

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
			logger.debug("value             = " + value);
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

		long pinnedNotPawn = ai.pinnedPieces & ~board.pawns & others;
		if (pinnedNotPawn != 0) {
			attacks += PINNED_PIECE * BitboardUtils.popCount(pinnedNotPawn);
		}
		return attacks;
	}
}