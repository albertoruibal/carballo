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
	/// Two tier Transposition table using two keys
	/// odd positions are conserved the greatest depth first
	/// even positions are allways replaced
	/// Uses part of the board's zobrish key (shifted) as the index
	/// </summary>
	/// <author>rui</author>
	public class TwoTierTranspositionTable : TranspositionTable
	{
		/// <summary>Logger for this class</summary>
		private static readonly Logger logger = Logger.GetLogger("TwoTierTranspositionTable"
			);

		public long[] keys;

		public long[] infos;

		private int sizeBits;

		private int index;

		private int size;

		private long info;

		private byte generation;

		/// <summary>
		/// Whe must indicate the number in bits of the size
		/// example: 23 =&gt; 2^23 are 8 million entries
		/// </summary>
		/// <param name="sizeBits"></param>
		public TwoTierTranspositionTable(int sizeBits)
		{
			this.sizeBits = sizeBits;
			size = 1 << sizeBits;
			keys = new long[size];
			infos = new long[size];
			generation = 0;
			index = -1;
			logger.Debug("Created Two-Tier transposition table, size = " + size + " entries "
				 + size * 16 / (1024 * 1024) + "MB");
		}

		/// <summary>Returns true if key matches with key stored</summary>
		public override bool Search(Board board, bool exclusion)
		{
			info = 0;
			index = (int)((long)(((ulong)(exclusion ? board.GetExclusionKey() : board.GetKey(
				))) >> (64 - sizeBits))) & ~unchecked((int)(0x01));
			// Get the first odd index
			long key2 = board.GetKey2();
			// Verifies that is really this board
			if (keys[index] == key2 || keys[++index] == key2)
			{
				// Already returns the correct index
				info = infos[index];
				return true;
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

		public override void Set(Board board, int nodeType, int bestMove, int score, byte
			 depthAnalyzed, bool exclusion)
		{
			long key2 = board.GetKey2();
			index = (int)((long)(((ulong)(exclusion ? board.GetExclusionKey() : board.GetKey(
				))) >> (64 - sizeBits))) & ~unchecked((int)(0x01));
			// Get the first odd index
			info = infos[index];
			if (keys[index] == 0 || ((sbyte)GetDepthAnalyzed()) <= depthAnalyzed || GetGeneration
				() != generation)
			{
			}
			else
			{
				// Replace odd entry
				// Replace even entry
				index++;
			}
			keys[index] = key2;
			info = (bestMove & unchecked((int)(0x1fffff))) | ((nodeType & unchecked((int)(0xf
				))) << 21) | (((long)(generation & unchecked((int)(0xff)))) << 32) | (((long)(depthAnalyzed
				 & unchecked((int)(0xff)))) << 40) | (((long)(score & unchecked((int)(0xffff))))
				 << 48);
			infos[index] = info;
		}

		// called at the start of each search
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
