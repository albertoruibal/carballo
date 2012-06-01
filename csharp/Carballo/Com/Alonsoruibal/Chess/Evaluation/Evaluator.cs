/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Evaluation;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Evaluation
{
	public abstract class Evaluator
	{
		public const int VICTORY = short.MaxValue - 1;

		//	private final static Random random = new Random(System.currentTimeMillis());
		/// <summary>Board evaluator</summary>
		public abstract int EvaluateBoard(Board board, int alpha, int beta);

		public static int Oe(int opening, int endgame)
		{
			return (int)(((short)(opening)) << 16) + (short)(endgame);
		}

		public static int O(int oe)
		{
			return oe >> 16;
		}

		public static int E(int oe)
		{
			return (short)(oe & unchecked((int)(0xffff)));
		}
	}
}
