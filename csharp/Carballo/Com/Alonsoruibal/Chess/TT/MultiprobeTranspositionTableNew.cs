/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Log;
using Com.Alonsoruibal.Chess.TT;
using Sharpen;

namespace Com.Alonsoruibal.Chess.TT
{
	/// <summary>
	/// Transposition table using two keys and multiprobe
	/// Uses part of the board's zobrish key (shifted) as the index
	/// </summary>
	/// <author>rui</author>
	public class MultiprobeTranspositionTableNew : TranspositionTable
	{
		/// <summary>Logger for this class</summary>
		private static readonly Logger logger = Logger.GetLogger("MultiprobeTranspositionTableNew"
			);

		private const int MAX_PROBES = 4;

		public long[] keys;

		public long[] infos;

		private int sizeBits;

		private int index;

		private int size;

		private long info;

		private byte generation;

		/// <summary>
		/// Whe must indicate the number in bits of the size example: 23 =&gt; 2^23 are
		/// 8 million entries
		/// </summary>
		/// <param name="sizeBits"></param>
		public MultiprobeTranspositionTableNew(int sizeBits)
		{
			this.sizeBits = sizeBits;
			size = 1 << sizeBits;
			keys = new long[size];
			infos = new long[size];
			generation = 0;
			index = -1;
			logger.Debug("Created Multiprobe transposition table New, size = " + size + " entries "
				 + size * 16 / (1024 * 1024) + "MB");
		}

		public override bool Search(Board board, bool exclusion)
		{
			info = 0;
			int startIndex = (int)((long)(((ulong)(exclusion ? board.GetExclusionKey() : board
				.GetKey())) >> (64 - sizeBits))) & ~unchecked((int)(0x03));
			// Verifies that is really this board
			for (index = startIndex; index < startIndex + MAX_PROBES && index < size; index++)
			{
				if (keys[index] == board.GetKey2())
				{
					info = infos[index];
					return true;
				}
			}
			return false;
		}

		public override int GetBestMove()
		{
			return (int)(info & unchecked((int)(0x1fffff)));
		}

		public override int GetNodeType()
		{
			return (int)(((long)(((ulong)info) >> 21)) & unchecked((int)(0xf)));
		}

		public override byte GetGeneration()
		{
			return unchecked((byte)(((long)(((ulong)info) >> 32)) & unchecked((int)(0xff))));
		}

		public override byte GetDepthAnalyzed()
		{
			return unchecked((byte)(((long)(((ulong)info) >> 40)) & unchecked((int)(0xff))));
		}

		public override int GetScore()
		{
			return (short)(((long)(((ulong)info) >> 48)) & unchecked((int)(0xffff)));
		}

		/// <summary>In case of collision overwrites replace the eldest keep pv nodes</summary>
		public override void Set(Board board, int nodeType, int bestMove, int score, byte
			 depthAnalyzed, bool exclusion)
		{
			long key2 = board.GetKey2();
			int startIndex = (int)((long)(((ulong)(exclusion ? board.GetExclusionKey() : board
				.GetKey())) >> (64 - sizeBits))) & ~unchecked((int)(0x03));
			// Verifies that is really this board
			index = -1;
			for (int i = startIndex; i < startIndex + MAX_PROBES; i++)
			{
				info = infos[i];
				if (keys[i] == 0 || (keys[i] == key2))
				{
					// Empty or replace
					if (keys[i] != 0 && GetGeneration() == generation && (depthAnalyzed == 0 || GetDepthAnalyzed
						() > depthAnalyzed))
					{
						// When
						// replacing
						// something
						// my
						// generation
						// || (getDepthAnalyzed() == depthAnalyzed &&
						// getNodeType() == TYPE_EXACT_SCORE && nodeType !=
						// TYPE_EXACT_SCORE)
						return;
					}
					// Never replace with eval values or lower depth or
					// exact scores with other nodetypes
					index = i;
					if (keys[i] == key2 && bestMove == 0)
					{
						bestMove = GetBestMove();
					}
					// Keep best move when replacing
					// and no move
					break;
				}
				if (GetGeneration() != generation || ((sbyte)GetDepthAnalyzed()) < depthAnalyzed)
				{
					// TODO
					// <
					// or
					// >=
					// ?
					index = i;
					break;
				}
			}
			if (index == -1)
			{
				return;
			}
			// No slot found
			keys[index] = key2;
			info = (bestMove & unchecked((int)(0x1fffff))) | ((nodeType & unchecked((int)(0xf
				))) << 21) | (((long)(generation & unchecked((int)(0xff)))) << 32) | (((long)(depthAnalyzed
				 & unchecked((int)(0xff)))) << 40) | (((long)(score & unchecked((int)(0xffff))))
				 << 48);
			infos[index] = info;
		}

		public override void NewGeneration()
		{
			generation++;
		}

		public override bool IsMyGeneration()
		{
			return GetGeneration() == generation;
		}

		public override void Clear()
		{
			Arrays.Fill(keys, 0);
		}
	}
}
