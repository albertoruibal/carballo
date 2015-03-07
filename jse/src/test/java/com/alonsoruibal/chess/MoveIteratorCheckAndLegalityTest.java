package com.alonsoruibal.chess;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.movesort.MoveIterator;
import com.alonsoruibal.chess.movesort.SortInfo;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MoveIteratorCheckAndLegalityTest {

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

	private void testPositionCountingMoves(String fen, int ttMove, int totalMoves, int totalCaptures, int totalEnPassant) {
		Board b = new Board();
		b.setFen(fen);
		System.out.println(b.toString());
		MoveIterator lmi = new MoveIterator(b, new AttacksInfo(), new SortInfo(), 0);
		lmi.genMoves(ttMove);
		int move;
		int moves = 0;
		int captures = 0;
		int enPassant = 0;
		while ((move = lmi.next()) != Move.NONE) {
			System.out.println(Move.toStringExt(move));
			moves++;
			if (Move.isCapture(move)) {
				captures++;
			}
			if (Move.getMoveType(move) == Move.TYPE_PASSANT) {
				enPassant++;
			}
		}
		assertTrue(totalMoves  + " moves", moves == totalMoves);
		assertTrue(totalCaptures + " captures", captures == totalCaptures);
		assertTrue(totalEnPassant + " en-pasasnt", enPassant == totalEnPassant);
	}

	@Test
	public void promoteCapturing() {
		testPositionCountingMoves("1n5k/2P5/8/8/8/8/8/7K w - - 0 1", Move.NONE, 11, 4, 0);
	}

	@Test
	public void checkEvasions() {
		testPositionCountingMoves("4k3/8/8/8/1b1Q4/2b5/1P6/4K3 w - - 0 1", Move.NONE, 7, 2, 0);
	}

	@Test
	public void avoidCheckPromoting() {
		testPositionCountingMoves("K6r/1P6/8/8/8/8/8/7k w - - 0 1", Move.NONE, 5, 0, 0);
	}

	@Test
	public void checkEvasionCapturingEnPassant() {
		testPositionCountingMoves("8/8/8/3k4/1pP5/8/8/5K2 b - c3 0 1", Move.NONE, 9, 2, 1);
	}

	@Test
	public void checkEvasionInterposeCapturingEnPassant() {
		testPositionCountingMoves("8/8/8/8/1pPk4/8/8/B4K2 b - c3 0 1", Move.NONE, 6, 2, 1);
	}

	@Test
	public void captureCheckingPieceWithKing() {
		testPositionCountingMoves("rq2r1k1/5Qp1/p4p2/4bNP1/1p2P2P/8/PP4K1/5R1R b - - 1 2", Move.NONE, 3, 1, 0);
	}

	// TODO verify sorting
	@Test
	public void captureCheckingPieceWithKingAndTwoPiecesGivingCheck() {
		testPositionCountingMoves("k4r2/R5pb/1pQp1n1p/3P4/5p1P/3P2P1/r1q1R2K/8 b - - 1 1", Move.NONE, 2, 1, 0);
	}

	@Test
	public void evadeCheckMoveKingCapturing() {
		testPositionCountingMoves("r5r1/p1q2pBk/1p1R2p1/3pP3/6bQ/2p5/P1P1NPPP/6K1 b - - 1 1", Move.NONE, 2, 1, 0);
	}

	@Test
	public void generatingDuplicatedTTMove() {
		Board b = new Board();
		b.setFen("rq2r1k1/5p2/p6p/4b1P1/1p2P2P/5Q2/PP4K1/5R1R w - - 0 2");

		testPositionCountingMoves("rq2r1k1/5p2/p6p/4b1P1/1p2P2P/5Q2/PP4K1/5R1R w - - 0 2", Move.getFromString(b, "Qf3xf7+", true), 35, 2, 0);
	}
}