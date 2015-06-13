package com.alonsoruibal.chess.book;

import com.alonsoruibal.chess.Board;


/**
 * Opening book support
 *
 * @author rui
 */
public interface Book {
	/**
	 * Gets a random move from the book taking care of weights
	 */
	int getMove(Board board);
}
