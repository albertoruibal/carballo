package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.movesort.MoveIterator;
import com.alonsoruibal.chess.movesort.SortInfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoveIteratorTest {

	private void testPositionCountingMoves(String fen, int generateMoves, String ttMoveString, int totalMoves, int totalCaptures, int totalEnPassant, int totalChecks) {
		Board b = new Board();
		b.setFen(fen);
		System.out.println(b.toString());
		MoveIterator moveIterator = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		int ttMove = ttMoveString == null ? Move.NONE : Move.getFromString(b, ttMoveString, true);
		moveIterator.genMoves(ttMove, generateMoves);
		int move;
		int moves = 0;
		int captures = 0;
		int enPassant = 0;
		int checks = 0;
		while ((move = moveIterator.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move) + " SEE=" + moveIterator.getLastMoveSee());
			moves++;
			if (Move.isCapture(move)) {
				captures++;
			}
			if (Move.getMoveType(move) == Move.TYPE_PASSANT) {
				enPassant++;
			}
			if (Move.isCheck(move)) {
				checks++;
			}
		}
		assertTrue(totalMoves + " moves", moves == totalMoves);
		assertTrue(totalCaptures + " captures", captures == totalCaptures);
		assertTrue(totalEnPassant + " en-passant", enPassant == totalEnPassant);
		assertTrue(totalChecks + " checks", checks == totalChecks);
	}

	@Test
	public void testCheckAfterPromotion() {
		Board b = new Board();
		b.setFen("7k/P7/8/8/8/8/8/7K w - - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		assertTrue("First move must be check", Move.isCheck(lmi.next()));
	}

	@Test
	public void testCheckAfterPromotionKingBehind() {
		Board b = new Board();
		b.setFen("8/P7/8/k7/8/8/8/7K w - - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		assertTrue("First move must be check", Move.isCheck(lmi.next()));
	}

	@Test
	public void castlingGivesCheck() {
		Board b = new Board();
		b.setFen("5k2/8/8/8/8/8/8/4K2R w K - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		int move;
		int castling = Move.NONE;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));

			if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING) {
				castling = move;
			}
		}
		assertTrue("Castling must give check", Move.isCheck(castling));
	}

	@Test
	public void castlingGivesCheck2() {
		Board b = new Board();
		b.setFen("K3k2r/8/8/8/8/8/8/8 b k - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		int move;
		int castling = Move.NONE;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));

			if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING) {
				castling = move;
			}
		}
		assertTrue("Castling must give check", Move.isCheck(castling));
	}

	@Test
	public void cannotCastleAttackedSquare1() {
		Board b = new Board();
		b.setFen("4k2r/8/8/8/8/8/K7/5R2 b k - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		int move;
		int castling = Move.NONE;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));

			if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING) {
				castling = move;
			}
		}
		assertTrue("Must not allow castling", castling == Move.NONE);
	}

	@Test
	public void cannotCastleAttackedSquare2() {
		Board b = new Board();
		b.setFen("4k2r/7B/8/8/8/8/K7/8 b k - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		int move;
		int castling = Move.NONE;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));

			if (Move.getMoveType(move) == Move.TYPE_KINGSIDE_CASTLING) {
				castling = move;
			}
		}
		assertTrue("Must not allow castling", castling == Move.NONE);
	}

	@Test
	public void canCastleLongAlthoughThereIsAnAttackedSquareNear() {
		Board b = new Board();
		b.setFen("7k/8/8/8/8/8/p5p1/R3K3 w Q - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		int move;
		int castling = Move.NONE;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));

			if (Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING) {
				castling = move;
			}
		}
		assertTrue("Must allow castling because the king does not crosses an attacked square", castling != Move.NONE);
	}

	@Test
	public void longCastlingGivesCheck2() {
		Board b = new Board();
		b.setFen("8/8/8/8/8/8/8/R3K2k w Q - 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		int move;
		int castling = Move.NONE;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));

			if (Move.getMoveType(move) == Move.TYPE_QUEENSIDE_CASTLING) {
				castling = move;
			}
		}
		assertTrue("Long castling must give check", Move.isCheck(castling));
	}

	@Test
	public void enPassantGivesCheck() {
		Board b = new Board();
		b.setFen("7k/8/8/1b6/1pP5/8/8/5K2 b - c3 0 1");
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(Move.NONE);
		int move;
		int enPassant = Move.NONE;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));

			if (Move.getMoveType(move) == Move.TYPE_PASSANT) {
				enPassant = move;
			}
		}
		assertTrue("En passant must give check", Move.isCheck(enPassant));
	}

	@Test
	public void enPassantGivesCheck2PiecesXray() {
		testPositionCountingMoves("8/8/8/R2Pp2k/8/8/8/7K w - e6 0 1", MoveIterator.GENERATE_ALL, null, 14, 1, 1, 1);
	}

	@Test
	public void cannotCaptureEnPassantCheck2PiecesXray() {
		testPositionCountingMoves("8/8/8/r2Pp2K/8/8/8/7k w - e6 0 1", MoveIterator.GENERATE_ALL, null, 6, 0, 0, 0);
	}

	@Test
	public void promoteCapturing() {
		testPositionCountingMoves("1n5k/2P5/8/8/8/8/8/7K w - - 0 1", MoveIterator.GENERATE_ALL, null, 11, 4, 0, 4);
	}

	@Test
	public void checkEvasions() {
		testPositionCountingMoves("4k3/8/8/8/1b1Q4/2b5/1P6/4K3 w - - 0 1", MoveIterator.GENERATE_ALL, null, 7, 2, 0, 0);
	}

	@Test
	public void avoidCheckPromoting() {
		testPositionCountingMoves("K6r/1P6/8/8/8/8/8/7k w - - 0 1", MoveIterator.GENERATE_ALL, null, 5, 0, 0, 0);
	}

	@Test
	public void checkEvasionCapturingEnPassant() {
		testPositionCountingMoves("8/8/8/3k4/1pP5/8/8/5K2 b - c3 0 1", MoveIterator.GENERATE_ALL, null, 9, 2, 1, 0);
	}

	@Test
	public void checkEvasionInterposeCapturingEnPassant() {
		testPositionCountingMoves("8/8/8/8/1pPk4/8/8/B4K2 b - c3 0 1", MoveIterator.GENERATE_ALL, null, 6, 2, 1, 0);
	}

	@Test
	public void captureCheckingPieceWithKing() {
		testPositionCountingMoves("rq2r1k1/5Qp1/p4p2/4bNP1/1p2P2P/8/PP4K1/5R1R b - - 1 2", MoveIterator.GENERATE_ALL, null, 3, 1, 0, 0);
	}

	@Test
	public void captureCheckingPieceWithKingAndTwoPiecesGivingCheck() {
		testPositionCountingMoves("k4r2/R5pb/1pQp1n1p/3P4/5p1P/3P2P1/r1q1R2K/8 b - - 1 1", MoveIterator.GENERATE_ALL, null, 2, 1, 0, 0);
	}

	@Test
	public void evadeCheckMoveKingCapturing() {
		testPositionCountingMoves("r5r1/p1q2pBk/1p1R2p1/3pP3/6bQ/2p5/P1P1NPPP/6K1 b - - 1 1", MoveIterator.GENERATE_ALL, null, 2, 1, 0, 0);
	}

	@Test
	public void generatingDuplicatedTTMove() {
		testPositionCountingMoves("rq2r1k1/5p2/p6p/4b1P1/1p2P2P/5Q2/PP4K1/5R1R w - - 0 2", MoveIterator.GENERATE_ALL, "Qf3xf7+", 35, 2, 0, 1);
	}

	@Test
	public void testGeneratePromotionsInQuiescence() {
		// It must only generate the promotion to queen
		testPositionCountingMoves("7k/P7/8/8/8/8/8/7K w - - 0 1", MoveIterator.GENERATE_CAPTURES_PROMOS, null, 1, 0, 0, 1);

		// Generates the underpromotion to rook
		testPositionCountingMoves("7k/P7/8/8/8/8/8/7K w - - 0 1", MoveIterator.GENERATE_CAPTURES_PROMOS_CHECKS, null, 2, 0, 0, 2);
	}

	@Test
	public void testGenerateCapturesInQuiescence() {
		testPositionCountingMoves("8/1kb2p2/4b1p1/8/2Q2NB1/8/8/K7 w - - 0 1", MoveIterator.GENERATE_CAPTURES_PROMOS, null, 2, 2, 0, 0);
	}

	@Test
	public void testGenerateOnlyGoodAnEqualCapturesInQuiescence() {
		testPositionCountingMoves("2q4k/3n4/1p6/2b5/8/1N2B3/8/6QK w - - 0 1", MoveIterator.GENERATE_CAPTURES_PROMOS, null, 2, 2, 0, 0);
	}

	@Test
	public void testChess960Castling() {
		testPositionCountingMoves("nqrkbbnr/pppppppp/8/8/8/8/PPPPPPPP/NQRKBBNR w KQkq - 0 1", MoveIterator.GENERATE_ALL, null, 20, 0, 0, 0);
	}

	@Test
	public void testChess960CastlingRookSameSquareGivesCheck() {
		testPositionCountingMoves("8/8/8/8/8/8/8/3RK2k w Q - 0 1", MoveIterator.GENERATE_ALL, null, 15, 0, 0, 4);
	}

	@Test
	public void testPawnPushDoesNotLeaveTheKingInCheck() {
		testPositionCountingMoves("5rk1/6P1/2Q5/b5Pp/p6P/8/1rPK4/q3R1R1 w - - 3 6", MoveIterator.GENERATE_ALL, null, 4, 0, 0, 0);
	}

	@Test
	public void testDoNotGenerateLongCastling() {
		Board b = new Board();

		// position startpos moves e2e4 c7c6 g1f3 d7d5 b1c3 c8g4 f1e2 e7e6 d2d4 g8f6 e4e5 f6e4 e1g1 h7h6 c1e3 e4c3 b2c3 d8a5 e3d2
		b.startPosition();
		b.doMove(Move.getFromString(b, "e2e4", false));
		b.doMove(Move.getFromString(b, "c7c6", false));
		b.doMove(Move.getFromString(b, "g1f3", false));
		b.doMove(Move.getFromString(b, "d7d5", false));
		b.doMove(Move.getFromString(b, "b1c3", false));
		b.doMove(Move.getFromString(b, "c8g4", false));
		b.doMove(Move.getFromString(b, "f1e2", false));
		b.doMove(Move.getFromString(b, "e7e6", false));
		b.doMove(Move.getFromString(b, "d2d4", false));
		b.doMove(Move.getFromString(b, "g8f6", false));
		b.doMove(Move.getFromString(b, "e4e5", false));
		b.doMove(Move.getFromString(b, "f6e4", false));
		b.doMove(Move.getFromString(b, "e1g1", false));
		b.doMove(Move.getFromString(b, "h7h6", false));
		b.doMove(Move.getFromString(b, "c1e3", false));
		b.doMove(Move.getFromString(b, "e4c3", false));
		b.doMove(Move.getFromString(b, "b2c3", false));
		b.doMove(Move.getFromString(b, "d8a5", false));
		b.doMove(Move.getFromString(b, "e3d2", false));

		System.out.println(b.toString());
		MoveIterator moveIterator = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		moveIterator.genMoves(0, MoveIterator.GENERATE_ALL);
		int move;
		boolean longCastling = false;
		while ((move = moveIterator.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));
			if ("O-O-O".equals(Move.toStringExt(move))) {
				longCastling = true;
			}
		}
		assertEquals("Must not allow black long castling", false, longCastling);
	}

	@Test
	public void testTtLastMoveSee() {
		Board b = new Board();
		b.setFen("rq2r1k1/5p2/p6p/4b1P1/1p2P2P/5Q2/PP4K1/5R1R w - - 0 2");
		System.out.println(b.toString());
		MoveIterator moveIterator = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		int ttMove = Move.getFromString(b, "Qc3", true);
		moveIterator.genMoves(ttMove, MoveIterator.GENERATE_ALL);
		int move = moveIterator.next();
		assertEquals("Qc3", Move.toSan(b, move));
		assertEquals(-900, moveIterator.getLastMoveSee());
	}

}