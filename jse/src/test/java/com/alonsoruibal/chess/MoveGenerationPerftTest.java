package com.alonsoruibal.chess;

import com.alonsoruibal.chess.movegen.LegalMoveGenerator;
import com.alonsoruibal.chess.movegen.MoveGenerator;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Do some move generation tests from http://chessprogramming.wikispaces.com/Perft+Results
 * Takes a looooong time
 */
public class MoveGenerationPerftTest {
	private static final int DEPTH = 7;

	MoveGenerator legalMovegen = new LegalMoveGenerator();
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
			System.out.println("Moves: " + moveCount[i] + " Captures="
					+ captures[i] + " E.P.=" + passantCaptures[i] + " Castles="
					+ castles[i] + " Promotions=" + promotions[i] + " Checks="
					+ checks[i] + " CheckMates=" + checkMates[i]);
		}
	}

	@Test
	@Category(SlowTest.class)
	public void testInitialPosition() {
		reset();
		System.out.println("TEST INITIAL POSITION");
		board.startPosition();
		recursive(0, 6);
		print(6);
		assertEquals(moveCount[5], 119060324);
		assertEquals(captures[5], 2812008);
		assertEquals(passantCaptures[5], 5248);
		assertEquals(castles[5], 0);
		assertEquals(promotions[5], 0);
		assertEquals(checks[5], 809099);
		assertEquals(checkMates[5], 10828);
	}

	@Test
	@Category(SlowTest.class)
	public void testPosition2() {
		reset();
		System.out.println("TEST POSITION 2");
		board.setFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		recursive(0, 5);
		print(5);
		assertEquals(moveCount[4], 193690690);
		assertEquals(captures[4], 35043416);
		assertEquals(passantCaptures[4], 73365);
		assertEquals(castles[4], 4993637);
		assertEquals(promotions[4], 8392);
		assertEquals(checks[4], 3309887);
		assertEquals(checkMates[4], 30171);
	}

	@Test
	@Category(SlowTest.class)
	public void testPosition3() {
		reset();
		System.out.println("TEST POSITION 3");
		board.setFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
		recursive(0, 7);
		print(7);

		assertEquals(moveCount[6], 178633661);
		assertEquals(captures[6], 14519036);
		assertEquals(passantCaptures[6], 294874);
		assertEquals(castles[6], 0);
		assertEquals(promotions[6], 140024);
		assertEquals(checks[6], 12797406);
		assertEquals(checkMates[6], 87);
	}

	@Test
	@Category(SlowTest.class)
	public void testPosition4() {
		reset();
		System.out.println("TEST POSITION 4");
		board.setFen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
		recursive(0, 6);
		print(6);

		assertEquals(moveCount[5], 706045033);
		assertEquals(captures[5], 210369132);
		assertEquals(passantCaptures[5], 212);
		assertEquals(castles[5], 10882006);
		assertEquals(promotions[5], 81102984);
		assertEquals(checks[5], 26973664);
		assertEquals(checkMates[5], 81076);
	}

	@Test
	@Category(SlowTest.class)
	public void testPosition5() {
		reset();
		System.out.println("TEST POSITION 5");
		board.setFen("rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6");
		recursive(0, 3);
		print(3);

		assertEquals(moveCount[0], 42);
		assertEquals(moveCount[1], 1352);
		assertEquals(moveCount[2], 53392);
	}

	private void recursive(int depth, int depthRemaining) {
		int moves[] = new int[256];
		int index = legalMovegen.generateMoves(board, moves, 0);
		for (int i = 0; i < index; i++) {
			int move = moves[i];
			board.doMove(move);
			if (depthRemaining > 0) {
				moveCount[depth]++;
				if ((moveCount[depth] % 100000) == 0) {
					System.out.println("movecount[" + depth + "]=" + moveCount[depth]);
				}
				if (Move.isCapture(move)) {
					captures[depth]++;
				}
				if (Move.getMoveType(move) == Move.TYPE_PASSANT) {
					passantCaptures[depth]++;
				}
				if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING
						|| Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING) {
					castles[depth]++;
				}
				if (Move.isPromotion(move)) {
					promotions[depth]++;
				}
				if (board.getCheck()) {
					checks[depth]++;
				}
				if (board.isMate()) {
					checkMates[depth]++;
				}

				recursive(depth + 1, depthRemaining - 1);
			}
			board.undoMove();
		}
	}

	@Test
	public void testWeirdError() {
		int legalMoves[] = new int[256];
		Board board = new Board();
		int legalMoveCount;

		// board.setFen("rnbqk1nr/pppp1ppp/8/4p3/1b1P4/7N/PPP1PPPP/RNBQKB1R w QKqk - 2 0");
		// legalMoveCount = board.getLegalMoves(legalMoves);
		board.setFen("r1bqkbnr/pppp1ppp/n7/4p3/3P4/7N/PPP1PPPP/RNBQKB1R w QKqk - 2 0");
		legalMoveCount = board.getLegalMoves(legalMoves);
		assertEquals(legalMoveCount, 29);
	}
}