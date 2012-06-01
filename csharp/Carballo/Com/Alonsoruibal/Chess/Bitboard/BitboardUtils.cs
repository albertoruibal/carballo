/*
 * Code converted with Sharpen
 * 
 */using System;
using System.Text;
using Com.Alonsoruibal.Chess.Bitboard;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Bitboard
{
	/// <author>rui</author>
	public class BitboardUtils
	{
		public const long A8 = unchecked((long)(0x8000000000000000L));

		public const long H1 = unchecked((long)(0x0000000000000001L));

		public const long BLACK_SQUARES = unchecked((long)(0x55aa55aa55aa55aaL));

		public const long WHITE_SQUARES = unchecked((long)(0xaa55aa55aa55aa55L));

		public const long b_d = unchecked((long)(0x00000000000000ffL));

		public const long b_u = unchecked((long)(0xff00000000000000L));

		public const long b_r = unchecked((long)(0x0101010101010101L));

		public const long b_l = unchecked((long)(0x8080808080808080L));

		public const long b2_d = unchecked((long)(0x000000000000ffffL));

		public const long b2_u = unchecked((long)(0xffff000000000000L));

		public const long b2_r = unchecked((long)(0x0303030303030303L));

		public const long b2_l = unchecked((long)(0xC0C0C0C0C0C0C0C0L));

		public const long b3_d = unchecked((long)(0x0000000000ffffffL));

		public const long b3_u = unchecked((long)(0xffffff0000000000L));

		public const long r2_d = unchecked((long)(0x000000000000ff00L));

		public const long r2_u = unchecked((long)(0x00ff000000000000L));

		public const long r3_d = unchecked((long)(0x0000000000ff0000L));

		public const long r3_u = unchecked((long)(0x0000ff0000000000L));

		public const long c4 = unchecked((long)(0x0000001818000000L));

		public const long c16 = unchecked((long)(0x00003C3C3C3C0000L));

		public const long c36 = unchecked((long)(0x007E7E7E7E7E7E00L));

		public const long r4 = unchecked((long)(0xC3C300000000C3C3L));

		public const long r9 = unchecked((long)(0xE7E7E70000E7E7E7L));

		public static readonly long[] COLUMN = new long[] { b_l, b_r << 6, b_r << 5, b_r 
			<< 4, b_r << 3, b_r << 2, b_r << 1, b_r };

		public static readonly long[] COLUMNS_ADJACENTS = new long[] { COLUMN[1], COLUMN[
			0] | COLUMN[1], COLUMN[1] | COLUMN[3], COLUMN[2] | COLUMN[4], COLUMN[3] | COLUMN
			[5], COLUMN[4] | COLUMN[6], COLUMN[5] | COLUMN[7], COLUMN[6] };

		public static readonly long[] ROWS_LEFT = new long[] { 0, COLUMN[0], COLUMN[0] | 
			COLUMN[1], COLUMN[0] | COLUMN[1] | COLUMN[2], COLUMN[0] | COLUMN[1] | COLUMN[2] 
			| COLUMN[3], COLUMN[0] | COLUMN[1] | COLUMN[2] | COLUMN[3] | COLUMN[4], COLUMN[0
			] | COLUMN[1] | COLUMN[2] | COLUMN[3] | COLUMN[4] | COLUMN[5], COLUMN[0] | COLUMN
			[1] | COLUMN[2] | COLUMN[3] | COLUMN[4] | COLUMN[5] | COLUMN[6] };

		public static readonly long[] ROWS_RIGHT = new long[] { COLUMN[1] | COLUMN[2] | COLUMN
			[3] | COLUMN[4] | COLUMN[5] | COLUMN[6] | COLUMN[7], COLUMN[2] | COLUMN[3] | COLUMN
			[4] | COLUMN[5] | COLUMN[6] | COLUMN[7], COLUMN[3] | COLUMN[4] | COLUMN[5] | COLUMN
			[6] | COLUMN[7], COLUMN[4] | COLUMN[5] | COLUMN[6] | COLUMN[7], COLUMN[5] | COLUMN
			[6] | COLUMN[7], COLUMN[6] | COLUMN[7], COLUMN[7], 0 };

		public static readonly long[] RANK = new long[] { b_d, b_d << 8, b_d << 16, b_d <<
			 24, b_d << 32, b_d << 40, b_d << 48, b_d << 56 };

		public static readonly long[] RANKS_UPWARDS = new long[] { RANK[1] | RANK[2] | RANK
			[3] | RANK[4] | RANK[5] | RANK[6] | RANK[7], RANK[2] | RANK[3] | RANK[4] | RANK[
			5] | RANK[6] | RANK[7], RANK[3] | RANK[4] | RANK[5] | RANK[6] | RANK[7], RANK[4]
			 | RANK[5] | RANK[6] | RANK[7], RANK[5] | RANK[6] | RANK[7], RANK[6] | RANK[7], 
			RANK[7], 0 };

		public static readonly long[] RANKS_DOWNWARDS = new long[] { 0, RANK[0], RANK[0] 
			| RANK[1], RANK[0] | RANK[1] | RANK[2], RANK[0] | RANK[1] | RANK[2] | RANK[3], RANK
			[0] | RANK[1] | RANK[2] | RANK[3] | RANK[4], RANK[0] | RANK[1] | RANK[2] | RANK[
			3] | RANK[4] | RANK[5], RANK[0] | RANK[1] | RANK[2] | RANK[3] | RANK[4] | RANK[5
			] | RANK[6] };

		public static readonly long[][] RANKS_FORWARD = new long[][] { RANKS_UPWARDS, RANKS_DOWNWARDS
			 };

		public static readonly long[][] RANKS_BACKWARD = new long[][] { RANKS_DOWNWARDS, 
			RANKS_UPWARDS };

		public static readonly string[] squareNames = ChangeEndianArray64(new string[] { 
			"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8", "a7", "b7", "c7", "d7", "e7", "f7"
			, "g7", "h7", "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6", "a5", "b5", "c5", 
			"d5", "e5", "f5", "g5", "h5", "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", "a3"
			, "b3", "c3", "d3", "e3", "f3", "g3", "h3", "a2", "b2", "c2", "d2", "e2", "f2", 
			"g2", "h2", "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1" });

		public static readonly byte[] bitTable = new byte[] { 63, 30, 3, 32, 25, 41, 22, 
			33, 15, 50, 42, 13, 11, 53, 19, 34, 61, 29, 2, 51, 21, 43, 45, 10, 18, 47, 1, 54
			, 9, 57, 0, 35, 62, 31, 40, 4, 49, 5, 52, 26, 60, 6, 23, 44, 46, 27, 56, 16, 7, 
			39, 48, 24, 59, 14, 12, 55, 38, 28, 58, 20, 37, 17, 36, 8 };

		// Board borders
		// down
		// up
		// right
		// left
		// Board borders (2 squares),for the knight
		// down
		// up
		// right
		// left
		// down
		// up
		// rank 2 down
		// up
		// rank 3 down
		// up
		// Board centers (for evaluation)
		// center (4 squares)
		// center (16 squares)
		// center (36 squares)
		// corners (4 squares)
		// corners (9 squares)
		// 0 is a, 7 is g
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		// 0 is 1, 7 is 8
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		// Ranks fordward in pawn direction
		//
		//
		//
		//
		//
		//
		//
		//
		// To use with square2Index
		/// <summary>Converts a square to its index 0=H1, 63=A8</summary>
		public static byte Square2Index(long square)
		{
			long b = square ^ (square - 1);
			int fold = (int)(b ^ ((long)(((ulong)b) >> 32)));
			return bitTable[(int)(((uint)(fold * unchecked((int)(0x783a9b23)))) >> 26)];
		}

		/// <summary>And viceversa</summary>
		public static long Index2Square(byte index)
		{
			return H1 << index;
		}

		/// <summary>
		/// Changes element 0 with 63 and consecuvely: this way array constants are
		/// more legible
		/// </summary>
		public static string[] ChangeEndianArray64(string[] sarray)
		{
			string[] @out = new string[64];
			for (int i = 0; i < 64; i++)
			{
				@out[i] = sarray[63 - i];
			}
			return @out;
		}

		public static int[] ChangeEndianArray64(int[] sarray)
		{
			int[] @out = new int[64];
			for (int i = 0; i < 64; i++)
			{
				@out[i] = sarray[63 - i];
			}
			return @out;
		}

		/// <summary>prints a BitBoard to standard output</summary>
		public static string ToString(long b)
		{
			StringBuilder sb = new StringBuilder();
			long i = A8;
			while (i != 0)
			{
				sb.Append(((b & i) != 0 ? "1 " : "0 "));
				if ((i & b_r) != 0)
				{
					sb.Append("\n");
				}
				i = (long)(((ulong)i) >> 1);
			}
			return sb.ToString();
		}

		/// <summary>
		/// Flips board vertically
		/// https://chessprogramming.wikispaces.com/Flipping+Mirroring+and+Rotating
		/// </summary>
		/// <param name="in"></param>
		/// <returns></returns>
		public static long Vflip(long @in)
		{
			long k1 = unchecked((long)(0x00FF00FF00FF00FFL));
			long k2 = unchecked((long)(0x0000FFFF0000FFFFL));
			@in = (((long)(((ulong)@in) >> 8)) & k1) | ((@in & k1) << 8);
			@in = (((long)(((ulong)@in) >> 16)) & k2) | ((@in & k2) << 16);
			@in = ((long)(((ulong)@in) >> 32)) | (@in << 32);
			return @in;
		}

		/// <summary>
		/// Counts the number of bits of one long
		/// http://chessprogramming.wikispaces.com/Population+Count
		/// </summary>
		/// <param name="x"></param>
		/// <returns></returns>
		public static int PopCount(long x)
		{
			if (x == 0)
			{
				return 0;
			}
			long k1 = unchecked((long)(0x5555555555555555L));
			long k2 = unchecked((long)(0x3333333333333333L));
			long k4 = unchecked((long)(0x0f0f0f0f0f0f0f0fL));
			long kf = unchecked((long)(0x0101010101010101L));
			x = x - ((x >> 1) & k1);
			// put count of each 2 bits into those 2 bits
			x = (x & k2) + ((x >> 2) & k2);
			// put count of each 4 bits into those 4
			// bits
			x = (x + (x >> 4)) & k4;
			// put count of each 8 bits into those 8 bits
			x = (x * kf) >> 56;
			// returns 8 most significant bits of x + (x<<8) +
			// (x<<16) + (x<<24) + ...
			return (int)x;
		}

		/// <summary>
		/// Convert a bitboard square to algebraic notation Number depends of rotated
		/// board.
		/// </summary>
		/// <remarks>
		/// Convert a bitboard square to algebraic notation Number depends of rotated
		/// board.
		/// </remarks>
		/// <param name="square"></param>
		/// <returns></returns>
		public static string Square2Algebraic(long square)
		{
			return squareNames[Square2Index(square)];
		}

		public static string Index2Algebraic(int index)
		{
			return squareNames[index];
		}

		public static int Algebraic2Index(string name)
		{
			for (int i = 0; i < 64; i++)
			{
				if (name.Equals(squareNames[i]))
				{
					return i;
				}
			}
			return -1;
		}

		public static long Algebraic2Square(string name)
		{
			long aux = H1;
			for (int i = 0; i < 64; i++)
			{
				if (name.Equals(squareNames[i]))
				{
					return aux;
				}
				aux <<= 1;
			}
			return 0;
		}

		/// <summary>gets the column (0..7) of the square</summary>
		/// <param name="square"></param>
		/// <returns></returns>
		public static int GetColumn(long square)
		{
			for (int column = 0; column < 8; column++)
			{
				if ((COLUMN[column] & square) != 0)
				{
					return column;
				}
			}
			return 0;
		}

		/// <summary>Gets a long with the less significative bit of the board</summary>
		public static long Lsb(long board)
		{
			return board & (-board);
		}

		/// <summary>Distance between two indexes</summary>
		public static int Distance(int index1, int index2)
		{
			return Math.Max(Math.Abs((index1 & 7) - (index2 & 7)), Math.Abs((index1 >> 3) - (
				index2 >> 3)));
		}
	}
}
