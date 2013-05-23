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
	/// Material imbalances from Larry KaufMan:
	/// http://home.comcast.net/~danheisman/Articles/evaluation_of_material_imbalance.htm
	/// Piece/square values like Fruit/Toga
	/// TODO: pawn races
	/// TODO: pawn storm
	/// TODO: pinned pieces
	/// </summary>
	/// <author>rui</author>
	public class CompleteEvaluator : Evaluator
	{
		/// <summary>Logger for this class</summary>
		private static readonly Logger logger = Logger.GetLogger("CompleteEvaluator");

		public const int PAWN = 100;

		public const int KNIGHT = 325;

		public const int BISHOP = 325;

		public const int BISHOP_PAIR = 50;

		public const int ROOK = 500;

		public const int QUEEN = 975;

		private const int BISHOP_M_UNITS = 6;

		private static readonly int BISHOP_M = Oe(5, 5);

		private static readonly int BISHOP_TRAPPED = Oe(-100, -100);

		private const int KNIGHT_M_UNITS = 4;

		private static readonly int KNIGHT_M = Oe(4, 4);

		private const int KNIGHT_KAUF_BONUS = 7;

		private const int ROOK_M_UNITS = 7;

		private static readonly int ROOK_M = Oe(2, 4);

		private static readonly int ROOK_COLUMN_OPEN = Oe(25, 20);

		private static readonly int ROOK_COLUMN_SEMIOPEN = Oe(15, 10);

		private static readonly int ROOK_CONNECT = Oe(20, 10);

		private const int ROOK_KAUF_BONUS = -12;

		private const int QUEEN_M_UNITS = 13;

		private static readonly int QUEEN_M = Oe(2, 4);

		private static readonly int KING_PAWN_NEAR = Oe(5, 0);

		private static readonly int PAWN_ATTACKS_KING = Oe(1, 0);

		private static readonly int KNIGHT_ATTACKS_KING = Oe(4, 0);

		private static readonly int BISHOP_ATTACKS_KING = Oe(2, 0);

		private static readonly int ROOK_ATTACKS_KING = Oe(3, 0);

		private static readonly int QUEEN_ATTACKS_KING = Oe(5, 0);

		private static readonly int PAWN_ISOLATED = Oe(-10, -20);

		private static readonly int PAWN_DOUBLED = Oe(-10, -20);

		private static readonly int PAWN_WEAK = Oe(-10, -15);

		private static readonly int[] PAWN_PASSER = new int[] { 0, Oe(5, 10), Oe(10, 20), 
			Oe(20, 40), Oe(30, 60), Oe(50, 100), Oe(75, 150), 0 };

		private static readonly int[] PAWN_PASSER_SUPPORT = new int[] { 0, 0, Oe(5, 10), 
			Oe(10, 20), Oe(15, 30), Oe(25, 50), Oe(37, 75), 0 };

		private static readonly int[] PAWN_PASSER_KING_D = new int[] { 0, 0, Oe(1, 2), Oe
			(2, 4), Oe(3, 6), Oe(5, 10), Oe(7, 15), 0 };

		private static readonly int[] KING_SAFETY_PONDER = new int[] { 0, 1, 4, 8, 16, 25
			, 36, 49, 50, 50, 50, 50, 50, 50, 50, 50 };

		private static readonly int HUNG_PIECES = Oe(16, 25);

		private static readonly int PINNED_PIECE = Oe(25, 35);

		public const int TEMPO = 10;

		private static readonly int[] KNIGTH_OUTPOST = new int[] { 0, 0, 0, 0, 0, 0, 0, 0
			, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Oe(7, 7), Oe(9, 9), Oe(9, 9), Oe(7, 7), 0, 0, 0, 
			Oe(5, 5), Oe(10, 10), Oe(20, 20), Oe(20, 20), Oe(10, 10), Oe(5, 5), 0, 0, Oe(5, 
			5), Oe(10, 10), Oe(20, 20), Oe(20, 20), Oe(10, 10), Oe(5, 5), 0, 0, 0, Oe(7, 7), 
			Oe(9, 9), Oe(9, 9), Oe(7, 7), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0 };

		private static readonly long[] BISHOP_TRAPPING = new long[] { unchecked((int)(0x00
			)), 1L << 10, unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), 1L << 13, unchecked((int)(0x00)), 1L << 17, unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), 1L << 22, 1L << 25, unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), 1L << 30, unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), 1L << 33, unchecked((int)(0x00)), unchecked((int)(0x00
			)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), 1L << 38, 1L << 41, unchecked((int)(0x00)), unchecked((int)(0x00))
			, unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked(
			(int)(0x00)), 1L << 46, unchecked((int)(0x00)), 1L << 50, unchecked((int)(0x00))
			, unchecked((int)(0x00)), unchecked((int)(0x00)), unchecked((int)(0x00)), 1L << 
			53, unchecked((int)(0x00)) };

		private static readonly int PawnColumnValue = Oe(5, 0);

		private static readonly int KnightCenterValue = Oe(5, 5);

		private static readonly int KnightRankValue = Oe(5, 0);

		private static readonly int KnightBackRankValue = Oe(0, 0);

		private static readonly int KnightTrappedValue = Oe(-100, 0);

		private static readonly int BishopCenterValue = Oe(2, 3);

		private static readonly int BishopBackRankValue = Oe(-10, 0);

		private static readonly int BishopDiagonalValue = Oe(4, 0);

		private static readonly int RookColumnValue = Oe(3, 0);

		private static readonly int QueenCenterValue = Oe(0, 4);

		private static readonly int QueenBackRankValue = Oe(-5, 0);

		private static readonly int KingCenterValue = Oe(0, 12);

		private static readonly int KingColumnValue = Oe(10, 0);

		private static readonly int KingRankValue = Oe(10, 0);

		private static readonly int[] PawnColumn = new int[] { -3, -1, +0, +1, +1, +0, -1
			, -3 };

		private static readonly int[] KnightLine = new int[] { -4, -2, +0, +1, +1, +0, -2
			, -4 };

		private static readonly int[] KnightRank = new int[] { -2, -1, +0, +1, +2, +3, +2
			, +1 };

		private static readonly int[] BishopLine = new int[] { -3, -1, +0, +1, +1, +0, -1
			, -3 };

		private static readonly int[] RookColumn = new int[] { -2, -1, +0, +1, +1, +0, -1
			, -2 };

		private static readonly int[] QueenLine = new int[] { -3, -1, +0, +1, +1, +0, -1, 
			-3 };

		private static readonly int[] KingLine = new int[] { -3, -1, +0, +1, +1, +0, -1, 
			-3 };

		private static readonly int[] KingColumn = new int[] { +3, +4, +2, +0, +0, +2, +4
			, +3 };

		private static readonly int[] KingRank = new int[] { +1, +0, -2, -3, -4, -5, -6, 
			-7 };

		private static readonly int[] pawnIndexValue = new int[64];

		private static readonly int[] knightIndexValue = new int[64];

		private static readonly int[] bishopIndexValue = new int[64];

		private static readonly int[] rookIndexValue = new int[64];

		private static readonly int[] queenIndexValue = new int[64];

		private static readonly int[] kingIndexValue = new int[64];

		internal Config config;

		public bool debug = false;

		public CompleteEvaluator(Config config)
		{
			// Bonus by having two bishops
			// Bishops
			// Mobility units: this value is added for each destination square not occupied by one of our pieces
			// Bishops
			// Rooks
			// No pawns in rook column
			// Only opposite pawns in rook column
			// Rook connects with other rook TODO???
			// Queen
			// Protection: sums for each pawn near king (opening)
			// King Safety: not in endgame!!!
			// Pawns
			// Penalty for each pawn in a doubled rank
			//	private final static int PAWN_BACKWARD         = oe(-8,-10);
			//	private final static int PAWN_BLOCKED          = oe(0,0); //-20; // Pawn blocked by opposite pawn
			// Weak pawn 
			// Depends of the rank 
			// Depends of the rank 
			// Sums by each square away of the other opposite king
			// Ponder kings attacks by the number of attackers (not pawns) later divided by 8
			// two or more pieces of the other side attacked by inferior pieces 
			// Tempo
			// Add to moving side score
			// The pair of values are {opening, endgame}
			// Values are rotated for whites, so when white is playing is like shown in the code
			this.config = config;
		}

		static CompleteEvaluator()
		{
			// Initialize Piece square values Fruit/Toga style 
			int i;
			for (i = 0; i < 64; i++)
			{
				int rank = i >> 3;
				int column = 7 - i & 7;
				pawnIndexValue[i] = PawnColumn[column] * PawnColumnValue;
				knightIndexValue[i] = KnightLine[column] * KnightCenterValue + KnightLine[rank] *
					 KnightCenterValue + KnightRank[rank] * KnightRankValue;
				bishopIndexValue[i] = BishopLine[column] * BishopCenterValue + BishopLine[rank] *
					 BishopCenterValue;
				rookIndexValue[i] = RookColumn[column] * RookColumnValue;
				queenIndexValue[i] = QueenLine[column] * QueenCenterValue + QueenLine[rank] * QueenCenterValue;
				kingIndexValue[i] = KingColumn[column] * KingColumnValue + KingRank[rank] * KingRankValue
					 + KingLine[column] * KingCenterValue + KingLine[rank] * KingCenterValue;
			}
			knightIndexValue[56] += KnightTrappedValue;
			// H8 
			knightIndexValue[63] += KnightTrappedValue;
			// A8
			for (i = 0; i < 8; i++)
			{
				queenIndexValue[i] += QueenBackRankValue;
				knightIndexValue[i] += KnightBackRankValue;
				bishopIndexValue[i] += BishopBackRankValue;
				bishopIndexValue[(i << 3) | i] += BishopDiagonalValue;
				bishopIndexValue[((i << 3) | i) ^ 0x38] += BishopDiagonalValue;
			}
			// Pawn opening corrections
			pawnIndexValue[19] += Oe(10, 0);
			// E3
			pawnIndexValue[20] += Oe(10, 0);
			// D3
			pawnIndexValue[27] += Oe(25, 0);
			// E4
			pawnIndexValue[28] += Oe(25, 0);
			// D4
			pawnIndexValue[35] += Oe(10, 0);
			// E5
			pawnIndexValue[36] += Oe(10, 0);
		}

		private int[] bishopCount = new int[] { 0, 0 };

		private long[] superiorPieceAttacked = new long[] { 0, 0 };

		private int[] material = new int[] { 0, 0 };

		private int[] pawnMaterial = new int[] { 0, 0 };

		private int[] mobility = new int[] { 0, 0 };

		private int[] attacks = new int[] { 0, 0 };

		private int[] center = new int[] { 0, 0 };

		private int[] positional = new int[] { 0, 0 };

		private int[] kingAttackersCount = new int[] { 0, 0 };

		private int[] kingSafety = new int[] { 0, 0 };

		private int[] pawnStructure = new int[] { 0, 0 };

		private int[] passedPawns = new int[] { 0, 0 };

		private long[] pawnAttacks = new long[] { 0, 0 };

		private long[] squaresNearKing = new long[] { 0, 0 };

		private int[] knightKaufBonus = new int[] { 0, 0 };

		private int[] rookKaufBonus = new int[] { 0, 0 };

		private long all;

		private long pieceAttacks;

		private long pieceAttacksXray;

		private long mines;

		private long others;

		private long square;

		private int auxInt;

		private int pcsqIndex;

		private int color;

		private int index;

		private bool isWhite;

		// D5
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
		//	int kingDefense[] = {0,0};
		// Squares attackeds by pawns
		// Squares surrounding King
		public override int EvaluateBoard(Board board, int alpha, int beta)
		{
			all = board.GetAll();
			Arrays.Fill(bishopCount, 0);
			Arrays.Fill(superiorPieceAttacked, 0);
			Arrays.Fill(material, 0);
			Arrays.Fill(pawnMaterial, 0);
			Arrays.Fill(mobility, 0);
			Arrays.Fill(attacks, 0);
			Arrays.Fill(center, 0);
			Arrays.Fill(positional, 0);
			Arrays.Fill(kingAttackersCount, 0);
			Arrays.Fill(kingSafety, 0);
			Arrays.Fill(pawnStructure, 0);
			Arrays.Fill(passedPawns, 0);
			// Squares attackeds by pawns
			pawnAttacks[0] = ((board.pawns & board.whites & ~BitboardUtils.b_l) << 9) | ((board
				.pawns & board.whites & ~BitboardUtils.b_r) << 7);
			pawnAttacks[1] = ((long)(((ulong)(board.pawns & board.blacks & ~BitboardUtils.b_r
				)) >> 9)) | ((long)(((ulong)(board.pawns & board.blacks & ~BitboardUtils.b_l)) >>
				 7));
			// Squares surrounding King
			squaresNearKing[0] = bbAttacks.king[BitboardUtils.Square2Index(board.whites & board
				.kings)];
			squaresNearKing[1] = bbAttacks.king[BitboardUtils.Square2Index(board.blacks & board
				.kings)];
			// From material imbalances (Larry Kaufmann):
			// A further refinement would be to raise the knight's value by 1/16 and lower the rook's value by 1/8
			// for each pawn above five of the side being valued, with the opposite adjustment for each pawn short of five
			int whitePawnsCount = BitboardUtils.PopCount(board.pawns & board.whites);
			int blackPawnsCount = BitboardUtils.PopCount(board.pawns & board.blacks);
			knightKaufBonus[0] = KNIGHT_KAUF_BONUS * (whitePawnsCount - 5);
			knightKaufBonus[1] = KNIGHT_KAUF_BONUS * (blackPawnsCount - 5);
			rookKaufBonus[0] = ROOK_KAUF_BONUS * (whitePawnsCount - 5);
			rookKaufBonus[1] = ROOK_KAUF_BONUS * (blackPawnsCount - 5);
			square = 1;
			index = 0;
			while (square != 0)
			{
				isWhite = ((board.whites & square) != 0);
				color = (isWhite ? 0 : 1);
				mines = (isWhite ? board.whites : board.blacks);
				others = (isWhite ? board.blacks : board.whites);
				pcsqIndex = (isWhite ? index : 63 - index);
				if ((square & all) != 0)
				{
					int rank = index >> 3;
					int column = 7 - index & 7;
					if ((square & board.pawns) != 0)
					{
						pawnMaterial[color] += PAWN;
						center[color] += pawnIndexValue[pcsqIndex];
						pieceAttacks = (isWhite ? bbAttacks.pawnUpwards[index] : bbAttacks.pawnDownwards[
							index]);
						superiorPieceAttacked[color] |= pieceAttacks & others & (board.knights | board.bishops
							 | board.rooks | board.queens);
						if ((pieceAttacks & squaresNearKing[1 - color]) != 0)
						{
							kingSafety[color] += PAWN_ATTACKS_KING;
						}
						// Doubled pawn detection
						if ((BitboardUtils.COLUMN[column] & BitboardUtils.RANKS_FORWARD[color][rank] & board
							.pawns & mines) != square)
						{
							pawnStructure[color] += PAWN_DOUBLED;
						}
						// Blocked Pawn
						//					boolean blocked = ((isWhite ? (square<< 8)  : (square >>> 8)) & others) != 0;
						//					if (blocked) pawnStructure[color] += PAWN_BLOCKED;
						// Backwards Pawn
						//					if (((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & ~BitboardUtils.RANKS_FORWARD[color][rank] & board.pawns & mines) == 0)
						//						pawnStructure[color] += PAWN_BACKWARD;
						// Passed Pawn
						if (((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) & (
							isWhite ? BitboardUtils.RANKS_UPWARDS[rank] : BitboardUtils.RANKS_DOWNWARDS[rank
							]) & board.pawns & others) == 0)
						{
							passedPawns[color] += PAWN_PASSER[(isWhite ? rank : 7 - rank)];
							if ((square & pawnAttacks[color]) != 0)
							{
								passedPawns[color] += PAWN_PASSER_SUPPORT[(isWhite ? rank : 7 - rank)];
							}
							passedPawns[color] += PAWN_PASSER_KING_D[(isWhite ? rank : 7 - rank)] * BitboardUtils
								.Distance(index, BitboardUtils.Square2Index(board.kings & others));
						}
						// Isolated pawn
						bool isolated = (BitboardUtils.COLUMNS_ADJACENTS[column] & board.pawns & mines) ==
							 0;
						if (isolated)
						{
							pawnStructure[color] += PAWN_ISOLATED;
						}
						long auxLong;
						long auxLong2;
						bool weak = !isolated && (pawnAttacks[color] & square) == 0;
						//						&& pcsqIndex >= 24
						// not defended is weak and only if over rank 2
						if (weak)
						{
							// Can be defended advancing one square
							auxLong = (isWhite ? bbAttacks.pawnDownwards[color] : bbAttacks.pawnUpwards[color
								]) & ~pawnAttacks[1 - color] & ~all;
							while (auxLong != 0)
							{
								// Not attacked by other pawn and empty
								auxLong2 = BitboardUtils.Lsb(auxLong);
								auxLong &= ~auxLong2;
								auxLong2 = isWhite ? (long)(((ulong)auxLong2) >> 8) : auxLong2 << 8;
								if ((auxLong2 & mines & board.pawns) != 0)
								{
									weak = false;
								}
								else
								{
									// Defended advancing one pawn two squares
									if ((auxLong2 & all) == 0)
									{
										// empty square								
										auxLong2 = (isWhite ? (long)(((ulong)auxLong2) >> 8) : auxLong2 << 8);
										if (((isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[6]) & auxLong2 & board
											.pawns & mines) != 0)
										{
											weak = false;
										}
									}
								}
							}
							if (weak)
							{
								// Can advance to be supported
								auxLong = (isWhite ? square << 8 : (long)(((ulong)square) >> 8)) & ~pawnAttacks[1
									 - color] & ~all;
								if (auxLong != 0)
								{
									if ((auxLong & pawnAttacks[color]) != 0)
									{
										weak = false;
									}
									else
									{
										// Checks advancing two squares if in initial position
										if (((isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[6]) & square) != 0)
										{
											auxLong = (isWhite ? square << 16 : (long)(((ulong)square) >> 16)) & ~pawnAttacks
												[1 - color] & ~all;
											if ((auxLong & pawnAttacks[color]) != 0)
											{
												weak = false;
											}
										}
									}
								}
							}
						}
						if (weak)
						{
							pawnStructure[color] += PAWN_WEAK;
						}
					}
					else
					{
						//					if (weak) {
						//						System.out.println("weak pawn: \n" + board.toString());
						//						System.out.println("square: \n" + BitboardUtils.toString(square));
						//					}
						if ((square & board.knights) != 0)
						{
							material[color] += KNIGHT + knightKaufBonus[color];
							center[color] += knightIndexValue[pcsqIndex];
							pieceAttacks = bbAttacks.knight[index];
							auxInt = BitboardUtils.PopCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) 
								- KNIGHT_M_UNITS;
							mobility[color] += KNIGHT_M * auxInt;
							if ((pieceAttacks & squaresNearKing[color]) != 0)
							{
								kingSafety[color] += KNIGHT_ATTACKS_KING;
								kingAttackersCount[color]++;
							}
							superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens
								);
							// Knight outpost: no opposite pawns can attack the square and is defended by one of our pawns
							if (((BitboardUtils.COLUMNS_ADJACENTS[column] & (isWhite ? BitboardUtils.RANKS_UPWARDS
								[rank] : BitboardUtils.RANKS_DOWNWARDS[rank]) & board.pawns & others) == 0) && (
								((isWhite ? bbAttacks.pawnDownwards[index] : bbAttacks.pawnUpwards[index]) & board
								.pawns & mines) != 0))
							{
								positional[color] += KNIGTH_OUTPOST[pcsqIndex];
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
								pieceAttacks = bbAttacks.GetBishopAttacks(index, all);
								auxInt = BitboardUtils.PopCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) 
									- BISHOP_M_UNITS;
								mobility[color] += BISHOP_M * auxInt;
								if ((pieceAttacks & squaresNearKing[1 - color]) != 0)
								{
									kingSafety[color] += BISHOP_ATTACKS_KING;
									kingAttackersCount[color]++;
								}
								pieceAttacksXray = bbAttacks.GetBishopAttacks(index, all & ~(pieceAttacks & others
									)) & ~pieceAttacks;
								if ((pieceAttacksXray & (board.rooks | board.queens | board.kings) & others) != 0)
								{
									attacks[color] += PINNED_PIECE;
								}
								superiorPieceAttacked[color] |= pieceAttacks & others & (board.rooks | board.queens
									);
								if ((BISHOP_TRAPPING[index] & board.pawns & others) != 0)
								{
									mobility[color] += BISHOP_TRAPPED;
								}
							}
							else
							{
								if ((square & board.rooks) != 0)
								{
									material[color] += ROOK + rookKaufBonus[color];
									center[color] += rookIndexValue[pcsqIndex];
									pieceAttacks = bbAttacks.GetRookAttacks(index, all);
									auxInt = BitboardUtils.PopCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) 
										- ROOK_M_UNITS;
									mobility[color] += ROOK_M * auxInt;
									pieceAttacksXray = bbAttacks.GetRookAttacks(index, all & ~(pieceAttacks & others)
										) & ~pieceAttacks;
									if ((pieceAttacksXray & (board.queens | board.kings) & others) != 0)
									{
										attacks[color] += PINNED_PIECE;
									}
									if ((pieceAttacks & squaresNearKing[1 - color]) != 0)
									{
										kingSafety[color] += ROOK_ATTACKS_KING;
										kingAttackersCount[color]++;
									}
									superiorPieceAttacked[color] |= pieceAttacks & others & board.queens;
									if ((pieceAttacks & mines & (board.rooks)) != 0)
									{
										positional[color] += ROOK_CONNECT;
									}
									pieceAttacks = BitboardUtils.COLUMN[column];
									if ((pieceAttacks & board.pawns) == 0)
									{
										positional[color] += ROOK_COLUMN_OPEN;
									}
									else
									{
										if ((pieceAttacks & board.pawns & mines) == 0)
										{
											positional[color] += ROOK_COLUMN_SEMIOPEN;
										}
									}
								}
								else
								{
									if ((square & board.queens) != 0)
									{
										center[color] += queenIndexValue[pcsqIndex];
										material[color] += QUEEN;
										pieceAttacks = bbAttacks.GetRookAttacks(index, all) | bbAttacks.GetBishopAttacks(
											index, all);
										auxInt = BitboardUtils.PopCount(pieceAttacks & ~mines & ~pawnAttacks[1 - color]) 
											- QUEEN_M_UNITS;
										mobility[color] += QUEEN_M * auxInt;
										if ((pieceAttacks & squaresNearKing[1 - color]) != 0)
										{
											kingSafety[color] += QUEEN_ATTACKS_KING;
											kingAttackersCount[color]++;
										}
										pieceAttacksXray = (bbAttacks.GetRookAttacks(index, all & ~(pieceAttacks & others
											)) | bbAttacks.GetBishopAttacks(index, all & ~(pieceAttacks & others))) & ~pieceAttacks;
										if ((pieceAttacksXray & board.kings & others) != 0)
										{
											attacks[color] += PINNED_PIECE;
										}
									}
									else
									{
										if ((square & board.kings) != 0)
										{
											pieceAttacks = bbAttacks.king[index];
											center[color] += kingIndexValue[pcsqIndex];
											// TODO
											if ((square & (isWhite ? BitboardUtils.RANK[1] : BitboardUtils.RANK[7])) != 0)
											{
												positional[color] += KING_PAWN_NEAR * BitboardUtils.PopCount(pieceAttacks & mines
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
				[0] - passedPawns[1]) + (config.GetEvalKingSafety() / 8) * ((KING_SAFETY_PONDER[
				kingAttackersCount[0]] * kingSafety[0] - KING_SAFETY_PONDER[kingAttackersCount[1
				]] * kingSafety[1])) + config.GetEvalAttacks() * ((BitboardUtils.PopCount(superiorPieceAttacked
				[0]) >= 2 ? HUNG_PIECES : 0) - (BitboardUtils.PopCount(superiorPieceAttacked[1])
				 >= 2 ? HUNG_PIECES : 0));
			// Divide by eight
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
