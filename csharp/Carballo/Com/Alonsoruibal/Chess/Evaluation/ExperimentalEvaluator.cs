/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Bitboard;
using Com.Alonsoruibal.Chess.Evaluation;
using Com.Alonsoruibal.Chess.Log;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Evaluation
{
	/// <summary>
	/// Evaluation is done in centipawns
	/// TODO: hung pieces & x-ray attacks: revise
	/// TODO: bishop / knights / rook traps: revise
	/// TODO: test knights and bishops only forward mobility
	/// TODO: test Runner
	/// TODO PAWNS
	/// </summary>
	/// <author>rui</author>
	public class ExperimentalEvaluator : Evaluator
	{
		/// <summary>Logger for this class</summary>
		private static readonly Logger logger = Logger.GetLogger("ExperimentalEvaluator");

		internal Config config;

		public bool debug = false;

		public ExperimentalEvaluator(Config config)
		{
			this.config = config;
		}

		private const int PAWN = 100;

		private const int KNIGHT = 325;

		private const int BISHOP = 325;

		private const int BISHOP_PAIR = 50;

		private const int ROOK = 500;

		private const int QUEEN = 975;

		private const int OPENING = 0;

		private const int ENDGAME = 1;

		private static readonly int BISHOP_M = Oe(5, 5);

		private static readonly int BISHOP_ATTACKS_KING = Oe(2, 1);

		private static readonly int BISHOP_DEFENDS_KING = Oe(2, 1);

		private static readonly int BISHOP_ATTACKS_PU_P = Oe(3, 4);

		private static readonly int BISHOP_ATTACKS_PU_K = Oe(5, 5);

		private static readonly int BISHOP_ATTACKS_RQ = Oe(7, 10);

		private static readonly int BISHOP_PAWN_IN_COLOR = Oe(1, 1);

		private static readonly int BISHOP_FORWARD_P_PU = Oe(0, 2);

		private static readonly int BISHOP_OUTPOST = Oe(1, 2);

		private static readonly int BISHOP_OUTPOST_ATT_NK_PU = Oe(3, 4);

		private static readonly int BISHOP_TRAPPED = Oe(-40, -40);

		private static readonly int KNIGHT_M = Oe(6, 8);

		private static readonly int KNIGHT_ATTACKS_KING = Oe(4, 2);

		private static readonly int KNIGHT_DEFENDS_KING = Oe(4, 2);

		private static readonly int KNIGHT_ATTACKS_PU_P = Oe(3, 4);

		private static readonly int KNIGHT_ATTACKS_PU_B = Oe(5, 5);

		private static readonly int KNIGHT_ATTACKS_RQ = Oe(7, 10);

		private static readonly int KNIGHT_OUTPOST = Oe(2, 3);

		private static readonly int ROOK_M = Oe(2, 3);

		private static readonly int ROOK_ATTACKS_KING = Oe(3, 1);

		private static readonly int ROOK_DEFENDS_KING = Oe(3, 1);

		private static readonly int ROOK_ATTACKS_PU_P = Oe(2, 3);

		private static readonly int ROOK_ATTACKS_PU_BK = Oe(4, 5);

		private static readonly int ROOK_ATTACKS_Q = Oe(5, 5);

		private static readonly int ROOK_COLUMN_OPEN_NO_MG = Oe(20, 10);

		private static readonly int ROOK_COLUMN_OPEN_MG_NP = Oe(10, 0);

		private static readonly int ROOK_COLUMN_OPEN_MG_P = Oe(15, 5);

		private static readonly int ROOK_COLUMN_SEMIOPEN = Oe(3, 6);

		private static readonly int ROOK_COLUMN_SEMIOPEN_BP = Oe(15, 5);

		private static readonly int ROOK_COLUMN_SEMIOPEN_K = Oe(3, 6);

		private static readonly int ROOK_8_KING_8 = Oe(5, 10);

		private static readonly int ROOK_7_KP_78 = Oe(10, 30);

		private static readonly int ROOK_7_P_78_K_8_RQ_7 = Oe(10, 20);

		private static readonly int ROOK_6_KP_678 = Oe(5, 15);

		private static readonly int ROOK_OUTPOST = Oe(1, 2);

		private static readonly int ROOK_OUTPOST_ATT_NK_PU = Oe(3, 4);

		private static readonly int QUEEN_M = Oe(2, 2);

		private static readonly int QUEEN_ATTACKS_KING = Oe(5, 2);

		private static readonly int QUEEN_DEFENDS_KING = Oe(5, 2);

		private static readonly int QUEEN_ATTACKS_PU = Oe(4, 4);

		private static readonly int QUEEN_7_KP_78 = Oe(5, 25);

		private static readonly int QUEEN_7_P_78_K_8_R_7 = Oe(10, 15);

		private static readonly int KING_PAWN_SHIELD = Oe(5, 0);

		private static readonly int PAWN_ATTACKS_KING = Oe(1, 0);

		private static readonly int PAWN_ATTACKS_KNIGHT = Oe(5, 7);

		private static readonly int PAWN_ATTACKS_BISHOP = Oe(5, 7);

		private static readonly int PAWN_ATTACKS_ROOK = Oe(7, 10);

		private static readonly int PAWN_ATTACKS_QUEEN = Oe(8, 12);

		private static readonly int PAWN_NO_FRONT_DOUBLED = Oe(-2, -4);

		private static readonly int PAWN_NO_FRONT_DOUB_ISO = Oe(-7, -12);

		private static readonly int PAWN_NO_FRONT_ISOLATED = Oe(-5, -8);

		private static readonly int PAWN_FRONT_DOUBLED = Oe(-4, -8);

		private static readonly int PAWN_FRONT_DOUB_ISO = Oe(-21, -22);

		private static readonly int PAWN_FRONT_ISOLATED = Oe(-15, -20);

		private static readonly int[] PAWN_CANDIDATE = new int[] { 0, Oe(5, 5), Oe(10, 12
			), Oe(20, 25), 0, 0, 0, 0 };

		private static readonly int[] PAWN_PASSER = new int[] { 0, 0, 0, Oe(10, 10), Oe(20
			, 25), Oe(40, 50), Oe(60, 75), 0 };

		private static readonly int[] PAWN_PASSER_OUTSIDE = new int[] { 0, 0, 0, 0, Oe(2, 
			5), Oe(5, 10), Oe(10, 20), 0 };

		private static readonly int[] PAWN_PASSER_PROTECTED = new int[] { 0, 0, 0, 0, Oe(
			5, 10), Oe(10, 15), Oe(15, 25), 0 };

		private static readonly int[] PAWN_PASSER_NO_MINES = new int[] { 0, 0, 0, 0, 0, Oe
			(3, 5), Oe(5, 10), 0 };

		private static readonly int[] PAWN_PASSER_NO_OTHERS = new int[] { 0, 0, 0, 0, Oe(
			5, 10), Oe(15, 30), Oe(25, 50), 0 };

		private static readonly int[] PAWN_PASSER_MOBILE = new int[] { 0, 0, 0, Oe(1, 2), 
			Oe(2, 3), Oe(3, 5), Oe(5, 10), 0 };

		private static readonly int[] PAWN_PASSER_RUNNER = new int[] { 0, 0, 0, 0, Oe(5, 
			10), Oe(10, 20), Oe(20, 40), 0 };

		private static readonly int[] PAWN_PASSER_ROOK_BEHIND = new int[] { 0, 0, 0, 0, 0
			, Oe(0, 15), Oe(20, 50), 0 };

		private static readonly int HUNG_PIECES = Oe(16, 25);

		private static readonly int PINNED_PIECE = Oe(25, 35);

		private const int TEMPO = 9;

		private static readonly long[] OUTPOST_MASK = new long[] { unchecked((long)(0x00007e7e7e000000L
			)), unchecked((long)(0x0000007e7e7e0000L)) };

		private static readonly int[] KNIGHT_OUTPOST_ATTACKS_NK_PU = new int[] { 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Oe(7, 7), Oe(7
			, 7), Oe(10, 10), Oe(10, 10), Oe(7, 7), Oe(7, 7), 0, 0, Oe(5, 5), Oe(5, 5), Oe(8
			, 8), Oe(8, 8), Oe(5, 5), Oe(5, 5), 0, 0, 0, Oe(5, 5), Oe(8, 8), Oe(8, 8), Oe(5, 
			5), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		private static readonly long[] BISHOP_TRAPPING = new long[] { unchecked((int)(0x00
			)), 1 << 10, unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), 1 << 13, unchecked((int)(0x00)), 1 << 17, unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), 1 << 22, 1 << 25, unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), 1 << 30, unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), 1 << 33, unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), 1 << 38, 1 << 41, unchecked((int)(0x00)), unchecked((int)(0x00)), 
			unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), 1 << 46, unchecked((int)(0x00)), 1 << 50, unchecked((int)(0x00)), 
			unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), 1 << 53, 
			unchecked((int)(0x00)) };

		private static readonly int[] KING_SAFETY_PONDER = new int[] { 0, 1, 2, 4, 8, 8, 
			8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };

		private static readonly int[][] PawnColumn = new int[][] { new int[] { -20, -8, -
			2, 5, 5, -2, -8, -20 }, new int[] { -4, -6, -8, -10, -10, -8, -6, -4 } };

		private static readonly int[][] PawnRank = new int[][] { new int[] { 0, -3, -2, -
			1, 1, 2, 3, 0 }, new int[] { 0, -3, -3, -2, -1, 0, 2, 0 } };

		private static readonly int[][] KnightColumn = new int[][] { new int[] { -26, -10
			, 1, 5, 5, 1, -10, -26 }, new int[] { -4, -1, 2, 4, 4, 2, -1, -4 } };

		private static readonly int[][] KnightRank = new int[][] { new int[] { -32, -10, 
			6, 15, 21, 19, 10, -11 }, new int[] { -10, -5, -2, 1, 3, 5, 2, -3 } };

		private static readonly int[][] KnightLine = new int[][] { new int[] { 0, 0, 0, 0
			, 0, 0, 0, 0 }, new int[] { 2, 1, 0, -1, -2, -4, -7, -10 } };

		private static readonly int[][] BishopLine = new int[][] { new int[] { 10, 5, 1, 
			-3, -5, -7, -8, -12 }, new int[] { 3, 2, 0, 0, -2, -2, -3, -3 } };

		private static readonly int[][] BishopRank = new int[][] { new int[] { -5, 0, 0, 
			0, 0, 0, 0, 0 }, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 } };

		private static readonly int[][] RookColumn = new int[][] { new int[] { -4, 0, 4, 
			8, 8, 4, 0, -4 }, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 } };

		private static readonly int[][] RookRank = new int[][] { new int[] { 0, 0, 0, 0, 
			0, 0, 0, 0 }, new int[] { 0, 0, 0, 0, 1, 1, 1, -2 } };

		private static readonly int[][] QueenColumn = new int[][] { new int[] { -2, 0, 1, 
			2, 2, 1, 0, -2 }, new int[] { -2, 0, 1, 2, 2, 1, 0, -2 } };

		private static readonly int[][] QueenRank = new int[][] { new int[] { -2, 0, 1, 2
			, 2, 1, 0, -2 }, new int[] { -2, 0, 1, 2, 2, 1, 0, -2 } };

		private static readonly int[][] QueenLine = new int[][] { new int[] { 3, 2, 1, 0, 
			-2, -4, -7, -10 }, new int[] { 1, 0, -1, -3, -4, -6, -8, -12 } };

		private static readonly int[][] KingColumn = new int[][] { new int[] { 40, 45, 15
			, -5, -5, 15, 45, 40 }, new int[] { -15, 0, 10, 15, 15, 10, 0, -15 } };

		private static readonly int[][] KingRank = new int[][] { new int[] { 4, 1, -2, -5
			, -10, -15, -25, -35 }, new int[] { -15, 0, 10, 15, 15, 10, 0, -15 } };

		private static readonly int[][] KingLine = new int[][] { new int[] { 0, 0, 0, 0, 
			0, 0, 0, 0 }, new int[] { 2, 0, -2, -5, -8, -12, -20, -30 } };

		private static readonly int[] pawnIndexValue = new int[64];

		private static readonly int[] knightIndexValue = new int[64];

		private static readonly int[] bishopIndexValue = new int[64];

		private static readonly int[] rookIndexValue = new int[64];

		private static readonly int[] queenIndexValue = new int[64];

		private static readonly int[] kingIndexValue = new int[64];

		static ExperimentalEvaluator()
		{
			// Bonus by having two bishops
			// Bishops
			// Mobility units: this value is added for each destination square not occupied by one of our pieces or attacked by opposite pawns
			// Sums for each of our pawns (or oppsite/2) in the bishop color
			// Sums for each of the undefended opposite pawns forward
			// Only if defended by pawn
			// attacks squares Near King or other opposite pieces Pawn Undefended
			// Knights
			// Adds one time if no opposite can can attack out knight and twice if it is defended by one of our pawns	
			// Rooks
			// Attacks pawn not defended by pawn (PU=Pawn Undefended)
			// Attacks bishop or knight not defended by pawn
			// Attacks queen
			// No pawns in rook column and no minor guarded
			// No pawns in rook column and minor guarded, my pawn can attack
			// No pawns in rook column and minor guarded, my pawn can attack
			// No pawns mines in column
			// And attacks a backward pawn
			// No pawns mines in column and opposite king
			// Rook in 8th rank and opposite king in 8th rank
			// Rook in 8th rank and opposite king/pawn in 7/8th rank
			// Rook in 7th rank and opposite king in 8th and attacked opposite queen/rook on 7th
			// Rook in 7th rank and opposite king/pawn in 6/7/8th
			// Only if defended by pawn
			// Also attacks other piece not defended by pawn or a square near king
			// Queen
			// Queen in 8th rank and opposite king/pawn in 7/8th rank
			// Queen in 7th my root in 7th defending queen and opposite king in 8th
			// King
			// Protection: sums for each pawn near king
			// Pawns
			// Sums for each pawn attacking an square near the king or the king
			// Sums for each pawn attacking a KNIGHT
			// no other pawns in front: isolated
			// no other pawns in front: doubled and isolated
			// no other pawns in front: isolated
			// other pawns in front: isolated
			// other pawns in front: doubled and isolated
			// other pawns in front: isolated
			//	private final static int PAWN_BACKWARDS          = oe(-10,-15); // Backwards pawn 
			//	private final static int PAWN_WEAK          	 = oe(-10,-15); // Backwards pawn 
			// Candidates to pawn passer
			// no opposite pawns at left or at right 
			// defended by pawn
			//	private final static int[] PAWN_PASSER_CONNECTED = {0, 0, 0, 0, oe(5, 10), oe(10,15), oe(20,30), 0};
			// With a rook behind 
			// two or more pieces of the other side attacked by inferior pieces 
			// Tempo
			// Add to moving side score
			// kNight outpost attacks squares Near King or other opposite pieces Pawn Undefended 
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			// Ponder kings attacks by the number of attackers (not pawns)
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			// Values are rotated for whites, so when white is playing is like shown in the code
			// Initialize Piece square values
			int i;
			for (i = 0; i < 64; i++)
			{
				int rank = i >> 3;
				int column = 7 - i & 7;
				int d = (((column - rank) >= 0) ? (column - rank) : -(column - rank));
				int e = (((column + rank - 7) >= 0) ? (column + rank - 7) : -(column + rank - 7));
				pawnIndexValue[i] = Oe(PawnColumn[OPENING][column] + PawnRank[OPENING][rank], PawnColumn
					[ENDGAME][column] + PawnRank[ENDGAME][rank]);
				knightIndexValue[i] = Oe(KnightColumn[OPENING][column] + KnightRank[OPENING][rank
					] + KnightLine[OPENING][d] + KnightLine[OPENING][e], KnightColumn[ENDGAME][column
					] + KnightRank[ENDGAME][rank] + KnightLine[ENDGAME][d] + KnightLine[ENDGAME][e]);
				bishopIndexValue[i] = Oe(BishopRank[OPENING][rank] + BishopLine[OPENING][d] + BishopLine
					[OPENING][e], BishopRank[ENDGAME][rank] + BishopLine[ENDGAME][d] + BishopLine[ENDGAME
					][e]);
				rookIndexValue[i] = Oe(RookColumn[OPENING][column] + RookRank[OPENING][rank], RookColumn
					[ENDGAME][column] + RookRank[ENDGAME][rank]);
				queenIndexValue[i] = Oe(QueenColumn[OPENING][column] + QueenRank[OPENING][rank] +
					 QueenLine[OPENING][d] + QueenLine[OPENING][e], QueenColumn[ENDGAME][column] + QueenRank
					[ENDGAME][rank] + QueenLine[ENDGAME][d] + QueenLine[ENDGAME][e]);
				kingIndexValue[i] = Oe(KingColumn[OPENING][column] + KingRank[OPENING][rank] + KingLine
					[OPENING][d] + KingLine[OPENING][e], KingColumn[ENDGAME][column] + KingRank[ENDGAME
					][rank] + KingLine[ENDGAME][d] + KingLine[ENDGAME][e]);
			}
		}

		private long all;

		private long pieceAttacks;

		private long pieceAttacksXray;

		private long auxLong;

		private long auxLong2;

		private long[] attacksSquare = new long[64];

		private long[] superiorPieceAttacked = new long[] { 0, 0 };

		private long[] attacksColor = new long[] { 0, 0 };

		private long[] minorPiecesDefendedByPawns = new long[] { 0, 0 };

		private long[] pawnAttacks = new long[] { 0, 0 };

		private long[] pawnCanAttack = new long[] { 0, 0 };

		private long[] squaresNearKing = new long[] { 0, 0 };

		private int[] material = new int[] { 0, 0 };

		private int[] pawnMaterial = new int[] { 0, 0 };

		private int[] center = new int[] { 0, 0 };

		private int[] positional = new int[] { 0, 0 };

		private int[] mobility = new int[] { 0, 0 };

		private int[] attacks = new int[] { 0, 0 };

		private int[] kingAttackersCount = new int[] { 0, 0 };

		private int[] kingSafety = new int[] { 0, 0 };

		private int[] kingDefense = new int[] { 0, 0 };

		private int[] pawnStructure = new int[] { 0, 0 };

		private int[] passedPawns = new int[] { 0, 0 };

		private int[] bishopCount = new int[] { 0, 0 };

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
		//	private static void printPcsq(int pcsq[]) {
		//		StringBuffer sb = new StringBuffer();
		//		for (int k=0; k<2; k++) {
		//			if (k==0) sb.append("Opening:\n");
		//			else sb.append("Endgame:\n");
		//			for (int i = 0; i<64; i++) {
		//				String aux = "     " + (k==0 ? o(pcsq[i]) : e(pcsq[i]));
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
		public override int EvaluateBoard(Board board, int alpha, int beta)
		{
			long square = 1;
			byte index = 0;
			all = board.GetAll();
			superiorPieceAttacked[0] = superiorPieceAttacked[1] = 0;
			attacksColor[0] = attacksColor[1] = 0;
			material[0] = material[1] = 0;
			pawnMaterial[0] = pawnMaterial[1] = 0;
			center[0] = center[1] = 0;
			positional[0] = positional[1] = 0;
			mobility[0] = mobility[1] = 0;
			attacks[0] = attacks[1] = 0;
			kingAttackersCount[0] = kingAttackersCount[1] = 0;
			kingSafety[0] = kingSafety[1] = 0;
			kingDefense[0] = kingDefense[1] = 0;
			pawnStructure[0] = pawnStructure[1] = 0;
			passedPawns[0] = passedPawns[1] = 0;
			bishopCount[0] = bishopCount[1] = 0;
			// Squares attackeds by pawns
			pawnAttacks[0] = ((board.pawns & board.whites & ~BitboardUtils.b_l) << 9) | ((board
				.pawns & board.whites & ~BitboardUtils.b_r) << 7);
			pawnAttacks[1] = ((long)(((ulong)(board.pawns & board.blacks & ~BitboardUtils.b_r
				)) >> 9)) | ((long)(((ulong)(board.pawns & board.blacks & ~BitboardUtils.b_l)) >>
				 7));
			// Square that pawn attacks or can attack by advancing
			pawnCanAttack[0] = pawnAttacks[0] | pawnAttacks[0] << 8 | pawnAttacks[0] << 16 | 
				pawnAttacks[0] << 24 | pawnAttacks[0] << 32 | pawnAttacks[0] << 40;
			pawnCanAttack[1] = pawnAttacks[1] | (long)(((ulong)pawnAttacks[1]) >> 8) | (long)
				(((ulong)pawnAttacks[1]) >> 16) | (long)(((ulong)pawnAttacks[1]) >> 24) | (long)
				(((ulong)pawnAttacks[1]) >> 32) | (long)(((ulong)pawnAttacks[1]) >> 40);
			// Squares surrounding King
			squaresNearKing[0] = BitboardAttacks.king[BitboardUtils.Square2Index(board.whites
				 & board.kings)] | board.whites & board.kings;
			squaresNearKing[1] = BitboardAttacks.king[BitboardUtils.Square2Index(board.blacks
				 & board.kings)] | board.blacks & board.kings;
			minorPiecesDefendedByPawns[0] = board.whites & (board.bishops | board.knights) & 
				pawnAttacks[0];
			minorPiecesDefendedByPawns[1] = board.blacks & (board.bishops | board.knights) & 
				pawnAttacks[1];
			// first build attacks info
			square = 1;
			for (index = 0; ((sbyte)index) < 64; index++)
			{
				if ((square & all) != 0)
				{
					bool isWhite = ((board.whites & square) != 0);
					int color = (isWhite ? 0 : 1);
					if ((square & board.pawns) != 0)
					{
						pieceAttacks = (isWhite ? BitboardAttacks.pawnUpwards[index] : BitboardAttacks.pawnDownwards
							[index]);
					}
					else
					{
						if ((square & board.knights) != 0)
						{
							pieceAttacks = BitboardAttacks.knight[index];
						}
						else
						{
							if ((square & board.bishops) != 0)
							{
								pieceAttacks = BitboardAttacks.GetBishopAttacks(index, all);
							}
							else
							{
								if ((square & board.rooks) != 0)
								{
									pieceAttacks = BitboardAttacks.GetRookAttacks(index, all);
								}
								else
								{
									if ((square & board.queens) != 0)
									{
										pieceAttacks = BitboardAttacks.GetRookAttacks(index, all) | BitboardAttacks.GetBishopAttacks
											(index, all);
									}
									else
									{
										if ((square & board.kings) != 0)
										{
											pieceAttacks = BitboardAttacks.king[index];
										}
										else
										{
											pieceAttacks = 0;
										}
									}
								}
							}
						}
					}
					attacksColor[color] |= pieceAttacks;
					attacksSquare[index] = pieceAttacks;
				}
				square <<= 1;
			}
			// Ok, ended initialization
			square = 1;
			index = 0;
			while (square != 0)
			{
				if ((square & all) != 0)
				{
					bool isWhite = ((board.whites & square) != 0);
					int color = (isWhite ? 0 : 1);
					long mines = (isWhite ? board.whites : board.blacks);
					long others = (isWhite ? board.blacks : board.whites);
					long otherPawnAttacks = (isWhite ? pawnAttacks[1] : pawnAttacks[0]);
					int pcsqIndex = (isWhite ? index : 63 - index);
					int rank = index >> 3;
					int column = 7 - index & 7;
					pieceAttacks = attacksSquare[index];
					if ((square & board.pawns) != 0)
					{
						pawnMaterial[color] += PAWN;
						center[color] += pawnIndexValue[pcsqIndex];
						if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0)
						{
							kingSafety[color] += PAWN_ATTACKS_KING;
						}
						// TODO: if two pawns attacks the same square, add only once
						if ((pieceAttacks & board.knights & others) != 0)
						{
							attacks[color] += PAWN_ATTACKS_KNIGHT;
						}
						if ((pieceAttacks & board.bishops & others) != 0)
						{
							attacks[color] += PAWN_ATTACKS_BISHOP;
						}
						if ((pieceAttacks & board.rooks & others) != 0)
						{
							attacks[color] += PAWN_ATTACKS_ROOK;
						}
						if ((pieceAttacks & board.queens & others) != 0)
						{
							attacks[color] += PAWN_ATTACKS_QUEEN;
						}
						superiorPieceAttacked[color] |= pieceAttacks & others & (board.knights | board.bishops
							 | board.rooks | board.queens);
						bool isolated = (BitboardUtils.COLUMNS_ADJACENTS[column] & board.pawns & mines) ==
							 0;
						bool doubled = (BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color]
							[rank] & board.pawns & mines) != 0;
						//					boolean backwards = ((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & ~BitboardUtils.RANKS_FORWARD[color][rank] & board.pawns & mines) == 0;
						//					boolean weak = !isolated && (pawnAttacks[color] & square) == 0 
						//					&& pcsqIndex >= 24;
						//					 // not defended is weak
						//					if (weak) {
						//						// Can be defended advancing one pawn
						//						auxLong = (isWhite ? BitboardAttacks.pawnDownwards[color] : BitboardAttacks.pawnUpwards[color]) & ~otherPawnAttacks & ~all; 
						//						while (auxLong != 0) { // Not attacked by other pawn and empty
						//							auxLong2 = BitboardUtils.lsb(auxLong);
						//							auxLong &= ~auxLong2;
						//							auxLong2 = isWhite ?  auxLong2 >>> 8 : auxLong2 << 8;
						//							if ((auxLong2 & mines & board.pawns) !=0) {
						//								weak = false;							
						//							} else { // Defended advancing one pawn two squares
						//								if ((auxLong2 & all) == 0) { // empty square								
						//									auxLong2 = (isWhite ? auxLong2 >>> 8 : auxLong2 << 8);
						//									if (((isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[6]) & auxLong2 & board.pawns & mines) != 0) {
						//										weak = false;
						//									}
						//								}
						//							}
						//						}
						//						
						//						if (weak) {
						//							// Can advance to be supported
						//							auxLong = (isWhite ? square << 8 : square >>> 8) & ~otherPawnAttacks & ~all;
						//							if (auxLong != 0) {
						//								if ((auxLong & pawnAttacks[color]) != 0) {
						//									weak = false;
						//								} else {
						//									// Checks advancing two squares if in initial position
						//									if (((isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[6]) & square) != 0) {
						//										auxLong = (isWhite ? square << 16 : square >>> 16) & ~otherPawnAttacks & ~all;
						//										if ((auxLong & pawnAttacks[color]) != 0) weak = false;
						//									}
						//								}
						//							}
						//						}
						//					}
						//					if (weak) pawnStructure[color] += PAWN_WEAK;
						////					if (weak) {
						////						System.out.println("weak pawn: \n" + board.toString());
						////						System.out.println("square: \n" + BitboardUtils.toString(square));
						////					}
						//					
						// No pawns in front
						if ((BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color][rank] & board
							.pawns) == 0)
						{
							if (doubled)
							{
								pawnStructure[color] += PAWN_NO_FRONT_DOUBLED;
								if (isolated)
								{
									pawnStructure[color] += PAWN_NO_FRONT_DOUB_ISO;
								}
							}
							else
							{
								if (isolated)
								{
									pawnStructure[color] += PAWN_NO_FRONT_ISOLATED;
								}
							}
						}
						else
						{
							// pawns in front
							if (doubled)
							{
								pawnStructure[color] += PAWN_FRONT_DOUBLED;
								if (isolated)
								{
									pawnStructure[color] += PAWN_FRONT_DOUB_ISO;
								}
							}
							else
							{
								if (isolated)
								{
									pawnStructure[color] += PAWN_FRONT_ISOLATED;
								}
							}
						}
						// Backwards pawns and advance squares attacked by opposite pawns (TODO only three) 
						//					if (backwards && (BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color][rank] & otherPawnAttacks) !=0)
						//						pawnStructure[color] += PAWN_BACKWARDS;
						// Passed Pawns
						if (((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & BitboardUtils
							.RANKS_FORWARD[color][rank] & board.pawns & others) == 0)
						{
							// Static part
							passedPawns[color] += PAWN_PASSER[(isWhite ? rank : 7 - rank)];
							if ((square & pawnAttacks[color]) != 0)
							{
								passedPawns[color] += PAWN_PASSER_PROTECTED[(isWhite ? rank : 7 - rank)];
							}
							if ((BitboardUtils.ROWS_LEFT[column] & board.pawns & others) == 0 && (BitboardUtils
								.ROWS_RIGHT[column] & board.pawns & others) == 0)
							{
								passedPawns[color] += PAWN_PASSER_OUTSIDE[(isWhite ? rank : 7 - rank)];
							}
							// Dynamic part
							auxLong = BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color][rank];
							if ((auxLong & mines) == 0)
							{
								passedPawns[color] += PAWN_PASSER_NO_MINES[(isWhite ? rank : 7 - rank)];
							}
							if ((auxLong & others) == 0)
							{
								passedPawns[color] += PAWN_PASSER_NO_OTHERS[(isWhite ? rank : 7 - rank)];
							}
							if (((isWhite ? square << 8 : (long)(((ulong)square) >> 8)) & others) == 0)
							{
								passedPawns[color] += PAWN_PASSER_MOBILE[(isWhite ? rank : 7 - rank)];
							}
							if ((auxLong & ~attacksColor[color] & attacksColor[1 - color]) == 0)
							{
								passedPawns[color] += PAWN_PASSER_RUNNER[(isWhite ? rank : 7 - rank)];
							}
							if ((BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_BACKWARD[color][rank] & board
								.rooks & mines) != 0)
							{
								passedPawns[color] += PAWN_PASSER_ROOK_BEHIND[(isWhite ? rank : 7 - rank)];
							}
						}
						else
						{
							// Candidates is the same check but removing opposite pawns attacking our square
							if (((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & BitboardUtils
								.RANKS_FORWARD[color][rank] & (isWhite ? BitboardAttacks.pawnUpwards[index] : BitboardAttacks
								.pawnDownwards[index]) & board.pawns & others) == 0)
							{
								passedPawns[color] += PAWN_CANDIDATE[(isWhite ? rank : 7 - rank)];
							}
						}
					}
					else
					{
						if ((square & board.knights) != 0)
						{
							material[color] += KNIGHT;
							center[color] += knightIndexValue[pcsqIndex];
							// Only mobility forward
							mobility[color] += KNIGHT_M * BitboardUtils.PopCount(pieceAttacks & ~mines & ~otherPawnAttacks
								 & BitboardUtils.RANKS_FORWARD[color][rank]);
							if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0)
							{
								kingSafety[color] += KNIGHT_ATTACKS_KING;
								kingAttackersCount[color]++;
							}
							if ((pieceAttacks & squaresNearKing[color]) != 0)
							{
								kingDefense[color] += KNIGHT_DEFENDS_KING;
							}
							if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0)
							{
								attacks[color] += KNIGHT_ATTACKS_PU_P;
							}
							if ((pieceAttacks & board.bishops & others & ~otherPawnAttacks) != 0)
							{
								attacks[color] += KNIGHT_ATTACKS_PU_B;
							}
							if ((pieceAttacks & (board.rooks | board.queens) & others) != 0)
							{
								attacks[color] += KNIGHT_ATTACKS_RQ;
							}
							superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens
								);
							// Knight Outpost: no opposite pawns can attack the square
							if ((square & OUTPOST_MASK[color] & ~pawnCanAttack[1 - color]) != 0)
							{
								positional[color] += KNIGHT_OUTPOST;
								// Defended by one of our pawns
								if ((square & pawnAttacks[color]) != 0)
								{
									positional[color] += KNIGHT_OUTPOST;
									// Attacks squares near king or other pieces pawn undefended
									if ((pieceAttacks & (squaresNearKing[1 - color] | others) & ~otherPawnAttacks) !=
										 0)
									{
										positional[color] += KNIGHT_OUTPOST_ATTACKS_NK_PU[pcsqIndex];
									}
								}
							}
						}
						else
						{
							if ((square & board.bishops) != 0)
							{
								material[color] += BISHOP;
								if (bishopCount[color]++ == 2)
								{
									material[color] += BISHOP_PAIR;
								}
								center[color] += bishopIndexValue[pcsqIndex];
								mobility[color] += BISHOP_M * BitboardUtils.PopCount(pieceAttacks & ~mines & ~otherPawnAttacks
									 & BitboardUtils.RANKS_FORWARD[color][rank]);
								if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0)
								{
									kingSafety[color] += BISHOP_ATTACKS_KING;
									kingAttackersCount[color]++;
								}
								if ((pieceAttacks & squaresNearKing[color]) != 0)
								{
									kingDefense[color] += BISHOP_DEFENDS_KING;
								}
								if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0)
								{
									attacks[color] += BISHOP_ATTACKS_PU_P;
								}
								if ((pieceAttacks & board.knights & others & ~otherPawnAttacks) != 0)
								{
									attacks[color] += BISHOP_ATTACKS_PU_K;
								}
								if ((pieceAttacks & (board.rooks | board.queens) & others) != 0)
								{
									attacks[color] += BISHOP_ATTACKS_RQ;
								}
								superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens
									);
								pieceAttacksXray = BitboardAttacks.GetBishopAttacks(index, all & ~(pieceAttacks &
									 others)) & ~pieceAttacks;
								if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0)
								{
									attacks[color] += PINNED_PIECE;
								}
								// Bishop Outpost: no opposite pawns can attack the square and defended by one of our pawns
								if ((square & OUTPOST_MASK[color] & ~pawnCanAttack[1 - color] & pawnAttacks[color
									]) != 0)
								{
									positional[color] += BISHOP_OUTPOST;
									// Attacks squares near king or other pieces pawn undefended
									if ((pieceAttacks & (squaresNearKing[1 - color] | others) & ~otherPawnAttacks) !=
										 0)
									{
										positional[color] += BISHOP_OUTPOST_ATT_NK_PU;
									}
								}
								// Pawns in our color
								if ((square & BitboardUtils.WHITE_SQUARES) != 0)
								{
									auxLong = BitboardUtils.WHITE_SQUARES;
								}
								else
								{
									auxLong = BitboardUtils.BLACK_SQUARES;
								}
								positional[color] += ((int)(((uint)BitboardUtils.PopCount(auxLong & board.pawns &
									 mines) + BitboardUtils.PopCount(auxLong & board.pawns & mines)) >> 1)) * BISHOP_PAWN_IN_COLOR;
								positional[color] += ((int)(((uint)BitboardUtils.PopCount(auxLong & board.pawns &
									 others & BitboardUtils.RANKS_FORWARD[color][rank])) >> 1)) * BISHOP_FORWARD_P_PU;
								if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0)
								{
									mobility[color] += BISHOP_TRAPPED;
								}
							}
							else
							{
								// TODO protection
								if ((square & board.rooks) != 0)
								{
									material[color] += ROOK;
									center[color] += rookIndexValue[pcsqIndex];
									mobility[color] += ROOK_M * BitboardUtils.PopCount(pieceAttacks & ~mines & ~otherPawnAttacks
										);
									if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0)
									{
										kingSafety[color] += ROOK_ATTACKS_KING;
										kingAttackersCount[color]++;
									}
									if ((pieceAttacks & squaresNearKing[color]) != 0)
									{
										kingDefense[color] += ROOK_DEFENDS_KING;
									}
									if ((pieceAttacks & board.pawns & others & ~otherPawnAttacks) != 0)
									{
										attacks[color] += ROOK_ATTACKS_PU_P;
									}
									if ((pieceAttacks & (board.bishops | board.knights) & others & ~otherPawnAttacks)
										 != 0)
									{
										attacks[color] += ROOK_ATTACKS_PU_BK;
									}
									if ((pieceAttacks & board.queens & others) != 0)
									{
										attacks[color] += ROOK_ATTACKS_Q;
									}
									superiorPieceAttacked[color] |= pieceAttacks & others & board.queens;
									pieceAttacksXray = BitboardAttacks.GetRookAttacks(index, all & ~(pieceAttacks & others
										)) & ~pieceAttacks;
									if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0)
									{
										attacks[color] += PINNED_PIECE;
									}
									auxLong = (isWhite ? BitboardUtils.b_u : BitboardUtils.b_d);
									if ((square & auxLong) != 0 && (others & board.kings & auxLong) != 0)
									{
										positional[color] += ROOK_8_KING_8;
									}
									if ((square & (isWhite ? BitboardUtils.r2_u : BitboardUtils.r2_d)) != 0 & (others
										 & (board.kings | board.pawns) & (isWhite ? BitboardUtils.b2_u : BitboardUtils.b2_d
										)) != 0)
									{
										positional[color] += ROOK_7_KP_78;
										if ((others & board.kings & auxLong) != 0 && (pieceAttacks & others & (board.queens
											 | board.rooks) & (isWhite ? BitboardUtils.r2_u : BitboardUtils.r2_d)) != 0)
										{
											positional[color] += ROOK_7_P_78_K_8_RQ_7;
										}
									}
									if ((square & (isWhite ? BitboardUtils.r3_u : BitboardUtils.r3_d)) != 0 & (others
										 & (board.kings | board.pawns) & (isWhite ? BitboardUtils.b3_u : BitboardUtils.b3_d
										)) != 0)
									{
										positional[color] += ROOK_6_KP_678;
									}
									auxLong = BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color][rank];
									if ((auxLong & board.pawns & mines) == 0)
									{
										positional[color] += ROOK_COLUMN_SEMIOPEN;
										if ((auxLong & board.pawns) == 0)
										{
											if ((auxLong & minorPiecesDefendedByPawns[1 - color]) == 0)
											{
												positional[color] += ROOK_COLUMN_OPEN_NO_MG;
											}
											else
											{
												if ((auxLong & minorPiecesDefendedByPawns[1 - color] & pawnCanAttack[color]) == 0)
												{
													positional[color] += ROOK_COLUMN_OPEN_MG_NP;
												}
												else
												{
													positional[color] += ROOK_COLUMN_OPEN_MG_P;
												}
											}
										}
										else
										{
											// There is an opposite backward pawn
											if ((auxLong & board.pawns & others & pawnCanAttack[1 - color]) == 0)
											{
												positional[color] += ROOK_COLUMN_SEMIOPEN_BP;
											}
										}
										if ((auxLong & board.kings & others) != 0)
										{
											positional[color] += ROOK_COLUMN_SEMIOPEN_K;
										}
									}
									// Rook Outpost: no opposite pawns can attack the square and defended by one of our pawns
									if ((square & OUTPOST_MASK[color] & ~pawnCanAttack[1 - color] & pawnAttacks[color
										]) != 0)
									{
										positional[color] += ROOK_OUTPOST;
										// Attacks squares near king or other pieces pawn undefended
										if ((pieceAttacks & (squaresNearKing[1 - color] | others) & ~otherPawnAttacks) !=
											 0)
										{
											positional[color] += ROOK_OUTPOST_ATT_NK_PU;
										}
									}
								}
								else
								{
									if ((square & board.queens) != 0)
									{
										material[color] += QUEEN;
										center[color] += queenIndexValue[pcsqIndex];
										mobility[color] += QUEEN_M * BitboardUtils.PopCount(pieceAttacks & ~mines & ~otherPawnAttacks
											);
										if ((pieceAttacks & squaresNearKing[1 - color] & ~otherPawnAttacks) != 0)
										{
											kingSafety[color] += QUEEN_ATTACKS_KING;
											kingAttackersCount[color]++;
										}
										if ((pieceAttacks & squaresNearKing[color]) != 0)
										{
											kingDefense[color] += QUEEN_DEFENDS_KING;
										}
										if ((pieceAttacks & others & ~otherPawnAttacks) != 0)
										{
											attacks[color] += QUEEN_ATTACKS_PU;
										}
										pieceAttacksXray = (BitboardAttacks.GetRookAttacks(index, all & ~(pieceAttacks & 
											others)) | BitboardAttacks.GetBishopAttacks(index, all & ~(pieceAttacks & others
											))) & ~pieceAttacks;
										if ((pieceAttacksXray & board.kings & others) != 0)
										{
											attacks[color] += PINNED_PIECE;
										}
										auxLong = (isWhite ? BitboardUtils.b_u : BitboardUtils.b_d);
										auxLong2 = (isWhite ? BitboardUtils.r2_u : BitboardUtils.r2_d);
										if ((square & auxLong2) != 0 && (others & (board.kings | board.pawns) & (auxLong 
											| auxLong2)) != 0)
										{
											attacks[color] += QUEEN_7_KP_78;
											if ((board.rooks & mines & auxLong2 & pieceAttacks) != 0 && (board.kings & others
												 & auxLong) != 0)
											{
												positional[color] += QUEEN_7_P_78_K_8_R_7;
											}
										}
									}
									else
									{
										if ((square & board.kings) != 0)
										{
											center[color] += kingIndexValue[pcsqIndex];
											// If king is in the first rank, we add the pawn shield
											if ((square & (isWhite ? BitboardUtils.RANK[0] : BitboardUtils.RANK[7])) != 0)
											{
												kingDefense[color] += KING_PAWN_SHIELD * BitboardUtils.PopCount(pieceAttacks & mines
													 & board.pawns);
											}
										}
									}
								}
							}
						}
					}
				}
				square <<= 1;
				index++;
			}
			// Ponder opening and Endgame value depending of the non-pawn pieces:
			// opening=> gamephase = 255 / ending => gamephase ~= 0
			int gamePhase = ((material[0] + material[1]) << 8) / 5000;
			if (gamePhase > 256)
			{
				gamePhase = 256;
			}
			// Security		
			int value = 0;
			// First Material
			value += pawnMaterial[0] - pawnMaterial[1] + material[0] - material[1];
			// Tempo
			value += (board.GetTurn() ? TEMPO : -TEMPO);
			int oe = config.GetEvalCenter() * (center[0] - center[1]) + config.GetEvalPositional
				() * (positional[0] - positional[1]) + config.GetEvalAttacks() * (attacks[0] - attacks
				[1]) + config.GetEvalMobility() * (mobility[0] - mobility[1]) + config.GetEvalPawnStructure
				() * (pawnStructure[0] - pawnStructure[1]) + config.GetEvalPassedPawns() * (passedPawns
				[0] - passedPawns[1]) + config.GetEvalKingSafety() * (kingDefense[0] - kingDefense
				[1]) + config.GetEvalKingSafety() * (KING_SAFETY_PONDER[kingAttackersCount[0]] *
				 kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]) + config
				.GetEvalAttacks() * ((BitboardUtils.PopCount(superiorPieceAttacked[0]) >= 2 ? HUNG_PIECES
				 : 0) - (BitboardUtils.PopCount(superiorPieceAttacked[1]) >= 2 ? HUNG_PIECES : 0
				));
			value += (gamePhase * O(oe)) / (256 * 100);
			// divide by 256
			value += ((256 - gamePhase) * E(oe)) / (256 * 100);
			if (debug)
			{
				logger.Debug("\n" + board.ToString());
				logger.Debug(board.GetFen());
				logger.Debug("materialValue          = " + (material[0] - material[1]));
				logger.Debug("pawnMaterialValue      = " + (pawnMaterial[0] - pawnMaterial[1]));
				logger.Debug("centerOpening          = " + O(center[0] - center[1]));
				logger.Debug("centerEndgame          = " + E(center[0] - center[1]));
				logger.Debug("positionalOpening      = " + O(positional[0] - positional[1]));
				logger.Debug("positionalEndgame      = " + E(positional[0] - positional[1]));
				logger.Debug("attacksO 				 = " + O(attacks[0] - attacks[1]));
				logger.Debug("attacksE 				 = " + E(attacks[0] - attacks[1]));
				logger.Debug("mobilityO              = " + O(mobility[0] - mobility[1]));
				logger.Debug("mobilityE              = " + E(mobility[0] - mobility[1]));
				logger.Debug("pawnsO                 = " + O(pawnStructure[0] - pawnStructure[1])
					);
				logger.Debug("pawnsE                 = " + E(pawnStructure[0] - pawnStructure[1])
					);
				logger.Debug("passedPawnsO           = " + O(passedPawns[0] - passedPawns[1]));
				logger.Debug("passedPawnsE           = " + E(passedPawns[0] - passedPawns[1]));
				logger.Debug("kingSafetyValueO       = " + O(KING_SAFETY_PONDER[kingAttackersCount
					[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]
					));
				logger.Debug("kingSafetyValueE       = " + E(KING_SAFETY_PONDER[kingAttackersCount
					[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1]] * kingSafety[1]
					));
				logger.Debug("kingDefenseO           = " + O(kingDefense[0] - kingDefense[1]));
				logger.Debug("kingDefenseE           = " + E(kingDefense[0] - kingDefense[1]));
				logger.Debug("HungPiecesO 		     = " + O((BitboardUtils.PopCount(superiorPieceAttacked
					[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.PopCount(superiorPieceAttacked[1])
					 >= 2 ? HUNG_PIECES : 0)));
				logger.Debug("HungPiecesE 		     = " + O((BitboardUtils.PopCount(superiorPieceAttacked
					[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.PopCount(superiorPieceAttacked[1])
					 >= 2 ? HUNG_PIECES : 0)));
				logger.Debug("gamePhase              = " + gamePhase);
				logger.Debug("tempo                  = " + (board.GetTurn() ? TEMPO : -TEMPO));
				logger.Debug("value                  = " + value);
			}
			return value;
		}
	}
}
