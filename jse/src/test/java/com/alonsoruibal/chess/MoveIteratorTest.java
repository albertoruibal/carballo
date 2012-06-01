package com.alonsoruibal.chess;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.movegen.MagicMoveGenerator;
import com.alonsoruibal.chess.movegen.MoveGenerator;
import com.alonsoruibal.chess.movesort.MoveIterator;
import com.alonsoruibal.chess.movesort.SortInfo;

/**
 * Helps searching for bottlenecks
 * 
 * @author rui
 */
public class MoveIteratorTest extends TestCase {
	private static final Logger logger = Logger.getLogger("MoveIteratorTest");

	private static final int DEPTH = 7;

	Board board = new Board();
	int moveCount[];
	int captures[];
	int passantCaptures[];
	int castles[];
	int promotions[];
	int checks[];
	int checkMates[];

	private void reset() {
		moveCount = new int[DEPTH];
		captures = new int[DEPTH];
		passantCaptures = new int[DEPTH];
		castles = new int[DEPTH];
		promotions = new int[DEPTH];
		checks = new int[DEPTH];
		checkMates = new int[DEPTH];
	}

	private void print(int depth) {
		for (int i = 0; i < depth; i++) {
			logger.debug("Moves: " + moveCount[i] + " Captures=" + captures[i]
					+ " E.P.=" + passantCaptures[i] + " Castles=" + castles[i]
					+ " Promotions=" + promotions[i] + " Checks=" + checks[i]
					+ " CheckMates=" + checkMates[i]);
		}
	}

	public void test1() {
		reset();
		System.out.println("TEST 1");
		board.startPosition();
		recursive(0, 6);
		print(6);
	}

	public void test2() {
		reset();
		System.out.println("TEST 2");
		board.setFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		recursive(0, 5);
		print(5);
	}

	public void test3() {
		reset();
		System.out.println("TEST 3");
		board.setFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
		recursive(0, 7);
		print(7);
	}

	private void recursive(int depth, int depthRemaining) {
		MoveGenerator moveGenerator = new MagicMoveGenerator();
		int moves[] = new int[256];
		//logger.debug("\n"+board);
		int moveSize = moveGenerator.generateMoves(board, moves, 0);
		List<Integer> moveList = new ArrayList<Integer>();
		for (int i = 0; i < moveSize; i++) {
			moveList.add(moves[i]);
		}

		// Move.printMoves(moves, index);
		MoveIterator moveIterator = new MoveIterator(board, new SortInfo(),	depth);
		moveIterator.genMoves(0);
		int move = 0;
		while ((move = moveIterator.next()) != 0) {
			if (!moveList.contains(move)) {
				logger.error("\n" + board);
				logger.error("Move not found: " + Move.toStringExt(move));
			} else {
				moveList.remove((Integer) move);
			}
			
			// logger.debug(depth + "->" + Move.toStringExt(move));
			if (board.doMove(move)) {


				if (depthRemaining > 0) {
					moveCount[depth]++;
					if (Move.isCapture(move))
						captures[depth]++;
					if (Move.getMoveType(move) == Move.TYPE_PASSANT)
						passantCaptures[depth]++;
					if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING
							|| Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING)
						castles[depth]++;
					if (Move.isPromotion(move))
						promotions[depth]++;
					if (board.getCheck()) {
						checks[depth]++;
						// logger.debug("\n"+board);
					}
					if (board.isMate()) {
						checkMates[depth]++;
					}

					recursive(depth + 1, depthRemaining - 1);
				}
				board.undoMove();
			}
		}
		if (moveList.size() > 0) {
			logger.debug("\n" + board);
			while (moveList.size() > 0) {
				logger.error("Move not generated: "
						+ Move.toStringExt(moveList.get(0)));
				moveList.remove(0);
			}
		}
	}

}