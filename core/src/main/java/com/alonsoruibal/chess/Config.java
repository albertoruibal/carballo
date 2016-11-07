package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.Book;

/**
 * Holds configuration parameters
 *
 * @author rui
 */
public class Config {

	// Default values are static fields used also from UCIEngine
	public static final int DEFAULT_TRANSPOSITION_TABLE_SIZE = 64;
	public static final boolean DEFAULT_PONDER = true;
	public static final boolean DEFAULT_USE_BOOK = true;
	public static final int DEFAULT_BOOK_KNOWGLEDGE = 100;
	public static final String DEFAULT_EVALUATOR = "complete";

	// >0 refuses draw <0 looks for draw
	public static final int DEFAULT_CONTEMPT_FACTOR = 90;
	public static final boolean DEFAULT_UCI_CHESS960 = false;

	public static final int DEFAULT_RAND = 0;
	public static final boolean DEFAULT_LIMIT_STRENGTH = false;
	public static final int DEFAULT_ELO = 2100;

	public int transpositionTableSize = DEFAULT_TRANSPOSITION_TABLE_SIZE;
	public boolean ponder = DEFAULT_PONDER;
	public boolean useBook = DEFAULT_USE_BOOK;
	public Book book;
	public String evaluator = DEFAULT_EVALUATOR;
	public int contemptFactor = DEFAULT_CONTEMPT_FACTOR;

	private boolean uciChess960 = DEFAULT_UCI_CHESS960;

	private int rand = DEFAULT_RAND;
	private int bookKnowledge = DEFAULT_BOOK_KNOWGLEDGE;
	private boolean limitStrength = DEFAULT_LIMIT_STRENGTH;
	private int elo = DEFAULT_ELO;

	public boolean getPonder() {
		return ponder;
	}

	public void setPonder(boolean ponder) {
		this.ponder = ponder;
	}

	public boolean getUseBook() {
		return useBook;
	}

	public void setUseBook(boolean useBook) {
		this.useBook = useBook;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public int getBookKnowledge() {
		return bookKnowledge;
	}

	public String getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(String evaluator) {
		this.evaluator = evaluator;
	}

	public int getTranspositionTableSize() {
		return transpositionTableSize;
	}

	public void setTranspositionTableSize(int transpositionTableSize) {
		this.transpositionTableSize = transpositionTableSize;
	}

	public int getContemptFactor() {
		return contemptFactor;
	}

	public void setContemptFactor(int contemptFactor) {
		this.contemptFactor = contemptFactor;
	}

	public int getRand() {
		return rand;
	}

	public boolean isUciChess960() {
		return uciChess960;
	}

	public void setUciChess960(boolean uciChess960) {
		this.uciChess960 = uciChess960;
	}

	public boolean isLimitStrength() {
		return limitStrength;
	}

	public void setLimitStrength(boolean limitStrength) {
		this.limitStrength = limitStrength;
		calculateErrorsFromElo();
	}

	public int getElo() {
		return elo;
	}

	public void setElo(int elo) {
		this.elo = elo;
		calculateErrorsFromElo();
	}

	/**
	 * Calculates the errors and the book knowledge using the limitStrength and elo params
	 * 2100 is the max elo, 500 the min
	 */
	private void calculateErrorsFromElo() {
		if (limitStrength) {
			rand = 900 - ((elo - 500) * 900) / 1600; // Errors per 1000
			bookKnowledge = ((elo - 500) * 100) / 1600; // In percentage
		} else {
			rand = 0;
			bookKnowledge = 100;
		}
	}
}