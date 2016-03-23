package com.alonsoruibal.chess.evaluation;

/**
 * Generates the java code with the Piece-Square Values for the evaluator
 * <p/>
 * The "Diagonal" value sums twice, the position 0 is the longest diagonal (7 squares)
 */
public class PcsqGenerator {

	//
	// PAWN
	//
	private final static int[][] PawnColumn = {
			{-20, -8, -2, 5, 5, -2, -8, -20}, //
			{-4, -6, -8, -10, -10, -8, -6, -4}
	};
	private final static int[][] PawnRank = {
			{0, -3, -2, -1, 1, 2, 3, 0},
			{0, -3, -3, -2, -1, 0, 2, 0}
	};
	private final static int[][] PawnDiagonal = {
			{0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0}
	};
	//
	// KNIGHT
	//
	private final static int[][] KnightColumn = {
			{-26, -10, 1, 5, 5, 1, -10, -26},
			{-4, -1, 2, 4, 4, 2, -1, -4}
	};
	private final static int[][] KnightRank = {
			{-32, -10, 6, 15, 21, 19, 10, -11},
			{-10, -5, -2, 1, 3, 5, 2, -3}
	};
	private final static int[][] KnightDiagonal = {
			{0, 0, 0, 0, 0, 0, 0, 0},
			{2, 1, 0, -1, -2, -4, -7, -10}
	};
	//
	// BISHOP
	//
	private final static int[][] BishopColumn = {
			{0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0}
	};
	private final static int[][] BishopRank = {
			{-5, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0}
	};
	private final static int[][] BishopDiagonal = {
			{10, 5, 1, -3, -5, -7, -8, -12},
			{3, 2, 0, 0, -2, -2, -3, -3}
	};
	//
	// ROOK
	//
	private final static int[][] RookColumn = {
			{-4, 0, 4, 8, 8, 4, 0, -4},
			{0, 0, 0, 0, 0, 0, 0, 0}
	};
	private final static int[][] RookRank = {
			{0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 1, 1, 1, -2}
	};
	private final static int[][] RookDiagonal = {
			{0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0}
	};
	//
	// QUEEN
	//
	private final static int[][] QueenColumn = {
			{-2, 0, 1, 2, 2, 1, 0, -2},
			{-2, 0, 1, 2, 2, 1, 0, -2}
	};
	private final static int[][] QueenRank = {
			{-2, 0, 1, 2, 2, 1, 0, -2},
			{-2, 0, 1, 2, 2, 1, 0, -2}
	};
	private final static int[][] QueenDiagonal = {
			{3, 2, 1, 0, -2, -4, -7, -10},
			{1, 0, -1, -3, -4, -6, -8, -12}
	};
	//
	// KING
	//
	private final static int[][] KingColumn = {
			{40, 45, 15, -5, -5, 15, 45, 40},
			{-15, 0, 10, 15, 15, 10, 0, -15}
	};
	private final static int[][] KingRank = {
			{4, 1, -2, -5, -10, -15, -25, -35},
			{-15, 0, 10, 15, 15, 10, 0, -15}
	};
	private final static int[][] KingDiagonal = {
			{0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, -2, -5, -8, -12, -20, -30}
	};

	// Values are rotated for whites, so when white is playing is like shown in the code
	public int[] pawnPcsq = new int[64];
	public int[] knightPcsq = new int[64];
	public int[] bishopPcsq = new int[64];
	public int[] rookPcsq = new int[64];
	public int[] queenPcsq = new int[64];
	public int[] kingPcsq = new int[64];

	private int calculateSquare(int i, int[][] columnValues, int[][] rankValues, int[][] diagonalValues) {
		int rank = i >> 3;
		int column = 7 - i & 7;
		int diagonal1 = (((column - rank) >= 0) ? (column - rank) : -(column - rank));
		int diagonal2 = (((column + rank - 7) >= 0) ? (column + rank - 7) : -(column + rank - 7));

		return Evaluator.oe(
				columnValues[0][column] + rankValues[0][rank] + diagonalValues[0][diagonal1] + diagonalValues[0][diagonal2],
				columnValues[1][column] + rankValues[1][rank] + diagonalValues[1][diagonal1] + diagonalValues[1][diagonal2]
		);
	}

	public void generate() {
		int i;

		for (i = 0; i < 64; i++) {
			pawnPcsq[i] = calculateSquare(i, PawnColumn, PawnRank, PawnDiagonal);
			knightPcsq[i] = calculateSquare(i, KnightColumn, KnightRank, KnightDiagonal);
			bishopPcsq[i] = calculateSquare(i, BishopColumn, BishopRank, BishopDiagonal);
			rookPcsq[i] = calculateSquare(i, RookColumn, RookRank, RookDiagonal);
			queenPcsq[i] = calculateSquare(i, QueenColumn, QueenRank, QueenDiagonal);
			kingPcsq[i] = calculateSquare(i, KingColumn, KingRank, KingDiagonal);
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
			String aux = "oe(" + Evaluator.o(pcsq[i]) + ", " + Evaluator.e(pcsq[i]) + ")";
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
		PcsqGenerator generator = new PcsqGenerator();
		generator.generate();
		generator.print();
	}
}