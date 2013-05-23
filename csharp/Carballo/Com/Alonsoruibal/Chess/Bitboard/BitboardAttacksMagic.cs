/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess.Bitboard;
using Com.Alonsoruibal.Chess.Log;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Bitboard
{
	public class BitboardAttacksMagic : BitboardAttacks
	{
		private static readonly Logger logger = Logger.GetLogger("BitboardAttacksMagic");

		public static readonly byte[] rookShiftBits = new byte[] { 12, 11, 11, 11, 11, 11
			, 11, 12, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10
			, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10
			, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 12, 11, 11, 11, 11, 11, 11, 12 };

		public static readonly byte[] bishopShiftBits = new byte[] { 6, 5, 5, 5, 5, 5, 5, 
			6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 7, 7, 7, 7, 5, 5, 5, 5, 7, 9, 9, 7, 5, 5, 5, 5, 
			7, 9, 9, 7, 5, 5, 5, 5, 7, 7, 7, 7, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 
			5, 5, 6 };

		public static readonly long[] rookMagicNumber = new long[] { unchecked((long)(0x1080108000400020L
			)), unchecked((long)(0x40200010004000L)), unchecked((long)(0x100082000441100L)), 
			unchecked((long)(0x480041000080080L)), unchecked((long)(0x100080005000210L)), unchecked(
			(long)(0x100020801000400L)), unchecked((long)(0x280010000800200L)), unchecked((long
			)(0x100008020420100L)), unchecked((long)(0x400800080400020L)), unchecked((long)(
			0x401000402000L)), unchecked((long)(0x100801000200080L)), unchecked((long)(0x801000800800L
			)), unchecked((long)(0x800400080080L)), unchecked((long)(0x800200800400L)), unchecked(
			(long)(0x1000200040100L)), unchecked((long)(0x4840800041000080L)), unchecked((long
			)(0x20008080004000L)), unchecked((long)(0x404010002000L)), unchecked((long)(0x808010002000L
			)), unchecked((long)(0x828010000800L)), unchecked((long)(0x808004000800L)), unchecked(
			(long)(0x14008002000480L)), unchecked((long)(0x40002100801L)), unchecked((long)(
			0x20001004084L)), unchecked((long)(0x802080004000L)), unchecked((long)(0x200080400080L
			)), unchecked((long)(0x810001080200080L)), unchecked((long)(0x10008080080010L)), 
			unchecked((long)(0x4000080080040080L)), unchecked((long)(0x40080020080L)), unchecked(
			(long)(0x1000100040200L)), unchecked((long)(0x80008200004124L)), unchecked((long
			)(0x804000800020L)), unchecked((long)(0x804000802000L)), unchecked((long)(0x801000802000L
			)), unchecked((long)(0x2000801000800804L)), unchecked((long)(0x80080800400L)), unchecked(
			(long)(0x80040080800200L)), unchecked((long)(0x800100800200L)), unchecked((long)
			(0x8042000104L)), unchecked((long)(0x208040008008L)), unchecked((long)(0x10500020004000L
			)), unchecked((long)(0x100020008080L)), unchecked((long)(0x2000100008008080L)), 
			unchecked((long)(0x200040008008080L)), unchecked((long)(0x8020004008080L)), unchecked(
			(long)(0x1000200010004L)), unchecked((long)(0x100040080420001L)), unchecked((long
			)(0x80004000200040L)), unchecked((long)(0x200040100140L)), unchecked((long)(0x20004800100040L
			)), unchecked((long)(0x100080080280L)), unchecked((long)(0x8100800400080080L)), 
			unchecked((long)(0x8004020080040080L)), unchecked((long)(0x9001000402000100L)), 
			unchecked((long)(0x40080410200L)), unchecked((long)(0x208040110202L)), unchecked(
			(long)(0x800810022004012L)), unchecked((long)(0x1000820004011L)), unchecked((long
			)(0x1002004100009L)), unchecked((long)(0x41001002480005L)), unchecked((long)(0x81000208040001L
			)), unchecked((long)(0x4000008201100804L)), unchecked((long)(0x2841008402L)) };

		public static readonly long[] bishopMagicNumber = new long[] { unchecked((long)(0x1020041000484080L
			)), unchecked((long)(0x20204010a0000L)), unchecked((long)(0x8020420240000L)), unchecked(
			(long)(0x404040085006400L)), unchecked((long)(0x804242000000108L)), unchecked((long
			)(0x8901008800000L)), unchecked((long)(0x1010110400080L)), unchecked((long)(0x402401084004L
			)), unchecked((long)(0x1000200810208082L)), unchecked((long)(0x20802208200L)), unchecked(
			(long)(0x4200100102082000L)), unchecked((long)(0x1024081040020L)), unchecked((long
			)(0x20210000000L)), unchecked((long)(0x8210400100L)), unchecked((long)(0x10110022000L
			)), unchecked((long)(0x80090088010820L)), unchecked((long)(0x8001002480800L)), unchecked(
			(long)(0x8102082008200L)), unchecked((long)(0x41001000408100L)), unchecked((long
			)(0x88000082004000L)), unchecked((long)(0x204000200940000L)), unchecked((long)(0x410201100100L
			)), unchecked((long)(0x2000101012000L)), unchecked((long)(0x40201008200c200L)), 
			unchecked((long)(0x10100004204200L)), unchecked((long)(0x2080020010440L)), unchecked(
			(long)(0x480004002400L)), unchecked((long)(0x2008008008202L)), unchecked((long)(
			0x1010080104000L)), unchecked((long)(0x1020001004106L)), unchecked((long)(0x1040200520800L
			)), unchecked((long)(0x8410000840101L)), unchecked((long)(0x1201000200400L)), unchecked(
			(long)(0x2029000021000L)), unchecked((long)(0x4002400080840L)), unchecked((long)
			(0x5000020080080080L)), unchecked((long)(0x1080200002200L)), unchecked((long)(0x4008202028800L
			)), unchecked((long)(0x2080210010080L)), unchecked((long)(0x800809200008200L)), 
			unchecked((long)(0x1082004001000L)), unchecked((long)(0x1080202411080L)), unchecked(
			(long)(0x840048010101L)), unchecked((long)(0x40004010400200L)), unchecked((long)
			(0x500811020800400L)), unchecked((long)(0x20200040800040L)), unchecked((long)(0x1008012800830a00L
			)), unchecked((long)(0x1041102001040L)), unchecked((long)(0x11010120200000L)), unchecked(
			(long)(0x2020222020c00L)), unchecked((long)(0x400002402080800L)), unchecked((long
			)(0x20880000L)), unchecked((long)(0x1122020400L)), unchecked((long)(0x11100248084000L
			)), unchecked((long)(0x210111000908000L)), unchecked((long)(0x2048102020080L)), 
			unchecked((long)(0x1000108208024000L)), unchecked((long)(0x1004100882000L)), unchecked(
			(long)(0x41044100L)), unchecked((long)(0x840400L)), unchecked((long)(0x4208204L)
			), unchecked((long)(0x80000200282020cL)), unchecked((long)(0x8a001240100L)), unchecked(
			(long)(0x2040104040080L)) };

		public long[] rookMask;

		public long[][] rookMagic;

		public long[] bishopMask;

		public long[][] bishopMagic;

		public BitboardAttacksMagic() : base()
		{
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
			// Magic numbers generated with MagicNumbersGen
			// Mask = Attacks without border for magic bitboards
			logger.Debug("Generating magic tables...");
			long time1 = Runtime.CurrentTimeMillis();
			rookMask = new long[64];
			rookMagic = new long[64][];
			bishopMask = new long[64];
			bishopMagic = new long[64][];
			long square = 1;
			byte i = 0;
			while (square != 0)
			{
				rookMask[i] = SquareAttackedAuxSliderMask(square, +8, BitboardUtils.b_u) | SquareAttackedAuxSliderMask
					(square, -8, BitboardUtils.b_d) | SquareAttackedAuxSliderMask(square, -1, BitboardUtils
					.b_r) | SquareAttackedAuxSliderMask(square, +1, BitboardUtils.b_l);
				//
				//
				//
				bishopMask[i] = SquareAttackedAuxSliderMask(square, +9, BitboardUtils.b_u | BitboardUtils
					.b_l) | SquareAttackedAuxSliderMask(square, +7, BitboardUtils.b_u | BitboardUtils
					.b_r) | SquareAttackedAuxSliderMask(square, -7, BitboardUtils.b_d | BitboardUtils
					.b_l) | SquareAttackedAuxSliderMask(square, -9, BitboardUtils.b_d | BitboardUtils
					.b_r);
				//
				//
				//
				// And now generate magics
				int rookPositions = (1 << rookShiftBits[i]);
				rookMagic[i] = new long[rookPositions];
				for (int j = 0; j < rookPositions; j++)
				{
					long pieces = GeneratePieces(j, rookShiftBits[i], rookMask[i]);
					int magicIndex = MagicTransform(pieces, rookMagicNumber[i], rookShiftBits[i]);
					rookMagic[i][magicIndex] = GetRookShiftAttacks(square, pieces);
				}
				int bishopPositions = (1 << bishopShiftBits[i]);
				bishopMagic[i] = new long[bishopPositions];
				for (int j_1 = 0; j_1 < bishopPositions; j_1++)
				{
					long pieces = GeneratePieces(j_1, bishopShiftBits[i], bishopMask[i]);
					int magicIndex = MagicTransform(pieces, bishopMagicNumber[i], bishopShiftBits[i]);
					bishopMagic[i][magicIndex] = GetBishopShiftAttacks(square, pieces);
				}
				square <<= 1;
				i++;
			}
			long time2 = Runtime.CurrentTimeMillis();
			logger.Debug("Generated attack tables in " + (time2 - time1) + "ms");
		}

		/// <summary>Fills pieces from a mask.</summary>
		/// <remarks>
		/// Fills pieces from a mask. Necessary for magic generation variable bits is
		/// the mask bytes number index goes from 0 to 2^bits
		/// </remarks>
		private long GeneratePieces(int index, int bits, long mask)
		{
			int i;
			long lsb;
			long result = 0L;
			for (i = 0; i < bits; i++)
			{
				lsb = mask & (-mask);
				mask ^= lsb;
				// Deactivates lsb bit of the mask to get next bit next
				// time
				if ((index & (1 << i)) != 0)
				{
					result |= lsb;
				}
			}
			// if bit is set to 1
			return result;
		}

		private long SquareAttackedAuxSliderMask(long square, int shift, long border)
		{
			long ret = 0;
			while ((square & border) == 0)
			{
				if (shift > 0)
				{
					square <<= shift;
				}
				else
				{
					square = (long)(((ulong)square) >> -shift);
				}
				if ((square & border) == 0)
				{
					ret |= square;
				}
			}
			return ret;
		}

		public static int MagicTransform(long b, long magic, byte bits)
		{
			return (int)((long)(((ulong)(b * magic)) >> (64 - bits)));
		}

		/// <summary>Magic! attacks, very fast method</summary>
		public override long GetRookAttacks(int index, long all)
		{
			int i = MagicTransform(all & rookMask[index], rookMagicNumber[index], rookShiftBits
				[index]);
			return rookMagic[index][i];
		}

		public override long GetBishopAttacks(int index, long all)
		{
			int i = MagicTransform(all & bishopMask[index], bishopMagicNumber[index], bishopShiftBits
				[index]);
			return bishopMagic[index][i];
		}
	}
}
