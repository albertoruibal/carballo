package com.alonsoruibal.chess;

import java.util.Random;

import junit.framework.TestCase;

import com.alonsoruibal.chess.hash.ZobristKey;
import com.alonsoruibal.chess.hash.ZobristKeyFen;
import com.alonsoruibal.chess.movegen.LegalMoveGenerator;
import com.alonsoruibal.chess.movegen.MoveGenerator;


/**
 * Test zobrish keys
 * Also test that after board.setfen(x), x==board.getFen();
 * 
 * @author rui
 */
public class ZobristKeyTest extends  TestCase {

	/**
	 * Test that the zobrish key of the board is equal than the obtanied with fen
	 * making random legal moves
	 */
	public void testBoardZobrishKey() {
		Board board = new Board();
		MoveGenerator movegen = new LegalMoveGenerator();
		board.startPosition();
		Random random = new Random();
		
		for (int i=1; i < 100000; i++) {
			int moves[] = new int[256];
			int moveCount = movegen.generateMoves(board, moves, 0);
			if ((moveCount >  0) && ((i%100) != 0)) {				
				int move = moves[(new Float(random.nextFloat() * moveCount)).intValue()];
				board.doMove(move);
				
				long key1 = ZobristKeyFen.getKey(board.getFen());
				long key2[] = ZobristKey.getKey(board);
				assertEquals(board.getKey(), key1);
				assertEquals(board.getKey(), key2[0] ^ key2[1]);
			} else {
				board.startPosition();
			}
		}
	}

	public void testZobrishKey1() {
		Board board = new Board();
		board.startPosition();
		long result = ZobristKeyFen.getKey(board.getFen());
		// Also test undoing
		board.doMove(Move.getFromString(board, "g1f3", true));
		board.undoMove();
		assertEquals(result, board.getKey());
		assertEquals(result, 0x463b96181691fc9cL);
	}

	public void testZobrishKey2() {	
		Board board = new Board();
		board.setFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x823c9b50fd114196L);
	}

	public void testZobrishKey3() {	
		Board board = new Board();
		board.setFen("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x0756b94461c50fb0L);
	}

	public void testZobrishKey4() {	
		Board board = new Board();
		board.setFen("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x662fafb965db29d4L);
	}

	public void testZobrishKey5() {	
		Board board = new Board();
		board.setFen("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x22a48b5a8e47ff78L);
	}

	public void testZobrishKey6() {	
		Board board = new Board();
		board.setFen("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPPKPPP/RNBQ1BNR b kq - 0 3");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x652a607ca3f242c1L);
	}

	public void testZobrishKey7() {	
		Board board = new Board();
		board.setFen("rnbq1bnr/ppp1pkpp/8/3pPp2/8/8/PPPPKPPP/RNBQ1BNR w - - 0 4");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x00fdd303c946bdd9L);
	}

	public void testZobrishKey8() {	
		Board board = new Board();
		board.setFen("rnbqkbnr/p1pppppp/8/8/PpP4P/8/1P1PPPP1/RNBQKBNR b KQkq c3 0 3");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x3c8123ea7b067637L);
	}

	public void testZobrishKey9() {	
		Board board = new Board();
		board.setFen("rnbqkbnr/p1pppppp/8/8/P6P/R1p5/1P1PPPP1/1NBQKBNR b Kkq - 0 4");
		long result = ZobristKeyFen.getKey(board.getFen());
		assertEquals(result, board.getKey());
		assertEquals(result, 0x5c3f9b829b279560L);		
	}
}
