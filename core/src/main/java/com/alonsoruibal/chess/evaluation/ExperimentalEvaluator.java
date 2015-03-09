package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.util.StringUtils;

/**
 * Evaluation is done in centipawns
 * <p/>
 * TODO: bishop / knights / rook traps: revise
 * TODO: test knights and bishops only forward mobility
 *
 * @author rui
 */
public class ExperimentalEvaluator extends Evaluator {
	private static final Logger logger = Logger.getLogger("ExperimentalEvaluator");

	public final static int PAWN = 100;
	public final static int KNIGHT = 325;
	public final static int BISHOP = 325;
	public final static int BISHOP_PAIR = 50; // Bonus by having two bishops in different colors
	public final static int ROOK = 500;
	public final static int QUEEN = 975;

	public final static int[] PIECE_VALUES = {0, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, 9999};

	private final static int OPENING = 0;
	private final static int ENDGAME = 1;

	// Bishops
	private final static int BISHOP_M = oe(5, 5); // Mobility units: this value is added for each destination square not occupied by one of our pieces or attacked by opposite pawns
	private final static int BISHOP_ATTACKS_KING = oe(2, 1);
	private final static int BISHOP_DEFENDS_KING = oe(2, 1);
	private final static int BISHOP_ATTACKS_PU_P = oe(3, 4);
	private final static int BISHOP_ATTACKS_PU_K = oe(5, 5);
	private final static int BISHOP_ATTACKS_RQ = oe(7, 10);
	private final static int BISHOP_PAWN_IN_COLOR = oe(1, 1); // Sums for each of our pawns (or opposite/2) in the bishop color
	private final static int BISHOP_FORWARD_P_PU = oe(0, 2); // Sums for each of the undefended opposite pawns forward
	private final static int BISHOP_OUTPOST = oe(1, 2); // Only if defended by pawn
	private final static int BISHOP_OUTPOST_ATT_NK_PU = oe(3, 4); // attacks squares Near King or other opposite pieces Pawn Undefended

	private final static int BISHOP_TRAPPED = oe(-40, -40);

	// Knights
	private final static int KNIGHT_M = oe(6, 8);
	private final static int KNIGHT_ATTACKS_KING = oe(4, 2);
	private final static int KNIGHT_DEFENDS_KING = oe(4, 2);
	private final static int KNIGHT_ATTACKS_PU_P = oe(3, 4);
	private final static int KNIGHT_ATTACKS_PU_B = oe(5, 5);
	private final static int KNIGHT_ATTACKS_RQ = oe(7, 10);
	private final static int KNIGHT_OUTPOST = oe(2, 3); // Adds one time if no opposite can can attack out knight and twice if it is defended by one of our pawns

	// Rooks
	private final static int ROOK_M = oe(2, 3);
	private final static int ROOK_ATTACKS_KING = oe(3, 1);
	private final static int ROOK_DEFENDS_KING = oe(3, 1);
	private final static int ROOK_ATTACKS_PU_P = oe(2, 3); // Attacks pawn not defended by pawn (PU=Pawn Undefended)
	private final static int ROOK_ATTACKS_PU_BK = oe(4, 5); // Attacks bishop or knight not defended by pawn
	private final static int ROOK_ATTACKS_Q = oe(5, 5); // Attacks queen
	private final static int ROOK_COLUMN_OPEN_NO_MG = oe(20, 10); // No pawns in rook column and no minor guarded
	private final static int ROOK_COLUMN_OPEN_MG_NP = oe(10, 0); // No pawns in rook column and minor guarded, my pawn can attack
	private final static int ROOK_COLUMN_OPEN_MG_P = oe(15, 5); // No pawns in rook column and minor guarded, my pawn can attack
	private final static int ROOK_COLUMN_SEMIOPEN = oe(3, 6); // No pawns mines in column
	private final static int ROOK_COLUMN_SEMIOPEN_BP = oe(15, 5); // And attacks a backward pawn
	private final static int ROOK_COLUMN_SEMIOPEN_K = oe(3, 6); // No pawns mines in column and opposite king
	private final static int ROOK_8_KING_8 = oe(5, 10); // Rook in 8th rank and opposite king in 8th rank
	private final static int ROOK_7_KP_78 = oe(10, 30); // Rook in 8th rank and opposite king/pawn in 7/8th rank
	private final static int ROOK_7_P_78_K_8_RQ_7 = oe(10, 20); // Rook in 7th rank and opposite king in 8th and attacked opposite queen/rook on 7th
	private final static int ROOK_6_KP_678 = oe(5, 15); // Rook in 7th rank and opposite king/pawn in 6/7/8th
	private final static int ROOK_OUTPOST = oe(1, 2); // Only if defended by pawn
	private final static int ROOK_OUTPOST_ATT_NK_PU = oe(3, 4); // Also attacks other piece not defended by pawn or a square near king

	// Queen
	private final static int QUEEN_M = oe(2, 2);
	private final static int QUEEN_ATTACKS_KING = oe(5, 2);
	private final static int QUEEN_DEFENDS_KING = oe(5, 2);
	private final static int QUEEN_ATTACKS_PU = oe(4, 4);
	private final static int QUEEN_7_KP_78 = oe(5, 25); // Queen in 8th rank and opposite king/pawn in 7/8th rank
	private final static int QUEEN_7_P_78_K_8_R_7 = oe(10, 15); // Queen in 7th my root in 7th defending queen and opposite king in 8th

	// King
	private final static int KING_PAWN_SHIELD = oe(5, 0);  // Protection: sums for each pawn near king

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
	private static final int PINNED_PIECE = oe(25, 35);

	// Tempo
	public final static int TEMPO = 9; // Add to moving side score

	private final static long[] OUTPOST_MASK = {0x00007e7e7e000000L, 0x0000007e7e7e0000L};

	private final static int[] KNIGHT_OUTPOST_ATTACKS_NK_PU = { // Knight outpost attacks squares Near King or other opposite pieces Pawn Undefended
			0, 0, 0, 0, 0, 0, 0, 0, //
			0, 0, 0, 0, 0, 0, 0, 0, //
			0, 0, 0, 0, 0, 0, 0, 0, //
			0, oe(7, 7), oe(7, 7), oe(10, 10), oe(10, 10), oe(7, 7), oe(7, 7), 0, //
			0, oe(5, 5), oe(5, 5), oe(8, 8), oe(8, 8), oe(5, 5), oe(5, 5), 0, //
			0, 0, oe(5, 5), oe(8, 8), oe(8, 8), oe(5, 5), 0, 0, //
			0, 0, 0, 0, 0, 0, 0, 0, //
			0, 0, 0, 0, 0, 0, 0, 0 //
	};

	private final static long[] BISHOP_TRAPPING = { //
			0x00, 1L << 10, 0x00, 0x00, 0x00, 0x00, 1L << 13, 0x00, //
			1L << 17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 22, //
			1L << 25, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 30, //
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
			1L << 33, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 38, //
			1L << 41, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 1L << 46, //
			0x00, 1L << 50, 0x00, 0x00, 0x00, 0x00, 1L << 53, 0x00 //
	};

	// Ponder kings attacks by the number of attackers (not pawns)
	private final static int[] KING_SAFETY_PONDER = {0, 1, 2, 4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};

	private final static int[][] PawnColumn = {{-20, -8, -2, 5, 5, -2, -8, -20}, //
			{-4, -6, -8, -10, -10, -8, -6, -4}};
	private final static int[][] PawnRank = {{0, -3, -2, -1, 1, 2, 3, 0}, //
			{0, -3, -3, -2, -1, 0, 2, 0}};
	private final static int[][] KnightColumn = {{-26, -10, 1, 5, 5, 1, -10, -26}, //
			{-4, -1, 2, 4, 4, 2, -1, -4}};
	private final static int[][] KnightRank = {{-32, -10, 6, 15, 21, 19, 10, -11}, //
			{-10, -5, -2, 1, 3, 5, 2, -3}};
	private final static int[][] KnightLine = {{0, 0, 0, 0, 0, 0, 0, 0}, //
			{2, 1, 0, -1, -2, -4, -7, -10}};
	private final static int[][] BishopLine = {{10, 5, 1, -3, -5, -7, -8, -12}, //
			{3, 2, 0, 0, -2, -2, -3, -3}};
	private final static int[][] BishopRank = {{-5, 0, 0, 0, 0, 0, 0, 0}, //
			{0, 0, 0, 0, 0, 0, 0, 0}};
	private final static int[][] RookColumn = {{-4, 0, 4, 8, 8, 4, 0, -4}, //
			{0, 0, 0, 0, 0, 0, 0, 0}};
	private final static int[][] RookRank = {{0, 0, 0, 0, 0, 0, 0, 0}, //
			{0, 0, 0, 0, 1, 1, 1, -2}};
	private final static int[][] QueenColumn = {{-2, 0, 1, 2, 2, 1, 0, -2}, //
			{-2, 0, 1, 2, 2, 1, 0, -2}};
	private final static int[][] QueenRank = {{-2, 0, 1, 2, 2, 1, 0, -2}, //
			{-2, 0, 1, 2, 2, 1, 0, -2}};
	private final static int[][] QueenLine = {{3, 2, 1, 0, -2, -4, -7, -10}, //
			{1, 0, -1, -3, -4, -6, -8, -12}};
	private final static int[][] KingColumn = {{40, 45, 15, -5, -5, 15, 45, 40}, //
			{-15, 0, 10, 15, 15, 10, 0, -15}};
	private final static int[][] KingRank = {{4, 1, -2, -5, -10, -15, -25, -35}, //
			{-15, 0, 10, 15, 15, 10, 0, -15}};
	private final static int[][] KingLine = {{0, 0, 0, 0, 0, 0, 0, 0}, //
			{2, 0, -2, -5, -8, -12, -20, -30}};

	// Values are rotated for whites, so when white is playing is like shown in the code
	public final static int[] pawnIndexValue = new int[64];
	public final static int[] knightIndexValue = new int[64];
	public final static int[] bishopIndexValue = new int[64];
	public final static int[] rookIndexValue = new int[64];
	public final static int[] queenIndexValue = new int[64];
	public final static int[] kingIndexValue = new int[64];

	static {
		// Initialize Piece square values
		int i;

		for (i = 0; i < 64; i++) {
			int rank = i >> 3;
			int column = 7 - i & 7;
			int d = (((column - rank) >= 0) ? (column - rank) : -(column - rank));
			int e = (((column + rank - 7) >= 0) ? (column + rank - 7) : -(column + rank - 7));

			pawnIndexValue[i] = oe(PawnColumn[OPENING][column] + PawnRank[OPENING][rank],
					PawnColumn[ENDGAME][column] + PawnRank[ENDGAME][rank]);
			knightIndexValue[i] = oe(KnightColumn[OPENING][column] + KnightRank[OPENING][rank] + KnightLine[OPENING][d] + KnightLine[OPENING][e],
					KnightColumn[ENDGAME][column] + KnightRank[ENDGAME][rank] + KnightLine[ENDGAME][d] + KnightLine[ENDGAME][e]);
			bishopIndexValue[i] = oe(BishopRank[OPENING][rank] + BishopLine[OPENING][d] + BishopLine[OPENING][e],
					BishopRank[ENDGAME][rank] + BishopLine[ENDGAME][d] + BishopLine[ENDGAME][e]);
			rookIndexValue[i] = oe(RookColumn[OPENING][column] + RookRank[OPENING][rank],
					RookColumn[ENDGAME][column] + RookRank[ENDGAME][rank]);
			queenIndexValue[i] = oe(QueenColumn[OPENING][column] + QueenRank[OPENING][rank] + QueenLine[OPENING][d] + QueenLine[OPENING][e],
					QueenColumn[ENDGAME][column] + QueenRank[ENDGAME][rank] + QueenLine[ENDGAME][d] + QueenLine[ENDGAME][e]);
			kingIndexValue[i] = oe(KingColumn[OPENING][column] + KingRank[OPENING][rank] + KingLine[OPENING][d] + KingLine[OPENING][e],
					KingColumn[ENDGAME][column] + KingRank[ENDGAME][rank] + KingLine[ENDGAME][d] + KingLine[ENDGAME][e]);
		}
	}

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

	// Squares attackeds by pawns
	private long[] pawnAttacks = {0, 0};
	private long[] pawnCanAttack = {0, 0};

	private long[] minorPiecesDefendedByPawns = {0, 0};

	// Squares surrounding King
	private long[] squaresNearKing = {0, 0};

	public ExperimentalEvaluator(Config config) {
		this.config = config;
	}

	public int evaluate(Board board, AttacksInfo attacksInfo) {
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

		pawnMaterial[0] = PAWN * whitePawns;
		pawnMaterial[1] = PAWN * blackPawns;
		material[0] = KNIGHT * whiteKnights + BISHOP * whiteBishops + ROOK * whiteRooks + QUEEN * whiteQueens + //
				((board.whites & board.bishops & BitboardUtils.WHITE_SQUARES) != 0 && //
						(board.whites & board.bishops & BitboardUtils.BLACK_SQUARES) != 0 ? BISHOP_PAIR : 0);
		material[1] = KNIGHT * blackKnights + BISHOP * blackBishops + ROOK * blackRooks + QUEEN * blackQueens + //
				((board.blacks & board.bishops & BitboardUtils.WHITE_SQUARES) != 0 && //
						(board.blacks & board.bishops & BitboardUtils.BLACK_SQUARES) != 0 ? BISHOP_PAIR : 0);

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
		kingDefense[0] = 0;
		kingDefense[1] = 0;
		pawnStructure[0] = 0;
		pawnStructure[1] = 0;
		passedPawns[0] = 0;
		passedPawns[1] = 0;

		superiorPieceAttacked[0] = 0;
		superiorPieceAttacked[1] = 0;

		// Squares attacked by pawns
		pawnAttacks[0] = ((board.pawns & board.whites & ~BitboardUtils.b_l) << 9) | ((board.pawns & board.whites & ~BitboardUtils.b_r) << 7);
		pawnAttacks[1] = ((board.pawns & board.blacks & ~BitboardUtils.b_r) >>> 9) | ((board.pawns & board.blacks & ~BitboardUtils.b_l) >>> 7);

		// Squares that pawns attack or can attack by advancing
		pawnCanAttack[0] = pawnAttacks[0] | pawnAttacks[0] << 8 | pawnAttacks[0] << 16 | pawnAttacks[0] << 24 | pawnAttacks[0] << 32 | pawnAttacks[0] << 40;
		pawnCanAttack[1] = pawnAttacks[1] | pawnAttacks[1] >>> 8 | pawnAttacks[1] >>> 16 | pawnAttacks[1] >>> 24 | pawnAttacks[1] >>> 32 | pawnAttacks[1] >>> 40;

		// Initialize attacks with pawn attacks
		attacks[0] = PAWN_ATTACKS_KNIGHT * BitboardUtils.popCount(pawnAttacks[0] & board.knights & board.blacks) + //
				PAWN_ATTACKS_BISHOP * BitboardUtils.popCount(pawnAttacks[0] & board.bishops & board.blacks) + //
				PAWN_ATTACKS_ROOK * BitboardUtils.popCount(pawnAttacks[0] & board.rooks & board.blacks) + //
				PAWN_ATTACKS_QUEEN * BitboardUtils.popCount(pawnAttacks[0] & board.queens & board.blacks);
		attacks[1] = PAWN_ATTACKS_KNIGHT * BitboardUtils.popCount(pawnAttacks[1] & board.knights & board.whites) + //
				PAWN_ATTACKS_BISHOP * BitboardUtils.popCount(pawnAttacks[1] & board.bishops & board.whites) + //
				PAWN_ATTACKS_ROOK * BitboardUtils.popCount(pawnAttacks[1] & board.rooks & board.whites) + //
				PAWN_ATTACKS_QUEEN * BitboardUtils.popCount(pawnAttacks[1] & board.queens & board.whites);

		minorPiecesDefendedByPawns[0] = board.whites & (board.bishops | board.knights) & pawnAttacks[0];
		minorPiecesDefendedByPawns[1] = board.blacks & (board.bishops | board.knights) & pawnAttacks[1];

		// Squares surrounding King
		squaresNearKing[0] = bbAttacks.king[BitboardUtils.square2Index(board.whites & board.kings)] | board.whites & board.kings;
		squaresNearKing[1] = bbAttacks.king[BitboardUtils.square2Index(board.blacks & board.kings)] | board.blacks & board.kings;

		attacksInfo.build(board);

		long all = board.getAll();
		long pieceAttacks, pieceAttacksXray, auxLong, auxLong2;
		long square = 1;
		for (int index = 0; index < 64; index++) {
			if ((square & all) != 0) {
				boolean isWhite = ((board.whites & square) != 0);
				int color = (isWhite ? 0 : 1);
				long mines = (isWhite ? board.whites : board.blacks);
				long others = (isWhite ? board.blacks : board.whites);
				long otherPawnAttacks = (isWhite ? pawnAttacks[1] : pawnAttacks[0]);
				int pcsqIndex = (isWhite ? index : 63 - index);
				int rank = index >> 3;
				int column = 7 - index & 7;

				pieceAttacks = attacksInfo.attacksFromSquare[index];

				if ((square & board.pawns) != 0) {
					center[color] += pawnIndexValue[pcsqIndex];

					if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0) {
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
								((routeToPromotion & attacksInfo.attackedSquares[1 - color]) | ((backColumn & (board.rooks | board.queens) & others) != 0 ? routeToPromotion : 0)) &
										~((routeToPromotion & attacksInfo.attackedSquares[color]) | ((backColumn & (board.rooks | board.queens) & mines) != 0 ? routeToPromotion : 0));
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
					center[color] += knightIndexValue[pcsqIndex];

					// Only mobility forward
					mobility[color] += KNIGHT_M * BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks &
							BitboardUtils.RANKS_FORWARD[color][rank]);

					if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0) {
						kingSafety[color] += KNIGHT_ATTACKS_KING;
						kingAttackersCount[color]++;
					}
					if ((pieceAttacks & squaresNearKing[color]) != 0) {
						kingDefense[color] += KNIGHT_DEFENDS_KING;
					}

					if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0) {
						attacks[color] += KNIGHT_ATTACKS_PU_P;
					}
					if ((pieceAttacks & board.bishops & others & ~otherPawnAttacks) != 0) {
						attacks[color] += KNIGHT_ATTACKS_PU_B;
					}
					if ((pieceAttacks & (board.rooks | board.queens) & others) != 0) {
						attacks[color] += KNIGHT_ATTACKS_RQ;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens);

					// Knight outpost: no opposite pawns can attack the square
					if ((square & OUTPOST_MASK[color] & ~pawnCanAttack[1 - color]) != 0) {
						positional[color] += KNIGHT_OUTPOST;
						// Defended by one of our pawns
						if ((square & pawnAttacks[color]) != 0) {
							positional[color] += KNIGHT_OUTPOST;
							// Attacks squares near king or other pieces pawn undefended
							if ((pieceAttacks & (squaresNearKing[1 - color] | others) & ~otherPawnAttacks) != 0) {
								positional[color] += KNIGHT_OUTPOST_ATTACKS_NK_PU[pcsqIndex];
							}
						}
					}

				} else if ((square & board.bishops) != 0) {
					center[color] += bishopIndexValue[pcsqIndex];

					mobility[color] += BISHOP_M * BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks &
							BitboardUtils.RANKS_FORWARD[color][rank]);

					if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0) {
						kingSafety[color] += BISHOP_ATTACKS_KING;
						kingAttackersCount[color]++;
					}
					if ((pieceAttacks & squaresNearKing[color]) != 0) {
						kingDefense[color] += BISHOP_DEFENDS_KING;
					}

					if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0) {
						attacks[color] += BISHOP_ATTACKS_PU_P;
					}
					if ((pieceAttacks & board.knights & others & ~otherPawnAttacks) != 0) {
						attacks[color] += BISHOP_ATTACKS_PU_K;
					}
					if ((pieceAttacks & (board.rooks | board.queens) & others) != 0) {
						attacks[color] += BISHOP_ATTACKS_RQ;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens);

					pieceAttacksXray = bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0) {
						attacks[color] += PINNED_PIECE;
					}

					// Bishop Outpost: no opposite pawns can attack the square and defended by one of our pawns
					if ((square & OUTPOST_MASK[color] & ~pawnCanAttack[1 - color] & pawnAttacks[color]) != 0) {
						positional[color] += BISHOP_OUTPOST;
						// Attacks squares near king or other pieces pawn undefended
						if ((pieceAttacks & (squaresNearKing[1 - color] | others) & ~otherPawnAttacks) != 0) {
							positional[color] += BISHOP_OUTPOST_ATT_NK_PU;
						}
					}

					// auxLong = pawns in bishop color
					if ((square & BitboardUtils.WHITE_SQUARES) != 0) {
						auxLong = BitboardUtils.WHITE_SQUARES & board.pawns;
					} else {
						auxLong = BitboardUtils.BLACK_SQUARES & board.pawns;
					}

					positional[color] += BISHOP_PAWN_IN_COLOR * (BitboardUtils.popCount(auxLong & mines) + BitboardUtils.popCount(auxLong & others) >>> 1)
							+ BISHOP_FORWARD_P_PU * (BitboardUtils.popCount(auxLong & others & BitboardUtils.RANKS_FORWARD[color][rank]) >>> 1);

					if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0) {
						mobility[color] += BISHOP_TRAPPED;
						// TODO protection
					}

				} else if ((square & board.rooks) != 0) {
					center[color] += rookIndexValue[pcsqIndex];

					mobility[color] += ROOK_M * BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks);

					if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0) {
						kingSafety[color] += ROOK_ATTACKS_KING;
						kingAttackersCount[color]++;
					}
					if ((pieceAttacks & squaresNearKing[color]) != 0) {
						kingDefense[color] += ROOK_DEFENDS_KING;
					}

					if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0) {
						attacks[color] += ROOK_ATTACKS_PU_P;
					}
					if ((pieceAttacks & (board.bishops | board.knights) & others & ~otherPawnAttacks) != 0) {
						attacks[color] += ROOK_ATTACKS_PU_BK;
					}
					if ((pieceAttacks & board.queens & others) != 0) {
						attacks[color] += ROOK_ATTACKS_Q;
					}

					superiorPieceAttacked[color] |= pieceAttacks & others & board.queens;

					pieceAttacksXray = bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) & ~pieceAttacks;
					if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0) {
						attacks[color] += PINNED_PIECE;
					}

					auxLong = (isWhite ? BitboardUtils.b_u : BitboardUtils.b_d);
					if ((square & auxLong) != 0 && (others & board.kings & auxLong) != 0) {
						positional[color] += ROOK_8_KING_8;
					}

					if ((square & (isWhite ? BitboardUtils.r2_u : BitboardUtils.r2_d)) != 0
							& (others & (board.kings | board.pawns) & (isWhite ? BitboardUtils.b2_u : BitboardUtils.b2_d)) != 0) {
						positional[color] += ROOK_7_KP_78;

						if ((others & board.kings & auxLong) != 0 && (pieceAttacks & others & (board.queens | board.rooks) & (isWhite ? BitboardUtils.r2_u : BitboardUtils.r2_d)) != 0) {
							positional[color] += ROOK_7_P_78_K_8_RQ_7;
						}
					}

					if ((square & (isWhite ? BitboardUtils.r3_u : BitboardUtils.r3_d)) != 0
							& (others & (board.kings | board.pawns) & (isWhite ? BitboardUtils.b3_u : BitboardUtils.b3_d)) != 0) {
						positional[color] += ROOK_6_KP_678;
					}

					auxLong = BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color][rank];
					if ((auxLong & board.pawns & mines) == 0) {
						positional[color] += ROOK_COLUMN_SEMIOPEN;
						if ((auxLong & board.pawns) == 0) {
							if ((auxLong & minorPiecesDefendedByPawns[1 - color]) == 0) {
								positional[color] += ROOK_COLUMN_OPEN_NO_MG;
							} else {
								if ((auxLong & minorPiecesDefendedByPawns[1 - color] & pawnCanAttack[color]) == 0) {
									positional[color] += ROOK_COLUMN_OPEN_MG_NP;
								} else {
									positional[color] += ROOK_COLUMN_OPEN_MG_P;
								}
							}
						} else {
							// There is an opposite backward pawn
							if ((auxLong & board.pawns & others & pawnCanAttack[1 - color]) == 0) {
								positional[color] += ROOK_COLUMN_SEMIOPEN_BP;
							}
						}

						if ((auxLong & board.kings & others) != 0) {
							positional[color] += ROOK_COLUMN_SEMIOPEN_K;
						}
					}
					// Rook Outpost: no opposite pawns can attack the square and defended by one of our pawns
					if ((square & OUTPOST_MASK[color] & ~pawnCanAttack[1 - color] & pawnAttacks[color]) != 0) {
						positional[color] += ROOK_OUTPOST;
						// Attacks squares near king or other pieces pawn undefended
						if ((pieceAttacks & (squaresNearKing[1 - color] | others) & ~otherPawnAttacks) != 0) {
							positional[color] += ROOK_OUTPOST_ATT_NK_PU;
						}
					}

				} else if ((square & board.queens) != 0) {
					center[color] += queenIndexValue[pcsqIndex];

					mobility[color] += QUEEN_M * BitboardUtils.popCount(pieceAttacks & ~mines & ~otherPawnAttacks);

					if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0) {
						kingSafety[color] += QUEEN_ATTACKS_KING;
						kingAttackersCount[color]++;
					}
					if ((pieceAttacks & squaresNearKing[color]) != 0) {
						kingDefense[color] += QUEEN_DEFENDS_KING;
					}
					if ((pieceAttacks & others & ~otherPawnAttacks) != 0) {
						attacks[color] += QUEEN_ATTACKS_PU;
					}

					pieceAttacksXray = (bbAttacks.getRookAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)) |
							bbAttacks.getBishopAttacks(index, all & ~(pieceAttacks & others & ~board.pawns)))
							& ~pieceAttacks;
					if ((pieceAttacksXray & board.kings & others) != 0) {
						attacks[color] += PINNED_PIECE;
					}

					auxLong = (isWhite ? BitboardUtils.b_u : BitboardUtils.b_d);
					auxLong2 = (isWhite ? BitboardUtils.r2_u : BitboardUtils.r2_d);
					if ((square & auxLong2) != 0 && (others & (board.kings | board.pawns) & (auxLong | auxLong2)) != 0) {
						attacks[color] += QUEEN_7_KP_78;
						if ((board.rooks & mines & auxLong2 & pieceAttacks) != 0
								&& (board.kings & others & auxLong) != 0) {
							positional[color] += QUEEN_7_P_78_K_8_R_7;
						}
					}

				} else if ((square & board.kings) != 0) {
					center[color] += kingIndexValue[pcsqIndex];

					// If king is in the first rank, we add the pawn shield
					if ((square & (isWhite ? BitboardUtils.RANK[0] : BitboardUtils.RANK[7])) != 0) {
						kingDefense[color] += KING_PAWN_SHIELD * BitboardUtils.popCount(pieceAttacks & mines & board.pawns);
					}
				}
			}
			square <<= 1;
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

		int supAttWhite = BitboardUtils.popCount(superiorPieceAttacked[0]);
		int supAttBlack = BitboardUtils.popCount(superiorPieceAttacked[1]);
		int hungPieces = (supAttWhite >= 2 ? supAttWhite * HUNG_PIECES : 0) - (supAttBlack >= 2 ? supAttBlack * HUNG_PIECES : 0);

		int oe = config.getEvalCenter() * (center[0] - center[1])
				+ config.getEvalPositional() * (positional[0] - positional[1])
				+ config.getEvalAttacks() * (attacks[0] - attacks[1] + hungPieces)
				+ config.getEvalMobility() * (mobility[0] - mobility[1])
				+ config.getEvalPawnStructure() * (pawnStructure[0] - pawnStructure[1])
				+ config.getEvalPassedPawns() * (passedPawns[0] - passedPawns[1])
				+ config.getEvalKingSafety() * (kingDefense[0] - kingDefense[1])
				+ config.getEvalKingSafety() * (KING_SAFETY_PONDER[kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]);

		value += (gamePhase * o(oe)) / (256 * 100); // divide by 256
		value += ((256 - gamePhase) * e(oe)) / (256 * 100);

		if (debug) {
			logger.debug(debugSB);

			logger.debug("material          = " + (material[0] - material[1]));
			logger.debug("pawnMaterial      = " + (pawnMaterial[0] - pawnMaterial[1]));
			logger.debug("centerOpening     = " + formatOE(center[0] - center[1]));
			logger.debug("positional        = " + formatOE(positional[0] - positional[1]));
			logger.debug("attacks           = " + formatOE(attacks[0] - attacks[1]));
			logger.debug("mobility          = " + formatOE(mobility[0] - mobility[1]));
			logger.debug("pawnStructure     = " + formatOE(pawnStructure[0] - pawnStructure[1]));
			logger.debug("passedPawns       = " + formatOE(passedPawns[0] - passedPawns[1]));
			logger.debug("kingSafety        = " + formatOE(KING_SAFETY_PONDER[kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]));
			logger.debug("kingDefense       = " + formatOE(kingDefense[0] - kingDefense[1]));
			logger.debug("gamePhase         = " + gamePhase);
			logger.debug("tempo             = " + (board.getTurn() ? TEMPO : -TEMPO));
			logger.debug("value             = " + value);
		}
		assert Math.abs(value) < Evaluator.KNOWN_WIN : "Eval is outside limits";
		return value;
	}

	private String formatOE(int value) {
		return StringUtils.padLeft(String.valueOf(o(value)), 6) + " " + StringUtils.padLeft(String.valueOf(e(value)), 6);
	}
}