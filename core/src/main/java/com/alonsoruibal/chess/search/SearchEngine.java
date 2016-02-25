package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.Piece;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;
import com.alonsoruibal.chess.evaluation.SimplifiedEvaluator;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.movesort.MoveIterator;
import com.alonsoruibal.chess.movesort.SortInfo;
import com.alonsoruibal.chess.tt.TranspositionTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Search engine
 *
 * @author Alberto Alonso Ruibal
 */
public class SearchEngine implements Runnable {
	private static final Logger logger = Logger.getLogger("SearchEngine");

	public static final int MAX_DEPTH = 64;
	public static final int VALUE_IS_MATE = Evaluator.MATE - MAX_DEPTH;
	private static final int PLY = 2;
	private static final int LMR_DEPTHS_NOT_REDUCED = 3 * PLY;
	private static final int RAZOR_DEPTH = 4 * PLY;
	private static final int[] SINGULAR_MOVE_DEPTH = {6 * PLY, 6 * PLY, 8 * PLY};
	private static final int[] IID_DEPTH = {5 * PLY, 5 * PLY, 8 * PLY};

	public static final int NODE_ROOT = 0;
	public static final int NODE_PV = 1;
	public static final int NODE_NULL = 2;

	public boolean debug = false;

	private SearchParameters searchParameters;

	private boolean searching = false;
	private boolean foundOneMove;

	private Config config;

	// Think limits
	private long thinkToTime = 0;
	private int thinkToNodes = 0;
	private int thinkToDepth = 0;

	private Board board;
	private SearchObserver observer;
	private Evaluator evaluator;
	private TranspositionTable tt;
	private SortInfo sortInfo;
	private AttacksInfo[] attacksInfos;
	private MoveIterator[] moveIterators;

	private int bestMoveScore;
	private int globalBestMove, ponderMove;
	private String pv;

	private int initialPly; // Initial Ply for search
	private int depth;
	private int selDepth;
	private int rootScore;
	private int[] aspWindows;
	private boolean panicTime;
	private boolean engineIsWhite;

	public long startTime;

	// For performance benching
	private long positionCounter;
	private long pvPositionCounter;
	private long qsPositionCounter;
	private long pvCutNodes;
	private long pvAllNodes;
	private long nullCutNodes;
	private long nullAllNodes;

	// Aspiration window
	private static long aspirationWindowProbe = 0;
	private static long aspirationWindowHit = 0;

	// Futility pruning
	private static long futilityHit = 0;

	// Aggresive Futility pruning
	private static long aggressiveFutilityHit = 0;

	// Razoring
	private static long razoringProbe = 0;
	private static long razoringHit = 0;

	// Singular Extension
	private static long singularExtensionProbe = 0;
	private static long singularExtensionHit = 0;

	// Null Move
	private static long nullMoveProbe = 0;
	private static long nullMoveHit = 0;

	// Transposition Table
	private static long ttProbe = 0;
	private static long ttPvHit = 0;
	private static long ttLBHit = 0;
	private static long ttUBHit = 0;
	private static long ttEvalHit = 0;
	private static long ttEvalProbe = 0;

	private boolean initialized;

	private Random random;

	private int[][] pvReductionMatrix, nonPvReductionMatrix;

	public SearchEngine(Config config) {
		this.config = config;
		random = new Random();
		board = new Board();
		sortInfo = new SortInfo();
		attacksInfos = new AttacksInfo[MAX_DEPTH];
		moveIterators = new MoveIterator[MAX_DEPTH];

		for (int i = 0; i < MAX_DEPTH; i++) {
			attacksInfos[i] = new AttacksInfo();
			moveIterators[i] = new MoveIterator(board, attacksInfos[i], sortInfo, i);
		}

		pvReductionMatrix = new int[MAX_DEPTH][MAX_DEPTH];
		nonPvReductionMatrix = new int[MAX_DEPTH][MAX_DEPTH];
		// Init our reduction lookup tables
		for (int depthRemaining = 1; depthRemaining < 64; depthRemaining++) { // OnePly = 2
			for (int moveNumber = 1; moveNumber < 64; moveNumber++) {
				double pvRed = 0.5 + Math.log(depthRemaining) * Math.log(moveNumber) / 6.0;
				double nonPVRed = 0.5 + Math.log(depthRemaining) * Math.log(moveNumber) / 3.0;
				pvReductionMatrix[depthRemaining][moveNumber] = (int) (pvRed >= 1.0 ? Math.floor(pvRed * PLY) : 0);
				nonPvReductionMatrix[depthRemaining][moveNumber] = (int) (nonPVRed >= 1.0 ? Math.floor(nonPVRed * PLY) : 0);
				// System.out.println(depthRemaining + " " + moveNumber + " " + pvReductionMatrix[depthRemaining][moveNumber] + " " + nonPvReductionMatrix[depthRemaining][moveNumber]);
			}
		}

		init();
	}

	public void init() {
		initialized = false;

		if (config.isUciChess960()) {
			board.chess960 = true;
		}

		board.startPosition();
		sortInfo.clear();

		String evaluatorName = config.getEvaluator();
		if ("simplified".equals(evaluatorName)) {
			evaluator = new SimplifiedEvaluator();
		} else if ("complete".equals(evaluatorName)) {
			evaluator = new CompleteEvaluator(config);
		} else if ("experimental".equals(evaluatorName)) {
			evaluator = new ExperimentalEvaluator(config);
		}

		tt = new TranspositionTable(config.getTranspositionTableSize());

		initialized = true;
		if (debug) {
			logger.debug(config.toString());
		}
	}

	public void clear() {
		sortInfo.clear();
		tt.clear();
	}

	public void destroy() {
		config = null;
		observer = null;
		tt = null;
		evaluator = null;
		sortInfo = null;
		if (moveIterators != null) {
			for (int i = 0; i < MAX_DEPTH; i++) {
				moveIterators[i] = null;
			}
		}
		System.gc();
	}

	private int getReduction(int nodeType, int depth, int movecount) {
		return nodeType == NODE_PV || nodeType == NODE_ROOT ? //
				pvReductionMatrix[Math.min(depth / PLY, 63)][Math.min(movecount, 63)] : //
				nonPvReductionMatrix[Math.min(depth / PLY, 63)][Math.min(movecount, 63)];
	}

	public void setObserver(SearchObserver observer) {
		this.observer = observer;
	}

	public Board getBoard() {
		return board;
	}

	public int getBestMove() {
		return globalBestMove;
	}

	public long getBestMoveScore() {
		return bestMoveScore;
	}

	public long getNodes() {
		return positionCounter + pvPositionCounter + qsPositionCounter;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * Decides when we are going to allow null move. Don't do null move in king and pawn endings
	 */
	private boolean boardAllowsNullMove() {
		return (board.getMines() & (board.knights | board.bishops | board.rooks | board.queens)) != 0;
	}

	/**
	 * Calculates the extension of a move in the actual position
	 * Now the move is not done
	 */
	private int extensions(int move, boolean mateThreat, int moveSee) {
		int ext = 0;

		if (Move.isCheck(move) && moveSee >= 0) {
			ext += Config.EXTENSIONS_CHECK;
			if (ext >= PLY) {
				return PLY;
			}
		}
		if (Move.getPieceMoved(move) == Piece.PAWN) {
			if (Config.EXTENSIONS_PAWN_PUSH != 0
					&& Move.isPawnPush678(move)) {
				ext += Config.EXTENSIONS_PAWN_PUSH;
			}
			if (Config.EXTENSIONS_PASSED_PAWN != 0
					&& board.isPassedPawn(Move.getToIndex(move))) {
				ext += Config.EXTENSIONS_PASSED_PAWN;
			}
			if (ext >= PLY) {
				return PLY;
			}
		}
		if (mateThreat) {
			ext += Config.EXTENSIONS_MATE_THREAT;
			if (ext >= PLY) {
				return PLY;
			}
		}
		return ext;
	}

	/**
	 * Returns true if we can use the value stored on the TT to return from search
	 */
	private boolean canUseTT(int depthRemaining, int alpha, int beta) {
		if (tt.getDepthAnalyzed() >= depthRemaining && tt.isMyGeneration()) {
			switch (tt.getNodeType()) {
				case TranspositionTable.TYPE_EXACT_SCORE:
					ttPvHit++;
					return true;
				case TranspositionTable.TYPE_FAIL_LOW:
					ttLBHit++;
					if (tt.getScore() <= alpha) {
						return true;
					}
					break;
				case TranspositionTable.TYPE_FAIL_HIGH:
					ttUBHit++;
					if (tt.getScore() >= beta) {
						return true;
					}
					break;
			}
		}
		return false;
	}

	/**
	 * It also changes the sign to the score depending of the turn
	 */
	public int evaluate(boolean foundTT, int distanceToInitialPly) {
		ttEvalProbe++;

		int eval;
		if (foundTT) {
			ttEvalHit++;
			return tt.getEval();
		}
		eval = evaluator.evaluate(board, attacksInfos[distanceToInitialPly]);
		if (!board.getTurn()) {
			eval = -eval;
		}
		tt.set(board, TranspositionTable.TYPE_EVAL, Move.NONE, 0, 0, eval, false);
		return eval;
	}

	public int refineEval(boolean foundTT, int eval) {
		if (foundTT &&
				((tt.getNodeType() == TranspositionTable.TYPE_EXACT_SCORE) ||
						((tt.getNodeType() == TranspositionTable.TYPE_FAIL_LOW) && (tt.getScore() < eval)) ||
						((tt.getNodeType() == TranspositionTable.TYPE_FAIL_HIGH) && (tt.getScore() > eval)))) {
			return tt.getScore();
		}
		return eval;
	}

	public int quiescentSearch(int qsdepth, int alpha, int beta) throws SearchFinishedException {
		qsPositionCounter++;

		int distanceToInitialPly = board.getMoveNumber() - initialPly;

		// It checks draw by three fold repetition, fifty moves rule and no material to mate
		if (board.isDraw()) {
			return evaluateDraw(distanceToInitialPly);
		}

		// Mate distance pruning
		alpha = Math.max(valueMatedIn(distanceToInitialPly), alpha);
		beta = Math.min(valueMateIn(distanceToInitialPly + 1), beta);
		if (alpha >= beta) {
			return alpha;
		}

		boolean isPv = beta - alpha > 1;
		int ttMove = Move.NONE;
		// Generate checks for PV on PLY 0
		boolean generateChecks = isPv && (qsdepth == 0);
		// If we generate check, the entry in the TT has depthAnalyzed=1, because is better than without checks (depthAnalyzed=0)
		int ttDepth = generateChecks ? TranspositionTable.DEPTH_QS_CHECKS : TranspositionTable.DEPTH_QS_NO_CHECKS;

		ttProbe++;
		boolean foundTT = tt.search(board, distanceToInitialPly, false);
		if (foundTT) {
			if (!isPv && canUseTT(ttDepth, alpha, beta)) {
				return tt.getScore();
			}
			ttMove = tt.getBestMove();
		}

		int bestScore = alpha;
		int bestMove = Move.NONE;
		int staticEval = Evaluator.NO_VALUE;
		int eval = -Evaluator.MATE;
		int futilityBase = -Evaluator.MATE;

		// Do not allow stand pat when in check
		if (!board.getCheck()) {
			staticEval = evaluate(foundTT, distanceToInitialPly);
			eval = refineEval(foundTT, staticEval);

			// Evaluation functions increase alpha and can originate beta cutoffs
			bestScore = Math.max(bestScore, eval);
			if (bestScore >= beta) {
				if (!foundTT) {
					tt.set(board, TranspositionTable.TYPE_FAIL_HIGH, Move.NONE, bestScore, TranspositionTable.DEPTH_QS_CHECKS, staticEval, false);
				}
				return bestScore;
			}

			futilityBase = eval + Config.FUTILITY_MARGIN_QS;
		}

		// If we have more depths than possible...
		if (distanceToInitialPly >= MAX_DEPTH - 1) {
			return board.getCheck() ? evaluateDraw(distanceToInitialPly) : eval; // Return a drawish score if we are in check
		}

		boolean validOperations = false;

		MoveIterator moveIterator = moveIterators[distanceToInitialPly];
		moveIterator.genMoves(ttMove, (generateChecks ? MoveIterator.GENERATE_CAPTURES_PROMOS_CHECKS : MoveIterator.GENERATE_CAPTURES_PROMOS));

		int move;
		while ((move = moveIterator.next()) != Move.NONE) {
			validOperations = true;

			// Futility pruning
			if (Config.FUTILITY //
					&& !moveIterator.checkEvasion //
					&& !Move.isCheck(move) //
					&& !isPv //
					&& move != ttMove //
					&& !Move.isPawnPush678(move) //
					&& futilityBase > -Evaluator.KNOWN_WIN) {
				int futilityValue = futilityBase;
				switch (Move.getPieceCaptured(board, move)) {
					case Piece.PAWN:
						futilityValue += Config.PAWN;
						break;
					case Piece.KNIGHT:
						futilityValue += Config.KNIGHT;
						break;
					case Piece.BISHOP:
						futilityValue += Config.BISHOP;
						break;
					case Piece.ROOK:
						futilityValue += Config.ROOK;
						break;
					case Piece.QUEEN:
						futilityValue += Config.QUEEN;
						break;
				}
				if (futilityValue < beta) {
					bestScore = Math.max(bestScore, futilityValue);
					continue;
				}
				if (futilityBase < beta && moveIterator.getLastMoveSee() <= 0) {
					bestScore = Math.max(bestScore, futilityBase);
					continue;
				}
			}

			board.doMove(move, false, false);
			assert board.getCheck() == Move.isCheck(move) : "Check flag not generated properly";

			int score = -quiescentSearch(qsdepth + 1, -beta, -bestScore);
			board.undoMove();
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
				if (score >= beta) {
					break;
				}
			}
		}

		if (board.getCheck() && !validOperations) {
			return valueMatedIn(distanceToInitialPly);
		}
		tt.save(board, distanceToInitialPly, ttDepth, bestMove, bestScore, alpha, beta, staticEval, false);

		return bestScore;
	}

	/**
	 * Search Root, PV and null window
	 */
	public int search(int nodeType, int depthRemaining, int alpha, int beta, boolean allowNullMove, int excludedMove) throws SearchFinishedException {
		if (nodeType != NODE_ROOT && foundOneMove && (System.currentTimeMillis() > thinkToTime || (positionCounter + pvPositionCounter + qsPositionCounter) > thinkToNodes)) {
			finishRun();
		}

		int distanceToInitialPly = board.getMoveNumber() - initialPly;

		if (nodeType == NODE_PV || nodeType == NODE_ROOT) {
			pvPositionCounter++;

			if (distanceToInitialPly > selDepth) {
				selDepth = distanceToInitialPly;
			}
		} else {
			positionCounter++;
		}

		// It checks draw by three fold repetition, fifty moves rule and no material to mate
		if (board.isDraw()) {
			return evaluateDraw(distanceToInitialPly);
		}

		// Mate distance pruning
		alpha = Math.max(valueMatedIn(distanceToInitialPly), alpha);
		beta = Math.min(valueMateIn(distanceToInitialPly + 1), beta);
		if (alpha >= beta) {
			return alpha;
		}

		int ttMove = Move.NONE;
		int ttScore = 0;
		int ttNodeType = 0;
		int ttDepthAnalyzed = 0;
		int score = 0;

		ttProbe++;
		boolean foundTT = tt.search(board, distanceToInitialPly, excludedMove != Move.NONE);
		if (foundTT) {
			if (nodeType != NODE_ROOT && canUseTT(depthRemaining, alpha, beta)) {
				return tt.getScore();
			}
			ttMove = tt.getBestMove();
			ttScore = tt.getScore();
			ttNodeType = tt.getNodeType();
			ttDepthAnalyzed = tt.getDepthAnalyzed();
		}

		boolean mateThreat = false;
		boolean futilityPrune = false;
		int futilityValue = -Evaluator.MATE;
		int staticEval = -Evaluator.MATE;
		int eval = -Evaluator.MATE;

		if (!board.getCheck()) {
			// Do a static eval, in case of exclusion and not found in the TT, search again with the normal key
			boolean evalTT = excludedMove == 0 || foundTT ? foundTT : tt.search(board, distanceToInitialPly, false);
			staticEval = evaluate(evalTT, distanceToInitialPly);
			eval = refineEval(foundTT, staticEval);
		}

		// If we have more depths than possible...
		if (distanceToInitialPly >= MAX_DEPTH - 1) {
			return board.getCheck() ? evaluateDraw(distanceToInitialPly) : eval; // Return a drawish score if we are in check
		}

		if (!board.getCheck()) {
			// Hyatt's Razoring http://chessprogramming.wikispaces.com/Razoring
			if (nodeType == NODE_NULL //
					&& Config.RAZORING //
					&& ttMove == 0 //
					&& allowNullMove // Not when last was a null move
					&& depthRemaining < RAZOR_DEPTH //
					&& Math.abs(beta) < VALUE_IS_MATE //
					&& eval + Config.RAZORING_MARGIN < beta //
					&& (board.pawns & ((board.whites & BitboardUtils.b2_u) | (board.blacks & BitboardUtils.b2_d))) == 0) { // No pawns on 7TH
				razoringProbe++;

				if (depthRemaining <= PLY) {
					razoringHit++;
					return quiescentSearch(0, alpha, beta);
				}

				int rbeta = beta - Config.RAZORING_MARGIN;
				int v = quiescentSearch(0, rbeta - 1, rbeta);
				if (v < rbeta) {
					razoringHit++;
					return v;
				}
			}

			// Static null move pruning or futility pruning in parent node
			if (nodeType == NODE_NULL //
					&& Config.STATIC_NULL_MOVE //
					&& allowNullMove //
					&& depthRemaining < RAZOR_DEPTH //
					&& Math.abs(beta) < VALUE_IS_MATE //
					&& Math.abs(eval) < Evaluator.KNOWN_WIN //
					&& eval - Config.FUTILITY_MARGIN >= beta //
					&& boardAllowsNullMove()) {
				return eval - Config.FUTILITY_MARGIN;
			}

			// Null move pruning and mate threat detection
			if (nodeType == NODE_NULL //
					&& Config.NULL_MOVE //
					&& allowNullMove //
					&& depthRemaining >= 2 * PLY //
					&& Math.abs(beta) < VALUE_IS_MATE //
					&& eval >= beta //
					&& boardAllowsNullMove()) {

				nullMoveProbe++;

				int R = 3 * PLY + (depthRemaining >>> 2);

				board.doMove(0, false, false);
				score = depthRemaining - R < PLY ? -quiescentSearch(0, -beta, -beta + 1) :
						-search(NODE_NULL, depthRemaining - R, -beta, -beta + 1, false, Move.NONE);
				board.undoMove();

				if (score >= beta) {
					if (score >= VALUE_IS_MATE) {
						score = beta;
					}

					// Verification search on initial depths
					if (depthRemaining < 12 * PLY //
							|| (depthRemaining - R < PLY ? quiescentSearch(0, beta - 1, beta) :
							search(NODE_NULL, depthRemaining - R, beta - 1, beta, false, Move.NONE)) >= beta) {
						nullMoveHit++;
						return score;
					}
				} else {
					// Detect mate threat
					if (score <= -VALUE_IS_MATE) {
						mateThreat = true;
					}
				}
			}

			// Internal Iterative Deepening (IID)
			// Do a reduced move to search for a ttMove that will improve sorting
			if (Config.IID //
					&& ttMove == 0 //
					&& depthRemaining >= IID_DEPTH[nodeType] //
					&& allowNullMove //
					&& (nodeType != NODE_NULL || staticEval + Config.IID_MARGIN > beta) //
					&& excludedMove == 0) {
				int d = (nodeType == NODE_PV ? depthRemaining - 2 * PLY : depthRemaining >> 1);
				search(nodeType, d, alpha, beta, false, 0);
				if (tt.search(board, distanceToInitialPly, false)) {
					ttMove = tt.getBestMove();
				}
			}

			// Futility pruning
			if (nodeType == NODE_NULL && Config.FUTILITY) {
				if (depthRemaining <= PLY) { // at frontier nodes
					futilityValue = staticEval + Config.FUTILITY_MARGIN;
					if (futilityValue < beta) {
						futilityHit++;
						futilityPrune = true;
					}
				} else if (depthRemaining <= 2 * PLY) { // at pre-frontier nodes
					futilityValue = staticEval + Config.FUTILITY_MARGIN_AGGRESSIVE;
					if (futilityValue < beta) {
						aggressiveFutilityHit++;
						futilityPrune = true;
					}
				}
			}
		}

		MoveIterator moveIterator = moveIterators[distanceToInitialPly];
		moveIterator.genMoves(ttMove);

		int movesDone = 0;
		boolean validOperations = false;
		int bestScore = -Evaluator.MATE;
		int move, bestMove = Move.NONE;
		bestMoveScore = -Evaluator.MATE;

		while ((move = moveIterator.next()) != Move.NONE) {
			validOperations = true;

			if (move == excludedMove) {
				continue;
			}

			int extension = extensions(move, mateThreat, moveIterator.getLastMoveSee());

			// Check singular move extension
			// It also detects singular replies
			if (nodeType != NODE_ROOT //
					&& move == ttMove //
					&& extension < PLY //
					&& excludedMove == 0 //
					&& Config.EXTENSIONS_SINGULAR > 0 //
					&& depthRemaining >= SINGULAR_MOVE_DEPTH[nodeType] //
					&& ttNodeType == TranspositionTable.TYPE_FAIL_HIGH //
					&& ttDepthAnalyzed >= depthRemaining - 3 * PLY //
					&& Math.abs(ttScore) < Evaluator.KNOWN_WIN) {

				singularExtensionProbe++;
				int seBeta = ttScore - Config.SINGULAR_EXTENSION_MARGIN;
				int excScore = search(nodeType, depthRemaining >> 1, seBeta - 1, seBeta, false, move);
				if (excScore < seBeta) {
					singularExtensionHit++;
					extension += Config.EXTENSIONS_SINGULAR;
					if (extension > PLY) {
						extension = PLY;
					}
				}
			}

			boolean importantMove = nodeType == NODE_ROOT //
					|| extension != 0 //
					|| moveIterator.checkEvasion //
					|| Move.isCheck(move) //
					|| Move.isCapture(move) // Include ALL captures
					|| Move.isPawnPush678(move) // Includes promotions
					|| Move.isCastling(move)
					|| move == ttMove //
					|| sortInfo.isKiller(move, distanceToInitialPly + 1);

			if (futilityPrune //
					&& bestScore > -Evaluator.KNOWN_WIN //
					&& !importantMove) {
				if (futilityValue <= alpha) {
					if (futilityValue > bestScore) {
						bestScore = futilityValue;
					}
				}
				continue;
			}

			board.doMove(move, false, false);
			assert board.getCheck() == Move.isCheck(move) : "Check flag not generated properly";

			movesDone++;

			int lowBound = (alpha > bestScore ? alpha : bestScore);
			if ((nodeType == NODE_PV || nodeType == NODE_ROOT) && movesDone == 1) {
				// PV move not null searched
				score = depthRemaining + extension - PLY < PLY ? -quiescentSearch(0, -beta, -lowBound) :
						-search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true, Move.NONE);
			} else {
				// Try searching null window
				boolean doFullSearch = true;

				// Late move reductions (LMR)
				int reduction = 0;
				if (Config.LMR //
						&& depthRemaining >= LMR_DEPTHS_NOT_REDUCED //
						&& !importantMove) {
					reduction += getReduction(nodeType, depthRemaining, movesDone);
				}

				if (reduction > 0) {
					score = depthRemaining - reduction - PLY < PLY ? -quiescentSearch(0, -lowBound - 1, -lowBound) :
							-search(NODE_NULL, depthRemaining - reduction - PLY, -lowBound - 1, -lowBound, true, Move.NONE);
					doFullSearch = (score > lowBound);
				}
				if (doFullSearch) {
					score = depthRemaining + extension - PLY < PLY ? -quiescentSearch(0, -lowBound - 1, -lowBound) :
							-search(NODE_NULL, depthRemaining + extension - PLY, -lowBound - 1, -lowBound, true, Move.NONE);

					// Finally search as PV if score on window
					if ((nodeType == NODE_PV || nodeType == NODE_ROOT) //
							&& score > lowBound //
							&& (nodeType == NODE_ROOT || score < beta)) {
						score = depthRemaining + extension - PLY < PLY ? -quiescentSearch(0, -beta, -lowBound) :
								-search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true, Move.NONE);
					}
				}
			}

			board.undoMove();

			// It tracks the best move and...
			if (score > bestScore &&
					(config.getRand() == 0 //... insert errors to lower the ELO
					|| bestScore == -Evaluator.MATE // it makes sure that has at least one move
					|| random.nextInt(1000) > config.getRand())){
				bestMove = move;
				bestScore = score;

				if (nodeType == NODE_ROOT) {
					globalBestMove = move;
					bestMoveScore = score;
					foundOneMove = true;

					if (depthRemaining > 6 * PLY) {
						notifyMoveFound(move, score, alpha, beta);
					}
				}
			}

			// alpha/beta cut (fail high)
			if (score >= beta) {
				break;
			}
		}

		// Checkmate or stalemate
		if (excludedMove == 0 && !validOperations) {
			bestScore = evaluateEndgame(distanceToInitialPly);
		}
		// Fix score for excluded moves
		if (bestScore == -Evaluator.MATE) {
			bestScore = valueMatedIn(distanceToInitialPly);
		}

		// Tells MoveSorter the move score
		if (bestScore >= beta) {
			if (excludedMove == 0 && validOperations) {
				sortInfo.betaCutoff(bestMove, distanceToInitialPly);
			}
			if (nodeType == NODE_NULL) {
				nullCutNodes++;
			} else {
				pvCutNodes++;
			}
		} else {
			if (nodeType == NODE_NULL) {
				nullAllNodes++;
			} else {
				pvAllNodes++;
			}
		}

		// Save in the transposition table
		tt.save(board, distanceToInitialPly, depthRemaining, bestMove, bestScore, alpha, beta, staticEval, excludedMove != Move.NONE);

		return bestScore;
	}

	/**
	 * Notifies the best move to the SearchObserver filling a SearchStatusInfo object
	 */
	private void notifyMoveFound(int move, int score, int alpha, int beta) {
		long time = System.currentTimeMillis();

		getPv(move);

		SearchStatusInfo info = new SearchStatusInfo();
		info.setDepth(depth);
		info.setSelDepth(selDepth);
		info.setTime(time - startTime);
		info.setPv(pv);
		info.setScore(score, alpha, beta);
		info.setNodes(positionCounter + pvPositionCounter + qsPositionCounter);
		info.setHashFull(tt.getHashFull());
		info.setNps((int) (1000 * (positionCounter + pvPositionCounter + qsPositionCounter) / ((time - startTime + 1))));

		if (observer != null) {
			observer.info(info);
		} else {
			logger.debug(info.toString());
		}
	}

	/**
	 * It searches for the best movement
	 */
	public void go(SearchParameters searchParameters) {
		if (!initialized) {
			return;
		}
		if (!searching) {
			this.searchParameters = searchParameters;
			try {
				prepareRun();
				run();
			} catch (Exception e) {
			}
		}
	}

	private void searchStats() {
		if ((positionCounter + pvPositionCounter + qsPositionCounter) > 0) {
			logger.debug("Positions PV      = " + pvPositionCounter + " " //
					+ (100 * pvPositionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
			logger.debug("Positions QS      = " + qsPositionCounter + " " //
					+ (100 * qsPositionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
			logger.debug("Positions Null    = " + positionCounter + " " //
					+ (100 * positionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
		}
		logger.debug("PV Cut            = " + pvCutNodes + " " + (100 * pvCutNodes / (pvCutNodes + pvAllNodes + 1)) + "%");
		logger.debug("PV All            = " + pvAllNodes);
		logger.debug("Null Cut          = " + nullCutNodes + " " + (100 * nullCutNodes / (nullCutNodes + nullAllNodes + 1)) + "%");
		logger.debug("Null All          = " + nullAllNodes);
		if (aspirationWindowProbe > 0) {
			logger.debug("Asp Win      Hits = " + (100 * aspirationWindowHit / aspirationWindowProbe) + "%");
		}
		if (ttEvalProbe > 0) {
			logger.debug("TT Eval      Hits = " + ttEvalHit + " " + (100 * ttEvalHit / ttEvalProbe) + "%");
		}
		if (ttProbe > 0) {
			logger.debug("TT PV        Hits = " + ttPvHit + " " + (1000000 * ttPvHit / ttProbe) + " per 10^6");
			logger.debug("TT LB        Hits = " + ttProbe + " " + (100 * ttLBHit / ttProbe) + "%");
			logger.debug("TT UB        Hits = " + ttUBHit + " " + (100 * ttUBHit / ttProbe) + "%");
		}
		logger.debug("Futility     Hits = " + futilityHit);
		logger.debug("Agg.Futility Hits = " + aggressiveFutilityHit);
		if (nullMoveProbe > 0) {
			logger.debug("Null Move    Hits = " + nullMoveHit + " " + (100 * nullMoveHit / nullMoveProbe) + "%");
		}
		if (razoringProbe > 0) {
			logger.debug("Razoring     Hits = " + razoringHit + " " + (100 * razoringHit / razoringProbe) + "%");
		}
		if (singularExtensionProbe > 0) {
			logger.debug("S.Extensions Hits = " + singularExtensionHit + " " + (100 * singularExtensionHit / singularExtensionProbe) + "%");
		}
	}

	public void prepareRun() throws SearchFinishedException {
		startTime = System.currentTimeMillis();
		setSearchLimits(searchParameters, false);
		panicTime = false;
		engineIsWhite = board.getTurn();

		foundOneMove = false;
		searching = true;

		logger.debug("Board\n" + board);

		positionCounter = 0;
		pvPositionCounter = 0;
		qsPositionCounter = 0;
		globalBestMove = Move.NONE;
		ponderMove = Move.NONE;
		pv = null;

		initialPly = board.getMoveNumber();

		if (config.getUseBook() && config.getBook() != null && board.isUsingBook()
				&& (config.getBookKnowledge() == 100 || ((random.nextFloat() * 100) < config.getBookKnowledge()))) {
			logger.debug("Searching move in book");
			int bookMove = config.getBook().getMove(board);
			if (bookMove != 0) {
				globalBestMove = bookMove;
				logger.debug("Move found in book");
				finishRun();
			} else {
				logger.debug("Move NOT found in book");
				board.setOutBookMove(board.getMoveNumber());
			}
		}

		depth = 1;
		rootScore = evaluate(tt.search(board, 0, false), 0);
		tt.newGeneration();
		aspWindows = Config.ASPIRATION_WINDOW_SIZES;
	}

	private void runStepped() throws SearchFinishedException {
		selDepth = 0;
		int failHighCount = 0;
		int failLowCount = 0;
		int initialScore = rootScore;
		int alpha = (initialScore - aspWindows[failLowCount] > -Evaluator.MATE ? initialScore - aspWindows[failLowCount] : -Evaluator.MATE);
		int beta = (initialScore + aspWindows[failHighCount] < Evaluator.MATE ? initialScore + aspWindows[failHighCount] : Evaluator.MATE);
		int previousRootScore = rootScore;
		long time1 = System.currentTimeMillis();

		// Iterate aspiration windows
		while (true) {
			aspirationWindowProbe++;
			rootScore = search(NODE_ROOT, depth * PLY, alpha, beta, false, Move.NONE);

			// logger.debug("alpha = " + alpha + ", beta = " + beta + ", rootScore=" + rootScore);

			if (rootScore <= alpha) {
				failLowCount++;
				alpha = (failLowCount < aspWindows.length && (initialScore - aspWindows[failLowCount] > -Evaluator.MATE) ? initialScore
						- aspWindows[failLowCount] : -Evaluator.MATE - 1);
			} else if (rootScore >= beta) {
				failHighCount++;
				beta = (failHighCount < aspWindows.length && (initialScore + aspWindows[failHighCount] < Evaluator.MATE) ? initialScore
						+ aspWindows[failHighCount] : Evaluator.MATE + 1);
			} else {
				aspirationWindowHit++;
				break;
			}
		}

		long time2 = System.currentTimeMillis();

		if (depth <= 6) {
			notifyMoveFound(globalBestMove, bestMoveScore, alpha, beta);
		} else if (!panicTime && rootScore < previousRootScore - 100) {
			panicTime = true;
			setSearchLimits(searchParameters, true);
		}

		if ((searchParameters.manageTime() && ( // Under time restrictions and...
				Math.abs(rootScore) > VALUE_IS_MATE // Mate found or
						|| (time2 + ((time2 - time1) << 1)) > thinkToTime)) // It will not likely finish the next iteration
				|| depth == MAX_DEPTH
				|| depth > thinkToDepth
				|| Math.abs(rootScore) == Evaluator.MATE) { // Search limit reached
			finishRun();
		}
		depth++;
	}

	public void setSearchLimits(SearchParameters searchParameters, boolean panicTime) {
		thinkToNodes = searchParameters.getNodes();
		thinkToDepth = searchParameters.getDepth();
		thinkToTime = searchParameters.calculateMoveTime(engineIsWhite, startTime, panicTime);
	}

	public void finishRun() throws SearchFinishedException {
		// Go back the board to the initial position
		board.undoMove(initialPly);

		if (observer != null && globalBestMove != Move.NONE) {
			observer.bestMove(globalBestMove, ponderMove);
		}
		if (debug) {
			searchStats();
		}
		searching = false;
		throw new SearchFinishedException();
	}

	public void run() {
		try {
			while (true) {
				runStepped();
			}
		} catch (SearchFinishedException ignored) {
		}
	}

	/**
	 * Gets the principal variation from the transposition table
	 */
	private void getPv(int firstMove) {
		StringBuilder sb = new StringBuilder();
		List<Long> keys = new ArrayList<Long>(); // To not repeat keys
		sb.append(Move.toString(firstMove));
		board.doMove(firstMove, true, false);

		int i = 1;
		while (i < 256) {
			if (tt.search(board, i, false)) {
				if (tt.getBestMove() == 0 || keys.contains(board.getKey())) {
					break;
				}
				keys.add(board.getKey());
				if (i == 1) {
					ponderMove = tt.getBestMove();
				}
				sb.append(" ");
				sb.append(Move.toString(tt.getBestMove()));
				board.doMove(tt.getBestMove(), true, false);
				i++;
				if (board.isMate()) {
					break;
				}
			} else {
				break;
			}
		}

		// Now undo moves
		for (int j = 0; j < i; j++) {
			board.undoMove();
		}
		pv = sb.toString();
	}

	public void stop() {
		thinkToTime = 0;
		thinkToNodes = 0;
		thinkToDepth = 0;
	}

	/**
	 * Is better to end before. Not necessary to change sign
	 */
	public int evaluateEndgame(int distanceToInitialPly) {
		if (board.getCheck()) {
			return valueMatedIn(distanceToInitialPly);
		} else {
			return evaluateDraw(distanceToInitialPly);
		}
	}

	public int evaluateDraw(int distanceToInitialPly) {
		return (distanceToInitialPly & 1) == 0 ? -config.getContemptFactor() : config.getContemptFactor();
	}

	private int valueMatedIn(int distanceToInitialPly) {
		return -Evaluator.MATE + distanceToInitialPly;
	}

	private int valueMateIn(int distanceToInitialPly) {
		return Evaluator.MATE - distanceToInitialPly;
	}

	public TranspositionTable getTT() {
		return tt;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isSearching() {
		return searching;
	}

	public void setSearchParameters(SearchParameters searchParameters) {
		this.searchParameters = searchParameters;
	}
}