/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Bitboard;
using Com.Alonsoruibal.Chess.Movesort;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Movesort
{
	/// <summary>
	/// Sort Moves based on heuristics
	/// short first GOOD captures (a piece of less value captures other of more value)
	/// SEE captures, and move captures with SEE&lt;0 to the end
	/// </summary>
	public class MoveIterator
	{
		public const int PHASE_TT = 0;

		public const int PHASE_GEN_CAPTURES = 1;

		public const int PHASE_GOOD_CAPTURES_AND_PROMOS = 2;

		public const int PHASE_EQUAL_CAPTURES = 3;

		public const int PHASE_GEN_NONCAPTURES = 4;

		public const int PHASE_KILLER1 = 5;

		public const int PHASE_KILLER2 = 6;

		public const int PHASE_NONCAPTURES = 7;

		public const int PHASE_BAD_CAPTURES = 8;

		public const int PHASE_END = 9;

		private static readonly int[] VICTIM_PIECE_VALUES = new int[] { 0, 100, 325, 330, 
			500, 975, 10000 };

		private static readonly int[] AGGRESSOR_PIECE_VALUES = new int[] { 0, 10, 32, 33, 
			50, 97, 99 };

		private const int SCORE_PROMOTION_QUEEN = 975;

		private const int SCORE_UNDERPROMOTION = int.MinValue + 1;

		private const int SCORE_LOWEST = int.MinValue;

		private Board board;

		private int ttMove;

		private int killer1;

		private int killer2;

		private bool foundKiller1;

		private bool foundKiller2;

		private bool quiescence;

		private bool generateChecks;

		private bool checkEvasion;

		private int nonCaptureIndex;

		private int goodCaptureIndex;

		private int equalCaptureIndex;

		private int badCaptureIndex;

		private long all;

		private long mines;

		private long others;

		private long[] attacks = new long[64];

		public int[] goodCaptures = new int[256];

		public int[] goodCapturesScores = new int[256];

		public int[] badCaptures = new int[256];

		public int[] badCapturesScores = new int[256];

		public int[] equalCaptures = new int[256];

		public int[] equalCapturesScores = new int[256];

		public int[] nonCaptures = new int[256];

		public int[] nonCapturesScores = new int[256];

		private int depth;

		internal SortInfo sortInfo;

		internal int phase;

		//	private static final Logger logger = Logger.getLogger(MoveIterator.class);
		// Stores slider pieces attacks
		// Stores captures and queen promotions
		// Stores captures and queen promotions
		// Stores captures and queen promotions
		// Stores non captures and underpromotions
		public virtual int GetPhase()
		{
			return phase;
		}

		public MoveIterator(Board board, SortInfo sortInfo, int depth)
		{
			this.sortInfo = sortInfo;
			this.board = board;
			this.depth = depth;
		}

		public virtual void SetBoard(Board board)
		{
			this.board = board;
		}

		/// <summary>Generates captures and tactical moves (not underpromotions)</summary>
		public virtual void GenerateCaptures()
		{
			//		logger.debug(board);
			all = board.GetAll();
			// only for clearity
			mines = board.GetMines();
			others = board.GetOthers();
			byte index = 0;
			long square = unchecked((long)(0x1L));
			while (square != 0)
			{
				attacks[index] = 0;
				if (board.GetTurn() == ((square & board.whites) != 0))
				{
					if ((square & board.rooks) != 0)
					{
						// Rook
						attacks[index] = BitboardAttacks.GetRookAttacks(index, all);
						GenerateCapturesFromAttacks(Move.ROOK, index, attacks[index] & others);
					}
					else
					{
						if ((square & board.bishops) != 0)
						{
							// Bishop
							attacks[index] = BitboardAttacks.GetBishopAttacks(index, all);
							GenerateCapturesFromAttacks(Move.BISHOP, index, attacks[index] & others);
						}
						else
						{
							if ((square & board.queens) != 0)
							{
								// Queen
								attacks[index] = BitboardAttacks.GetRookAttacks(index, all) | BitboardAttacks.GetBishopAttacks
									(index, all);
								GenerateCapturesFromAttacks(Move.QUEEN, index, attacks[index] & others);
							}
							else
							{
								if ((square & board.kings) != 0)
								{
									// King
									GenerateCapturesFromAttacks(Move.KING, index, BitboardAttacks.king[index] & others
										);
								}
								else
								{
									if ((square & board.knights) != 0)
									{
										// Knight
										GenerateCapturesFromAttacks(Move.KNIGHT, index, BitboardAttacks.knight[index] & others
											);
									}
									else
									{
										if ((square & board.pawns) != 0)
										{
											// Pawns
											if ((square & board.whites) != 0)
											{
												GeneratePawnCapturesAndGoodPromos(index, (BitboardAttacks.pawnUpwards[index] & (others
													 | board.GetPassantSquare())) | (((square << 8) & all) == 0 ? (square << 8) : 0)
													, board.GetPassantSquare());
											}
											else
											{
												GeneratePawnCapturesAndGoodPromos(index, (BitboardAttacks.pawnDownwards[index] & 
													(others | board.GetPassantSquare())) | ((((long)(((ulong)square) >> 8)) & all) ==
													 0 ? ((long)(((ulong)square) >> 8)) : 0), board.GetPassantSquare());
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
		}

		/// <summary>Generates underpromotions and non tactical moves</summary>
		public virtual void GenerateNonCaptures()
		{
			all = board.GetAll();
			// only for clearity
			mines = board.GetMines();
			others = board.GetOthers();
			byte index = 0;
			long square = unchecked((long)(0x1L));
			while (square != 0)
			{
				if (board.GetTurn() == ((square & board.whites) != 0))
				{
					if ((square & board.rooks) != 0)
					{
						// Rook
						GenerateNonCapturesFromAttacks(Move.ROOK, index, attacks[index] & ~all);
					}
					else
					{
						if ((square & board.bishops) != 0)
						{
							// Bishop
							GenerateNonCapturesFromAttacks(Move.BISHOP, index, attacks[index] & ~all);
						}
						else
						{
							if ((square & board.queens) != 0)
							{
								// Queen
								GenerateNonCapturesFromAttacks(Move.QUEEN, index, attacks[index] & ~all);
							}
							else
							{
								if ((square & board.kings) != 0)
								{
									// King
									GenerateNonCapturesFromAttacks(Move.KING, index, BitboardAttacks.king[index] & ~all
										);
								}
								else
								{
									if ((square & board.knights) != 0)
									{
										// Knight
										GenerateNonCapturesFromAttacks(Move.KNIGHT, index, BitboardAttacks.knight[index] 
											& ~all);
									}
								}
							}
						}
					}
					if ((square & board.pawns) != 0)
					{
						// Pawns
						if ((square & board.whites) != 0)
						{
							GeneratePawnNonCapturesAndBadPromos(index, (BitboardAttacks.pawnUpwards[index] & 
								others) | (((square << 8) & all) == 0 ? (square << 8) : 0) | ((square & BitboardUtils
								.b2_d) != 0 && (((square << 8) | (square << 16)) & all) == 0 ? (square << 16) : 
								0));
						}
						else
						{
							GeneratePawnNonCapturesAndBadPromos(index, (BitboardAttacks.pawnDownwards[index] 
								& others) | ((((long)(((ulong)square) >> 8)) & all) == 0 ? ((long)(((ulong)square
								) >> 8)) : 0) | ((square & BitboardUtils.b2_u) != 0 && ((((long)(((ulong)square)
								 >> 8)) | ((long)(((ulong)square) >> 16))) & all) == 0 ? ((long)(((ulong)square)
								 >> 16)) : 0));
						}
					}
				}
				square <<= 1;
				index++;
			}
			square = board.kings & mines;
			// my king
			byte myKingIndex = unchecked((byte)(-1));
			// Castling: disabled when in check or squares attacked
			if ((((all & (board.GetTurn() ? unchecked((long)(0x06L)) : unchecked((long)(0x0600000000000000L
				)))) == 0 && (board.GetTurn() ? board.GetWhiteKingsideCastling() : board.GetBlackKingsideCastling
				()))))
			{
				myKingIndex = BitboardUtils.Square2Index(square);
				if (!board.GetCheck() && !BitboardAttacks.IsIndexAttacked(board, unchecked((byte)
					(myKingIndex - 1)), board.GetTurn()) && !BitboardAttacks.IsIndexAttacked(board, 
					unchecked((byte)(myKingIndex - 2)), board.GetTurn()))
				{
					AddNonCapturesAndBadPromos(Move.KING, myKingIndex, myKingIndex - 2, 0, false, Move
						.TYPE_KINGSIDE_CASTLING);
				}
			}
			if ((((all & (board.GetTurn() ? unchecked((long)(0x70L)) : unchecked((long)(0x7000000000000000L
				)))) == 0 && (board.GetTurn() ? board.GetWhiteQueensideCastling() : board.GetBlackQueensideCastling
				()))))
			{
				if (myKingIndex == -1)
				{
					myKingIndex = BitboardUtils.Square2Index(square);
				}
				if (!board.GetCheck() && !BitboardAttacks.IsIndexAttacked(board, unchecked((byte)
					(myKingIndex + 1)), board.GetTurn()) && !BitboardAttacks.IsIndexAttacked(board, 
					unchecked((byte)(myKingIndex + 2)), board.GetTurn()))
				{
					AddNonCapturesAndBadPromos(Move.KING, myKingIndex, myKingIndex + 2, 0, false, Move
						.TYPE_QUEENSIDE_CASTLING);
				}
			}
		}

		/// <summary>Generates moves from an attack mask</summary>
		private void GenerateCapturesFromAttacks(int pieceMoved, int fromIndex, long attacks
			)
		{
			while (attacks != 0)
			{
				long to = BitboardUtils.Lsb(attacks);
				AddCapturesAndGoodPromos(pieceMoved, fromIndex, BitboardUtils.Square2Index(to), to
					, true, 0);
				attacks ^= to;
			}
		}

		private void GenerateNonCapturesFromAttacks(int pieceMoved, int fromIndex, long attacks
			)
		{
			while (attacks != 0)
			{
				long to = BitboardUtils.Lsb(attacks);
				AddNonCapturesAndBadPromos(pieceMoved, fromIndex, BitboardUtils.Square2Index(to), 
					to, false, 0);
				attacks ^= to;
			}
		}

		private void GeneratePawnCapturesAndGoodPromos(int fromIndex, long attacks, long 
			passant)
		{
			while (attacks != 0)
			{
				long to = BitboardUtils.Lsb(attacks);
				if ((to & passant) != 0)
				{
					AddCapturesAndGoodPromos(Move.PAWN, fromIndex, BitboardUtils.Square2Index(to), to
						, true, Move.TYPE_PASSANT);
				}
				else
				{
					bool capture = (to & others) != 0;
					if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0)
					{
						AddCapturesAndGoodPromos(Move.PAWN, fromIndex, BitboardUtils.Square2Index(to), to
							, capture, Move.TYPE_PROMOTION_QUEEN);
					}
					else
					{
						if (capture)
						{
							AddCapturesAndGoodPromos(Move.PAWN, fromIndex, BitboardUtils.Square2Index(to), to
								, capture, 0);
						}
					}
				}
				attacks ^= to;
			}
		}

		private void GeneratePawnNonCapturesAndBadPromos(int fromIndex, long attacks)
		{
			while (attacks != 0)
			{
				long to = BitboardUtils.Lsb(attacks);
				bool capture = (to & others) != 0;
				if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0)
				{
					AddNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.Square2Index(to), 
						to, capture, Move.TYPE_PROMOTION_KNIGHT);
					AddNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.Square2Index(to), 
						to, capture, Move.TYPE_PROMOTION_ROOK);
					AddNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.Square2Index(to), 
						to, capture, Move.TYPE_PROMOTION_BISHOP);
				}
				else
				{
					if (!capture)
					{
						AddNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.Square2Index(to), 
							to, capture, 0);
					}
				}
				attacks ^= to;
			}
		}

		private void AddNonCapturesAndBadPromos(int pieceMoved, int fromIndex, int toIndex
			, long to, bool capture, int moveType)
		{
			int move = Move.GenMove(fromIndex, toIndex, pieceMoved, capture, moveType);
			if (move == killer1)
			{
				foundKiller1 = true;
			}
			else
			{
				if (move == killer2)
				{
					foundKiller2 = true;
				}
				else
				{
					if (move != ttMove)
					{
						// Score non captures
						int score = sortInfo.GetMoveScore(move);
						if (moveType == Move.TYPE_PROMOTION_KNIGHT || moveType == Move.TYPE_PROMOTION_ROOK
							 || moveType == Move.TYPE_PROMOTION_BISHOP)
						{
							score -= SCORE_UNDERPROMOTION;
						}
						//			System.out.println("* " + score + " - " + Move.toStringExt(move));
						nonCaptures[nonCaptureIndex] = move;
						nonCapturesScores[nonCaptureIndex] = score;
						nonCaptureIndex++;
					}
				}
			}
		}

		private void AddCapturesAndGoodPromos(int pieceMoved, int fromIndex, int toIndex, 
			long to, bool capture, int moveType)
		{
			int move = Move.GenMove(fromIndex, toIndex, pieceMoved, capture, moveType);
			if (move != ttMove)
			{
				// Score captures
				int pieceCaptured = 0;
				if ((to & board.knights) != 0)
				{
					pieceCaptured = Move.KNIGHT;
				}
				else
				{
					if ((to & board.bishops) != 0)
					{
						pieceCaptured = Move.BISHOP;
					}
					else
					{
						if ((to & board.rooks) != 0)
						{
							pieceCaptured = Move.ROOK;
						}
						else
						{
							if ((to & board.queens) != 0)
							{
								pieceCaptured = Move.QUEEN;
							}
							else
							{
								if (capture)
								{
									pieceCaptured = Move.PAWN;
								}
							}
						}
					}
				}
				int see = 0;
				if (capture)
				{
					see = board.See(fromIndex, toIndex, pieceMoved, pieceCaptured);
				}
				if (see >= 0)
				{
					int score = 0;
					// Order GOOD captures by MVV/LVA (Hyatt dixit)
					if (capture)
					{
						score = VICTIM_PIECE_VALUES[pieceCaptured] - AGGRESSOR_PIECE_VALUES[pieceMoved];
					}
					if (see > 0 || moveType == Move.TYPE_PROMOTION_QUEEN)
					{
						if (moveType == Move.TYPE_PROMOTION_QUEEN)
						{
							score += SCORE_PROMOTION_QUEEN;
						}
						goodCaptures[goodCaptureIndex] = move;
						goodCapturesScores[goodCaptureIndex] = score;
						goodCaptureIndex++;
					}
					else
					{
						equalCaptures[equalCaptureIndex] = move;
						equalCapturesScores[equalCaptureIndex] = score;
						equalCaptureIndex++;
					}
				}
				else
				{
					badCaptures[badCaptureIndex] = move;
					badCapturesScores[badCaptureIndex] = see;
					badCaptureIndex++;
				}
			}
		}

		/// <summary>Moves are sorted ascending (best moves at the end)</summary>
		public virtual void GenMoves(int ttMove)
		{
			GenMoves(ttMove, false, true);
		}

		public virtual void GenMoves(int ttMove, bool quiescence, bool generateChecks)
		{
			this.ttMove = ttMove;
			foundKiller1 = false;
			foundKiller2 = false;
			this.quiescence = quiescence;
			this.generateChecks = generateChecks;
			this.checkEvasion = board.GetCheck();
			killer1 = sortInfo.killerMove1[depth];
			killer2 = sortInfo.killerMove2[depth];
			phase = 0;
			goodCaptureIndex = 0;
			badCaptureIndex = 0;
			equalCaptureIndex = 0;
			nonCaptureIndex = 0;
		}

		public virtual int Next()
		{
			int maxScore;
			int bestIndex;
			switch (phase)
			{
				case PHASE_TT:
				{
					phase++;
					if (ttMove != 0)
					{
						return ttMove;
					}
					goto case PHASE_GEN_CAPTURES;
				}

				case PHASE_GEN_CAPTURES:
				{
					phase++;
					GenerateCaptures();
					goto case PHASE_GOOD_CAPTURES_AND_PROMOS;
				}

				case PHASE_GOOD_CAPTURES_AND_PROMOS:
				{
					maxScore = SCORE_LOWEST;
					bestIndex = -1;
					for (int i = 0; i < goodCaptureIndex; i++)
					{
						if (goodCapturesScores[i] > maxScore)
						{
							maxScore = goodCapturesScores[i];
							bestIndex = i;
						}
					}
					if (bestIndex != -1)
					{
						goodCapturesScores[bestIndex] = SCORE_LOWEST;
						return goodCaptures[bestIndex];
					}
					phase++;
					goto case PHASE_EQUAL_CAPTURES;
				}

				case PHASE_EQUAL_CAPTURES:
				{
					maxScore = SCORE_LOWEST;
					bestIndex = -1;
					for (int i_1 = 0; i_1 < equalCaptureIndex; i_1++)
					{
						if (equalCapturesScores[i_1] > maxScore)
						{
							maxScore = equalCapturesScores[i_1];
							bestIndex = i_1;
						}
					}
					if (bestIndex != -1)
					{
						equalCapturesScores[bestIndex] = SCORE_LOWEST;
						return equalCaptures[bestIndex];
					}
					phase++;
					goto case PHASE_GEN_NONCAPTURES;
				}

				case PHASE_GEN_NONCAPTURES:
				{
					phase++;
					if (quiescence && !generateChecks && !checkEvasion)
					{
						phase = PHASE_END;
						return 0;
					}
					GenerateNonCaptures();
					goto case PHASE_KILLER1;
				}

				case PHASE_KILLER1:
				{
					phase++;
					if (foundKiller1)
					{
						return killer1;
					}
					goto case PHASE_KILLER2;
				}

				case PHASE_KILLER2:
				{
					phase++;
					if (foundKiller2)
					{
						return killer2;
					}
					goto case PHASE_NONCAPTURES;
				}

				case PHASE_NONCAPTURES:
				{
					maxScore = SCORE_LOWEST;
					bestIndex = -1;
					for (int i_2 = 0; i_2 < nonCaptureIndex; i_2++)
					{
						if (nonCapturesScores[i_2] > maxScore)
						{
							maxScore = nonCapturesScores[i_2];
							bestIndex = i_2;
						}
					}
					if (bestIndex != -1)
					{
						nonCapturesScores[bestIndex] = SCORE_LOWEST;
						return nonCaptures[bestIndex];
					}
					phase++;
					goto case PHASE_BAD_CAPTURES;
				}

				case PHASE_BAD_CAPTURES:
				{
					maxScore = SCORE_LOWEST;
					bestIndex = -1;
					for (int i_3 = 0; i_3 < badCaptureIndex; i_3++)
					{
						if (badCapturesScores[i_3] > maxScore)
						{
							maxScore = badCapturesScores[i_3];
							bestIndex = i_3;
						}
					}
					if (bestIndex != -1)
					{
						badCapturesScores[bestIndex] = SCORE_LOWEST;
						return badCaptures[bestIndex];
					}
					break;
				}
			}
			return 0;
		}
	}
}
