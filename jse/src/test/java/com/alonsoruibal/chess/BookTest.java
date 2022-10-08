package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.FileBook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTest {

	@Test
	void testBook() {
		int count = 0;
		Board board = new Board();
		board.startPosition();
		FileBook book = new FileBook("/book_small.bin");
		int move = book.getMove(board);
		while (move != 0) {
			System.out.println(Move.toString(move));
			board.doMove(move);
			System.out.println(board);
			move = book.getMove(board);
			count++;
		}
		assertTrue(count > 3);
	}
}