package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;

/**
 * Evaluation is done in centipawns
 * Piece/square values like Fruit/Toga
 *
 * @author rui
 */
public class CompleteEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger("CompleteEvaluator");

	// Mobility units: this value is added for the number of destination square not occupied by one of our pieces or attacked by opposite pawns
	private final static int[][] MOBILITY = {
			{}, {},
			{oe(-16, -16), oe(-12, -12), oe(-8, -8), oe(-4, -4), oe(0, 0), oe(4, 4), oe(8, 8), oe(12, 12), oe(16, 16)},
			{oe(-30, -30), oe(-25, -25), oe(-20, -20), oe(-15, -15), oe(-10, -10), oe(-5, -5), oe(0, 0), oe(5, 5), oe(10, 10), oe(15, 15), oe(20, 20), oe(25, 25), oe(30, 30), oe(35, 35)},
			{oe(-14, -28), oe(-12, -24), oe(-10, -20), oe(-8, -16), oe(-6, -12), oe(-4, -8), oe(-2, -4), oe(0, 0), oe(2, 4), oe(4, 8), oe(6, 12), oe(8, 16), oe(10, 20), oe(12, 24), oe(14, 28)},
			{oe(-26, -52), oe(-24, -48), oe(-22, -44), oe(-20, -40), oe(-18, -36), oe(-16, -32), oe(-14, -28), oe(-12, -24), oe(-10, -20), oe(-8, -16), oe(-6, -12), oe(-4, -8), oe(-2, -4), oe(0, 0), oe(2, 4), oe(4, 8), oe(6, 12), oe(8, 16), oe(10, 20), oe(12, 24), oe(14, 28), oe(16, 32), oe(18, 36), oe(20, 40), oe(22, 44), oe(24, 48), oe(26, 52), oe(28, 56)}
	};

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

	// Knights
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

	// Bishops
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
	private final static int ROOK_FILE_OPEN = oe(25, 20); // No pawns in rook file
	private final static int ROOK_FILE_SEMIOPEN = oe(15, 10); // Only opposite pawns in rook file
	private final static int ROOK_CONNECT = oe(20, 10); // Rook connects with other rook x 2
	private final static int ROOK_7 = oe(10, 30); // Rook in 7th rank and opposite king in 8th or pawn in 7th

	// Queen

	// King Safety: not in endgame!!!
	private final static int PAWN_ATTACKS_KING = oe(1, 0);
	private final static int KNIGHT_ATTACKS_KING = oe(4, 0);
	private final static int BISHOP_ATTACKS_KING = oe(2, 0);
	private final static int ROOK_ATTACKS_KING = oe(3, 0);
	private final static int QUEEN_ATTACKS_KING = oe(5, 0);

	private final static int KING_PAWN_SHIELD = oe(5, 0); // Protection: sums for each pawn near king (opening)

	// Ponder kings attacks by the number of attackers (not pawns) later divided by 8
	private final static int[] KING_SAFETY_PONDER = {0, 1, 4, 8, 16, 25, 36, 49, 50, 50, 50, 50, 50, 50, 50, 50};

	// Pawns
	private final static int PAWN_UNSUPPORTED = oe(-2, -1);
	private final static int PAWN_BACKWARDS = oe(-10, -15);
	// Array is not opposed, opposed
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

	private final static int pawnPcsq[] = {
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(5, 0), oe(5, 0), oe(0, 0), oe(-5, 0), oe(-15, 0),
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(5, 0), oe(5, 0), oe(0, 0), oe(-5, 0), oe(-15, 0),
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(15, 0), oe(15, 0), oe(0, 0), oe(-5, 0), oe(-15, 0),
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(25, 0), oe(25, 0), oe(0, 0), oe(-5, 0), oe(-15, 0),
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(15, 0), oe(15, 0), oe(0, 0), oe(-5, 0), oe(-15, 0),
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(5, 0), oe(5, 0), oe(0, 0), oe(-5, 0), oe(-15, 0),
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(5, 0), oe(5, 0), oe(0, 0), oe(-5, 0), oe(-15, 0),
			oe(-15, 0), oe(-5, 0), oe(0, 0), oe(5, 0), oe(5, 0), oe(0, 0), oe(-5, 0), oe(-15, 0)
	};
	private final static int knightPcsq[] = {
			oe(-49, -40), oe(-39, -30), oe(-30, -20), oe(-25, -15), oe(-25, -15), oe(-30, -20), oe(-39, -30), oe(-49, -40),
			oe(-34, -30), oe(-24, -20), oe(-15, -10), oe(-10, -5), oe(-10, -5), oe(-15, -10), oe(-24, -20), oe(-34, -30),
			oe(-20, -20), oe(-10, -10), oe(0, 0), oe(5, 5), oe(5, 5), oe(0, 0), oe(-10, -10), oe(-20, -20),
			oe(-10, -15), oe(0, -5), oe(10, 5), oe(15, 10), oe(15, 10), oe(10, 5), oe(0, -5), oe(-10, -15),
			oe(-5, -15), oe(5, -5), oe(15, 5), oe(20, 10), oe(20, 10), oe(15, 5), oe(5, -5), oe(-5, -15),
			oe(-5, -20), oe(5, -10), oe(15, 0), oe(20, 5), oe(20, 5), oe(15, 0), oe(5, -10), oe(-5, -20),
			oe(-19, -30), oe(-9, -20), oe(0, -10), oe(5, -5), oe(5, -5), oe(0, -10), oe(-9, -20), oe(-19, -30),
			oe(-134, -40), oe(-24, -30), oe(-15, -20), oe(-10, -15), oe(-10, -15), oe(-15, -20), oe(-24, -30), oe(-134, -40)
	};
	private final static int bishopPcsq[] = {
			oe(-17, -18), oe(-17, -12), oe(-16, -9), oe(-14, -6), oe(-14, -6), oe(-16, -9), oe(-17, -12), oe(-17, -18),
			oe(-7, -12), oe(1, -6), oe(-2, -3), oe(1, 0), oe(1, 0), oe(-2, -3), oe(1, -6), oe(-7, -12),
			oe(-6, -9), oe(-2, -3), oe(4, 0), oe(2, 3), oe(2, 3), oe(4, 0), oe(-2, -3), oe(-6, -9),
			oe(-4, -6), oe(1, 0), oe(2, 3), oe(8, 6), oe(8, 6), oe(2, 3), oe(1, 0), oe(-4, -6),
			oe(-4, -6), oe(1, 0), oe(2, 3), oe(8, 6), oe(8, 6), oe(2, 3), oe(1, 0), oe(-4, -6),
			oe(-6, -9), oe(-2, -3), oe(4, 0), oe(2, 3), oe(2, 3), oe(4, 0), oe(-2, -3), oe(-6, -9),
			oe(-7, -12), oe(1, -6), oe(-2, -3), oe(1, 0), oe(1, 0), oe(-2, -3), oe(1, -6), oe(-7, -12),
			oe(-7, -18), oe(-7, -12), oe(-6, -9), oe(-4, -6), oe(-4, -6), oe(-6, -9), oe(-7, -12), oe(-7, -18)
	};
	private final static int rookPcsq[] = {
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0),
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0),
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0),
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0),
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0),
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0),
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0),
			oe(-6, 0), oe(-3, 0), oe(0, 0), oe(3, 0), oe(3, 0), oe(0, 0), oe(-3, 0), oe(-6, 0)
	};
	private final static int queenPcsq[] = {
			oe(-4, -24), oe(-4, -16), oe(-5, -12), oe(-5, -8), oe(-5, -8), oe(-5, -12), oe(-4, -16), oe(-4, -24),
			oe(1, -16), oe(1, -8), oe(0, -4), oe(1, 0), oe(1, 0), oe(0, -4), oe(1, -8), oe(1, -16),
			oe(0, -12), oe(0, -4), oe(0, 0), oe(0, 4), oe(0, 4), oe(0, 0), oe(0, -4), oe(0, -12),
			oe(0, -8), oe(1, 0), oe(0, 4), oe(0, 8), oe(0, 8), oe(0, 4), oe(1, 0), oe(0, -8),
			oe(0, -8), oe(1, 0), oe(0, 4), oe(0, 8), oe(0, 8), oe(0, 4), oe(1, 0), oe(0, -8),
			oe(0, -12), oe(0, -4), oe(0, 0), oe(0, 4), oe(0, 4), oe(0, 0), oe(0, -4), oe(0, -12),
			oe(1, -16), oe(1, -8), oe(0, -4), oe(1, 0), oe(1, 0), oe(0, -4), oe(1, -8), oe(1, -16),
			oe(1, -24), oe(1, -16), oe(0, -12), oe(0, -8), oe(0, -8), oe(0, -12), oe(1, -16), oe(1, -24)
	};
	private final static int kingPcsq[] = {
			oe(41, -72), oe(51, -48), oe(30, -36), oe(10, -24), oe(10, -24), oe(30, -36), oe(51, -48), oe(41, -72),
			oe(31, -48), oe(41, -24), oe(20, -12), oe(1, 0), oe(1, 0), oe(20, -12), oe(41, -24), oe(31, -48),
			oe(10, -36), oe(20, -12), oe(0, 0), oe(-20, 12), oe(-20, 12), oe(0, 0), oe(20, -12), oe(10, -36),
			oe(0, -24), oe(11, 0), oe(-10, 12), oe(-30, 24), oe(-30, 24), oe(-10, 12), oe(11, 0), oe(0, -24),
			oe(-10, -24), oe(1, 0), oe(-20, 12), oe(-40, 24), oe(-40, 24), oe(-20, 12), oe(1, 0), oe(-10, -24),
			oe(-20, -36), oe(-10, -12), oe(-30, 0), oe(-50, 12), oe(-50, 12), oe(-30, 0), oe(-10, -12), oe(-20, -36),
			oe(-29, -48), oe(-19, -24), oe(-40, -12), oe(-59, 0), oe(-59, 0), oe(-40, -12), oe(-19, -24), oe(-29, -48),
			oe(-39, -72), oe(-29, -48), oe(-50, -36), oe(-70, -24), oe(-70, -24), oe(-50, -36), oe(-29, -48), oe(-39, -72)
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

	private long[] pawnCanAttack = {0, 0};

	// Squares surrounding King
	private long[] squaresNearKing = {0, 0};

	private long[] mobilitySquares = {0, 0};

	public CompleteEvaluator(Config config) {
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

		pawnMaterial[W] = Config.PAWN * whitePawns;
		pawnMaterial[B] = Config.PAWN * blackPawns;
		material[W] = Config.KNIGHT * whiteKnights + Config.BISHOP * whiteBishops + Config.ROOK * whiteRooks + Config.QUEEN * whiteQueens + //
				((board.whites & board.bishops & BitboardUtils.WHITE_SQUARES) != 0 //
						&& (board.whites & board.bishops & BitboardUtils.BLACK_SQUARES) != 0 ? Config.BISHOP_PAIR : 0);
		material[B] = Config.KNIGHT * blackKnights + Config.BISHOP * blackBishops + Config.ROOK * blackRooks + Config.QUEEN * blackQueens + //
				((board.blacks & board.bishops & BitboardUtils.WHITE_SQUARES) != 0 //
						&& (board.blacks & board.bishops & BitboardUtils.BLACK_SQUARES) != 0 ? Config.BISHOP_PAIR : 0);

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

		// Squares that pawns attack or can attack by advancing
		pawnCanAttack[W] = ai.pawnAttacks[W] | ai.pawnAttacks[W] << 8 | ai.pawnAttacks[W] << 16 | ai.pawnAttacks[W] << 24 | ai.pawnAttacks[W] << 32 | ai.pawnAttacks[W] << 40;
		pawnCanAttack[B] = ai.pawnAttacks[B] | ai.pawnAttacks[B] >>> 8 | ai.pawnAttacks[B] >>> 16 | ai.pawnAttacks[B] >>> 24 | ai.pawnAttacks[B] >>> 32 | ai.pawnAttacks[B] >>> 40;

		// Calculate attacks
		attacks[W] = evalAttacks(board, ai, W);
		attacks[B] = evalAttacks(board, ai, B);

		// Squares surrounding King
		squaresNearKing[W] = bbAttacks.king[ai.kingIndex[W]];
		squaresNearKing[B] = bbAttacks.king[ai.kingIndex[B]];

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
				int pcsqIndex = (isWhite ? index : 63 - index);
				int rank = index >> 3;
				int file = 7 - index & 7;

				pieceAttacks = ai.attacksFromSquare[index];

				if ((square & board.pawns) != 0) {
					center[us] += pawnPcsq[pcsqIndex];

					if ((pieceAttacks & squaresNearKing[them] & ~ai.pawnAttacks[them]) != 0) {
						kingSafety[us] += PAWN_ATTACKS_KING;
					}

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
							routeToPromotion & (board.pawns | ai.pawnAttacks[them])) != 0; // Other pawns stopping it from advance, opposing or capturing it before reaching my pawns

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

					if ((pieceAttacks & squaresNearKing[them] & ~ai.pawnAttacks[them]) != 0) {
						kingSafety[us] += KNIGHT_ATTACKS_KING;
						kingAttackersCount[us]++;
					}

					// Knight outpost: no opposite pawns can attack the square and it is defended by one of our pawns
					if ((square & ~pawnCanAttack[them] & ai.pawnAttacks[us]) != 0) {
						positional[us] += KNIGTH_OUTPOST[pcsqIndex];
					}

				} else if ((square & board.bishops) != 0) {
					center[us] += bishopPcsq[pcsqIndex];

					mobility[us] += MOBILITY[Piece.BISHOP][BitboardUtils.popCount(pieceAttacks & mobilitySquares[us])];

					if ((pieceAttacks & squaresNearKing[them] & ~~ai.pawnAttacks[them]) != 0) {
						kingSafety[us] += BISHOP_ATTACKS_KING;
						kingAttackersCount[us]++;
					}

					pieceAttacksXray = bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

					if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0) {
						mobility[us] += BISHOP_TRAPPED;
					}

				} else if ((square & board.rooks) != 0) {
					center[us] += rookPcsq[pcsqIndex];

					mobility[us] += MOBILITY[Piece.ROOK][BitboardUtils.popCount(pieceAttacks & mobilitySquares[us])];

					if ((pieceAttacks & squaresNearKing[them] & ~ai.pawnAttacks[them]) != 0) {
						kingSafety[us] += ROOK_ATTACKS_KING;
						kingAttackersCount[us]++;
					}

					pieceAttacksXray = bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

					if ((pieceAttacks & mines & board.rooks) != 0) {
						positional[us] += ROOK_CONNECT;
					}

					long rookFile = BitboardUtils.FILE[file];
					if ((rookFile & board.pawns) == 0) {
						positional[us] += ROOK_FILE_OPEN;
					} else if ((rookFile & board.pawns & mines) == 0) {
						positional[us] += ROOK_FILE_SEMIOPEN;
					}

					long rank7 = isWhite ? BitboardUtils.RANK[6] : BitboardUtils.RANK[1];
					long rank8 = isWhite ? BitboardUtils.RANK[7] : BitboardUtils.RANK[0];
					if ((square & rank7) != 0
							&& ((others & board.kings & rank8) != 0 || (others & board.pawns & rank7) != 0)) {
						positional[us] += ROOK_7;
					}

				} else if ((square & board.queens) != 0) {
					center[us] += queenPcsq[pcsqIndex];

					mobility[us] += MOBILITY[Piece.QUEEN][BitboardUtils.popCount(pieceAttacks & mobilitySquares[us])];

					if ((pieceAttacks & squaresNearKing[them] & ~ai.pawnAttacks[them]) != 0) {
						kingSafety[us] += QUEEN_ATTACKS_KING;
						kingAttackersCount[us]++;
					}

					pieceAttacksXray = (bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) |
							bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)))
							& ~pieceAttacks;
					if ((pieceAttacksXray & board.kings & others) != 0) {
						attacks[us] += PINNED_PIECE;
					}

				} else if ((square & board.kings) != 0) {
					center[us] += kingPcsq[pcsqIndex];

					// If king is in the first or second rank, we add the pawn shield
					if ((square & (isWhite ? BitboardUtils.RANK[0] | BitboardUtils.RANK[1] : BitboardUtils.RANK[6] | BitboardUtils.RANK[7])) != 0) {
						kingDefense[us] += KING_PAWN_SHIELD * BitboardUtils.popCount(pieceAttacks & mines & board.pawns);
					}
				}
			}
			square <<= 1;
		}

		// Ponder opening and Endgame value depending of the non-pawn pieces:
		// opening=> gamephase = 255 / ending => gamephase ~= 0
		int nonPawnMaterial = material[W] + material[B];
		int gamePhase = nonPawnMaterial >= Config.NON_PAWN_MATERIAL_MIDGAME_MAX ? 256 :
				nonPawnMaterial <= Config.NON_PAWN_MATERIAL_ENDGAME_MIN ? 0 :
						((nonPawnMaterial - Config.NON_PAWN_MATERIAL_ENDGAME_MIN) << 8) / (Config.NON_PAWN_MATERIAL_MIDGAME_MAX - Config.NON_PAWN_MATERIAL_ENDGAME_MIN);

		int value = 0;
		// First Material
		value += pawnMaterial[W] - pawnMaterial[B] + material[W] - material[B];
		// Tempo
		value += (board.getTurn() ? TEMPO : -TEMPO);

		int oe = oeMul(config.getEvalCenter(), center[W] - center[B])
				+ oeMul(config.getEvalPositional(), positional[W] - positional[B])
				+ oeMul(config.getEvalAttacks(), attacks[W] - attacks[B])
				+ oeMul(config.getEvalMobility(), mobility[W] - mobility[B])
				+ oeMul(config.getEvalPawnStructure(), pawnStructure[W] - pawnStructure[B])
				+ oeMul(config.getEvalPassedPawns(), passedPawns[W] - passedPawns[B])
				+ oeMul(config.getEvalKingSafety(), kingDefense[W] - kingDefense[B])
				+ oeMul(config.getEvalKingSafety() >>> 3, (KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B]));

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
			logger.debug("kingSafety x8     = " + formatOE(KING_SAFETY_PONDER[kingAttackersCount[W]] * kingSafety[W] - KING_SAFETY_PONDER[kingAttackersCount[B]] * kingSafety[B]));
			logger.debug("kingDefense       = " + formatOE(kingDefense[W] - kingDefense[B]));
			logger.debug("value             = " + value);
		}
		assert Math.abs(value) < Evaluator.KNOWN_WIN : "Eval is outside limits";
		return value;
	}

	private int evalAttacks(Board board, AttacksInfo ai, int us) {
		int attacks = 0;
		long others = (us == 0 ? board.blacks : board.whites);

		long attackedByPawn = others & ai.pawnAttacks[us];
		while (attackedByPawn != 0) {
			long lsb = BitboardUtils.lsb(attackedByPawn);
			attacks += PAWN_ATTACKS[board.getPieceIntAt(lsb)];
			attackedByPawn &= ~lsb;
		}

		long otherWeak = others & ai.attackedSquares[us] & ~ai.pawnAttacks[1 - us];
		if (otherWeak != 0) {
			long attackedByMinor = otherWeak & (ai.knightAttacks[us] | ai.bishopAttacks[us]);
			while (attackedByMinor != 0) {
				long lsb = BitboardUtils.lsb(attackedByMinor);
				attacks += MINOR_ATTACKS[board.getPieceIntAt(lsb)];
				attackedByMinor &= ~lsb;
			}
			long attackedByMajor = otherWeak & (ai.rookAttacks[us] | ai.queenAttacks[us]);
			while (attackedByMajor != 0) {
				long lsb = BitboardUtils.lsb(attackedByMajor);
				attacks += MAJOR_ATTACKS[board.getPieceIntAt(lsb)];
				attackedByMajor &= ~lsb;
			}
		}

		long superiorAttacks = others & ai.pawnAttacks[us] & (board.knights | board.bishops | board.rooks | board.queens)
				| others & (ai.knightAttacks[us] | ai.bishopAttacks[us]) & (board.rooks | board.queens)
				| others & ai.rookAttacks[us] & board.queens;
		int superiorAttacksCount = BitboardUtils.popCount(superiorAttacks);
		if (superiorAttacksCount >= 2) {
			attacks += superiorAttacksCount * HUNG_PIECES;
		}

		return attacks;
	}
}