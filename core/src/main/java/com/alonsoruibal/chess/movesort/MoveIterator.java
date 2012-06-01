package com.alonsoruibal.chess.movesort;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardAttacks;
import com.alonsoruibal.chess.bitboard.BitboardUtils;

/**
 * Sort Moves based on heuristics
 * short first GOOD captures (a piece of less value captures other of more value)
 * 
 * SEE captures, and move captures with SEE<0 to the end
 * 
 */
public class MoveIterator {
//	private static final Logger logger = Logger.getLogger(MoveIterator.class);
	public final static int PHASE_TT = 0;
	public final static int PHASE_GEN_CAPTURES = 1;
	public final static int PHASE_GOOD_CAPTURES_AND_PROMOS = 2;
	public final static int PHASE_EQUAL_CAPTURES = 3;
	public final static int PHASE_GEN_NONCAPTURES = 4;
	public final static int PHASE_KILLER1 = 5;
	public final static int PHASE_KILLER2 = 6;
	public final static int PHASE_NONCAPTURES = 7;
	public final static int PHASE_BAD_CAPTURES = 8;
	public final static int PHASE_END = 9;

	private static final int[] VICTIM_PIECE_VALUES = {0,100,325,330,500,975,10000};
	private static final int[] AGGRESSOR_PIECE_VALUES = {0,10,32,33,50,97,99};
	private static final int SCORE_PROMOTION_QUEEN = 975;
	private static final int SCORE_UNDERPROMOTION = Integer.MIN_VALUE+1;
	private static final int SCORE_LOWEST = Integer.MIN_VALUE;
	
	private Board board;
	private int ttMove;
	private int killer1;
	private int killer2;
	private boolean foundKiller1;
	private boolean foundKiller2;
	private boolean quiescence;
	private boolean generateChecks;
	private boolean checkEvasion;
	
	private int nonCaptureIndex;
	private int goodCaptureIndex;
	private int equalCaptureIndex;
	private int badCaptureIndex;
	private long all;
	private long mines;
	private long others;
	private long[] attacks = new long[64]; // Stores slider pieces attacks

	public int[] goodCaptures = new int[256]; // Stores captures and queen promotions
	public int[] goodCapturesScores = new int[256];
	public int[] badCaptures = new int[256]; // Stores captures and queen promotions
	public int[] badCapturesScores = new int[256];
	public int[] equalCaptures = new int[256]; // Stores captures and queen promotions
	public int[] equalCapturesScores = new int[256];
	public int[] nonCaptures = new int[256]; // Stores non captures and underpromotions
	public int[] nonCapturesScores = new int[256];
		
	private int depth;
	SortInfo sortInfo;
	int phase;

	public int getPhase() {
		return phase;
	}

	public MoveIterator(Board board, SortInfo sortInfo, int depth) {
		this.sortInfo = sortInfo;
		this.board = board;
		this.depth = depth;
	}
	
	public void setBoard(Board board)  {
		this.board = board;
	}

	/**
	 * Generates captures and tactical moves (not underpromotions)
	 */
	public void generateCaptures() {
	//		logger.debug(board);
		
		all = board.getAll(); // only for clearity
		mines = board.getMines();
		others = board.getOthers();

		
		byte index = 0;
		long square = 0x1L;
		while (square != 0) {
			attacks[index] = 0;
			if (board.getTurn() == ((square & board.whites ) != 0)) {
				
				if ((square & board.rooks) != 0) { // Rook
					attacks[index] = BitboardAttacks.getRookAttacks(index, all);
					generateCapturesFromAttacks(Move.ROOK, index, attacks[index] & others); 
				} else if ((square & board.bishops) != 0) { // Bishop
					attacks[index] = BitboardAttacks.getBishopAttacks(index, all);
					generateCapturesFromAttacks(Move.BISHOP, index, attacks[index] & others); 
				} else if ((square & board.queens) != 0) { // Queen
					attacks[index] = BitboardAttacks.getRookAttacks(index, all) | BitboardAttacks.getBishopAttacks(index, all);
					generateCapturesFromAttacks(Move.QUEEN, index, attacks[index] & others); 
				} else if ((square & board.kings) != 0) { // King
					generateCapturesFromAttacks(Move.KING, index, BitboardAttacks.king[index] & others); 
				} else if ((square & board.knights) != 0) { // Knight
					generateCapturesFromAttacks(Move.KNIGHT, index, BitboardAttacks.knight[index] & others); 
				} else if ((square & board.pawns) != 0) { // Pawns
					if ((square & board.whites) != 0) {
						generatePawnCapturesAndGoodPromos(index,
								(BitboardAttacks.pawnUpwards[index] & (others | board.getPassantSquare()))
								| (((square << 8) & all) == 0 ? (square << 8) : 0),
								board.getPassantSquare());
					} else {
						generatePawnCapturesAndGoodPromos(index,
								(BitboardAttacks.pawnDownwards[index] & (others | board.getPassantSquare()))
								| (((square >>> 8) & all) == 0 ? (square >>> 8) : 0),
								board.getPassantSquare());
					}
				}
			}
			square <<= 1;
			index++;
		}
	}
	
	/**
	 * Generates underpromotions and non tactical moves
	 */
	public void generateNonCaptures() {
		all = board.getAll(); // only for clearity
		mines = board.getMines();
		others = board.getOthers();

		byte index = 0;
		long square = 0x1L;
		while (square != 0) {
			if (board.getTurn() == ((square & board.whites ) != 0)) {
				if ((square & board.rooks) != 0) { // Rook
					generateNonCapturesFromAttacks(Move.ROOK, index, attacks[index] & ~all); 
				} else if ((square & board.bishops) != 0) { // Bishop
					generateNonCapturesFromAttacks(Move.BISHOP, index, attacks[index] & ~all); 
				} else if ((square & board.queens) != 0) { // Queen
					generateNonCapturesFromAttacks(Move.QUEEN, index, attacks[index] & ~all); 
				} else if ((square & board.kings) != 0) { // King
					generateNonCapturesFromAttacks(Move.KING, index, BitboardAttacks.king[index] & ~all); 
				} else if ((square & board.knights) != 0) { // Knight
					generateNonCapturesFromAttacks(Move.KNIGHT, index, BitboardAttacks.knight[index] & ~all); 
				} if ((square & board.pawns) != 0) { // Pawns
					if ((square & board.whites) != 0) {
						generatePawnNonCapturesAndBadPromos(index,
								(BitboardAttacks.pawnUpwards[index] & others)
								| (((square << 8) & all) == 0 ? (square << 8) : 0)
								| ((square & BitboardUtils.b2_d) != 0 && (((square << 8) | (square << 16)) & all) == 0 ? (square << 16) : 0));
					} else {
						generatePawnNonCapturesAndBadPromos(index,
								(BitboardAttacks.pawnDownwards[index] & others)
								| (((square >>> 8) & all) == 0 ? (square >>> 8) : 0)
								| ((square & BitboardUtils.b2_u) != 0 && (((square >>> 8) | (square >>> 16)) & all) == 0 ? (square >>> 16) : 0));
					}
				}
			}
			square <<= 1;
			index++;
		} 

		square = board.kings & mines; // my king
		byte myKingIndex = -1;
		// Castling: disabled when in check or squares attacked
		if ((((all & (board.getTurn() ? 0x06L : 0x0600000000000000L)) == 0  &&
			  (board.getTurn() ? board.getWhiteKingsideCastling() : board.getBlackKingsideCastling())))) {
			myKingIndex = BitboardUtils.square2Index(square);
			if (!board.getCheck() &&
				!BitboardAttacks.isIndexAttacked(board, (byte) (myKingIndex-1), board.getTurn()) &&
				!BitboardAttacks.isIndexAttacked(board, (byte) (myKingIndex-2), board.getTurn()))
				addNonCapturesAndBadPromos(Move.KING, myKingIndex, myKingIndex-2, 0, false, Move.TYPE_KINGSIDE_CASTLING);	
		}
		if ((((all & (board.getTurn() ? 0x70L : 0x7000000000000000L)) == 0 &&
				(board.getTurn() ? board.getWhiteQueensideCastling() : board.getBlackQueensideCastling())))) {
			if (myKingIndex == -1) myKingIndex = BitboardUtils.square2Index(square);
			if (!board.getCheck() &&
				!BitboardAttacks.isIndexAttacked(board, (byte) (myKingIndex+1), board.getTurn()) &&
				!BitboardAttacks.isIndexAttacked(board, (byte) (myKingIndex+2), board.getTurn()))
				addNonCapturesAndBadPromos(Move.KING, myKingIndex, myKingIndex+2, 0, false, Move.TYPE_QUEENSIDE_CASTLING);	
		}
	}
	
	/**
	 * Generates moves from an attack mask
	 */
	private final void generateCapturesFromAttacks(int pieceMoved, int fromIndex, long attacks) { 
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			addCapturesAndGoodPromos(pieceMoved, fromIndex, BitboardUtils.square2Index(to), to, true, 0);
			attacks ^= to;
		}
	}
	
	private final void generateNonCapturesFromAttacks(int pieceMoved, int fromIndex, long attacks) { 
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			addNonCapturesAndBadPromos(pieceMoved, fromIndex, BitboardUtils.square2Index(to), to, false, 0);
			attacks ^= to;
		}
	}
	
	private final void generatePawnCapturesAndGoodPromos(int fromIndex, long attacks, long passant) {
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			if ((to & passant) != 0) { 
				addCapturesAndGoodPromos(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, true, Move.TYPE_PASSANT);
			} else {
				boolean capture = (to & others) != 0; 
				if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0) {
					addCapturesAndGoodPromos(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_QUEEN);
				} else if (capture) {
					addCapturesAndGoodPromos(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, 0);					
				}				
			}
			attacks ^= to;
		}
	}

	private final void generatePawnNonCapturesAndBadPromos(int fromIndex, long attacks) { 
		while (attacks != 0) {
			long to = BitboardUtils.lsb(attacks);
			boolean capture = (to & others) != 0;
			if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0) {
				addNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_KNIGHT);
				addNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_ROOK);
				addNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, Move.TYPE_PROMOTION_BISHOP);
			} else if (!capture) {
				addNonCapturesAndBadPromos(Move.PAWN, fromIndex, BitboardUtils.square2Index(to), to, capture, 0);
			}
			attacks ^= to;
		}
	}
	
	private void addNonCapturesAndBadPromos(int pieceMoved, int fromIndex, int toIndex, long to, boolean capture, int moveType) {
		int move = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
		if (move == killer1) {
			foundKiller1 = true;
		} else if (move == killer2) {
			foundKiller2 = true;
		} else if (move != ttMove) {
			// Score non captures
			int score = sortInfo.getMoveScore(move);
			if (moveType == Move.TYPE_PROMOTION_KNIGHT ||
				moveType == Move.TYPE_PROMOTION_ROOK ||
				moveType == Move.TYPE_PROMOTION_BISHOP) score -= SCORE_UNDERPROMOTION;
			
//			System.out.println("* " + score + " - " + Move.toStringExt(move));
			nonCaptures[nonCaptureIndex] = move;
			nonCapturesScores[nonCaptureIndex] = score;
			nonCaptureIndex++;
		}
	}

	private void addCapturesAndGoodPromos(int pieceMoved, int fromIndex, int toIndex, long to, boolean capture, int moveType) {
		int move = Move.genMove(fromIndex, toIndex, pieceMoved, capture, moveType);
		if (move != ttMove) {	
			// Score captures
			int pieceCaptured = 0;
			
			if ((to & board.knights) != 0) pieceCaptured = Move.KNIGHT;
			else if ((to & board.bishops) != 0) pieceCaptured = Move.BISHOP;
			else if ((to & board.rooks) != 0) pieceCaptured = Move.ROOK;
			else if ((to & board.queens) != 0) pieceCaptured = Move.QUEEN;
			else if (capture) pieceCaptured = Move.PAWN;
			
			int see = 0;	

			if (capture) {
				see = board.see(fromIndex, toIndex, pieceMoved, pieceCaptured);				
			}

			if (see >= 0) {
				int score = 0;
				// Order GOOD captures by MVV/LVA (Hyatt dixit)
				if (capture) score = VICTIM_PIECE_VALUES[pieceCaptured] - AGGRESSOR_PIECE_VALUES[pieceMoved];
				
				if (see > 0 || moveType == Move.TYPE_PROMOTION_QUEEN) {
					if (moveType == Move.TYPE_PROMOTION_QUEEN) score += SCORE_PROMOTION_QUEEN;
					goodCaptures[goodCaptureIndex] = move;
					goodCapturesScores[goodCaptureIndex] = score;
					goodCaptureIndex++;
				} else {
					equalCaptures[equalCaptureIndex] = move;
					equalCapturesScores[equalCaptureIndex] = score;
					equalCaptureIndex++;					
				}
			} else {
				badCaptures[badCaptureIndex] = move;
				badCapturesScores[badCaptureIndex] = see;
				badCaptureIndex++;		
			}
		}
	}

	/**
	 * Moves are sorted ascending (best moves at the end)
	 */
	public void genMoves(int ttMove) {
		genMoves(ttMove, false, true);
	}
	
	public void genMoves(int ttMove, boolean quiescence, boolean generateChecks) {
		this.ttMove = ttMove;
		foundKiller1 = false;
		foundKiller2 = false; 
		
		this.quiescence = quiescence;
		this.generateChecks = generateChecks;
		this.checkEvasion = board.getCheck();
		
		killer1 = sortInfo.killerMove1[depth];
		killer2 = sortInfo.killerMove2[depth];
		
		phase = 0;
		goodCaptureIndex = 0;
		badCaptureIndex = 0;
		equalCaptureIndex = 0;
		nonCaptureIndex = 0;
	}
	
	public int next() {
		int maxScore, bestIndex;
		switch(phase) {
		case PHASE_TT:
			phase++;
			if (ttMove != 0) {
				return ttMove;
			}
		case PHASE_GEN_CAPTURES:
			phase++;
			generateCaptures();
		case PHASE_GOOD_CAPTURES_AND_PROMOS:
			maxScore = SCORE_LOWEST;
			bestIndex = -1;
			for (int i = 0; i<goodCaptureIndex; i++) {
				if (goodCapturesScores[i] > maxScore) {
					maxScore = goodCapturesScores[i];
					bestIndex = i;
				}
			}
			if (bestIndex != -1) {
				goodCapturesScores[bestIndex] = SCORE_LOWEST;
				return goodCaptures[bestIndex];
			}
			phase++;
		case PHASE_EQUAL_CAPTURES:
			maxScore = SCORE_LOWEST;
			bestIndex = -1;
			for (int i = 0; i<equalCaptureIndex; i++) {
				if (equalCapturesScores[i] > maxScore) {
					maxScore = equalCapturesScores[i];
					bestIndex = i;
				}
			}
			if (bestIndex != -1) {
				equalCapturesScores[bestIndex] = SCORE_LOWEST;
				return equalCaptures[bestIndex];
			}			
			phase++;
		case PHASE_GEN_NONCAPTURES:
			phase++;
			
			if (quiescence && !generateChecks && !checkEvasion) {
				phase = PHASE_END;
				return 0;
			}
			
			generateNonCaptures();
		case PHASE_KILLER1:
			phase++;
			if (foundKiller1) {
				return killer1;
			}
		case PHASE_KILLER2:
			phase++;
			if (foundKiller2) {
				return killer2;
			}
		case PHASE_NONCAPTURES:
			maxScore = SCORE_LOWEST;
			bestIndex = -1;
			for (int i = 0; i<nonCaptureIndex; i++) {
				if (nonCapturesScores[i] > maxScore) {
					maxScore = nonCapturesScores[i];
					bestIndex = i;
				}
			}
			if (bestIndex != -1) {
				nonCapturesScores[bestIndex] = SCORE_LOWEST;
				return nonCaptures[bestIndex];
			}
			phase++;
		case PHASE_BAD_CAPTURES:
			maxScore = SCORE_LOWEST;
			bestIndex = -1;
			for (int i = 0; i<badCaptureIndex; i++) {
				if (badCapturesScores[i] > maxScore) {
					maxScore = badCapturesScores[i];
					bestIndex = i;
				}
			}
			if (bestIndex != -1) {
				badCapturesScores[bestIndex] = SCORE_LOWEST;
				return badCaptures[bestIndex];
			}
			break;
		}
		
		return 0;
	}
}