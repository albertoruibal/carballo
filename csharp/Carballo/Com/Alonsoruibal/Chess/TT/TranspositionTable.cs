/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.TT;
using Sharpen;

namespace Com.Alonsoruibal.Chess.TT
{
	public abstract class TranspositionTable
	{
		public const int TYPE_EXACT_SCORE = 1;

		public const int TYPE_FAIL_LOW = 2;

		public const int TYPE_FAIL_HIGH = 3;

		public const int TYPE_EVAL = 4;

		/// <summary>Returns true if key matches with key stored</summary>
		public abstract bool Search(Board board, bool exclusion);

		public abstract int GetBestMove();

		public abstract int GetNodeType();

		public abstract byte GetGeneration();

		public abstract bool IsMyGeneration();

		public abstract byte GetDepthAnalyzed();

		public abstract int GetScore();

		public virtual void Save(Board board, byte depthAnalyzed, int bestMove, int score
			, int lowerBound, int upperBound, bool exclusion)
		{
			if (score <= lowerBound)
			{
				Set(board, TranspositionTable.TYPE_FAIL_LOW, bestMove, score, depthAnalyzed, exclusion
					);
			}
			else
			{
				if (score >= upperBound)
				{
					Set(board, TranspositionTable.TYPE_FAIL_HIGH, bestMove, score, depthAnalyzed, exclusion
						);
				}
				else
				{
					Set(board, TranspositionTable.TYPE_EXACT_SCORE, bestMove, score, depthAnalyzed, exclusion
						);
				}
			}
		}

		public abstract void Set(Board board, int nodeType, int bestMove, int score, byte
			 depthAnalyzed, bool exclusion);

		// called at the start of each search
		public abstract void NewGeneration();

		public abstract void Clear();
	}
}
