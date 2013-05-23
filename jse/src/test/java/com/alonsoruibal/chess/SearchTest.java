package com.alonsoruibal.chess;

import junit.framework.TestCase;

import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchParameters;

/**

 * @author rui
 */
public class SearchTest extends TestCase {

	Config config;
	SearchEngine search;
	
	protected void setUp() throws Exception {
		config = new Config();
		config.setBook(new FileBook("/book_small.bin"));
		search = new SearchEngine(config);
	}

    /**
     * Late move pruning must not be used in mate search.
     */
    public void testLateMovePruning()  {
		search.getBoard().setFen("2r2rk1/6p1/p3pq1p/1p1b1p2/3P1n2/PP3N2/3N1PPP/1Q2RR1K b"); // Find mate in WAC 174.
		search.go(SearchParameters.get(30000));
    }

}