package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.Book;

import java.util.Arrays;

/**
 * Holds configuration parameters
 *
 * @author rui
 */
public class Config {

	// For manual testing
	public boolean experimental = false;

	// Default values are static fields used also from UCIEngine
	public final static boolean DEFAULT_USE_BOOK = true;
	public final static int DEFAULT_BOOK_KNOWGLEDGE = 100;
	public final static String DEFAULT_EVALUATOR = "experimental";
	public final static boolean DEFAULT_NULL_MOVE = true;
	public final static int DEFAULT_NULL_MOVE_MARGIN = 200;
	public final static boolean DEFAULT_STATIC_NULL_MOVE = true;
	public final static boolean DEFAULT_IID = true;
	public final static int DEFAULT_IID_MARGIN = 300;
	public final static boolean DEFAULT_LMR = true;
	public final static int DEFAULT_EXTENSIONS_CHECK = 2; // 2 = 1 PLY
	public final static int DEFAULT_EXTENSIONS_MATE_THREAT = 2;
	public final static int DEFAULT_EXTENSIONS_PAWN_PUSH = 0;
	public final static int DEFAULT_EXTENSIONS_PASSED_PAWN = 0;
	public final static int DEFAULT_EXTENSIONS_RECAPTURE = 0;
	public final static int DEFAULT_EXTENSIONS_SINGULAR = 2;
	public final static int DEFAULT_SINGULAR_EXTENSION_MARGIN = 50;
	public final static boolean DEFAULT_ASPIRATION_WINDOW = true;
	public final static String DEFAULT_ASPIRATION_WINDOW_SIZES = "10,25,150,400,550,1025";
	public final static int DEFAULT_TRANSPOSITION_TABLE_SIZE = 64;
	public final static boolean DEFAULT_FUTILITY = true;
	public final static int DEFAULT_FUTILITY_MARGIN = 100;
	public final static boolean DEFAULT_AGGRESIVE_FUTILITY = true;
	public final static int DEFAULT_AGGRESIVE_FUTILITY_MARGIN = 200;
	public final static int DEFAULT_FUTILITY_MARGIN_QS = 150;
	public final static boolean DEFAULT_RAZORING = true;
	public final static int DEFAULT_RAZORING_MARGIN = 900;

	// >0 refuses draw <0 looks for draw
	public final static int DEFAULT_CONTEMPT_FACTOR = 90;

	public final static int DEFAULT_EVAL_CENTER = 100;
	public final static int DEFAULT_EVAL_POSITIONAL = 100;
	public final static int DEFAULT_EVAL_ATTACKS = 100;
	public final static int DEFAULT_EVAL_MOBILITY = 100;
	public final static int DEFAULT_EVAL_PAWN_STRUCTURE = 100;
	public final static int DEFAULT_EVAL_PASSED_PAWNS = 100;
	public final static int DEFAULT_EVAL_KING_SAFETY = 100;

	public final static int DEFAULT_RAND = 0;

	private boolean useBook = DEFAULT_USE_BOOK;
	private Book book;
	private int bookKnowledge = DEFAULT_BOOK_KNOWGLEDGE;
	private String evaluator = DEFAULT_EVALUATOR;
	private boolean nullMove = DEFAULT_NULL_MOVE;
	private int nullMoveMargin = DEFAULT_NULL_MOVE_MARGIN;
	private boolean staticNullMove = DEFAULT_STATIC_NULL_MOVE;
	private boolean iid = DEFAULT_IID;
	private int iidMargin = DEFAULT_IID_MARGIN;
	private boolean lmr = DEFAULT_LMR;
	private int extensionsCheck = DEFAULT_EXTENSIONS_CHECK;
	private int extensionsMateThreat = DEFAULT_EXTENSIONS_MATE_THREAT;
	private int extensionsPawnPush = DEFAULT_EXTENSIONS_PAWN_PUSH;
	private int extensionsPassedPawn = DEFAULT_EXTENSIONS_PASSED_PAWN;
	private int extensionsRecapture = DEFAULT_EXTENSIONS_RECAPTURE;
	private int extensionsSingular = DEFAULT_EXTENSIONS_SINGULAR;
	private int singularExtensionMargin = DEFAULT_SINGULAR_EXTENSION_MARGIN;
	private boolean aspirationWindow = DEFAULT_ASPIRATION_WINDOW;
	private int[] aspirationWindowSizes; // It is initialized in the constructor
	private int transpositionTableSize = DEFAULT_TRANSPOSITION_TABLE_SIZE;
	private boolean futility = DEFAULT_FUTILITY;
	private int futilityMargin = DEFAULT_FUTILITY_MARGIN;
	private boolean aggressiveFutility = DEFAULT_AGGRESIVE_FUTILITY;
	private int aggressiveFutilityMargin = DEFAULT_AGGRESIVE_FUTILITY_MARGIN;
	private int futilityMarginQS = DEFAULT_FUTILITY_MARGIN_QS;
	private boolean razoring = DEFAULT_RAZORING;
	private int razoringMargin = DEFAULT_RAZORING_MARGIN;
	private int contemptFactor = DEFAULT_CONTEMPT_FACTOR;

	private int evalCenter = DEFAULT_EVAL_CENTER;
	private int evalPositional = DEFAULT_EVAL_POSITIONAL;
	private int evalAttacks = DEFAULT_EVAL_ATTACKS;
	private int evalMobility = DEFAULT_EVAL_MOBILITY;
	private int evalPawnStructure = DEFAULT_EVAL_PAWN_STRUCTURE;
	private int evalPassedPawns = DEFAULT_EVAL_PASSED_PAWNS;
	private int evalKingSafety = DEFAULT_EVAL_KING_SAFETY;

	private int rand = DEFAULT_RAND;

	public Config() {
		setAspirationWindowSizes(DEFAULT_ASPIRATION_WINDOW_SIZES);
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

	public boolean getNullMove() {
		return nullMove;
	}

	public void setNullMove(boolean nullMove) {
		this.nullMove = nullMove;
	}

	public int getNullMoveMargin() {
		return nullMoveMargin;
	}

	public void setNullMoveMargin(int nullMoveMargin) {
		this.nullMoveMargin = nullMoveMargin;
	}

	public boolean getStaticNullMove() {
		return staticNullMove;
	}

	public void setStaticNullMove(boolean staticNullMove) {
		this.staticNullMove = staticNullMove;
	}

	public boolean getIid() {
		return iid;
	}

	public void getIid(boolean iid) {
		this.iid = iid;
	}

	public int getIidMargin() {
		return iidMargin;
	}

	public void setIidMargin(int iidMargin) {
		this.iidMargin = iidMargin;
	}

	public boolean getLmr() {
		return lmr;
	}

	public void setLmr(boolean lmr) {
		this.lmr = lmr;
	}

	public boolean getFutility() {
		return futility;
	}

	public void setFutility(boolean futility) {
		this.futility = futility;
	}

	public int getFutilityMargin() {
		return futilityMargin;
	}

	public void setFutilityMargin(int futilityMargin) {
		this.futilityMargin = futilityMargin;
	}

	public boolean getAggressiveFutility() {
		return aggressiveFutility;
	}

	public void setAggressiveFutility(boolean aggressiveFutility) {
		this.aggressiveFutility = aggressiveFutility;
	}

	public int getAggressiveFutilityMargin() {
		return aggressiveFutilityMargin;
	}

	public void setAggressiveFutilityMargin(int aggressiveFutilityMargin) {
		this.aggressiveFutilityMargin = aggressiveFutilityMargin;
	}

	public int getFutilityMarginQS() {
		return futilityMarginQS;
	}

	public void setFutilityMarginQS(int futilityMarginQS) {
		this.futilityMarginQS = futilityMarginQS;
	}

	public boolean getAspirationWindow() {
		return aspirationWindow;
	}

	public void setAspirationWindow(boolean aspirationWindow) {
		this.aspirationWindow = aspirationWindow;
	}

	public int[] getAspirationWindowSizes() {
		return aspirationWindowSizes;
	}

	public void setAspirationWindowSizes(String aspirationWindowSizesString) {
		String aux[] = aspirationWindowSizesString.split(",");

		aspirationWindowSizes = new int[aux.length];
		for (int i = 0; i < aux.length; i++) {
			aspirationWindowSizes[i] = Integer.parseInt(aux[i]);
		}
	}

	public int getTranspositionTableSize() {
		return transpositionTableSize;
	}

	public void setTranspositionTableSize(int transpositionTableSize) {
		this.transpositionTableSize = transpositionTableSize;
	}

	public int getExtensionsCheck() {
		return extensionsCheck;
	}

	public void setExtensionsCheck(int extensionsCheck) {
		this.extensionsCheck = extensionsCheck;
	}

	public int getExtensionsMateThreat() {
		return extensionsMateThreat;
	}

	public void setExtensionsMateThreat(int extensionsMateThreat) {
		this.extensionsMateThreat = extensionsMateThreat;
	}

	public int getExtensionsPawnPush() {
		return extensionsPawnPush;
	}

	public void setExtensionsPawnPush(int extensionsPawnPush) {
		this.extensionsPawnPush = extensionsPawnPush;
	}

	public int getExtensionsPassedPawn() {
		return extensionsPassedPawn;
	}

	public void setExtensionsPassedPawn(int extensionsPassedPawn) {
		this.extensionsPassedPawn = extensionsPassedPawn;
	}

	public int getExtensionsRecapture() {
		return extensionsRecapture;
	}

	public void setExtensionsRecapture(int extensionsRecapture) {
		this.extensionsRecapture = extensionsRecapture;
	}

	public int getExtensionsSingular() {
		return extensionsSingular;
	}

	public void setExtensionsSingular(int extensionsSingular) {
		this.extensionsSingular = extensionsSingular;
	}

	public int getSingularExtensionMargin() {
		return singularExtensionMargin;
	}

	public void setSingularExtensionMargin(int singularExtensionMargin) {
		this.singularExtensionMargin = singularExtensionMargin;
	}

	public int getContemptFactor() {
		return contemptFactor;
	}

	public void setContemptFactor(int contemptFactor) {
		this.contemptFactor = contemptFactor;
	}

	public int getEvalCenter() {
		return evalCenter;
	}

	public void setEvalCenter(int evalCenter) {
		this.evalCenter = evalCenter;
	}

	public int getEvalPositional() {
		return evalPositional;
	}

	public void setEvalPositional(int evalPositional) {
		this.evalPositional = evalPositional;
	}

	public int getEvalAttacks() {
		return evalAttacks;
	}

	public void setEvalAttacks(int evalAttacks) {
		this.evalAttacks = evalAttacks;
	}

	public int getEvalMobility() {
		return evalMobility;
	}

	public void setEvalMobility(int evalMobility) {
		this.evalMobility = evalMobility;
	}

	public int getEvalPawnStructure() {
		return evalPawnStructure;
	}

	public void setEvalPawnStructure(int evalPawnStructure) {
		this.evalPawnStructure = evalPawnStructure;
	}

	public int getEvalPassedPawns() {
		return evalPassedPawns;
	}

	public void setEvalPassedPawns(int evalPassedPawns) {
		this.evalPassedPawns = evalPassedPawns;
	}

	public int getEvalKingSafety() {
		return evalKingSafety;
	}

	public void setEvalKingSafety(int evalKingSafety) {
		this.evalKingSafety = evalKingSafety;
	}

	public int getRand() {
		return rand;
	}

	public void setRand(int rand) {
		this.rand = rand;
	}

	public boolean getRazoring() {
		return razoring;
	}

	public void setRazoring(boolean razoring) {
		this.razoring = razoring;
	}

	public int getRazoringMargin() {
		return razoringMargin;
	}

	public void setRazoringMargin(int razoringMargin) {
		this.razoringMargin = razoringMargin;
	}

	/**
	 * 2100 is the max, 500 the min
	 *
	 * @param engineElo
	 */
	public void setElo(int engineElo) {
		int kPercentage = ((engineElo - 500) * 100) / 1600; // knowledge percentage
		int bookPercentage = ((engineElo - 500) * 100) / 1600; // book knowledge percentage
		int ePercentage = 88 - ((engineElo - 500) * 88) / 1600; // percentage of errors
		setRand(ePercentage);
		setUseBook(true);
		setBookKnowledge(bookPercentage);
		setEvalPawnStructure(kPercentage);
		setEvalPassedPawns(kPercentage);
		setEvalKingSafety(kPercentage);
		setEvalMobility(kPercentage);
		setEvalPositional(kPercentage);
		setEvalCenter(kPercentage);
	}

	@Override
	public String toString() {
		return "------------------ Config ---------------------------------------------------------------------\n" +
				"Book              " + useBook + " (" + book + ")\n" +
				"TT Size           " + transpositionTableSize + "\n" +
				"Aspiration Window " + aspirationWindow + " " + Arrays.toString(aspirationWindowSizes) + "\n" +
				"Extensions:       Check=" + extensionsCheck + " MateThreat=" + extensionsMateThreat + " PawnPush=" + extensionsPawnPush + " PassedPawn=" + extensionsPassedPawn + " Recapture=" + extensionsRecapture + " Singular=" + extensionsSingular + " (" + singularExtensionMargin + ")\n" +
				"Razoring          " + razoring + " (" + razoringMargin + ")\n" +
				"Null Move         " + nullMove + " (" + nullMoveMargin + ")\n" +
				"Fut. Pruning      " + futility + " (" + futilityMargin + ") QS: (" + futilityMarginQS + ")\n" +
				"Agg. Futility     " + aggressiveFutility + " (" + aggressiveFutilityMargin + ")\n" +
				"Static Null Move  " + staticNullMove + "\n" +
				"IID               " + iid + " (" + iidMargin + ")\n" +
				"LMR               " + lmr + "\n" +
				"Evaluator         " + evaluator + " KingSafety=" + evalKingSafety + " Mobility=" + evalMobility + " PassedPawns=" + evalPassedPawns + " PawnStructure=" + evalPawnStructure + "\n" +
				"Contempt Factor   " + contemptFactor;
	}
}