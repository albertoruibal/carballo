package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.Book;

/**
 * Holds configuration parameters
 *
 * @author rui
 */
public class Config {
	// Values that cannot be dynamically changed
	public final static int PAWN = 100;
	public final static int KNIGHT = 325;
	public final static int BISHOP = 325;
	public final static int BISHOP_PAIR = 50; // Bonus by having two bishops in different colors
	public final static int ROOK = 500;
	public final static int QUEEN = 975;
	public final static int NON_PAWN_MATERIAL_MIDGAME_MAX = 2 * KNIGHT + 2 * BISHOP + 4 * ROOK + 2 * QUEEN;
	public final static int NON_PAWN_MATERIAL_ENDGAME_MIN = QUEEN + ROOK;

	public final static boolean NULL_MOVE = true;
	public final static boolean STATIC_NULL_MOVE = true;
	public final static boolean IID = true;
	public final static int IID_MARGIN = 300;
	public final static boolean LMR = true;
	public final static int EXTENSIONS_CHECK = 2; // 2 = 1 PLY
	public final static int EXTENSIONS_MATE_THREAT = 2;
	public final static int EXTENSIONS_PAWN_PUSH = 0;
	public final static int EXTENSIONS_PASSED_PAWN = 0;
	public final static int EXTENSIONS_SINGULAR = 2;
	public final static int SINGULAR_EXTENSION_MARGIN = 50;
	public final static int[] ASPIRATION_WINDOW_SIZES = {10, 25, 150, 400, 550, 1025};
	public final static boolean FUTILITY = true;
	public final static int FUTILITY_MARGIN_QS = 80;
	public final static int FUTILITY_MARGIN = 100;
	public final static int FUTILITY_MARGIN_AGGRESSIVE = 150;
	public final static boolean RAZORING = true;
	public final static int RAZORING_MARGIN = 325;

	// Default values are static fields used also from UCIEngine
	public final static int DEFAULT_TRANSPOSITION_TABLE_SIZE = 64;
	public final static boolean DEFAULT_PONDER = true;
	public final static boolean DEFAULT_USE_BOOK = true;
	public final static int DEFAULT_BOOK_KNOWGLEDGE = 100;
	public final static String DEFAULT_EVALUATOR = "experimental";

	// >0 refuses draw <0 looks for draw
	public final static int DEFAULT_CONTEMPT_FACTOR = 90;

	public final static int DEFAULT_RAND = 0;
	public final static int DEFAULT_ELO = 2100;
	public final static boolean DEFAULT_UCI_CHESS960 = false;

	private int transpositionTableSize = DEFAULT_TRANSPOSITION_TABLE_SIZE;
	private boolean ponder = DEFAULT_PONDER;
	private boolean useBook = DEFAULT_USE_BOOK;
	private Book book;
	private int bookKnowledge = DEFAULT_BOOK_KNOWGLEDGE;
	private String evaluator = DEFAULT_EVALUATOR;
	private int contemptFactor = DEFAULT_CONTEMPT_FACTOR;

	private int rand = DEFAULT_RAND;
	private boolean uciChess960 = DEFAULT_UCI_CHESS960;

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

	public void setBookKnowledge(int bookKnowledge) {
		this.bookKnowledge = bookKnowledge;
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

	public void setRand(int rand) {
		this.rand = rand;
	}

	public boolean isUciChess960() {
		return uciChess960;
	}

	public void setUciChess960(boolean uciChess960) {
		this.uciChess960 = uciChess960;
	}

	/**
	 * 2100 is the max, 500 the min
	 *
	 * @param engineElo
	 */
	public void setElo(int engineElo) {
		int errorsPerMil = 900 - ((engineElo - 500) * 900) / 1600;
		setRand(errorsPerMil);

		int kPercentage = ((engineElo - 500) * 100) / 1600; // knowledge percentage
		setBookKnowledge(kPercentage);
	}
}