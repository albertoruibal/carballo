/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Bitboard;
using Com.Alonsoruibal.Chess.Log;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Bitboard
{
	/// <summary>Discover attacks to squares</summary>
	public class BitboardAttacks
	{
		private static readonly Logger logger = Logger.GetLogger("BitboardAttacks");

		public long[] rook;

		public long[] bishop;

		public long[] knight;

		public long[] king;

		public long[] pawnDownwards;

		public long[] pawnUpwards;

		/// <summary>
		/// If disabled, does not use Magic Bitboards, improves loading speed in GWT
		/// and avoids memory crashes in mobile browsers
		/// </summary>
		public static bool USE_MAGIC = true;

		internal static Com.Alonsoruibal.Chess.Bitboard.BitboardAttacks instance;

		public static Com.Alonsoruibal.Chess.Bitboard.BitboardAttacks GetInstance()
		{
			if (instance == null)
			{
				if (USE_MAGIC)
				{
					instance = new BitboardAttacksMagic();
				}
				else
				{
					instance = new Com.Alonsoruibal.Chess.Bitboard.BitboardAttacks();
				}
			}
			return instance;
		}

		internal virtual long SquareAttackedAux(long square, int shift, long border)
		{
			if ((square & border) == 0)
			{
				if (shift > 0)
				{
					square <<= shift;
				}
				else
				{
					square = (long)(((ulong)square) >> -shift);
				}
				return square;
			}
			return 0;
		}

		internal virtual long SquareAttackedAuxSlider(long square, int shift, long border
			)
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
				ret |= square;
			}
			return ret;
		}

		public BitboardAttacks()
		{
			logger.Debug("Generating attack tables...");
			long time1 = Runtime.CurrentTimeMillis();
			rook = new long[64];
			bishop = new long[64];
			knight = new long[64];
			king = new long[64];
			pawnDownwards = new long[64];
			pawnUpwards = new long[64];
			long square = 1;
			byte i = 0;
			while (square != 0)
			{
				rook[i] = SquareAttackedAuxSlider(square, +8, BitboardUtils.b_u) | SquareAttackedAuxSlider
					(square, -8, BitboardUtils.b_d) | SquareAttackedAuxSlider(square, -1, BitboardUtils
					.b_r) | SquareAttackedAuxSlider(square, +1, BitboardUtils.b_l);
				//
				//
				//
				bishop[i] = SquareAttackedAuxSlider(square, +9, BitboardUtils.b_u | BitboardUtils
					.b_l) | SquareAttackedAuxSlider(square, +7, BitboardUtils.b_u | BitboardUtils.b_r
					) | SquareAttackedAuxSlider(square, -7, BitboardUtils.b_d | BitboardUtils.b_l) |
					 SquareAttackedAuxSlider(square, -9, BitboardUtils.b_d | BitboardUtils.b_r);
				//
				//
				//
				knight[i] = SquareAttackedAux(square, +17, BitboardUtils.b2_u | BitboardUtils.b_l
					) | SquareAttackedAux(square, +15, BitboardUtils.b2_u | BitboardUtils.b_r) | SquareAttackedAux
					(square, -15, BitboardUtils.b2_d | BitboardUtils.b_l) | SquareAttackedAux(square
					, -17, BitboardUtils.b2_d | BitboardUtils.b_r) | SquareAttackedAux(square, +10, 
					BitboardUtils.b_u | BitboardUtils.b2_l) | SquareAttackedAux(square, +6, BitboardUtils
					.b_u | BitboardUtils.b2_r) | SquareAttackedAux(square, -6, BitboardUtils.b_d | BitboardUtils
					.b2_l) | SquareAttackedAux(square, -10, BitboardUtils.b_d | BitboardUtils.b2_r);
				//
				//
				//
				//
				//
				//
				//
				pawnUpwards[i] = SquareAttackedAux(square, 7, BitboardUtils.b_u | BitboardUtils.b_r
					) | SquareAttackedAux(square, 9, BitboardUtils.b_u | BitboardUtils.b_l);
				//
				pawnDownwards[i] = SquareAttackedAux(square, -7, BitboardUtils.b_d | BitboardUtils
					.b_l) | SquareAttackedAux(square, -9, BitboardUtils.b_d | BitboardUtils.b_r);
				//
				king[i] = SquareAttackedAux(square, +8, BitboardUtils.b_u) | SquareAttackedAux(square
					, -8, BitboardUtils.b_d) | SquareAttackedAux(square, -1, BitboardUtils.b_r) | SquareAttackedAux
					(square, +1, BitboardUtils.b_l) | SquareAttackedAux(square, +9, BitboardUtils.b_u
					 | BitboardUtils.b_l) | SquareAttackedAux(square, +7, BitboardUtils.b_u | BitboardUtils
					.b_r) | SquareAttackedAux(square, -7, BitboardUtils.b_d | BitboardUtils.b_l) | SquareAttackedAux
					(square, -9, BitboardUtils.b_d | BitboardUtils.b_r);
				//
				//
				//
				//
				//
				//
				//
				square <<= 1;
				i++;
			}
			long time2 = Runtime.CurrentTimeMillis();
			logger.Debug("Generated attack tables in " + (time2 - time1) + "ms");
		}

		/// <summary>Discover attacks to squares using magics: expensive version</summary>
		public virtual bool IsSquareAttacked(Board board, long square, bool white)
		{
			return IsIndexAttacked(board, BitboardUtils.Square2Index(square), white);
		}

		/// <summary>Discover attacks to squares using magics: cheap version</summary>
		public virtual bool IsIndexAttacked(Board board, byte index, bool white)
		{
			if (((sbyte)index) < 0 || index > 63)
			{
				return false;
			}
			long others = (white ? board.blacks : board.whites);
			long all = board.GetAll();
			if (((white ? pawnUpwards[index] : pawnDownwards[index]) & board.pawns & others) 
				!= 0)
			{
				return true;
			}
			else
			{
				if ((king[index] & board.kings & others) != 0)
				{
					return true;
				}
				else
				{
					if ((knight[index] & board.knights & others) != 0)
					{
						return true;
					}
					else
					{
						if ((GetRookAttacks(index, all) & (board.rooks | board.queens) & others) != 0)
						{
							return true;
						}
						else
						{
							if ((GetBishopAttacks(index, all) & (board.bishops | board.queens) & others) != 0)
							{
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		/// <summary>Discover attacks to squares using magics: cheap version</summary>
		public virtual long GetIndexAttacks(Board board, int index)
		{
			if (index < 0 || index > 63)
			{
				return 0;
			}
			long all = board.GetAll();
			return ((board.blacks & pawnUpwards[index] | board.whites & pawnDownwards[index])
				 & board.pawns) | (king[index] & board.kings) | (knight[index] & board.knights) 
				| (GetRookAttacks(index, all) & (board.rooks | board.queens)) | (GetBishopAttacks
				(index, all) & (board.bishops | board.queens));
		}

		public virtual long GetXrayAttacks(Board board, int index, long all)
		{
			if (index < 0 || index > 63)
			{
				return 0;
			}
			return ((GetRookAttacks(index, all) & (board.rooks | board.queens)) | (GetBishopAttacks
				(index, all) & (board.bishops | board.queens))) & all;
		}

		/// <summary>without magic bitboards, too expensive, but uses less memory</summary>
		public virtual long GetRookAttacks(int index, long all)
		{
			return GetRookShiftAttacks(BitboardUtils.Index2Square(unchecked((byte)index)), all
				);
		}

		public virtual long GetBishopAttacks(int index, long all)
		{
			return GetBishopShiftAttacks(BitboardUtils.Index2Square(unchecked((byte)index)), 
				all);
		}

		public virtual long GetRookShiftAttacks(long square, long all)
		{
			return CheckSquareAttackedAux(square, all, +8, BitboardUtils.b_u) | CheckSquareAttackedAux
				(square, all, -8, BitboardUtils.b_d) | CheckSquareAttackedAux(square, all, -1, BitboardUtils
				.b_r) | CheckSquareAttackedAux(square, all, +1, BitboardUtils.b_l);
		}

		public virtual long GetBishopShiftAttacks(long square, long all)
		{
			return CheckSquareAttackedAux(square, all, +9, BitboardUtils.b_u | BitboardUtils.
				b_l) | CheckSquareAttackedAux(square, all, +7, BitboardUtils.b_u | BitboardUtils
				.b_r) | CheckSquareAttackedAux(square, all, -7, BitboardUtils.b_d | BitboardUtils
				.b_l) | CheckSquareAttackedAux(square, all, -9, BitboardUtils.b_d | BitboardUtils
				.b_r);
		}

		/// <summary>Attacks for sliding pieces</summary>
		private long CheckSquareAttackedAux(long square, long all, int shift, long border
			)
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
				ret |= square;
				// If we collide with other piece
				if ((square & all) != 0)
				{
					break;
				}
			}
			return ret;
		}
	}
}
