package com.alonsoruibal.chess.evaluation;

import com.alonsoruibal.chess.util.StringUtils;

public class CompletePcsqGenerator {

	// The pair of values are {opening, endgame}
	private final static int PawnColumnValue = Evaluator.oe(5, 0);
	private final static int KnightCenterValue = Evaluator.oe(5, 5);
	private final static int KnightRankValue = Evaluator.oe(5, 0);
	private final static int KnightBackRankValue = Evaluator.oe(0, 0);
	private final static int KnightTrappedValue = Evaluator.oe(-100, 0);
	private final static int BishopCenterValue = Evaluator.oe(2, 3);
	private final static int BishopBackRankValue = Evaluator.oe(-10, 0);
	private final static int BishopDiagonalValue = Evaluator.oe(4, 0);
	private final static int RookColumnValue = Evaluator.oe(3, 0);
	private final static int QueenCenterValue = Evaluator.oe(0, 4);
	private final static int QueenBackRankValue = Evaluator.oe(-5, 0);
	private final static int KingCenterValue = Evaluator.oe(0, 12);
	private final static int KingColumnValue = Evaluator.oe(10, 0);
	private final static int KingRankValue = Evaluator.oe(10, 0);

	private final static int[] PawnColumn = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] KnightLine = {-4, -2, +0, +1, +1, +0, -2, -4};
	private final static int[] KnightRank = {-2, -1, +0, +1, +2, +3, +2, +1};
	private final static int[] BishopLine = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] RookColumn = {-2, -1, +0, +1, +1, +0, -1, -2};
	private final static int[] QueenLine = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] KingLine = {-3, -1, +0, +1, +1, +0, -1, -3};
	private final static int[] KingColumn = {+3, +4, +2, +0, +0, +2, +4, +3};
	private final static int[] KingRank = {+1, +0, -2, -3, -4, -5, -6, -7};

	// Values are rotated for whites, so when white is playing is like shown in the code
	public int[] pawnPcsq = new int[64];
	public int[] knightPcsq = new int[64];
	public int[] bishopPcsq = new int[64];
	public int[] rookPcsq = new int[64];
	public int[] queenPcsq = new int[64];
	public int[] kingPcsq = new int[64];

	public void generate() {
		// Initialize Piece square values Fruit/Toga style
		int i;

		for (i = 0; i < 64; i++) {
			int rank = i >> 3;
			int column = 7 - i & 7;

			pawnPcsq[i] = Evaluator.oeMul(PawnColumn[column], PawnColumnValue);
			knightPcsq[i] = Evaluator.oeMul(KnightLine[column], KnightCenterValue) + Evaluator.oeMul(KnightLine[rank], KnightCenterValue) + Evaluator.oeMul(KnightRank[rank], KnightRankValue);
			bishopPcsq[i] = Evaluator.oeMul(BishopLine[column], BishopCenterValue) + Evaluator.oeMul(BishopLine[rank], BishopCenterValue);
			rookPcsq[i] = Evaluator.oeMul(RookColumn[column], RookColumnValue);
			queenPcsq[i] = Evaluator.oeMul(QueenLine[column], QueenCenterValue) + Evaluator.oeMul(QueenLine[rank], QueenCenterValue);
			kingPcsq[i] = Evaluator.oeMul(KingColumn[column], KingColumnValue) + Evaluator.oeMul(KingRank[rank], KingRankValue) + Evaluator.oeMul(KingLine[column], KingCenterValue) + Evaluator.oeMul(KingLine[rank], KingCenterValue);
		}

		knightPcsq[56] += KnightTrappedValue; // H8
		knightPcsq[63] += KnightTrappedValue; // A8

		for (i = 0; i < 8; i++) {
			queenPcsq[i] += QueenBackRankValue;
			knightPcsq[i] += KnightBackRankValue;
			bishopPcsq[i] += BishopBackRankValue;
			bishopPcsq[(i << 3) | i] += BishopDiagonalValue;
			bishopPcsq[((i << 3) | i) ^ 0x38] += BishopDiagonalValue;
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
		// Prints Pcsq in java array format
		CompletePcsqGenerator generator = new CompletePcsqGenerator();
		generator.generate();
		generator.print();
	}
}
