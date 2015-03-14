package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.util.StringUtils;

public class ExperimentalPcsqGenerator {

	private final static int OPENING = 0;
	private final static int ENDGAME = 1;

	private final static int[][] PawnColumn = {{-20, -8, -2, 5, 5, -2, -8, -20}, //
			{-4, -6, -8, -10, -10, -8, -6, -4}};
	private final static int[][] PawnRank = {{0, -3, -2, -1, 1, 2, 3, 0}, //
			{0, -3, -3, -2, -1, 0, 2, 0}};
	private final static int[][] KnightColumn = {{-26, -10, 1, 5, 5, 1, -10, -26}, //
			{-4, -1, 2, 4, 4, 2, -1, -4}};
	private final static int[][] KnightRank = {{-32, -10, 6, 15, 21, 19, 10, -11}, //
			{-10, -5, -2, 1, 3, 5, 2, -3}};
	private final static int[][] KnightLine = {{0, 0, 0, 0, 0, 0, 0, 0}, //
			{2, 1, 0, -1, -2, -4, -7, -10}};
	private final static int[][] BishopLine = {{10, 5, 1, -3, -5, -7, -8, -12}, //
			{3, 2, 0, 0, -2, -2, -3, -3}};
	private final static int[][] BishopRank = {{-5, 0, 0, 0, 0, 0, 0, 0}, //
			{0, 0, 0, 0, 0, 0, 0, 0}};
	private final static int[][] RookColumn = {{-4, 0, 4, 8, 8, 4, 0, -4}, //
			{0, 0, 0, 0, 0, 0, 0, 0}};
	private final static int[][] RookRank = {{0, 0, 0, 0, 0, 0, 0, 0}, //
			{0, 0, 0, 0, 1, 1, 1, -2}};
	private final static int[][] QueenColumn = {{-2, 0, 1, 2, 2, 1, 0, -2}, //
			{-2, 0, 1, 2, 2, 1, 0, -2}};
	private final static int[][] QueenRank = {{-2, 0, 1, 2, 2, 1, 0, -2}, //
			{-2, 0, 1, 2, 2, 1, 0, -2}};
	private final static int[][] QueenLine = {{3, 2, 1, 0, -2, -4, -7, -10}, //
			{1, 0, -1, -3, -4, -6, -8, -12}};
	private final static int[][] KingColumn = {{40, 45, 15, -5, -5, 15, 45, 40}, //
			{-15, 0, 10, 15, 15, 10, 0, -15}};
	private final static int[][] KingRank = {{4, 1, -2, -5, -10, -15, -25, -35}, //
			{-15, 0, 10, 15, 15, 10, 0, -15}};
	private final static int[][] KingLine = {{0, 0, 0, 0, 0, 0, 0, 0}, //
			{2, 0, -2, -5, -8, -12, -20, -30}};

	// Values are rotated for whites, so when white is playing is like shown in the code
	public final static int[] pawnPcsq = new int[64];
	public final static int[] knightPcsq = new int[64];
	public final static int[] bishopPcsq = new int[64];
	public final static int[] rookPcsq = new int[64];
	public final static int[] queenPcsq = new int[64];
	public final static int[] kingPcsq = new int[64];

	public void generate() {
		// Initialize Piece square values
		int i;

		for (i = 0; i < 64; i++) {
			int rank = i >> 3;
			int column = 7 - i & 7;
			int d = (((column - rank) >= 0) ? (column - rank) : -(column - rank));
			int e = (((column + rank - 7) >= 0) ? (column + rank - 7) : -(column + rank - 7));

			pawnPcsq[i] = Evaluator.oe(PawnColumn[OPENING][column] + PawnRank[OPENING][rank],
					PawnColumn[ENDGAME][column] + PawnRank[ENDGAME][rank]);
			knightPcsq[i] = Evaluator.oe(KnightColumn[OPENING][column] + KnightRank[OPENING][rank] + KnightLine[OPENING][d] + KnightLine[OPENING][e],
					KnightColumn[ENDGAME][column] + KnightRank[ENDGAME][rank] + KnightLine[ENDGAME][d] + KnightLine[ENDGAME][e]);
			bishopPcsq[i] = Evaluator.oe(BishopRank[OPENING][rank] + BishopLine[OPENING][d] + BishopLine[OPENING][e],
					BishopRank[ENDGAME][rank] + BishopLine[ENDGAME][d] + BishopLine[ENDGAME][e]);
			rookPcsq[i] = Evaluator.oe(RookColumn[OPENING][column] + RookRank[OPENING][rank],
					RookColumn[ENDGAME][column] + RookRank[ENDGAME][rank]);
			queenPcsq[i] = Evaluator.oe(QueenColumn[OPENING][column] + QueenRank[OPENING][rank] + QueenLine[OPENING][d] + QueenLine[OPENING][e],
					QueenColumn[ENDGAME][column] + QueenRank[ENDGAME][rank] + QueenLine[ENDGAME][d] + QueenLine[ENDGAME][e]);
			kingPcsq[i] = Evaluator.oe(KingColumn[OPENING][column] + KingRank[OPENING][rank] + KingLine[OPENING][d] + KingLine[OPENING][e],
					KingColumn[ENDGAME][column] + KingRank[ENDGAME][rank] + KingLine[ENDGAME][d] + KingLine[ENDGAME][e]);
		}

		// Pawn opening corrections
		pawnPcsq[19] += Evaluator.oe(10, 0); // E3
		pawnPcsq[20] += Evaluator.oe(10, 0); // D3
		pawnPcsq[27] += Evaluator.oe(20, 0); // E4
		pawnPcsq[28] += Evaluator.oe(20, 0); // D4
		pawnPcsq[35] += Evaluator.oe(10, 0); // E5
		pawnPcsq[36] += Evaluator.oe(10, 0); // D5
	}

	public void print() {
		System.out.println("private final static int pawnPcsq[] = {");
		printPcsq(pawnPcsq);
		System.out.println("};");
		System.out.println("private final static int knightPcsq[] = {");
		printPcsq(knightPcsq);
		System.out.println("};");
		System.out.println("private final static int bishopPcsq[] = {");
		printPcsq(bishopPcsq);
		System.out.println("};");
		System.out.println("private final static int rookPcsq[] = {");
		printPcsq(rookPcsq);
		System.out.println("};");
		System.out.println("private final static int queenPcsq[] = {");
		printPcsq(queenPcsq);
		System.out.println("};");
		System.out.println("private final static int kingPcsq[] = {");
		printPcsq(kingPcsq);
		System.out.println("};");
	}

	private static void printPcsq(int pcsq[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 64; i++) {
			String aux = "oe(" + StringUtils.padLeft(String.valueOf(Evaluator.o(pcsq[i])), 4) + ", " + StringUtils.padLeft(String.valueOf(Evaluator.e(pcsq[i])), 4) + ")";
			sb.append(aux);
			if (i % 8 != 7) {
				sb.append(", ");
			} else if (i == 63) {
				sb.append("\n");
			} else {
				sb.append(",\n");
			}
		}
		System.out.print(sb.toString());
	}

	public static void main(String args[]) {
		// Prints bitbase in java array format
		ExperimentalPcsqGenerator generator = new ExperimentalPcsqGenerator();
		generator.generate();
		generator.print();
	}
}