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
	public static final int VALUE_IS_MATE = Evaluator.VICTORY - MAX_DEPTH;
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

	// Think limits
	private long thinkToTime = 0;
	private int thinkToNodes = 0;
	private int thinkToDepth = 0;

	private Board board;
	private SearchObserver observer;
	private Evaluator evaluator;
	private TranspositionTable tt;
	private SortInfo sortInfo;
	private MoveIterator[] moveIterators;

	private int bestMoveScore;
	private int globalBestMove, ponderMove;
	private String pv;

	private int initialPly; // Initial Ply for search
	private int depth;
	private int selDepth;
	private int rootScore;
	private int[] aspWindows;

	long startTime;

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

		pvReductionMatrix = new int[64][64];
		nonPvReductionMatrix = new int[64][64];
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

		logger.debug("Creating TT");
		tt = new TranspositionTable(config.getTranspositionTableSize());

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

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * Decides when we are going to allow null move Don't do null move in king and pawn endings
	 */
	private boolean boardAllowsNullMove() {
		return (board.getMines() & (board.knights | board.bishops | board.rooks | board.queens)) != 0;
	}

	/**
	 * Calculates the extension of a move in the actual position (with the move done)
	 */
	private int extensions(int move, boolean mateThreat, int moveSee) {
		int ext = 0;

		if (board.getCheck()
				&& moveSee >= 0) {
			ext += config.getExtensionsCheck();
			if (ext >= PLY) {
				return PLY;
			}
		}
		if (Move.getPieceMoved(move) == Move.PAWN) {
			if (Move.isPawnPush678(move)) {
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
		if (board.getLastMoveIsRecapture()) {
			int firstCapturedPieceValue = pieceValue(board.getCapturedPiece(2));
			if (moveSee >= firstCapturedPieceValue - 50) {
				ext += config.getExtensionsRecapture();
			}
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
	public int eval(boolean foundTT) {
		ttEvalProbe++;

		int eval;
		if (foundTT) {
			ttEvalHit++;
			return tt.getEval();
		}
		eval = evaluator.evaluate(board);
		if (!board.getTurn()) {
			eval = -eval;
		}
		tt.set(board, TranspositionTable.TYPE_EVAL, 0, 0, (byte) 0, eval, false);
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

	private int lastCapturedPieceValue(Board board) {
		return pieceValue(board.getLastCapturedPiece());
	}

	private int pieceValue(char piece) {
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
		return 0;
	}

	public int quiescentSearch(int qsdepth, int alpha, int beta) throws SearchFinishedException {
		if (foundOneMove && (System.currentTimeMillis() > thinkToTime || (positionCounter + pvPositionCounter + qsPositionCounter) > thinkToNodes)) {
			throw new SearchFinishedException();
		}
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
		int ttMove = 0;

		ttProbe++;
		boolean foundTT = tt.search(board, distanceToInitialPly, false);
		if (foundTT) {
			if (!isPv && canUseTT(0, alpha, beta)) {
				return tt.getScore();
			}
			ttMove = tt.getBestMove();
		}

		int bestScore = alpha;
		int bestMove = 0;
		int staticEval = 0;
		int eval = -Evaluator.VICTORY;

		// Do not allow stand pat when in check
		if (!board.getCheck()) {
			staticEval = eval(foundTT);
			eval = refineEval(foundTT, staticEval);

			if (eval > bestScore) {
				bestScore = eval;
			}
			// Evaluation functions increase alpha and can originate beta cutoffs
			if (bestScore >= beta) {
				if (!foundTT) {
					tt.set(board, TranspositionTable.TYPE_FAIL_HIGH, 0, bestScore, (byte) 0, staticEval, false);
				}
				return bestScore;
			}
		}

		// If we have more depths than possible...
		if (distanceToInitialPly >= MAX_DEPTH) {
//			System.out.println("Quiescence exceeds depth qsdepth=" + qsdepth);
//			System.out.println(board.toString());
//			for (int i = 0; i < board.getMoveNumber(); i++) {
//				System.out.print(Move.toStringExt(board.moveHistory[i]));
//				System.out.print(" ");
//			}
//			System.out.println();
			if (board.getCheck()) {
				return evaluateDraw(distanceToInitialPly);
			} else {
				return eval;
			}
		}

		boolean validOperations = false;
		boolean checkEvasion = board.getCheck();
		// Generate checks for PV on PLY 0
		boolean generateChecks = isPv && (qsdepth == 0);

		MoveIterator moveIterator = moveIterators[distanceToInitialPly];
		moveIterator.genMoves(ttMove, true, generateChecks);
		int move;

		while ((move = moveIterator.next()) != 0) {
			if (board.doMove(move, false)) {
				validOperations = true;

				if (!checkEvasion //
						&& !(board.getCheck() && generateChecks) // Necessary because TT move can be a no promotion or capture
						&& moveIterator.getPhase() > MoveIterator.PHASE_GOOD_CAPTURES_AND_PROMOS) {
					board.undoMove();
					continue;
				}

				// Futility pruning
				if (!checkEvasion //
						&& !board.getCheck() //
						&& !isPv //
						&& move != ttMove //
						&& !Move.isPawnPush678(move) // TODO test if necessary
						&& Math.abs(eval) < Evaluator.KNOWN_WIN) {
					int futilityValue = eval + lastCapturedPieceValue(board) + config.getFutilityMarginQS();
					if (futilityValue < beta) {
						if (futilityValue > bestScore) {
							bestScore = futilityValue;
						}
						board.undoMove();
						continue;
					}
				}

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
		}

		if (board.getCheck() && !validOperations) {
			return valueMatedIn(distanceToInitialPly);
		}
		tt.save(board, distanceToInitialPly, 0, bestMove, bestScore, alpha, beta, staticEval, false);

		return bestScore;
	}

	/**
	 * Search Root, PV and null window
	 */
	public int search(int nodeType, int depthRemaining, int alpha, int beta, boolean allowNullMove, int excludedMove) throws SearchFinishedException {
		if (foundOneMove && (System.currentTimeMillis() > thinkToTime || (positionCounter + pvPositionCounter + qsPositionCounter) > thinkToNodes)) {
			throw new SearchFinishedException();
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

		int ttMove = 0;
		int ttScore = 0;
		int ttNodeType = 0;
		int ttDepthAnalyzed = 0;
		int score = 0;

		ttProbe++;
		boolean foundTT = tt.search(board, distanceToInitialPly, excludedMove != 0);
		if (foundTT) {
			if (nodeType != NODE_ROOT //
					&& canUseTT(depthRemaining, alpha, beta)) {
				return tt.getScore();
			}
			ttMove = tt.getBestMove();
			ttScore = tt.getScore();
			ttNodeType = tt.getNodeType();
			ttDepthAnalyzed = tt.getDepthAnalyzed();
		}

		if (depthRemaining < PLY || distanceToInitialPly >= MAX_DEPTH - 1) {
			return quiescentSearch(0, alpha, beta);
		}

		boolean mateThreat = false;
		boolean futilityPrune = false;
		int futilityValue = -Evaluator.VICTORY;
		int staticEval = -Evaluator.VICTORY;

		if (!board.getCheck()) {
			// Do a static eval, in case of exclusion and not found in the TT, search again with the normal key
			boolean evalTT = excludedMove == 0 || foundTT ? foundTT : tt.search(board, distanceToInitialPly, false);
			staticEval = eval(evalTT);
			int eval = refineEval(foundTT, staticEval);

			// Hyatt's Razoring http://chessprogramming.wikispaces.com/Razoring
			if (nodeType == NODE_NULL //
					&& config.getRazoring() //
					&& ttMove == 0 //
					&& allowNullMove // Not when last was a null move
					&& depthRemaining < RAZOR_DEPTH //
					&& Math.abs(beta) < VALUE_IS_MATE //
					&& eval < beta - config.getRazoringMargin() //
					&& (board.pawns & ((board.whites & BitboardUtils.b2_u) | (board.blacks & BitboardUtils.b2_d))) == 0) { // No pawns on 7TH
				razoringProbe++;

				int rbeta = beta - config.getRazoringMargin();
				int v = quiescentSearch(0, rbeta - 1, rbeta);
				if (v < rbeta) {
					razoringHit++;
					return v;
				}
			}

			// Static null move pruning or futility pruning in parent node
			if (nodeType == NODE_NULL //
					&& config.getStaticNullMove() //
					&& allowNullMove //
					&& depthRemaining < RAZOR_DEPTH //
					&& Math.abs(beta) < VALUE_IS_MATE //
					&& Math.abs(eval) < Evaluator.KNOWN_WIN //
					&& eval - config.getFutilityMargin() >= beta //
					&& boardAllowsNullMove()) {
				return eval - config.getFutilityMargin();
			}

			// Null move pruning and mate threat detection
			if (nodeType == NODE_NULL //
					&& config.getNullMove() //
					&& allowNullMove //
					&& depthRemaining > 3 * PLY //
					&& Math.abs(beta) < VALUE_IS_MATE //
					&& eval > beta - (depthRemaining >= 4 * PLY ? config.getNullMoveMargin() : 0) //
					&& boardAllowsNullMove()) {

				nullMoveProbe++;
				board.doMove(0, false);
				int R = 3 * PLY + (depthRemaining >= 5 * PLY ? depthRemaining / (4 * PLY) : 0);
				if (eval - beta > CompleteEvaluator.PAWN) {
					R++; // TODO TEST adding PLY
				}
				score = -search(NODE_NULL, depthRemaining - R, -beta, -beta + 1, false, 0);
				board.undoMove();
				if (score >= beta) {
					if (score >= VALUE_IS_MATE) {
						score = beta;
					}

					// Verification search on initial depths
					if (depthRemaining < 6 * PLY //
							|| search(NODE_NULL, depthRemaining - 5 * PLY, beta - 1, beta, false, 0) >= beta) {
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
			if (config.getIid() //
					&& ttMove == 0 //
					&& depthRemaining >= iidDepth[nodeType] //
					&& allowNullMove //
					&& (nodeType != NODE_NULL || staticEval + config.getIidMargin() > beta) //
					&& excludedMove == 0) {
				int d = (nodeType == NODE_PV ? depthRemaining - 2 * PLY : depthRemaining >> 1);
				search(nodeType, d, alpha, beta, true, 0); // TODO Allow null move ?
				if (tt.search(board, distanceToInitialPly, false)) {
					ttMove = tt.getBestMove();
				}
			}

			// Futility pruning
			if (nodeType == NODE_NULL) {
				if (depthRemaining <= PLY) { // at frontier nodes
					if (config.getFutility()) {
						futilityValue = staticEval + config.getFutilityMargin();
						if (futilityValue < beta) {
							futilityHit++;
							futilityPrune = true;
						}
					}
				} else if (depthRemaining <= 2 * PLY) { // at pre-frontier nodes
					if (config.getAggressiveFutility()) {
						futilityValue = staticEval + config.getAggressiveFutilityMargin();
						if (futilityValue < beta) {
							aggressiveFutilityHit++;
							futilityPrune = true;
						}
					}
				}
			}
		}

		MoveIterator moveIterator = moveIterators[distanceToInitialPly];
		moveIterator.genMoves(ttMove);

		int movesDone = 0;
		boolean validOperations = false;
		boolean checkEvasion = board.getCheck();
		int bestScore = -Evaluator.VICTORY;
		int move, bestMove = 0;

		while ((move = moveIterator.next()) != 0) {
			// Operations are pseudo-legal, doMove checks if they lead to a valid state
			if (board.doMove(move, false)) {
				validOperations = true;

				if (move == excludedMove) {
					board.undoMove();
					continue;
				}

				int extension = extensions(move, mateThreat, moveIterator.getLastMoveSee());

				// Check singular move extension
				// It also detects singular replies
				if (nodeType != NODE_ROOT //
						&& move == ttMove //
						&& extension < PLY //
						&& excludedMove == 0 //
						&& config.getExtensionsSingular() > 0 //
						&& depthRemaining >= singularMoveDepth[nodeType] //
						&& ttNodeType == TranspositionTable.TYPE_FAIL_HIGH //
						&& ttDepthAnalyzed >= depthRemaining - 3 * PLY //
						&& Math.abs(ttScore) < Evaluator.KNOWN_WIN) {

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
						|| checkEvasion //
						|| board.getCheck() //
						|| Move.isCapture(move) // Include ALL captures
						|| Move.isPawnPush678(move) // Includes promotions
						|| Move.isCastling(move)
						|| move == ttMove //
						|| sortInfo.isKiller(move, distanceToInitialPly + 1);

				if (futilityPrune //
						&& bestScore > -Evaluator.KNOWN_WIN //
						&& !importantMove) {
					board.undoMove();
					if (futilityValue <= alpha) {
						if (futilityValue > bestScore) {
							bestScore = futilityValue;
						}
					}
					continue;
				}

				movesDone++;

				int lowBound = (alpha > bestScore ? alpha : bestScore);
				if ((nodeType == NODE_PV || nodeType == NODE_ROOT) && movesDone == 1) {
					// PV move not null searched
					score = -search(NODE_PV, depthRemaining + extension - PLY, -beta, -lowBound, true, 0);
				} else {
					// Try searching null window
					boolean doFullSearch = true;

					// Late move reductions (LMR)
					int reduction = 0;
					if (config.getLmr() //
							&& depthRemaining >= LMR_DEPTHS_NOT_REDUCED //
							&& !importantMove) {
						reduction += getReduction(nodeType, depthRemaining, movesDone);
					}

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

					if (nodeType == NODE_ROOT) {
						long time = System.currentTimeMillis();

						globalBestMove = move;
						bestMoveScore = score;
						foundOneMove = true;

						getPv(move);

						SearchStatusInfo info = new SearchStatusInfo();
						info.setDepth(depth);
						info.setSelDepth(selDepth);
						info.setTime(time - startTime);
						info.setPv(pv);
						info.setScore(score);
						info.setNodes(positionCounter + pvPositionCounter + qsPositionCounter);
						info.setNps((int) (1000 * (positionCounter + pvPositionCounter + qsPositionCounter) / ((time - startTime + 1))));

						if (observer != null) {
							observer.info(info);
						} else {
							logger.debug(info.toString());
						}
					}
				}

				// alpha/beta cut (fail high)
				if (score >= beta) {
					break;
				}
			}
		}

		// Checkmate or stalemate
		if (excludedMove == 0 && !validOperations) {
			bestScore = evaluateEndgame(distanceToInitialPly);
		}
		// Fix score for excluded moves
		if (bestScore == -Evaluator.VICTORY) {
			bestScore = valueMatedIn(distanceToInitialPly);
		}

		// Tells MoveSorter the move score
		if (bestScore >= beta) {
			if (excludedMove == 0 && validOperations) {
				// TODO test use absolute move number
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
		tt.save(board, distanceToInitialPly, (byte) depthRemaining, bestMove, bestScore, alpha, beta, staticEval, excludedMove != 0);

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
				+ String.format("%.2f", 100.0 * pvPositionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
		logger.debug("Positions QS      = " + qsPositionCounter + " " //
				+ String.format("%.2f", 100.0 * qsPositionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
		logger.debug("Positions Null    = " + positionCounter + " " //
				+ String.format("%.2f", 100.0 * positionCounter / (positionCounter + pvPositionCounter + qsPositionCounter)) + "%");
		logger.debug("PV Cut            = " + pvCutNodes + " " + String.format("%.2f", 100.0 * pvCutNodes / (pvCutNodes + pvAllNodes + 1)) + "%");
		logger.debug("PV All            = " + pvAllNodes);
		logger.debug("Null Cut          = " + nullCutNodes + " " + String.format("%.2f", 100.0 * nullCutNodes / (nullCutNodes + nullAllNodes + 1)) + "%");
		logger.debug("Null All          = " + nullAllNodes);
		logger.debug("Asp Win      Hits = " + String.format("%.2f", 100.0 * aspirationWindowHit / aspirationWindowProbe) + "%");
		logger.debug("TT Eval      Hits = " + ttEvalHit + " " + String.format("%.2f", 100.0 * ttEvalHit / ttEvalProbe) + "%");
		logger.debug("TT PV        Hits = " + ttPvHit + " " + String.format("%.2f", 100.0 * ttPvHit / ttProbe) + "%");
		logger.debug("TT LB        Hits = " + ttProbe + " " + String.format("%.2f", 100.0 * ttLBHit / ttProbe) + "%");
		logger.debug("TT UB        Hits = " + ttUBHit + " " + String.format("%.2f", 100.0 * ttUBHit / ttProbe) + "%");
		logger.debug("Futility     Hits = " + futilityHit);
		logger.debug("Agg.Futility Hits = " + aggressiveFutilityHit);
		logger.debug("Null Move    Hits = " + nullMoveHit + " " + String.format("%.2f", 100.0 * nullMoveHit / nullMoveProbe) + "%");
		logger.debug("Razoring     Hits = " + razoringHit + " " + String.format("%.2f", 100.0 * razoringHit / razoringProbe) + "%");
		logger.debug("S.Extensions Hits = " + singularExtensionHit + " " + String.format("%.2f", 100.0 * singularExtensionHit / singularExtensionProbe) + "%");
	}

	public void newRun() throws SearchFinishedException {
		startTime = System.currentTimeMillis();
		foundOneMove = false;
		searching = true;

		logger.debug("Board\n" + board);

		positionCounter = 0;
		pvPositionCounter = 0;
		qsPositionCounter = 0;
		globalBestMove = 0;
		ponderMove = 0;
		pv = null;

		initialPly = board.getMoveNumber();

		thinkToNodes = searchParameters.getNodes();
		thinkToDepth = searchParameters.getDepth();
		thinkToTime = searchParameters.calculateMoveTime(board, startTime);

		if (config.getUseBook() && config.getBook() != null && board.isUsingBook()
				&& (config.getBookKnowledge() == 100 || ((random.nextFloat() * 100) < config.getBookKnowledge()))) {
			logger.debug("Searching move in book");
			int bookMove = config.getBook().getMove(board);
			if (bookMove != 0) {
				globalBestMove = bookMove;
				logger.debug("Move found in book");
				throw new SearchFinishedException();
			} else {
				logger.debug("Move NOT found in book");
				board.setOutBookMove(board.getMoveNumber());
			}
		}

		depth = 1;
		rootScore = eval(tt.search(board, 0, false));
		tt.newGeneration();
		aspWindows = config.getAspirationWindowSizes();
	}

	public void runStepped() throws SearchFinishedException {
		selDepth = 0;
		int failHighCount = 0;
		int failLowCount = 0;
		int initialScore = rootScore;
		int alpha = (initialScore - aspWindows[failLowCount] > -Evaluator.VICTORY ? initialScore - aspWindows[failLowCount] : -Evaluator.VICTORY);
		int beta = (initialScore + aspWindows[failHighCount] < Evaluator.VICTORY ? initialScore + aspWindows[failHighCount] : Evaluator.VICTORY);

		// Iterate aspiration windows
		while (true) {
			aspirationWindowProbe++;
			rootScore = search(NODE_ROOT, depth * PLY, alpha, beta, false, 0);

			// logger.debug("alpha = " + alpha + ", beta = " + beta + ", score=" + score);

			if (rootScore <= alpha) {
				failLowCount++;
				alpha = (failLowCount < aspWindows.length && (initialScore - aspWindows[failLowCount] > -Evaluator.VICTORY) ? initialScore
						- aspWindows[failLowCount] : -Evaluator.VICTORY);
			} else if (rootScore >= beta) {
				failHighCount++;
				beta = (failHighCount < aspWindows.length && (initialScore + aspWindows[failHighCount] < Evaluator.VICTORY) ? initialScore
						+ aspWindows[failHighCount] : Evaluator.VICTORY);
			} else {
				aspirationWindowHit++;
				break;
			}
		}

		// If mate found and time is not infinite, exit
		if ((thinkToTime != Long.MAX_VALUE) && ((rootScore <= -VALUE_IS_MATE) || (rootScore > VALUE_IS_MATE))) {
			throw new SearchFinishedException();
		}

		depth++;
		if (depth == MAX_DEPTH || depth > thinkToDepth) {
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
	 * Gets the principal variation from the transposition table
	 */
	private void getPv(int firstMove) {
		StringBuilder sb = new StringBuilder();
		List<Long> keys = new ArrayList<Long>(); // To not repeat keys
		sb.append(Move.toSan(board, firstMove));
		board.doMove(firstMove);
		if (board.getCheck()) {
			sb.append("+");
		}

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
				sb.append(Move.toSan(board, tt.getBestMove()));
				board.doMove(tt.getBestMove(), false);
				i++;
				if (board.isMate()) {
					sb.append("#");
					break;
				}
				if (board.getCheck()) {
					sb.append("+");
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
		return -Evaluator.VICTORY + distanceToInitialPly;
	}

	private int valueMateIn(int distanceToInitialPly) {
		return Evaluator.VICTORY - distanceToInitialPly;
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