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
import com.alonsoruibal.chess.movesort.MoveIterator;
import com.alonsoruibal.chess.movesort.SortInfo;
import com.alonsoruibal.chess.tt.MultiprobeTranspositionTable;
import com.alonsoruibal.chess.tt.TranspositionTable;

import java.util.ArrayList;
import java.util.Date;
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
	private static final int PLY = 2;
	private static final int LMR_DEPTHS_NOT_REDUCED = 3 * PLY;
	private static final int RAZOR_DEPTH = 4 * PLY;

	public static final int NODE_ROOT = 0;
	public static final int NODE_PV = 1;
	public static final int NODE_NULL = 2;

	private SearchParameters searchParameters;

	private boolean searching = false;
	private boolean foundOneMove = false;

	private Config config;

	// time to think to
	private long thinkTo = 0;

	private Board board;
	private SearchObserver observer;
	private Evaluator evaluator;
	private TranspositionTable tt;
	private SortInfo sortInfo;
	private MoveIterator[] moveIterators;

	private long bestMoveTime; // For testing suites
	private int bestMoveScore; // For testing suites
	private int globalBestMove, ponderMove;
	private String pv;

	private int initialPly; // Inital Ply of search
	private int depth;
	private int score;
	private int[] aspWindows;

	long startTime;

	// For performance Benching
	private long positionCounter;
	private long pvPositionCounter;
	private long qsPositionCounter;
	private long pvCutNodes;
	private long pvAllNodes;
	private long nullCutNodes;
	private long nullAllNodes;

	// aspiration window
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
	private int[] singularMoveDepth = {6 * PLY, 6 * PLY, 8 * PLY};
	private int[] iidDepth = {5 * PLY, 5 * PLY, 8 * PLY};

	public SearchEngine(Config config) {
		this.config = config;
		random = new Random();
		board = new Board();
		sortInfo = new SortInfo();
		moveIterators = new MoveIterator[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			moveIterators[i] = new MoveIterator(board, sortInfo, i);
		}

		pvReductionMatrix = new int[MAX_DEPTH][64];
		nonPvReductionMatrix = new int[MAX_DEPTH][64];
		// Init our reduction lookup tables
		for (int depth = 1; depth < MAX_DEPTH; depth++) { // OnePly = 1
			for (int moveNumber = 1; moveNumber < 64; moveNumber++) {

				double pvRed = 0.5 + Math.log(depth) * Math.log(moveNumber) / 6.0;
				double nonPVRed = 0.5 + Math.log(depth) * Math.log(moveNumber) / 3.0;
				pvReductionMatrix[depth][moveNumber] = (int) (pvRed >= 1.0 ? Math.floor(pvRed * PLY) : 0);
				nonPvReductionMatrix[depth][moveNumber] = (int) (nonPVRed >= 1.0 ? Math.floor(nonPVRed * PLY) : 0);
				// System.out.println(i + " " + j + " " +
				// pvReductionMatrix[i][j] + " " + nonPvReductionMatrix[i][j]);
			}
		}

		init();
	}

	public void init() {
		logger.debug(new Date());
		initialized = false;

		board.startPosition();
		sortInfo.clear();

		logger.debug("Creating Evaluator");

		String evaluatorName = config.getEvaluator();
		if ("simplified".equals(evaluatorName)) {
			evaluator = new SimplifiedEvaluator();
		} else if ("complete".equals(evaluatorName)) {
			evaluator = new CompleteEvaluator(config);
		} else if ("experimental".equals(evaluatorName)) {
			evaluator = new ExperimentalEvaluator(config);
		}

		int size = BitboardUtils.square2Index((long) config.getTranspositionTableSize()) + 16;

		logger.debug("Creating TT");

		// tt = new TwoTierTranspositionTable(size);
		// tt = new MultiprobeTranspositionTableNew(size);
		tt = new MultiprobeTranspositionTable(size);

		initialized = true;
		logger.debug(config.toString());
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
		return nodeType == NODE_PV || nodeType == NODE_ROOT ? pvReductionMatrix[Math.min(depth / PLY, 63)][Math.min(movecount, 63)] : nonPvReductionMatrix[Math
				.min(depth / PLY, 63)][Math.min(movecount, 63)];
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

	public long getBestMoveTime() {
		return bestMoveTime;
	}

	public long getBestMoveScore() {
		return bestMoveScore;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * Decides when we are going to allow null move Don't do null move in king
	 * and pawn endings
	 */
	private boolean boardAllowNullMove() {
		return !board.getCheck() && (board.getMines() & (board.knights | board.bishops | board.rooks | board.queens)) != 0;
	}

	/**
	 * Calculates the extension of a move in the actual position (with the move
	 * done)
	 */
	private int extensions(int move, boolean mateThreat) {
		int ext = 0;

		if (board.getCheck()) {
			ext += config.getExtensionsCheck();
			if (ext >= PLY) {
				return PLY;
			}
		}
		if (Move.getPieceMoved(move) == Move.PAWN) {
			if (Move.isPawnPush(move)) {
				ext += config.getExtensionsPawnPush();
			}
			if (board.isPassedPawn(Move.getToIndex(move))) {
				ext += config.getExtensionsPassedPawn();
			}
			if (ext >= PLY) {
				return PLY;
			}
		}
		if (mateThreat) {
			ext += config.getExtensionsMateThreat();
			if (ext >= PLY) {
				return PLY;
			}
		}
		if (ext < config.getExtensionsRecapture() && board.getLastMoveIsRecapture()) {
			int seeValue = board.see(move);
			int capturedPieceValue = pieceValue(board.getPieceAt(Move.getToSquare(move)));
			if (seeValue > capturedPieceValue - 50) {
				ext += config.getExtensionsRecapture();
			}
		}
		if (ext >= PLY) {
			return PLY;
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
	 * Also changes sign to score depending of turn usetTT requires to do a
	 * previous search on TT
	 */
	private int eval(boolean foundTT, boolean refine) {
		ttEvalProbe++;

		if (foundTT) {
			if (tt.getNodeType() == TranspositionTable.TYPE_EVAL) {
				ttEvalHit++;
				// // uncomment to test if eval tt is Working
				// if (evaluator.evaluate(board) !=
				// tt.getScore()) {
				// System.out.println("Error Garrafal!!!");
				// System.out.println(tt.getScore());
				// System.out.println(evaluator.evaluate(board));
				// System.out.println(board.toString());
				// System.exit(-1);
				// }
				int score = tt.getScore();
				if (!board.getTurn()) {
					score = -score;
				}
				return score;
			}
		}
		int score = evaluator.evaluate(board);
		tt.set(board, TranspositionTable.TYPE_EVAL, 0, score, (byte) 0, false);
		if (!board.getTurn()) {
			score = -score;
		}
		if (foundTT && refine) {
			// Refine Value with TT
			switch (tt.getNodeType()) {
				case TranspositionTable.TYPE_FAIL_LOW:
					if (tt.getScore() > score) {
						score = tt.getScore();
					}
					break;
				case TranspositionTable.TYPE_FAIL_HIGH:
					if (tt.getScore() < score) {
						score = tt.getScore();
					}
					break;
			}
		}
		return score;
	}

	private int lastCapturedPieceValue(Board board) {
		return pieceValue(board.getLastCapturedPiece());
	}

	private int pieceValue(char piece) {
		int capturedPieceValue = 0;
		switch (Character.toLowerCase(piece)) {
			case 'p':
				return CompleteEvaluator.PAWN;
			case 'n':
				return CompleteEvaluator.KNIGHT;
			case 'b':
				return CompleteEvaluator.BISHOP;
			case 'r':
				return CompleteEvaluator.ROOK;
			case 'q':
				return CompleteEvaluator.QUEEN;
		}
		return capturedPieceValue;
	}

	public int quiescentSearch(int qsdepth, int alpha, int beta) throws SearchFinishedException {
		if (System.currentTimeMillis() > thinkTo && foundOneMove) {
			throw new SearchFinishedException();
		}
		qsPositionCounter++;

		// It checks draw by three fold repetition, fifty moves rule and no material to mate
		if (board.isDraw()) {
			return evaluateDraw();
		}

		int eval = -Evaluator.VICTORY;
		int score;
		boolean pv = beta - alpha > 1;

		ttProbe++;
		boolean foundTT = tt.search(board, false);
		if (foundTT) {
			if (!pv && canUseTT(0, alpha, beta)) {
				return tt.getScore();
			}
		}

		// Do not allow stand pat when in check
		if (!board.getCheck()) {
			eval = eval(foundTT, true);

			// Evaluation functions increase alpha and can originate beta cutoffs
			if (eval >= beta) {
				return eval;
			}
			if (eval > alpha) {
				alpha = eval;
			}
		}

		// If we have more depths than possible...
		if (board.getMoveNumber() - initialPly >= MAX_DEPTH) {
			System.out.println("Quiescence exceeds depth qsdepth=" + qsdepth);
			System.out.println(board.toString());
			for (int i = 0; i < board.getMoveNumber(); i++) {
				System.out.print(Move.toStringExt(board.moveHistory[i]));
				System.out.print(" ");
			}
			System.out.println();
			return eval;
		}

		boolean validOperations = false;
		boolean checkEvasion = board.getCheck();
		// Generate checks for PV on PLY 0
		boolean generateChecks = pv && (qsdepth == 0);

		MoveIterator moveIterator = moveIterators[board.getMoveNumber() - initialPly];
		moveIterator.genMoves(0, true, generateChecks);
		int move;

		while ((move = moveIterator.next()) != 0) {
			if (board.doMove(move, false)) {
				validOperations = true;

				// Futility pruning
				if (!board.getCheck()
						&& !checkEvasion
						&& !Move.isPromotion(move)
						&& !Move.isPawnPush(move)
						&& !pv
						&& (((board.queens | board.rooks) & board.getMines()) != 0 || (BitboardUtils.popCount(board.bishops | board.knights) & board.getMines()) > 1)) {
					int futilityValue = eval + lastCapturedPieceValue(board) + config.getFutilityMarginQS();
					if (futilityValue < alpha) {
						// if (futilityValue > bestValue) bestValue =
						// futilityValue; //TODO
						// System.out.println("PRUNE!");
						board.undoMove();
						continue;
					}
				}

				// Necessary because TT move can be a no promotion or capture
				if (!checkEvasion && !(board.getCheck() //
						&& generateChecks) //
						&& moveIterator.getPhase() > MoveIterator.PHASE_GOOD_CAPTURES_AND_PROMOS) {
					board.undoMove();
					continue;
				}

				score = -quiescentSearch(qsdepth + 1, -beta, -alpha);
				board.undoMove();
				if (score > alpha) {
					alpha = score;
					if (score >= beta) {
						break;
					}
				}
			}
		}

		if (board.getCheck() && !validOperations) {
			return valueMatedIn(board.getMoveNumber() - initialPly);
		}

		return alpha;
	}

	/**
	 * Search Root, PV and null window
	 */
	public int search(int nodeType, int depthRemaining, int alpha, int beta, boolean allowNullMove, int excludedMove) throws SearchFinishedException {
		if (System.currentTimeMillis() > thinkTo && foundOneMove) {
			throw new SearchFinishedException();
		}
		if (nodeType == NODE_PV || nodeType == NODE_ROOT) {
			pvPositionCounter++;
		} else {
			positionCounter++;
		}

		// It checks draw by three fold repetition, fifty moves rule and no material to mate
		if (board.isDraw()) {
			return evaluateDraw();
		}

		// Mate distance pruning
		alpha = Math.max(valueMatedIn(board.getMoveNumber() - initialPly), alpha);
		beta = Math.min(valueMateIn(board.getMoveNumber() - initialPly + 1), beta);
		if (alpha >= beta) {
			return alpha;
		}

		int ttMove = 0;
		int ttScore = 0;
		int score = 0;

		ttProbe++;
		boolean foundTT = tt.search(board, excludedMove != 0);
		if (foundTT) {
			if (nodeType != NODE_ROOT //
					&& canUseTT(depthRemaining, alpha, beta)) {
				return tt.getScore();
			}
			ttMove = tt.getBestMove();
			ttScore = tt.getScore();
		}

		if (depthRemaining < PLY || board.getMoveNumber() - initialPly >= MAX_DEPTH - 1) {
			return quiescentSearch(0, alpha, beta);
		}

		int eval = -Evaluator.VICTORY;

		// Do a static eval
		if (!board.getCheck()) {
			eval = eval(foundTT, true);
		}

		// Hyatt's Razoring http://chessprogramming.wikispaces.com/Razoring
		if (nodeType == NODE_NULL //
				&& config.getRazoring() //
				&& !board.getCheck() //
				&& ttMove == 0 //
				&& allowNullMove // Not when last was a null move
				&& depthRemaining < RAZOR_DEPTH //
				&& !valueIsMate(beta) //
				&& eval < beta - config.getRazoringMargin() //
				// No pawns on 7TH
				&& (board.pawns & ((board.whites & BitboardUtils.b2_u) | (board.blacks & BitboardUtils.b2_d))) == 0) {
			razoringProbe++;

			int rbeta = beta - config.getRazoringMargin();
			int v = quiescentSearch(0, rbeta - 1, rbeta);
			if (v < rbeta) {
				razoringHit++;
				return v;
			}
		}

		// Static null move pruning
		if (nodeType == NODE_NULL //
				&& config.getStaticNullMove() //
				&& allowNullMove //
				&& boardAllowNullMove() //
				&& depthRemaining < RAZOR_DEPTH //
				&& !valueIsMate(beta) //
				&& eval >= beta + config.getFutilityMargin()) {
			return eval - config.getFutilityMargin();
		}

		// Null move pruning and mate threat detection
		boolean mateThreat = false;
		if (nodeType == NODE_NULL //
				&& config.getNullMove() //
				&& allowNullMove //
				&& boardAllowNullMove() //
				&& depthRemaining > 3 * PLY //
				&& !valueIsMate(beta) //
				&& eval > beta - (depthRemaining >= 4 * PLY ? config.getNullMoveMargin() : 0)) {

			nullMoveProbe++;
			board.doMove(0, false);
			int R = 3 * PLY + (depthRemaining >= 5 * PLY ? depthRemaining / (4 * PLY) : 0);
			if (eval - beta > CompleteEvaluator.PAWN) {
				R++;
			}
			score = -search(NODE_NULL, depthRemaining - R, -beta, -beta + 1, false, 0);
			board.undoMove();
			if (score >= beta) {
				if (valueIsMate(score)) {
					score = beta;
				}

				// Verification search on initial depths
				if (depthRemaining < 6 * PLY //
						|| search(NODE_NULL, depthRemaining - 5 * PLY, beta - 1, beta, false, 0) >= beta) {
					nullMoveHit++;
					return score;
				}
			} else {
				// Detect mate threat to exit
				if (score < (-Evaluator.VICTORY + 100)) {
					mateThreat = true;
				}
			}
		}

		// Internal Iterative Deepening
		if (config.getIid() //
				&& ttMove == 0 //
				&& depthRemaining >= iidDepth[nodeType] //
				&& allowNullMove //
				&& !board.getCheck() //
				&& (nodeType != NODE_NULL || eval > beta - config.getIidMargin()) //
				&& excludedMove == 0) {
			int d = (nodeType == NODE_PV ? depthRemaining - 2 * PLY : depthRemaining >> 1);
			search(nodeType, d, alpha, beta, true, 0); // TODO Allow null move ?
			if (tt.search(board, false)) {
				ttMove = tt.getBestMove();
			}
		}

		// Singular Move
		boolean singularMoveExtension = nodeType != NODE_ROOT //
				&& ttMove != 0 //
				&& config.getExtensionsSingular() > 0 //
				&& depthRemaining >= singularMoveDepth[nodeType] //
				&& tt.getNodeType() == TranspositionTable.TYPE_FAIL_HIGH // ???
				&& tt.getDepthAnalyzed() >= depthRemaining - 3 * PLY //
				&& Math.abs(ttScore) < Evaluator.KNOWN_WIN;

		// Futility pruning
		boolean futilityPrune = false;
		if (nodeType == NODE_NULL //
				&& !board.getCheck()) {
			if (depthRemaining <= PLY) { // at frontier nodes
				if (config.getFutility() //
						&& eval < beta - config.getFutilityMargin()) {
					futilityHit++;
					futilityPrune = true;
				}
			} else if (depthRemaining <= 2 * PLY) { // at pre-frontier nodes
				if (config.getAggressiveFutility() //
						&& eval < beta - config.getAggressiveFutilityMargin()) {
					aggressiveFutilityHit++;
					futilityPrune = true;
				}
			}
		}

		MoveIterator moveIterator = moveIterators[board.getMoveNumber() - initialPly];
		moveIterator.genMoves(ttMove);

		int movesDone = 0;
		boolean validOperations = false;
		boolean checkEvasion = board.getCheck();
		int bestScore = -Evaluator.VICTORY;
		int move, bestMove = 0;

		while ((move = moveIterator.next()) != 0) {
			int extension = 0;
			int reduction = 0;

			// Operations are pseudo-legal, doMove checks if they lead to a valid state
			if (board.doMove(move, false)) {
				validOperations = true;

				if (move == excludedMove) {
					board.undoMove();
					continue;
				}

				extension += extensions(move, mateThreat);

				// Check singular reply extension
				if (singularMoveExtension //
						&& move == ttMove //
						&& extension < PLY //
						&& excludedMove == 0) {
					singularExtensionProbe++;
					board.undoMove();
					int seBeta = ttScore - config.getSingularExtensionMargin();
					int excScore = search(nodeType, depthRemaining >> 1, seBeta - 1, seBeta, false, move);
					board.doMove(move);
					if (excScore < seBeta) {
						singularExtensionHit++;
						extension += config.getExtensionsSingular();
						if (extension > PLY) {
							extension = PLY;
						}
					}
				}

				boolean importantMove = nodeType == NODE_ROOT //
						|| extension != 0 //
						|| Move.isCapture(move) //
						|| Move.isPromotion(move) //
						|| Move.isCastling(move) //
						|| checkEvasion //
						|| move == ttMove //
						|| sortInfo.isKiller(move, board.getMoveNumber() - initialPly);

				if (futilityPrune //
						&& bestScore > -Evaluator.KNOWN_WIN //
						&& !importantMove) {
					board.undoMove();
					continue;
				}

				// Late move reductions (LMR)
				if (config.getLmr() //
						&& depthRemaining >= LMR_DEPTHS_NOT_REDUCED //
						&& !importantMove) {
					reduction += getReduction(nodeType, depthRemaining, movesDone);
				}

				movesDone++;

				int lowBound = (alpha > bestScore ? alpha : bestScore);
				if ((nodeType == NODE_PV || nodeType == NODE_ROOT) && movesDone == 1) {
					// PV move not null searched
					score = -search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true, 0);
				} else {
					// Try searching null window
					boolean doFullSearch = true;

					if (reduction > 0) {
						score = -search(NODE_NULL, depthRemaining - reduction - PLY, -lowBound - 1, -lowBound, true, 0);
						doFullSearch = (score > lowBound);
					}
					if (doFullSearch) {
						score = -search(NODE_NULL, depthRemaining + extension - PLY, -lowBound - 1, -lowBound, true, 0);

						// Finally search as PV if score on window
						if ((nodeType == NODE_PV || nodeType == NODE_ROOT) //
								&& score > lowBound //
								&& (nodeType == NODE_ROOT || score < beta)) {
							score = -search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true, 0);
						}
					}
				}

				board.undoMove();

				// It tracks the best move and it also insert errors on the root node
				if (score > bestScore && (nodeType != NODE_ROOT || config.getRand() == 0 || (random.nextInt(100) > config.getRand()))) {
					bestMove = move;
					bestScore = score;
				}

				// alpha/beta cut (fail high)
				if (score >= beta) {
					break;
				}
			}
		}

		// Checkmate or stalemate
		if (excludedMove == 0 && !validOperations) {
			bestScore = evaluateEndgame();
		}

		// Tells MoveSorter the move score
		if (bestScore >= beta) {
			if (excludedMove == 0) {
				sortInfo.betaCutoff(bestMove, board.getMoveNumber() - initialPly);
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

		// Save in the transposition Table
		tt.save(board, (byte) depthRemaining, bestMove, bestScore, alpha, beta, excludedMove != 0);

		return bestScore;
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
			run();
		}
	}

	private void searchStats() {
		logger.debug("Positions PV      = " + pvPositionCounter + " " //
				+ (100.0 * pvPositionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
		logger.debug("Positions QS      = " + qsPositionCounter + " " //
				+ (100.0 * qsPositionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
		logger.debug("Positions Null    = " + positionCounter + " " //
				+ (100.0 * positionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
		logger.debug("PV Cut            = " + pvCutNodes + " " + (100 * pvCutNodes / (pvCutNodes + pvAllNodes + 1)) + "%");
		logger.debug("PV All            = " + pvAllNodes);
		logger.debug("Null Cut          = " + nullCutNodes + " " + (100 * nullCutNodes / (nullCutNodes + nullAllNodes + 1)) + "%");
		logger.debug("Null All          = " + nullAllNodes);
		logger.debug("Asp Win      Hits = " + (100.0 * aspirationWindowHit / aspirationWindowProbe) + "%");
		logger.debug("TT Eval      Hits = " + ttEvalHit + " " + (100.0 * ttEvalHit / ttEvalProbe) + "%");
		logger.debug("TT PV        Hits = " + ttPvHit + " " + (100.0 * ttPvHit / ttProbe) + "%");
		logger.debug("TT LB        Hits = " + ttProbe + " " + (100.0 * ttLBHit / ttProbe) + "%");
		logger.debug("TT UB        Hits = " + ttUBHit + " " + (100.0 * ttUBHit / ttProbe) + "%");
		logger.debug("Futility     Hits = " + futilityHit);
		logger.debug("Agg.Futility Hits = " + aggressiveFutilityHit);
		logger.debug("Null Move    Hits = " + nullMoveHit + " " + (100.0 * nullMoveHit / nullMoveProbe) + "%");
		logger.debug("Razoring     Hits = " + razoringHit + " " + (100.0 * razoringHit / razoringProbe) + "%");
		logger.debug("S.Extensions Hits = " + singularExtensionHit + " " + (100.0 * singularExtensionHit / singularExtensionProbe) + "%");
	}

	public void newRun() throws SearchFinishedException {
		startTime = System.currentTimeMillis();
		foundOneMove = false;
		searching = true;

		logger.debug("Board\n" + board);

		positionCounter = 0;
		pvPositionCounter = 0;
		qsPositionCounter = 0;
		bestMoveTime = 0;
		globalBestMove = 0;
		ponderMove = 0;
		pv = null;

		initialPly = board.getMoveNumber();
		thinkTo = startTime + searchParameters.calculateMoveTime(board);

		if (config.getUseBook() && config.getBook() != null && board.isUsingBook()
				&& (config.getBookKnowledge() == 100 || ((random.nextFloat() * 100) < config.getBookKnowledge()))) {
			logger.debug("Searching Move in Book");
			int bookMove = config.getBook().getMove(board);
			if (bookMove != 0) {
				globalBestMove = bookMove;
				logger.debug("Found Move in Book");
				throw new SearchFinishedException();
			} else {
				logger.debug("NOT Found Move in Book");
				board.setOutBookMove(board.getMoveNumber());
			}
		}

		depth = 1;
		score = eval(false, false);
		tt.newGeneration();
		aspWindows = config.getAspirationWindowSizes();
	}

	public void runStepped() throws SearchFinishedException {
		int failHighCount = 0;
		int failLowCount = 0;
		int initialScore = score;
		int alpha = (initialScore - aspWindows[failLowCount] > -Evaluator.VICTORY ? initialScore - aspWindows[failLowCount] : -Evaluator.VICTORY);
		int beta = (initialScore + aspWindows[failHighCount] < Evaluator.VICTORY ? initialScore + aspWindows[failHighCount] : Evaluator.VICTORY);

		// Iterate aspiration windows
		while (true) {
			aspirationWindowProbe++;

			score = search(NODE_ROOT, depth * PLY, alpha, beta, false, 0);

			// logger.debug("alpha = " + alpha + ", beta = " + beta
			// + ", score=" + score);

			if (score <= alpha) {
				failLowCount++;
				alpha = (failLowCount < aspWindows.length && (initialScore - aspWindows[failLowCount] > -Evaluator.VICTORY) ? initialScore
						- aspWindows[failLowCount] : -Evaluator.VICTORY);
			} else if (score >= beta) {
				failHighCount++;
				beta = (failHighCount < aspWindows.length && (initialScore + aspWindows[failHighCount] < Evaluator.VICTORY) ? initialScore
						+ aspWindows[failHighCount] : Evaluator.VICTORY);
			} else {
				aspirationWindowHit++;
				break;
			}
		}

		long time = System.currentTimeMillis();
		long oldBestMove = globalBestMove;
		getPv();
		if (globalBestMove != 0) {
			foundOneMove = true;
		}

		// update best move time
		if (oldBestMove != globalBestMove) {
			bestMoveTime = time - startTime;
		}
		bestMoveScore = score;

		SearchStatusInfo info = new SearchStatusInfo();
		info.setDepth(depth);
		info.setTime(time - startTime);
		info.setPv(pv);
		info.setScore(score);
		info.setNodes(positionCounter + pvPositionCounter + qsPositionCounter);
		info.setNps((int) (1000 * (positionCounter + pvPositionCounter + qsPositionCounter) / ((time - startTime + 1))));
		logger.debug(info.toString());

		if (observer != null) {
			observer.info(info);
		}

		// if mate found exit
		if ((score < -Evaluator.VICTORY + 1000) || (score > Evaluator.VICTORY - 1000)) {
			throw new SearchFinishedException();
		}

		depth++;
		if (depth == MAX_DEPTH) {
			throw new SearchFinishedException();
		}
	}

	public void finishRun() {
		// puts the board in the initial position
		board.undoMove(initialPly);
		searchStats();
		searching = false;
		if (observer != null) {
			observer.bestMove(globalBestMove, ponderMove);
		}
	}

	public void run() {
		try {
			newRun();
			while (true) {
				runStepped();
			}
		} catch (SearchFinishedException ignored) {
		}
		finishRun();
	}

	/**
	 * Gets the principal variation and the best move from the transposition
	 * table
	 */
	private void getPv() {
		StringBuilder sb = new StringBuilder();
		List<Long> keys = new ArrayList<Long>(); // To not repeat keys
		int i = 0;
		while (i < 256) {
			if (tt.search(board, false)) {
				if (keys.contains(board.getKey())) {
					break;
				}
				keys.add(board.getKey());
				if (tt.getBestMove() == 0) {
					break;
				}
				if (i == 0) {
					globalBestMove = tt.getBestMove();
				} else if (i == 1) {
					ponderMove = tt.getBestMove();
				}
				sb.append(Move.toString(tt.getBestMove()));
				sb.append(" ");
				i++;
				board.doMove(tt.getBestMove(), false);
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
		thinkTo = 0;
	}

	/**
	 * Is better to end before Not necessary to change sign after Takes into
	 * account the contempt factor
	 */
	public int evaluateEndgame() {
		if (board.getCheck()) {
			return valueMatedIn(board.getMoveNumber() - initialPly);
		} else {
			return evaluateDraw();
		}
	}

	public int evaluateDraw() {
		return ((board.getMoveNumber() - initialPly) & 1) == 0 ? -config.getContemptFactor() : config.getContemptFactor();
	}

	private int valueMatedIn(int depth) {
		return -Evaluator.VICTORY + depth;
	}

	private int valueMateIn(int depth) {
		return Evaluator.VICTORY - depth;
	}

	boolean valueIsMate(int value) {
		return value <= valueMatedIn(MAX_DEPTH) || value >= valueMateIn(MAX_DEPTH);
	}

	public TranspositionTable getTT() {
		return tt;
	}

	public SearchParameters getSearchParameters() {
		return searchParameters;
	}

	public void setSearchParameters(SearchParameters searchParameters) {
		this.searchParameters = searchParameters;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isSearching() {
		return searching;
	}
}