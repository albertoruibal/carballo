/*
 * Code converted with Sharpen
 * 
 */using System;
using System.Collections.Generic;
using System.Text;
using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Bitboard;
using Com.Alonsoruibal.Chess.Evaluation;
using Com.Alonsoruibal.Chess.Log;
using Com.Alonsoruibal.Chess.Movesort;
using Com.Alonsoruibal.Chess.Search;
using Com.Alonsoruibal.Chess.TT;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Search
{
	/// <summary>Search engine</summary>
	/// <author>Alberto Alonso Ruibal</author>
	public class SearchEngine : Runnable
	{
		/// <summary>Logger for this class</summary>
		private static readonly Logger logger = Logger.GetLogger("SearchEngine");

		public const int MAX_DEPTH = 64;

		private const int PLY = 2;

		private const int LMR_DEPTHS_NOT_REDUCED = 3 * PLY;

		private const int RAZOR_DEPTH = 4 * PLY;

		public const int NODE_ROOT = 0;

		public const int NODE_PV = 1;

		public const int NODE_NULL = 2;

		private SearchParameters searchParameters;

		private bool searching = false;

		private bool foundOneMove = false;

		private Config config;

		private long thinkTo = 0;

		private Board board;

		private SearchObserver observer;

		private Evaluator evaluator;

		private TranspositionTable tt;

		private SortInfo sortInfo;

		private MoveIterator[] moveIterators;

		private long bestMoveTime;

		private int globalBestMove;

		private int ponderMove;

		private string pv;

		private int initialPly;

		private int depth;

		private int score;

		private int[] aspWindows;

		internal long startTime;

		private long positionCounter;

		private long pvPositionCounter;

		private long qsPositionCounter;

		private long pvCutNodes;

		private long pvAllNodes;

		private long nullCutNodes;

		private long nullAllNodes;

		private static long aspirationWindowProbe = 0;

		private static long aspirationWindowHit = 0;

		private static long futilityHit = 0;

		private static long aggressiveFutilityHit = 0;

		private static long razoringProbe = 0;

		private static long razoringHit = 0;

		private static long singularExtensionProbe = 0;

		private static long singularExtensionHit = 0;

		private static long nullMoveProbe = 0;

		private static long nullMoveHit = 0;

		private static long ttProbe = 0;

		private static long ttPvHit = 0;

		private static long ttLBHit = 0;

		private static long ttUBHit = 0;

		private static long ttEvalHit = 0;

		private static long ttEvalProbe = 0;

		private bool initialized;

		private Random random;

		private int[][] pvReductionMatrix;

		private int[][] nonPvReductionMatrix;

		private int[] singularMoveDepth = new int[] { 6 * PLY, 6 * PLY, 8 * PLY };

		private int[] iidDepth = new int[] { 5 * PLY, 5 * PLY, 8 * PLY };

		public SearchEngine(Config config)
		{
			// time to think to
			// For testing suites
			// Inital Ply of search
			// For performance Benching
			// aspiration window
			// Futility pruning
			// Aggresive Futility pruning
			// Razoring
			// Singular Extension
			// Null Move
			// Transposition Table
			this.config = config;
			random = new Random();
			board = new Board();
			sortInfo = new SortInfo();
			moveIterators = new MoveIterator[MAX_DEPTH];
			for (int i = 0; i < MAX_DEPTH; i++)
			{
				moveIterators[i] = new MoveIterator(board, sortInfo, i);
			}
			pvReductionMatrix = new int[][] { new int[64], new int[64], new int[64], new int[
				64], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64
				], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], 
				new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new 
				int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int
				[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64
				], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], 
				new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new 
				int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int
				[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64
				], new int[64], new int[64], new int[64], new int[64] };
			nonPvReductionMatrix = new int[][] { new int[64], new int[64], new int[64], new int
				[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64
				], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], 
				new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new 
				int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int
				[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64
				], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], 
				new int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new 
				int[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int
				[64], new int[64], new int[64], new int[64], new int[64], new int[64], new int[64
				], new int[64], new int[64], new int[64], new int[64] };
			// Init our reduction lookup tables
			for (int depth = 1; depth < MAX_DEPTH; depth++)
			{
				// OnePly = 1
				for (int moveNumber = 1; moveNumber < 64; moveNumber++)
				{
					double pvRed = 0.5 + Math.Log(depth) * Math.Log(moveNumber) / 6.0;
					double nonPVRed = 0.5 + Math.Log(depth) * Math.Log(moveNumber) / 3.0;
					pvReductionMatrix[depth][moveNumber] = (int)(pvRed >= 1.0 ? Math.Floor(pvRed * PLY
						) : 0);
					nonPvReductionMatrix[depth][moveNumber] = (int)(nonPVRed >= 1.0 ? Math.Floor(nonPVRed
						 * PLY) : 0);
				}
			}
			// System.out.println(i + " " + j + " " +
			// pvReductionMatrix[i][j] + " " + nonPvReductionMatrix[i][j]);
			Init();
		}

		private int GetReduction(int nodeType, int depth, int movecount)
		{
			return nodeType == NODE_PV || nodeType == NODE_ROOT ? pvReductionMatrix[Math.Min(
				depth / PLY, 63)][Math.Min(movecount, 63)] : nonPvReductionMatrix[Math.Min(depth
				 / PLY, 63)][Math.Min(movecount, 63)];
		}

		public virtual void Destroy()
		{
			config = null;
			observer = null;
			tt = null;
			evaluator = null;
			sortInfo = null;
			if (moveIterators != null)
			{
				for (int i = 0; i < MAX_DEPTH; i++)
				{
					moveIterators[i] = null;
				}
			}
			System.GC.Collect();
		}

		public virtual void Init()
		{
			logger.Debug(new DateTime());
			initialized = false;
			board.StartPosition();
			sortInfo.Clear();
			logger.Debug("Creating Evaluator");
			string evaluatorName = config.GetEvaluator();
			if ("simplified".Equals(evaluatorName))
			{
				evaluator = new SimplifiedEvaluator();
			}
			else
			{
				if ("complete".Equals(evaluatorName))
				{
					evaluator = new CompleteEvaluator(config);
				}
				else
				{
					if ("experimental".Equals(evaluatorName))
					{
						evaluator = new ExperimentalEvaluator(config);
					}
				}
			}
			int size = BitboardUtils.Square2Index(Sharpen.Extensions.ValueOf(config.GetTranspositionTableSize
				())) + 16;
			logger.Debug("Creating TT");
			// tt = new TwoTierTranspositionTable(size);
			// tt = new MultiprobeTranspositionTableNew(size);
			tt = new MultiprobeTranspositionTable(size);
			initialized = true;
			logger.Debug(config.ToString());
		}

		public virtual void SetObserver(SearchObserver observer)
		{
			this.observer = observer;
		}

		public virtual Board GetBoard()
		{
			return board;
		}

		public virtual int GetBestMove()
		{
			return globalBestMove;
		}

		public virtual long GetBestMoveTime()
		{
			return bestMoveTime;
		}

		public virtual Config GetConfig()
		{
			return config;
		}

		public virtual void SetConfig(Config config)
		{
			this.config = config;
		}

		/// <summary>
		/// Decides when we are going to allow null move Don't do null move in king
		/// and pawn endings
		/// </summary>
		private bool BoardAllowNullMove()
		{
			return !board.GetCheck() && (board.GetMines() & (board.knights | board.bishops | 
				board.rooks | board.queens)) != 0;
		}

		/// <summary>
		/// Calculates the extension of a move in the actual position (with the move
		/// done)
		/// </summary>
		private int Extensions(int move, bool mateThreat, bool pv)
		{
			int ext = 0;
			if (board.GetCheck())
			{
				ext += pv ? config.GetExtensionsCheck() : config.GetExtensionsCheck() >> 1;
			}
			if (Move.GetPieceMoved(move) == Move.PAWN)
			{
				if (Move.IsPawnPush(move))
				{
					ext += config.GetExtensionsPawnPush();
				}
				if (board.IsPassedPawn(Move.GetToIndex(move)))
				{
					// ext += pv ? config.getExtensionsPassedPawn() :
					// config.getExtensionsPassedPawn() >> 1;
					ext += config.GetExtensionsPassedPawn();
				}
			}
			if (mateThreat)
			{
				ext += config.GetExtensionsMateThreat();
			}
			if (ext < config.GetExtensionsRecapture() && board.GetLastMoveIsRecapture())
			{
				int seeValue = board.See(move);
				int capturedPieceValue = PieceValue(board.GetPieceAt(Move.GetToSquare(move)));
				if (seeValue > capturedPieceValue - 50)
				{
					ext = config.GetExtensionsRecapture();
				}
			}
			if (ext > PLY)
			{
				ext = PLY;
			}
			return ext;
		}

		/// <summary>
		/// Returns true if we can use the value stored on the TT to return from
		/// search
		/// </summary>
		private bool CanUseTT(int depthRemaining, int alpha, int beta)
		{
			if (tt.GetDepthAnalyzed() >= depthRemaining && tt.IsMyGeneration())
			{
				switch (tt.GetNodeType())
				{
					case TranspositionTable.TYPE_EXACT_SCORE:
					{
						ttPvHit++;
						return true;
					}

					case TranspositionTable.TYPE_FAIL_LOW:
					{
						ttLBHit++;
						if (tt.GetScore() <= alpha)
						{
							return true;
						}
						break;
					}

					case TranspositionTable.TYPE_FAIL_HIGH:
					{
						ttUBHit++;
						if (tt.GetScore() >= beta)
						{
							return true;
						}
						break;
					}
				}
			}
			return false;
		}

		/// <summary>
		/// Also changes sign to score depending of turn usetTT requires to do a
		/// previous search on TT
		/// </summary>
		private int Eval(int alpha, int beta, bool foundTT, bool refine)
		{
			ttEvalProbe++;
			if (foundTT)
			{
				if (tt.GetNodeType() == TranspositionTable.TYPE_EVAL)
				{
					ttEvalHit++;
					// // uncomment to test if eval tt is Working: seems yes
					// if (evaluator.evaluateBoard(board, alpha, beta) !=
					// tt.getScore()) {
					// System.out.println("Error Garrafal!!!");
					// System.out.println(tt.getScore());
					// System.out.println(evaluator.evaluateBoard(board, alpha,
					// beta));
					// System.out.println(board.toString());
					// System.exit(-1);
					// }
					int score = tt.GetScore();
					if (!board.GetTurn())
					{
						score = -score;
					}
					return score;
				}
			}
			int score_1 = evaluator.EvaluateBoard(board, alpha, beta);
			tt.Set(board, TranspositionTable.TYPE_EVAL, 0, score_1, unchecked((byte)0), false
				);
			if (!board.GetTurn())
			{
				score_1 = -score_1;
			}
			if (foundTT && refine)
			{
				switch (tt.GetNodeType())
				{
					case TranspositionTable.TYPE_FAIL_LOW:
					{
						// Refine Value with TT
						if (tt.GetScore() > score_1)
						{
							score_1 = tt.GetScore();
						}
						break;
					}

					case TranspositionTable.TYPE_FAIL_HIGH:
					{
						if (tt.GetScore() < score_1)
						{
							score_1 = tt.GetScore();
						}
						break;
					}
				}
			}
			return score_1;
		}

		private int LastCapturedPieceValue(Board board)
		{
			return PieceValue(board.GetLastCapturedPiece());
		}

		private int PieceValue(char piece)
		{
			int capturedPieceValue = 0;
			switch (System.Char.ToLower(piece))
			{
				case 'p':
				{
					return CompleteEvaluator.PAWN;
				}

				case 'n':
				{
					return CompleteEvaluator.KNIGHT;
				}

				case 'b':
				{
					return CompleteEvaluator.BISHOP;
				}

				case 'r':
				{
					return CompleteEvaluator.ROOK;
				}

				case 'q':
				{
					return CompleteEvaluator.QUEEN;
				}
			}
			return capturedPieceValue;
		}

		/// <summary>Search horizon node (depth == 0) some kind of quiescent search</summary>
		/// <returns></returns>
		/// <exception cref="SearchFinishedException">SearchFinishedException</exception>
		/// <exception cref="Com.Alonsoruibal.Chess.Search.SearchFinishedException"></exception>
		public virtual int QuiescentSearch(int qsdepth, int alpha, int beta)
		{
			if (Runtime.CurrentTimeMillis() > thinkTo && foundOneMove)
			{
				throw new SearchFinishedException();
			}
			qsPositionCounter++;
			// checks draw by three fold repetition. and fifty moves rule
			if (board.IsDraw())
			{
				return EvaluateDraw();
			}
			int ttMove = 0;
			int eval = -Evaluator.VICTORY;
			int score = -Evaluator.VICTORY;
			bool pv = beta - alpha > 1;
			// int initialAlpha = alpha;
			// int bestMove = 0;
			ttProbe++;
			bool foundTT = tt.Search(board, false);
			if (foundTT)
			{
				if (!pv && CanUseTT(0, alpha, beta))
				{
					return tt.GetScore();
				}
			}
			//ttMove = tt.getBestMove();
			// Do not allow stand pat when in check
			if (!board.GetCheck())
			{
				eval = Eval(alpha, beta, foundTT, true);
				// Evaluation functions increase alpha and can originate beta
				// cutoffs
				if (eval >= beta)
				{
					return eval;
				}
				if (eval > alpha)
				{
					alpha = eval;
				}
			}
			// If we have more depths than needed...
			if (board.GetMoveNumber() - initialPly >= MAX_DEPTH)
			{
				System.Console.Out.WriteLine("Quiescence exceeds depth qsdepth=" + qsdepth);
				System.Console.Out.WriteLine(board.ToString());
				for (int i = 0; i < board.GetMoveNumber(); i++)
				{
					System.Console.Out.WriteLine(Move.ToStringExt(board.moveHistory[i]));
				}
				// System.exit(-1);
				return eval;
			}
			bool validOperations = false;
			bool checkEvasion = board.GetCheck();
			// Generate checks for PV on PLY 0
			bool generateChecks = pv && (qsdepth == 0);
			MoveIterator moveIterator = moveIterators[board.GetMoveNumber() - initialPly];
			moveIterator.GenMoves(ttMove, true, generateChecks);
			int move = 0;
			while ((move = moveIterator.Next()) != 0)
			{
				if (board.DoMove(move, false))
				{
					validOperations = true;
					// Futility pruning
					if (!board.GetCheck() && !checkEvasion && !Move.IsPromotion(move) && !Move.IsPawnPush
						(move) && !pv && (((board.queens | board.rooks) & board.GetMines()) != 0 || (BitboardUtils
						.PopCount(board.bishops | board.knights) & board.GetMines()) > 1))
					{
						int futilityValue = eval + LastCapturedPieceValue(board) + config.GetFutilityMarginQS
							();
						if (futilityValue < alpha)
						{
							// if (futilityValue > bestValue) bestValue =
							// futilityValue; //TODO
							// System.out.println("PRUNE!");
							board.UndoMove();
							continue;
						}
					}
					// Necessary because TT move can be a no promotion or capture
					if (!checkEvasion && !(board.GetCheck() && generateChecks) && moveIterator.GetPhase
						() > MoveIterator.PHASE_GOOD_CAPTURES_AND_PROMOS)
					{
						//
						//
						board.UndoMove();
						continue;
					}
					score = -QuiescentSearch(qsdepth + 1, -beta, -alpha);
					board.UndoMove();
					if (score > alpha)
					{
						alpha = score;
						// bestMove = move;
						if (score >= beta)
						{
							break;
						}
					}
				}
			}
			if (board.GetCheck() && !validOperations)
			{
				return ValueMatedIn(board.GetMoveNumber() - initialPly);
			}
			return alpha;
		}

		/// <summary>Search Root, PV and null window</summary>
		/// <exception cref="Com.Alonsoruibal.Chess.Search.SearchFinishedException"></exception>
		public virtual int Search(int nodeType, int depthRemaining, int alpha, int beta, 
			bool allowNullMove, int excludedMove)
		{
			if (Runtime.CurrentTimeMillis() > thinkTo && foundOneMove)
			{
				throw new SearchFinishedException();
			}
			if (nodeType == NODE_PV || nodeType == NODE_ROOT)
			{
				pvPositionCounter++;
			}
			else
			{
				positionCounter++;
			}
			// checks draw by treefold rep. and fifty moves rule
			if (board.IsDraw())
			{
				return EvaluateDraw();
			}
			// Mate distance pruning
			alpha = Math.Max(ValueMatedIn(board.GetMoveNumber() - initialPly), alpha);
			beta = Math.Min(ValueMateIn(board.GetMoveNumber() - initialPly + 1), beta);
			if (alpha >= beta)
			{
				return alpha;
			}
			int ttMove = 0;
			int ttScore = 0;
			int bestMove = 0;
			int bestScore = -Evaluator.VICTORY;
			int score = 0;
			bool mateThreat = false;
			ttProbe++;
			bool foundTT = tt.Search(board, excludedMove != 0);
			if (foundTT)
			{
				if (nodeType != NODE_ROOT && CanUseTT(depthRemaining, alpha, beta))
				{
					//
					return tt.GetScore();
				}
				ttMove = tt.GetBestMove();
				ttScore = tt.GetScore();
			}
			if (depthRemaining < PLY)
			{
				score = QuiescentSearch(0, alpha, beta);
				return score;
			}
			int eval = -Evaluator.VICTORY;
			// Do a static eval
			if (!board.GetCheck())
			{
				eval = Eval(beta - 1, beta, foundTT, true);
			}
			// Hyatt's Razoring http://chessprogramming.wikispaces.com/Razoring
			if (nodeType == NODE_NULL && config.GetRazoring() && !board.GetCheck() && ttMove 
				== 0 && allowNullMove && depthRemaining < RAZOR_DEPTH && !ValueIsMate(beta) && eval
				 < beta - config.GetRazoringMargin() && (board.pawns & ((board.whites & BitboardUtils
				.b2_u) | (board.blacks & BitboardUtils.b2_d))) == 0)
			{
				//
				//
				//
				//
				// Not when last was a null move
				//
				//
				//
				// No pawns on 7TH
				razoringProbe++;
				int rbeta = beta - config.GetRazoringMargin();
				int v = QuiescentSearch(0, rbeta - 1, rbeta);
				if (v < rbeta)
				{
					razoringHit++;
					return v;
				}
			}
			// Static null move pruning
			if (nodeType == NODE_NULL && config.GetStaticNullMove() && allowNullMove && BoardAllowNullMove
				() && depthRemaining < RAZOR_DEPTH && !ValueIsMate(beta) && eval >= beta + config
				.GetFutilityMargin())
			{
				//
				//
				//
				//
				//
				//
				return eval - config.GetFutilityMargin();
			}
			// Null move pruning and mate threat detection
			if (nodeType == NODE_NULL && config.GetNullMove() && allowNullMove && BoardAllowNullMove
				() && depthRemaining > 3 * PLY && !ValueIsMate(beta) && eval > beta - (depthRemaining
				 >= 4 * PLY ? config.GetNullMoveMargin() : 0))
			{
				//
				//
				//
				//
				//
				//
				nullMoveProbe++;
				board.DoMove(0, false);
				int R = 3 * PLY + (depthRemaining >= 5 * PLY ? depthRemaining / (4 * PLY) : 0);
				if (eval - beta > CompleteEvaluator.PAWN)
				{
					R++;
				}
				score = -Search(NODE_NULL, depthRemaining - R, -beta, -beta + 1, false, 0);
				board.UndoMove();
				if (score >= beta)
				{
					if (ValueIsMate(score))
					{
						score = beta;
					}
					// Verification search on initial depths
					if (depthRemaining < 6 * PLY || Search(NODE_NULL, depthRemaining - 5 * PLY, beta 
						- 1, beta, false, 0) >= beta)
					{
						//
						nullMoveHit++;
						return score;
					}
				}
				else
				{
					// Detect mate threat to exit
					if (score < (-Evaluator.VICTORY + 100))
					{
						mateThreat = true;
					}
				}
			}
			// Internal Iterative Deepening
			if (config.GetIid() && ttMove == 0 && depthRemaining >= iidDepth[nodeType] && allowNullMove
				 && !board.GetCheck() && (nodeType != NODE_NULL || eval > beta - config.GetIidMargin
				()) && excludedMove == 0)
			{
				//
				//
				//
				//
				//
				//
				int d = (nodeType == NODE_PV ? depthRemaining - 2 * PLY : depthRemaining >> 1);
				Search(nodeType, d, alpha, beta, true, 0);
				// TODO Allow null move ?
				if (tt.Search(board, false))
				{
					ttMove = tt.GetBestMove();
				}
			}
			// Singular Move
			bool singularMoveExtension = nodeType != NODE_ROOT && ttMove != 0 && config.GetExtensionsSingular
				() > 0 && depthRemaining >= singularMoveDepth[nodeType] && tt.GetNodeType() == TranspositionTable
				.TYPE_FAIL_HIGH && tt.GetDepthAnalyzed() >= depthRemaining - 3 * PLY && Math.Abs
				(ttScore) < Evaluator.VICTORY - 100;
			//
			//
			//
			//
			// ???
			//
			// Futility pruning
			bool futilityPrune = false;
			if (nodeType == NODE_NULL && !board.GetCheck())
			{
				//
				if (depthRemaining <= PLY)
				{
					// at frontier nodes
					if (config.GetFutility() && eval < beta - config.GetFutilityMargin())
					{
						//
						futilityHit++;
						futilityPrune = true;
					}
				}
				else
				{
					if (depthRemaining <= 2 * PLY)
					{
						// at pre-frontier nodes
						if (config.GetAggressiveFutility() && eval < beta - config.GetAggressiveFutilityMargin
							())
						{
							//
							aggressiveFutilityHit++;
							futilityPrune = true;
						}
					}
				}
			}
			int movesDone = 0;
			MoveIterator moveIterator = moveIterators[board.GetMoveNumber() - initialPly];
			moveIterator.GenMoves(ttMove);
			bool validOperations = false;
			bool checkEvasion = board.GetCheck();
			int move = 0;
			while ((move = moveIterator.Next()) != 0)
			{
				int extension = 0;
				int reduction = 0;
				// Operations are pseudo-legal, doMove checks if they lead to a
				// valid state
				if (board.DoMove(move, false))
				{
					validOperations = true;
					if (move == excludedMove)
					{
						board.UndoMove();
						continue;
					}
					extension += Extensions(move, mateThreat, false);
					// Check singular reply extension
					if (singularMoveExtension && move == ttMove && extension < PLY && excludedMove ==
						 0)
					{
						//
						//
						//
						singularExtensionProbe++;
						board.UndoMove();
						int seBeta = ttScore - config.GetSingularExtensionMargin();
						int excScore = Search(nodeType, depthRemaining >> 1, seBeta - 1, seBeta, false, move
							);
						board.DoMove(move);
						if (excScore < seBeta)
						{
							singularExtensionHit++;
							extension += config.GetExtensionsSingular();
							if (extension > PLY)
							{
								extension = PLY;
							}
						}
					}
					bool importantMove = nodeType == NODE_ROOT || extension != 0 || Move.IsCapture(move
						) || Move.IsPromotion(move) || Move.IsCastling(move) || checkEvasion || move == 
						ttMove || sortInfo.IsKiller(move, board.GetMoveNumber() - initialPly);
					//
					//
					//
					//
					//
					//
					//
					if (futilityPrune && bestScore > -Evaluator.VICTORY + 100 && !importantMove)
					{
						//
						//
						board.UndoMove();
						continue;
					}
					// Late move reductions (LMR)
					if (config.GetLmr() && depthRemaining >= LMR_DEPTHS_NOT_REDUCED && !importantMove)
					{
						//
						//
						reduction += GetReduction(nodeType, depthRemaining, movesDone);
					}
					movesDone++;
					int lowBound = (alpha > bestScore ? alpha : bestScore);
					if ((nodeType == NODE_PV || nodeType == NODE_ROOT) && movesDone == 1)
					{
						// PV move not null searched
						score = -Search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true
							, 0);
					}
					else
					{
						// Try searching null window
						bool doFullSearch = true;
						if (reduction > 0)
						{
							score = -Search(NODE_NULL, depthRemaining - reduction - PLY, -lowBound - 1, -lowBound
								, true, 0);
							doFullSearch = (score > lowBound);
						}
						if (doFullSearch)
						{
							score = -Search(NODE_NULL, depthRemaining + extension - PLY, -lowBound - 1, -lowBound
								, true, 0);
							// Finally search as PV if score on window
							if ((nodeType == NODE_PV || nodeType == NODE_ROOT) && score > lowBound && (nodeType
								 == NODE_ROOT || score < beta))
							{
								//
								//
								score = -Search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true
									, 0);
							}
						}
					}
					board.UndoMove();
					// Tracks the best move also insert errors on the root node
					if (score > bestScore && (nodeType != NODE_ROOT || config.GetRand() == 0 || (random
						.Next(100) > config.GetRand())))
					{
						bestMove = move;
						bestScore = score;
					}
					// alpha/beta cut (fail high)
					if (score >= beta)
					{
						break;
					}
				}
			}
			// Checkmate or stalemate
			if (excludedMove == 0 && !validOperations)
			{
				bestScore = EvaluateEndgame();
			}
			// Tells MoveSorter the move score
			if (bestScore >= beta)
			{
				if (excludedMove == 0)
				{
					sortInfo.BetaCutoff(board, bestMove, board.GetMoveNumber() - initialPly);
				}
				if (nodeType == NODE_NULL)
				{
					nullCutNodes++;
				}
				else
				{
					pvCutNodes++;
				}
			}
			else
			{
				if (nodeType == NODE_NULL)
				{
					nullAllNodes++;
				}
				else
				{
					pvAllNodes++;
				}
			}
			// Save in the transposition Table
			tt.Save(board, unchecked((byte)depthRemaining), bestMove, bestScore, alpha, beta, 
				excludedMove != 0);
			return bestScore;
		}

		/// <summary>looks for the best movement</summary>
		public virtual void Go(SearchParameters searchParameters)
		{
			if (!initialized)
			{
				return;
			}
			if (!searching)
			{
				this.searchParameters = searchParameters;
				Run();
			}
		}

		private void SearchStats()
		{
			logger.Debug("Positions PV      = " + pvPositionCounter + " " + (100.0 * pvPositionCounter
				 / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
			//
			logger.Debug("Positions QS      = " + qsPositionCounter + " " + (100.0 * qsPositionCounter
				 / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
			//
			logger.Debug("Positions Null    = " + positionCounter + " " + (100.0 * positionCounter
				 / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
			//
			logger.Debug("PV Cut            = " + pvCutNodes + " " + (100 * pvCutNodes / (pvCutNodes
				 + pvAllNodes + 1)) + "%");
			logger.Debug("PV All            = " + pvAllNodes);
			logger.Debug("Null Cut          = " + nullCutNodes + " " + (100 * nullCutNodes / 
				(nullCutNodes + nullAllNodes + 1)) + "%");
			logger.Debug("Null All          = " + nullAllNodes);
			logger.Debug("Asp Win      Hits = " + (100.0 * aspirationWindowHit / aspirationWindowProbe
				) + "%");
			logger.Debug("TT Eval      Hits = " + ttEvalHit + " " + (100.0 * ttEvalHit / ttEvalProbe
				) + "%");
			logger.Debug("TT PV        Hits = " + ttPvHit + " " + (100.0 * ttPvHit / ttProbe)
				 + "%");
			logger.Debug("TT LB        Hits = " + ttProbe + " " + (100.0 * ttLBHit / ttProbe)
				 + "%");
			logger.Debug("TT UB        Hits = " + ttUBHit + " " + (100.0 * ttUBHit / ttProbe)
				 + "%");
			logger.Debug("Futility     Hits = " + futilityHit);
			logger.Debug("Agg.Futility Hits = " + aggressiveFutilityHit);
			logger.Debug("Null Move    Hits = " + nullMoveHit + " " + (100.0 * nullMoveHit / 
				nullMoveProbe) + "%");
			logger.Debug("Razoring     Hits = " + razoringHit + " " + (100.0 * razoringHit / 
				razoringProbe) + "%");
			logger.Debug("S.Extensions Hits = " + singularExtensionHit + " " + (100.0 * singularExtensionHit
				 / singularExtensionProbe) + "%");
		}

		/// <exception cref="Com.Alonsoruibal.Chess.Search.SearchFinishedException"></exception>
		public virtual void NewRun()
		{
			foundOneMove = false;
			searching = true;
			startTime = Runtime.CurrentTimeMillis();
			logger.Debug("Board\n" + board);
			positionCounter = 0;
			pvPositionCounter = 0;
			qsPositionCounter = 0;
			bestMoveTime = 0;
			globalBestMove = 0;
			ponderMove = 0;
			pv = null;
			initialPly = board.GetMoveNumber();
			thinkTo = startTime + searchParameters.CalculateMoveTime(board) - 100;
			if (config.GetUseBook() && config.GetBook() != null && board.GetOutBookMove() > board
				.GetMoveNumber() && (config.GetBookKnowledge() == 100 || ((random.NextDouble() *
				 100) < config.GetBookKnowledge())))
			{
				logger.Debug("Searching Move in Book");
				int bookMove = config.GetBook().GetMove(board);
				if (bookMove != 0)
				{
					globalBestMove = bookMove;
					logger.Debug("Found Move in Book");
					throw new SearchFinishedException();
				}
				else
				{
					logger.Debug("NOT Found Move in Book");
					board.SetOutBookMove(board.GetMoveNumber());
				}
			}
			depth = 1;
			score = Eval(-Evaluator.VICTORY, Evaluator.VICTORY, false, false);
			tt.NewGeneration();
			aspWindows = config.GetAspirationWindowSizes();
		}

		/// <exception cref="Com.Alonsoruibal.Chess.Search.SearchFinishedException"></exception>
		public virtual void RunStepped()
		{
			int failHighCount = 0;
			int failLowCount = 0;
			int initialScore = score;
			int alpha = (initialScore - aspWindows[failLowCount] > -Evaluator.VICTORY ? initialScore
				 - aspWindows[failLowCount] : -Evaluator.VICTORY);
			int beta = (initialScore + aspWindows[failHighCount] < Evaluator.VICTORY ? initialScore
				 + aspWindows[failHighCount] : Evaluator.VICTORY);
			// Iterate aspiration windows
			while (true)
			{
				aspirationWindowProbe++;
				score = Search(NODE_ROOT, depth * PLY, alpha, beta, false, 0);
				// logger.debug("alpha = " + alpha + ", beta = " + beta
				// + ", score=" + score);
				if (score <= alpha)
				{
					failLowCount++;
					alpha = (failLowCount < aspWindows.Length && (initialScore - aspWindows[failLowCount
						] > -Evaluator.VICTORY) ? initialScore - aspWindows[failLowCount] : -Evaluator.VICTORY
						);
				}
				else
				{
					if (score >= beta)
					{
						failHighCount++;
						beta = (failHighCount < aspWindows.Length && (initialScore + aspWindows[failHighCount
							] < Evaluator.VICTORY) ? initialScore + aspWindows[failHighCount] : Evaluator.VICTORY
							);
					}
					else
					{
						aspirationWindowHit++;
						break;
					}
				}
			}
			long time = Runtime.CurrentTimeMillis();
			long oldBestMove = globalBestMove;
			GetPv();
			if (globalBestMove != 0)
			{
				foundOneMove = true;
			}
			// update best move time
			if (oldBestMove != globalBestMove)
			{
				bestMoveTime = time - startTime;
			}
			SearchStatusInfo info = new SearchStatusInfo();
			info.SetDepth(depth);
			info.SetTime(time - startTime);
			info.SetPv(pv);
			info.SetScore(score);
			info.SetNodes(positionCounter + pvPositionCounter + qsPositionCounter);
			info.SetNps((int)(1000 * (positionCounter + pvPositionCounter + qsPositionCounter
				) / ((time - startTime + 1))));
			logger.Debug(info.ToString());
			if (observer != null)
			{
				observer.Info(info);
			}
			// if mate found exit
			if ((score < -Evaluator.VICTORY + 1000) || (score > Evaluator.VICTORY - 1000))
			{
				throw new SearchFinishedException();
			}
			depth++;
			if (depth == MAX_DEPTH)
			{
				throw new SearchFinishedException();
			}
		}

		public virtual void FinishRun()
		{
			// puts the board in the initial position
			board.UndoMove(initialPly);
			SearchStats();
			searching = false;
			if (observer != null)
			{
				observer.BestMove(globalBestMove, ponderMove);
			}
		}

		public virtual void Run()
		{
			try
			{
				NewRun();
				while (true)
				{
					RunStepped();
				}
			}
			catch (SearchFinishedException)
			{
			}
			FinishRun();
		}

		/// <summary>
		/// Gets the principal variation and the best move from the transposition
		/// table
		/// </summary>
		private void GetPv()
		{
			StringBuilder sb = new StringBuilder();
			IList<long> keys = new AList<long>();
			// To not repeat keys
			int i = 0;
			while (i < 256)
			{
				if (tt.Search(board, false))
				{
					if (keys.Contains(board.GetKey()))
					{
						break;
					}
					keys.AddItem(board.GetKey());
					if (tt.GetBestMove() == 0)
					{
						break;
					}
					if (i == 0)
					{
						globalBestMove = tt.GetBestMove();
					}
					else
					{
						if (i == 1)
						{
							ponderMove = tt.GetBestMove();
						}
					}
					sb.Append(Move.ToString(tt.GetBestMove()));
					sb.Append(" ");
					i++;
					board.DoMove(tt.GetBestMove(), false);
				}
				else
				{
					break;
				}
			}
			// Now undo moves
			for (int j = 0; j < i; j++)
			{
				board.UndoMove();
			}
			pv = sb.ToString();
		}

		public virtual void Stop()
		{
			thinkTo = 0;
		}

		/// <summary>
		/// Is better to end before Not necessary to change sign after Takes into
		/// account the contempt factor
		/// </summary>
		public virtual int EvaluateEndgame()
		{
			if (board.GetCheck())
			{
				return ValueMatedIn(board.GetMoveNumber() - initialPly);
			}
			else
			{
				return EvaluateDraw();
			}
		}

		public virtual int EvaluateDraw()
		{
			return ((board.GetMoveNumber() - initialPly) & 1) == 0 ? -config.GetContemptFactor
				() : config.GetContemptFactor();
		}

		private int ValueMatedIn(int depth)
		{
			return -Evaluator.VICTORY + depth;
		}

		private int ValueMateIn(int depth)
		{
			return Evaluator.VICTORY - depth;
		}

		internal virtual bool ValueIsMate(int value)
		{
			return value <= ValueMatedIn(MAX_DEPTH) || value >= ValueMateIn(MAX_DEPTH);
		}

		public virtual TranspositionTable GetTT()
		{
			return tt;
		}

		public virtual SearchParameters GetSearchParameters()
		{
			return searchParameters;
		}

		public virtual void SetSearchParameters(SearchParameters searchParameters)
		{
			this.searchParameters = searchParameters;
		}

		public virtual bool IsInitialized()
		{
			return initialized;
		}

		public virtual bool IsSearching()
		{
			return searching;
		}
	}
}
