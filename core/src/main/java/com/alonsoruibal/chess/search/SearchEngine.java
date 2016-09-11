package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;
import com.alonsoruibal.chess.evaluation.SimplifiedEvaluator;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.tt.TranspositionTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Search engine
 *
 * @author Alberto Alonso Ruibal
 */
public class SearchEngine implements Runnable {
	private static final Logger logger = Logger.getLogger("SearchEngine");

	public boolean debug = false;

	Object searchLock = new Object();
	Object startStopSearchLock = new Object();

	public static final int MAX_DEPTH = 64;
	public static final int VALUE_IS_MATE = Evaluator.MATE - MAX_DEPTH;
	public static final int HISTORY_MAX = Integer.MAX_VALUE - 1;

	private static final int NODE_ROOT = 0;
	private static final int NODE_PV = 1;
	private static final int NODE_NULL = 2;

	private static final int PLY = 1;
	private static final int LMR_DEPTHS_NOT_REDUCED = 3 * PLY;
	private static final int[] SINGULAR_MOVE_DEPTH = {0, 6 * PLY, 8 * PLY}; // By node type
	private static final int[] IID_DEPTH = {5 * PLY, 5 * PLY, 8 * PLY};

	private static final int IID_MARGIN = 150;
	private static final int SINGULAR_EXTENSION_MARGIN_PER_PLY = 1;
	private static final int[] ASPIRATION_WINDOW_SIZES = {10, 25, 150, 400, 550, 1025};
	private static final int FUTILITY_MARGIN_QS = 50;
	// Margins by depthRemaining in PLYs
	private static final int[] FUTILITY_MARGIN_PARENT = {0, 80, 160, 240, 320, 400, 480};
	private static final int[] FUTILITY_MARGIN_CHILD = {100, 180, 260, 340, 420, 500, 580};
	private static final int[] RAZORING_MARGIN = {190, 225, 230, 235};

	private SearchParameters searchParameters;

	protected boolean initialized = false;
	protected boolean searching = false;

	private Config config;

	// Think limits
	private long thinkToTime = 0;
	private int thinkToNodes = 0;
	private int thinkToDepth = 0;

	private Board board;
	private SearchObserver observer;
	private Evaluator evaluator;
	private TranspositionTable tt;
	public Node[] nodes;
	public int[][] history; // By piece type and destiny square

	private int bestMoveScore;
	private int globalBestMove, globalPonderMove;

	private int initialPly; // Initial Ply for search
	private int depth;
	private int selDepth;
	private int rootScore;
	private int[] aspWindows;
	private boolean panicTime;
	private boolean engineIsWhite;

	public long startTime;

	// For performance benching
	private long nodeCount;
	private long pvCutNodes;
	private long pvAllNodes;
	private long nullCutNodes;
	private long nullAllNodes;

	// Aspiration window
	private static long aspirationWindowProbe = 0;
	private static long aspirationWindowHit = 0;

	// Futility pruning
	private static long futilityHit = 0;

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

	private Random random;

	private int[][][] reductionMatrix;

	public SearchEngine(Config config) {
		this.config = config;
		random = new Random();
		board = new Board();
		history = new int[6][64];
		nodes = new Node[MAX_DEPTH];

		for (int i = 0; i < MAX_DEPTH; i++) {
			nodes[i] = new Node(this, i);
		}

		// Init our reduction lookup tables
		reductionMatrix = new int[2][64][64];
		final double[] REDUCTION_COEFS1 = {0.5, 0.5};
		final double[] REDUCTION_COEFS2 = {3.0, 6.0};
		for (int pv = 0; pv < 2; pv++) {
			for (int depthRemaining = 1; depthRemaining < 64; depthRemaining++) {
				for (int moveNumber = 1; moveNumber < 64; moveNumber++) {
					double reduction = REDUCTION_COEFS1[pv] + Math.log(depthRemaining) * Math.log(moveNumber) / REDUCTION_COEFS2[pv];
					reductionMatrix[pv][depthRemaining][moveNumber] = reduction >= 1.0 ? (int) (reduction * PLY) : 0;
				}
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
		for (int i = 0; i < MAX_DEPTH; i++) {
			nodes[i].clear();
		}
		clearHistory();

		String evaluatorName = config.getEvaluator();
		if ("simplified".equals(evaluatorName)) {
			evaluator = new SimplifiedEvaluator();
		} else if ("complete".equals(evaluatorName)) {
			evaluator = new CompleteEvaluator();
		} else if ("experimental".equals(evaluatorName)) {
			evaluator = new ExperimentalEvaluator();
		}

		tt = new TranspositionTable(config.getTranspositionTableSize());

		initialized = true;
		if (debug) {
			logger.debug(config.toString());
		}
	}

	public void clear() {
		clearHistory();
		// And transposition table
		tt.clear();
	}

	public void clearHistory() {
		for (int i = 0; i < 6; i++) {
			Arrays.fill(history[i], 0);
		}
	}

	public void destroy() {
		config = null;
		observer = null;
		tt = null;
		evaluator = null;
		history = null;
		if (nodes != null) {
			for (int i = 0; i < MAX_DEPTH; i++) {
				nodes[i].destroy();
				nodes[i] = null;
			}
		}
		System.gc();
	}

	private int getReduction(int nodeType, int depthRemaining, int movecount) {
		return reductionMatrix[nodeType == NODE_PV || nodeType == NODE_ROOT ? 1 : 0][Math.min(depthRemaining / PLY, 63)][Math.min(movecount, 63)];
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

	public long getNodeCount() {
		return nodeCount;
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
	 * Returns true if we can use the value stored on the TT to return from search
	 */
	private boolean canUseTT(int depthRemaining, int alpha, int beta) {
		if (tt.getDepthAnalyzed() >= depthRemaining) {
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
	public void evaluate(Node node, boolean foundTT) {
		ttEvalProbe++;

		if (foundTT) {
			ttEvalHit++;
			node.staticEval = tt.getEval();
			return;
		}
		node.staticEval = board.getTurn() ?
				evaluator.evaluate(board, node.attacksInfo) :
				-evaluator.evaluate(board, node.attacksInfo);

		// Store always the eval value in the TT
		tt.set(board, TranspositionTable.TYPE_EVAL,
				node.distanceToInitialPly, 0,
				Move.NONE, 0, node.staticEval, false);
	}

	public int refineEval(Node node, boolean foundTT) {
		return foundTT
				&& (tt.getNodeType() == TranspositionTable.TYPE_EXACT_SCORE
				|| (tt.getNodeType() == TranspositionTable.TYPE_FAIL_LOW && tt.getScore() < node.staticEval)
				|| (tt.getNodeType() == TranspositionTable.TYPE_FAIL_HIGH && tt.getScore() > node.staticEval)) ?
				tt.getScore() : node.staticEval;
	}

	public int quiescentSearch(int qsdepth, int alpha, int beta) throws SearchFinishedException {
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

		Node node = nodes[distanceToInitialPly];

		boolean isPv = beta - alpha > 1;
		boolean checkEvasion = board.getCheck();
		// Generate checks on PLY 0
		boolean generateChecks = (qsdepth == 0);
		// If we generate check, the entry in the TT has depthAnalyzed=1, because is better than without checks (depthAnalyzed=0)
		int ttDepth = generateChecks || checkEvasion ? TranspositionTable.DEPTH_QS_CHECKS : TranspositionTable.DEPTH_QS_NO_CHECKS;

		ttProbe++;
		boolean foundTT = tt.search(board, distanceToInitialPly, false);
		if (foundTT) {
			if (!isPv && canUseTT(ttDepth, alpha, beta)) {
				return tt.getScore();
			}
			node.ttMove = tt.getBestMove();
		} else {
			node.ttMove = Move.NONE;
		}

		int bestScore = alpha;
		int bestMove = Move.NONE;
		node.staticEval = Evaluator.NO_VALUE;
		int eval = -Evaluator.MATE;
		int futilityBase = -Evaluator.MATE;

		// Do not allow stand pat when in check
		if (!checkEvasion) {
			evaluate(node, foundTT);
			eval = refineEval(node, foundTT);

			// Evaluation functions increase alpha and can originate beta cutoffs
			bestScore = Math.max(bestScore, eval);
			if (bestScore >= beta) {
				if (!foundTT) {
					tt.set(board,
							TranspositionTable.TYPE_FAIL_HIGH,
							distanceToInitialPly, TranspositionTable.DEPTH_QS_CHECKS,
							Move.NONE, bestScore, node.staticEval, false);
				}
				return bestScore;
			}

			futilityBase = eval + FUTILITY_MARGIN_QS;
		}

		// If we have more depths than possible...
		if (distanceToInitialPly >= MAX_DEPTH - 1) {
			return checkEvasion ? evaluateDraw(distanceToInitialPly) : eval; // Return a drawish score if we are in check
		}

		node.moveIterator.genMoves(node.ttMove, (generateChecks ? MoveIterator.GENERATE_CAPTURES_PROMOS_CHECKS : MoveIterator.GENERATE_CAPTURES_PROMOS));

		int moveCount = 0;
		while ((node.move = node.moveIterator.next()) != Move.NONE) {
			nodeCount++;
			moveCount++;

			// Futility pruning
			if (!node.moveIterator.checkEvasion
					&& !Move.isCheck(node.move)
					&& !Move.isPawnPush678(node.move)
					&& futilityBase > -Evaluator.KNOWN_WIN) {
				int futilityValue = futilityBase + Evaluator.PIECE_VALUES[Move.getPieceCaptured(board, node.move)];
				if (futilityValue <= alpha) {
					bestScore = Math.max(bestScore, futilityValue);
					continue;
				}
				if (futilityBase <= alpha && node.moveIterator.getLastMoveSee() <= 0) {
					bestScore = Math.max(bestScore, futilityBase);
					continue;
				}
			}

			board.doMove(node.move, false, false);
			assert board.getCheck() == Move.isCheck(node.move) : "Check flag not generated properly";

			int score = -quiescentSearch(qsdepth + 1, -beta, -bestScore);
			board.undoMove();
			if (score > bestScore) {
				bestScore = score;
				bestMove = node.move;
				if (score >= beta) {
					break;
				}
			}
		}

		if (checkEvasion && moveCount == 0) {
			return valueMatedIn(distanceToInitialPly);
		}
		tt.set(board,
				bestScore <= alpha ? TranspositionTable.TYPE_FAIL_LOW
						: bestScore >= beta ? TranspositionTable.TYPE_FAIL_HIGH
						: TranspositionTable.TYPE_EXACT_SCORE,
				distanceToInitialPly, ttDepth,
				bestMove, bestScore, node.staticEval, false);

		return bestScore;
	}

	/**
	 * Search Root, PV and null window
	 */
	public int search(int nodeType, int depthRemaining, int alpha, int beta, boolean allowPrePruning, int excludedMove) throws SearchFinishedException {
		if (nodeType != NODE_ROOT && globalBestMove != Move.NONE && (System.currentTimeMillis() > thinkToTime || nodeCount > thinkToNodes)) {
			throw new SearchFinishedException();
		}

		int distanceToInitialPly = board.getMoveNumber() - initialPly;

		if (nodeType == NODE_PV || nodeType == NODE_ROOT) {
			if (distanceToInitialPly > selDepth) {
				selDepth = distanceToInitialPly;
			}
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

		Node node = nodes[distanceToInitialPly];

		int ttScore = 0;
		int ttNodeType = 0;
		int ttDepthAnalyzed = 0;
		int score = 0;

		ttProbe++;
		boolean foundTT = tt.search(board, distanceToInitialPly, excludedMove != Move.NONE);
		if (foundTT) {
			if (nodeType != NODE_ROOT && canUseTT(depthRemaining, alpha, beta)) {
				if (distanceToInitialPly + tt.getDepthAnalyzed() > selDepth) {
					selDepth = distanceToInitialPly + tt.getDepthAnalyzed();
				}

				return tt.getScore();
			}
			node.ttMove = tt.getBestMove();
			ttScore = tt.getScore();
			ttNodeType = tt.getNodeType();
			ttDepthAnalyzed = tt.getDepthAnalyzed();
		} else {
			node.ttMove = Move.NONE;
		}

		boolean checkEvasion = board.getCheck();
		boolean mateThreat = false;
		int eval = -Evaluator.MATE;
		node.staticEval = -Evaluator.MATE;

		if (!checkEvasion) {
			// Do a static eval, in case of exclusion and not found in the TT, search again with the normal key
			boolean evalTT = excludedMove == Move.NONE || foundTT ? foundTT : tt.search(board, distanceToInitialPly, false);
			evaluate(node, evalTT);
			eval = refineEval(node, foundTT);
		}

		// If we have more depths than possible...
		if (distanceToInitialPly >= MAX_DEPTH - 1) {
			return checkEvasion ? evaluateDraw(distanceToInitialPly) : eval; // Return a drawish score if we are in check
		}

		if (!checkEvasion
				&& allowPrePruning) {
			// Hyatt's Razoring http://chessprogramming.wikispaces.com/Razoring
			if (nodeType == NODE_NULL
					&& node.ttMove == Move.NONE
					&& depthRemaining < RAZORING_MARGIN.length
					&& Math.abs(beta) < VALUE_IS_MATE
					&& Math.abs(eval) < Evaluator.KNOWN_WIN
					&& eval + RAZORING_MARGIN[depthRemaining] < beta
					&& (board.pawns & ((board.whites & BitboardUtils.R7) | (board.blacks & BitboardUtils.R2))) == 0) { // No pawns on 7TH
				razoringProbe++;

				if (depthRemaining <= PLY
						&& eval + RAZORING_MARGIN[RAZORING_MARGIN.length - 1] < beta) {
					razoringHit++;
					return quiescentSearch(0, alpha, beta);
				}

				int rbeta = beta - RAZORING_MARGIN[depthRemaining];
				int v = quiescentSearch(0, rbeta - 1, rbeta);
				if (v < rbeta) {
					razoringHit++;
					return v;
				}
			}

			// Static null move pruning or futility pruning in parent node
			if (nodeType != NODE_ROOT
					&& depthRemaining < FUTILITY_MARGIN_PARENT.length
					&& Math.abs(beta) < VALUE_IS_MATE
					&& Math.abs(eval) < Evaluator.KNOWN_WIN
					&& eval - FUTILITY_MARGIN_PARENT[depthRemaining] >= beta
					&& boardAllowsNullMove()) {
				return eval - FUTILITY_MARGIN_PARENT[depthRemaining];
			}

			// Null move pruning and mate threat detection
			if (nodeType == NODE_NULL
					&& depthRemaining >= 2 * PLY
					&& Math.abs(beta) < VALUE_IS_MATE
					&& eval >= beta
					&& boardAllowsNullMove()) {

				nullMoveProbe++;

				int R = 3 * PLY + (depthRemaining >> 2);

				board.doMove(Move.NULL, false, false);
				score = depthRemaining - R < PLY ? -quiescentSearch(0, -beta, -beta + 1) :
						-search(NODE_NULL, depthRemaining - R, -beta, -beta + 1, false, Move.NONE);
				board.undoMove();

				if (score >= beta) {
					if (score >= VALUE_IS_MATE) {
						score = beta;
					}

					// Verification search on initial depths
					if (depthRemaining < 12 * PLY
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
			if (node.ttMove == Move.NONE
					&& depthRemaining >= IID_DEPTH[nodeType]
					&& (nodeType != NODE_NULL || (node.staticEval + IID_MARGIN) > beta)) {
				int d = (nodeType == NODE_PV ? depthRemaining - 2 * PLY : depthRemaining >> 1);
				search(nodeType, d, alpha, beta, false, Move.NONE);
				if (tt.search(board, distanceToInitialPly, false)) {
					node.ttMove = tt.getBestMove();
				}
			}
		}

		node.moveIterator.genMoves(node.ttMove);

		int bestScore = -Evaluator.MATE;
		int bestMove = Move.NONE;
		int moveCount = 0;

		while ((node.move = node.moveIterator.next()) != Move.NONE) {
			if (node.move == excludedMove) {
				continue;
			}
			nodeCount++;
			moveCount++;

			//
			// Calculates the extension of a move in the actual position
			//
			int extension = mateThreat ? PLY :
					(Move.isCheck(node.move) && node.moveIterator.getLastMoveSee() >= 0) ? PLY :
							0;

			// Check singular move extension
			// It also detects singular replies
			if (nodeType != NODE_ROOT
					&& node.move == node.ttMove
					&& extension < PLY
					&& excludedMove == Move.NONE
					&& depthRemaining >= SINGULAR_MOVE_DEPTH[nodeType]
					&& ttNodeType == TranspositionTable.TYPE_FAIL_HIGH
					&& ttDepthAnalyzed >= depthRemaining - 3 * PLY
					&& Math.abs(ttScore) < Evaluator.KNOWN_WIN) {

				int savedMove = node.move;

				singularExtensionProbe++;
				int seBeta = ttScore - SINGULAR_EXTENSION_MARGIN_PER_PLY * depthRemaining / PLY;
				int excScore = search(nodeType, depthRemaining >> 1, seBeta - 1, seBeta, false, node.move);
				if (excScore < seBeta) {
					singularExtensionHit++;
					extension = PLY;
				}

				// ****** FIX NODE AND MOVE ITERATOR ******
				// The same move iterator is used in the excluded search, so reset it to the point
				// where it was previously (the TT move)
				node.move = savedMove;
				node.ttMove = savedMove;
				node.moveIterator.genMoves(node.ttMove);
				node.moveIterator.next();

//				printNodeTree(distanceToInitialPly);
//				System.out.println("seBeta = " + seBeta + " excScore=" + excScore + " extension=" + extension);
			}

			int reduction = 0;
			// If the move is not important
			if (nodeType != NODE_ROOT
					&& node.move != node.ttMove
					&& !checkEvasion
					&& !Move.isCaptureOrCheck(node.move) // Include ALL captures
					&& !Move.isPawnPush678(node.move) // Includes promotions
					&& !node.moveIterator.getLastMoveIsKiller()) {

				// Late move reductions (LMR)
				if (depthRemaining >= LMR_DEPTHS_NOT_REDUCED) {
					reduction += getReduction(nodeType, depthRemaining, moveCount);
				}

				// Futility Pruning
				if (bestScore > -Evaluator.KNOWN_WIN) { // There is a best move
					int newDepth = depthRemaining - PLY + extension - reduction;
					if (newDepth < FUTILITY_MARGIN_PARENT.length) {
						int futilityValue = node.staticEval + FUTILITY_MARGIN_CHILD[newDepth];
						if (futilityValue <= alpha) {
							futilityHit++;
							if (futilityValue > bestScore) {
								bestScore = futilityValue;
							}
							continue;
						}
					}

					if (depthRemaining < 3 * PLY
							&& node.moveIterator.getLastMoveSee() < 0) {
						continue;
					}
				}
			}

			board.doMove(node.move, false, false);
			assert board.getCheck() == Move.isCheck(node.move) : "Check flag not generated properly";

			int lowBound = alpha > bestScore ? alpha : bestScore;
			if ((nodeType == NODE_PV || nodeType == NODE_ROOT) && moveCount == 1) {
				// PV move not null searched
				score = depthRemaining + extension - PLY < PLY ? -quiescentSearch(0, -beta, -lowBound) :
						-search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true, Move.NONE);
			} else {
				// Try searching null window
				boolean doFullSearch = true;
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
							|| random.nextInt(1000) > config.getRand())) {
				bestMove = node.move;
				bestScore = score;

				if (nodeType == NODE_ROOT) {
					globalBestMove = node.move;
					bestMoveScore = score;

					if (depthRemaining > 6 * PLY) {
						notifyMoveFound(node.move, score, alpha, beta);
					}
				}
			}

			// alpha/beta cut (fail high)
			if (score >= beta) {
				break;
			}
		}

		// Checkmate or stalemate
		if (moveCount == 0) {
			bestScore = excludedMove != Move.NONE ? alpha :
					checkEvasion ? valueMatedIn(distanceToInitialPly) :
							evaluateDraw(distanceToInitialPly);
		}

		// Tells MoveSorter the move score
		if (bestScore >= beta) {
			if (excludedMove == Move.NONE && moveCount > 0) {
				betaCutoff(node, bestMove);
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
		tt.set(board,
				bestScore <= alpha ? TranspositionTable.TYPE_FAIL_LOW
						: bestScore >= beta ? TranspositionTable.TYPE_FAIL_HIGH
						: TranspositionTable.TYPE_EXACT_SCORE,
				distanceToInitialPly, depthRemaining,
				bestMove, bestScore, node.staticEval, excludedMove != Move.NONE);

		return bestScore;
	}

	/**
	 * Notifies the best move to the SearchObserver filling a SearchStatusInfo object
	 */
	private void notifyMoveFound(int move, int score, int alpha, int beta) {
		long time = System.currentTimeMillis();

		SearchStatusInfo info = new SearchStatusInfo();
		info.setDepth(depth);
		info.setSelDepth(selDepth);
		info.setTime(time - startTime);
		info.setPv(getPv(move));
		info.setScore(score, alpha, beta);
		info.setNodes(nodeCount);
		info.setHashFull(tt.getHashFull());
		info.setNps((int) (1000 * nodeCount / (time - startTime + 1)));

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
		synchronized (startStopSearchLock) {
			if (!initialized || searching) {
				return;
			}
			searching = true;
			setInitialSearchParameters(searchParameters);
		}
		run();
	}

	private void searchStats() {
		logger.debug("Positions         = " + nodeCount);
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

	private void prepareRun() throws SearchFinishedException {
		logger.debug("Board\n" + board);

		panicTime = false;
		globalBestMove = Move.NONE;
		globalPonderMove = Move.NONE;

		initialPly = board.getMoveNumber();

		if (config.getUseBook() && config.getBook() != null && board.isUsingBook()
				&& (config.getBookKnowledge() == 100 || ((random.nextFloat() * 100) < config.getBookKnowledge()))) {
			logger.debug("Searching move in book");
			int bookMove = config.getBook().getMove(board);
			if (bookMove != Move.NONE) {
				globalBestMove = bookMove;
				logger.debug("Move found in book");
				throw new SearchFinishedException();
			} else {
				logger.debug("Move NOT found in book");
				board.setOutBookMove(board.getMoveNumber());
			}
		}

		depth = 1;
		boolean foundTT = tt.search(board, 0, false);
		if (canUseTT(0, -Evaluator.MATE, Evaluator.MATE)) {
			rootScore = tt.getScore();
		} else {
			evaluate(nodes[0], foundTT);
			rootScore = nodes[0].staticEval;
		}
		tt.newGeneration();
		aspWindows = ASPIRATION_WINDOW_SIZES;
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
			updateSearchParameters(searchParameters);
		}

		if ((searchParameters.manageTime() && ( // Under time restrictions and...
				Math.abs(rootScore) > VALUE_IS_MATE // Mate found or
						|| (time2 + ((time2 - time1) << 1)) > thinkToTime)) // It will not likely finish the next iteration
				|| depth == MAX_DEPTH
				|| depth >= thinkToDepth
				|| Math.abs(rootScore) == Evaluator.MATE) { // Search limit reached
			throw new SearchFinishedException();
		}
		depth++;
	}

	public void run() {
		int bestMove = Move.NONE, ponderMove = Move.NONE;

		synchronized (searchLock) {
			try {
				prepareRun();
				while (true) {
					runStepped();
				}
			} catch (SearchFinishedException ignored) {
			}

			// Return the board to the initial position
			board.undoMove(initialPly);

			bestMove = globalBestMove;
			ponderMove = globalPonderMove;

			searching = false;
		}

		if (observer != null) {
			observer.bestMove(bestMove, ponderMove);
		}
		if (debug) {
			searchStats();
		}
	}

	/**
	 * Cannot be called during search (!)
	 */
	public void setInitialSearchParameters(SearchParameters searchParameters) {
		engineIsWhite = board.getTurn();
		startTime = System.currentTimeMillis();
		nodeCount = 0;
		updateSearchParameters(searchParameters);
	}

	/**
	 * This is used to update the search parameters while searching
	 */
	public void updateSearchParameters(SearchParameters searchParameters) {
		this.searchParameters = searchParameters;

		thinkToNodes = searchParameters.getNodes();
		thinkToDepth = searchParameters.getDepth();
		thinkToTime = searchParameters.calculateMoveTime(engineIsWhite, startTime, panicTime);
	}

	/**
	 * Gets the principal variation from the transposition table
	 */
	private String getPv(int firstMove) {
		if (firstMove == Move.NONE) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		List<Long> keys = new ArrayList<>(); // To not repeat keys
		sb.append(Move.toString(firstMove));
		int savedMoveNumber = board.getMoveNumber();
		board.doMove(firstMove, true, false);

		int i = 1;
		while (i < 256) {
			if (tt.search(board, i, false)) {
				if (tt.getBestMove() == Move.NONE || keys.contains(board.getKey())) {
					break;
				}
				keys.add(board.getKey());
				if (i == 1) {
					globalPonderMove = tt.getBestMove();
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
		board.undoMove(savedMoveNumber);
		return sb.toString();
	}

	public void stop() {
		thinkToTime = 0;
		thinkToNodes = 0;
		thinkToDepth = 0;
	}

	public int evaluateDraw(int distanceToInitialPly) {
		int nonPawnMat = BitboardUtils.popCount(board.knights) * Evaluator.KNIGHT +
				BitboardUtils.popCount(board.bishops) * Evaluator.BISHOP +
				BitboardUtils.popCount(board.rooks) * Evaluator.ROOK +
				BitboardUtils.popCount(board.queens) * Evaluator.QUEEN;
		int gamePhase = nonPawnMat >= Evaluator.NON_PAWN_MATERIAL_MIDGAME_MAX ? Evaluator.GAME_PHASE_MIDGAME :
				nonPawnMat <= Evaluator.NON_PAWN_MATERIAL_ENDGAME_MIN ? Evaluator.GAME_PHASE_ENDGAME :
						((nonPawnMat - Evaluator.NON_PAWN_MATERIAL_ENDGAME_MIN) * Evaluator.GAME_PHASE_MIDGAME) / (Evaluator.NON_PAWN_MATERIAL_MIDGAME_MAX - Evaluator.NON_PAWN_MATERIAL_ENDGAME_MIN);

		return ((distanceToInitialPly & 1) == 0 ? -config.contemptFactor : config.contemptFactor) * gamePhase / Evaluator.GAME_PHASE_MIDGAME;
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

	public boolean isSearching() {
		return searching;
	}

	/**
	 * We are informed of the score produced by the move at any level
	 */
	public void betaCutoff(Node node, int move) {
		// removes captures and promotions from killers
		if (move == Move.NONE || Move.isTactical(move)) {
			return;
		}

		if (move != node.killerMove1) {
			node.killerMove2 = node.killerMove1;
			node.killerMove1 = move;
		}

		int pieceMoved = Move.getPieceMoved(move) - 1;
		int toIndex = Move.getToIndex(move);

		history[pieceMoved][toIndex]++;

		// Detect history overflows and divide all values by two
		if (history[pieceMoved][toIndex] >= HISTORY_MAX) {
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 64; j++) {
					history[i][j] >>>= 1;
				}
			}
		}
	}

	public int getMoveScore(int move) {
		return history[Move.getPieceMoved(move) - 1][Move.getToIndex(move)];
	}

	private void printNodeTree(int distanceToInitialPly) {
		for (int i = 0; i <= distanceToInitialPly; i++) {
			Node node = nodes[i];
			System.out.print(Move.toString(node.move) + " (" + Move.toString(node.ttMove) + ") ");
		}
		System.out.println();
	}
}