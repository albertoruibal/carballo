/*
 * Code converted with Sharpen
 * 
 */using System.Text;
using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Bitboard;
using Sharpen;

namespace Com.Alonsoruibal.Chess
{
	/// <summary>
	/// For efficience Moves are int, this is a static class to threat with this
	/// moves score is limited to 10 bits (positive) 1024 values
	/// </summary>
	/// <author>Alberto Alonso Ruibal</author>
	public class Move
	{
		public const int PAWN = 1;

		public const int KNIGHT = 2;

		public const int BISHOP = 3;

		public const int ROOK = 4;

		public const int QUEEN = 5;

		public const int KING = 6;

		public const int TYPE_KINGSIDE_CASTLING = 1;

		public const int TYPE_QUEENSIDE_CASTLING = 2;

		public const int TYPE_PASSANT = 3;

		public const int TYPE_PROMOTION_QUEEN = 4;

		public const int TYPE_PROMOTION_KNIGHT = 5;

		public const int TYPE_PROMOTION_BISHOP = 6;

		public const int TYPE_PROMOTION_ROOK = 7;

		// private static final Logger logger = Logger.getLogger(Move.class);
		// Move pieces ordered by value
		// Move Types
		// promotions must be
		// always >=
		// TYPE_PROMOTION_QUEEN
		public static int GenMove(int fromIndex, int toIndex, int pieceMoved, bool capture
			, int moveType)
		{
			return toIndex | fromIndex << 6 | pieceMoved << 12 | (capture ? 1 << 15 : 0) | moveType
				 << 16;
		}

		public static int GetToIndex(int move)
		{
			return move & unchecked((int)(0x3f));
		}

		public static long GetToSquare(int move)
		{
			return unchecked((long)(0x1L)) << (move & unchecked((int)(0x3f)));
		}

		public static int GetFromIndex(int move)
		{
			return (((int)(((uint)move) >> 6)) & unchecked((int)(0x3f)));
		}

		public static long GetFromSquare(int move)
		{
			return unchecked((long)(0x1L)) << (((int)(((uint)move) >> 6)) & unchecked((int)(0x3f
				)));
		}

		/// <summary>square index in a 64*64 array (12 bits)</summary>
		public static int GetFromToIndex(int move)
		{
			return move & unchecked((int)(0xfff));
		}

		public static int GetPieceMoved(int move)
		{
			return (((int)(((uint)move) >> 12)) & unchecked((int)(0x7)));
		}

		public static bool GetCapture(int move)
		{
			return (((int)(((uint)move) >> 15)) & unchecked((int)(0x1))) != 0;
		}

		public static int GetMoveType(int move)
		{
			return (((int)(((uint)move) >> 16)) & unchecked((int)(0x7)));
		}

		public static bool IsCapture(int move)
		{
			return (move & (unchecked((int)(0x1)) << 15)) != 0;
		}

		// Pawn push to 7th/8th rank
		public static bool IsPawnPush(int move)
		{
			return Move.GetPieceMoved(move) == PAWN && (Move.GetToIndex(move) < 16 || Move.GetToIndex
				(move) > 47);
		}

		/// <summary>Checks if this move is a promotion</summary>
		public static bool IsPromotion(int move)
		{
			return Move.GetMoveType(move) >= TYPE_PROMOTION_QUEEN;
		}

		public static bool IsTactical(int move)
		{
			return (Move.IsCapture(move) || Move.IsPromotion(move));
		}

		public static bool IsCastling(int move)
		{
			return Move.GetMoveType(move) == TYPE_KINGSIDE_CASTLING || Move.GetMoveType(move)
				 == TYPE_QUEENSIDE_CASTLING;
		}

		/// <summary>
		/// Given a boards creates a move from a String in uci format or short
		/// algebraic form
		/// </summary>
		/// <param name="board"></param>
		/// <param name="move"></param>
		public static int GetFromString(Board board, string move, bool checkLegality)
		{
			int fromIndex = 0;
			int toIndex = 0;
			int moveType = 0;
			int pieceMoved = 0;
			// Ignore checks, captures indicators...
			move = move.Replace("+", string.Empty).Replace("x", string.Empty).Replace("-", string.Empty
				).Replace("=", string.Empty).Replace("#", string.Empty).ReplaceAll(" ", string.Empty
				).ReplaceAll("0", "o").ReplaceAll("O", "o");
			if ("ooo".Equals(move))
			{
				if (board.GetTurn())
				{
					move = "e1c1";
				}
				else
				{
					move = "e8c8";
				}
			}
			else
			{
				if ("oo".Equals(move))
				{
					if (board.GetTurn())
					{
						move = "e1g1";
					}
					else
					{
						move = "e8g8";
					}
				}
			}
			char promo = move[move.Length - 1];
			switch (System.Char.ToLower(promo))
			{
				case 'q':
				{
					moveType = TYPE_PROMOTION_QUEEN;
					break;
				}

				case 'n':
				{
					moveType = TYPE_PROMOTION_KNIGHT;
					break;
				}

				case 'b':
				{
					moveType = TYPE_PROMOTION_BISHOP;
					break;
				}

				case 'r':
				{
					moveType = TYPE_PROMOTION_ROOK;
					break;
				}
			}
			// If promotion, remove the last char
			if (moveType != 0)
			{
				move = Sharpen.Runtime.Substring(move, 0, move.Length - 1);
			}
			// To is always the last 2 characters
			toIndex = BitboardUtils.Algebraic2Index(Sharpen.Runtime.Substring(move, move.Length
				 - 2, move.Length));
			long to = unchecked((long)(0x1L)) << toIndex;
			long from = 0;
			switch (move[0])
			{
				case 'N':
				{
					// Fills from with a mask of possible from values
					from = board.knights & board.GetMines() & BitboardAttacks.knight[toIndex];
					break;
				}

				case 'K':
				{
					from = board.kings & board.GetMines() & BitboardAttacks.king[toIndex];
					break;
				}

				case 'R':
				{
					from = board.rooks & board.GetMines() & BitboardAttacks.GetRookAttacks(toIndex, board
						.GetAll());
					break;
				}

				case 'B':
				{
					from = board.bishops & board.GetMines() & BitboardAttacks.GetBishopAttacks(toIndex
						, board.GetAll());
					break;
				}

				case 'Q':
				{
					from = board.queens & board.GetMines() & (BitboardAttacks.GetRookAttacks(toIndex, 
						board.GetAll()) | BitboardAttacks.GetBishopAttacks(toIndex, board.GetAll()));
					break;
				}
			}
			if (from != 0)
			{
				// remove the piece char
				move = Sharpen.Runtime.Substring(move, 1);
			}
			else
			{
				// Pawn moves
				if (move.Length == 2)
				{
					if (board.GetTurn())
					{
						from = board.pawns & board.GetMines() & (((long)(((ulong)to) >> 8)) | ((((long)((
							(ulong)to) >> 8)) & board.GetAll()) == 0 ? ((long)(((ulong)to) >> 16)) : 0));
					}
					else
					{
						from = board.pawns & board.GetMines() & ((to << 8) | (((to << 8) & board.GetAll()
							) == 0 ? (to << 16) : 0));
					}
				}
				if (move.Length == 3)
				{
					// Pawn capture
					from = board.pawns & board.GetMines() & (board.GetTurn() ? BitboardAttacks.pawnDownwards
						[toIndex] : BitboardAttacks.pawnUpwards[toIndex]);
				}
			}
			if (move.Length == 3)
			{
				// now disambiaguate
				char disambiguate = move[0];
				int i = "abcdefgh".IndexOf(disambiguate);
				if (i >= 0)
				{
					from &= BitboardUtils.COLUMN[i];
				}
				int j = "12345678".IndexOf(disambiguate);
				if (j >= 0)
				{
					from &= BitboardUtils.RANK[j];
				}
			}
			// if (BitboardUtils.popCount(from) > 1) {
			// logger.error("Move NOT disambiaguated:\n"+board.toString() + "\n" +
			// in);
			// System.exit(-1);
			// return -1;
			// }
			if (move.Length == 4)
			{
				// was algebraic complete e2e4 (=UCI!)
				from = BitboardUtils.Algebraic2Square(Sharpen.Runtime.Substring(move, 0, 2));
			}
			if (from == 0)
			{
				return -1;
			}
			// Treats multiple froms, choosing the first Legal Move
			while (from != 0)
			{
				long myFrom = BitboardUtils.Lsb(from);
				from ^= myFrom;
				fromIndex = BitboardUtils.Square2Index(myFrom);
				bool capture = false;
				if ((myFrom & board.pawns) != 0)
				{
					pieceMoved = PAWN;
					// for passant captures
					if ((toIndex != (fromIndex - 8)) && (toIndex != (fromIndex + 8)) && (toIndex != (
						fromIndex - 16)) && (toIndex != (fromIndex + 16)))
					{
						if ((to & board.GetAll()) == 0)
						{
							moveType = TYPE_PASSANT;
							capture = true;
						}
					}
					// later is changed if it was not a pawn
					// Default promotion to queen if not specified
					if ((to & (BitboardUtils.b_u | BitboardUtils.b_d)) != 0 && (moveType < TYPE_PROMOTION_QUEEN
						))
					{
						moveType = TYPE_PROMOTION_QUEEN;
					}
				}
				if ((myFrom & board.bishops) != 0)
				{
					pieceMoved = BISHOP;
				}
				else
				{
					if ((myFrom & board.knights) != 0)
					{
						pieceMoved = KNIGHT;
					}
					else
					{
						if ((myFrom & board.rooks) != 0)
						{
							pieceMoved = ROOK;
						}
						else
						{
							if ((myFrom & board.queens) != 0)
							{
								pieceMoved = QUEEN;
							}
							else
							{
								if ((myFrom & board.kings) != 0)
								{
									pieceMoved = KING;
									// Only if origin square is king's initial square TODO FRC
									if (fromIndex == 3 || fromIndex == 3 + (8 * 7))
									{
										if (toIndex == (fromIndex + 2))
										{
											moveType = TYPE_QUEENSIDE_CASTLING;
										}
										if (toIndex == (fromIndex - 2))
										{
											moveType = TYPE_KINGSIDE_CASTLING;
										}
									}
								}
							}
						}
					}
				}
				// Now set captured piece flag
				if ((to & (board.whites | board.blacks)) != 0)
				{
					capture = true;
				}
				int moveInt = Move.GenMove(fromIndex, toIndex, pieceMoved, capture, moveType);
				if (checkLegality && board.DoMove(moveInt, false))
				{
					board.UndoMove();
					return moveInt;
				}
				else
				{
					return moveInt;
				}
			}
			return -1;
		}

		/// <summary>Gets an UCI-String representation of the move</summary>
		/// <param name="move"></param>
		/// <returns></returns>
		public static string ToString(int move)
		{
			if (move == 0 || move == -1)
			{
				return "none";
			}
			StringBuilder sb = new StringBuilder();
			sb.Append(BitboardUtils.Index2Algebraic(Move.GetFromIndex(move)));
			sb.Append(BitboardUtils.Index2Algebraic(Move.GetToIndex(move)));
			switch (Move.GetMoveType(move))
			{
				case TYPE_PROMOTION_QUEEN:
				{
					sb.Append("q");
					break;
				}

				case TYPE_PROMOTION_KNIGHT:
				{
					sb.Append("n");
					break;
				}

				case TYPE_PROMOTION_BISHOP:
				{
					sb.Append("b");
					break;
				}

				case TYPE_PROMOTION_ROOK:
				{
					sb.Append("r");
					break;
				}
			}
			return sb.ToString();
		}

		public static string ToStringExt(int move)
		{
			if (move == 0 || move == -1)
			{
				return "none";
			}
			else
			{
				if (Move.GetMoveType(move) == TYPE_KINGSIDE_CASTLING)
				{
					return "O-O";
				}
				else
				{
					if (Move.GetMoveType(move) == TYPE_QUEENSIDE_CASTLING)
					{
						return "O-O-O";
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			if (GetPieceMoved(move) != Move.PAWN)
			{
				sb.Append(" PNBRQK"[GetPieceMoved(move)]);
			}
			sb.Append(BitboardUtils.Index2Algebraic(Move.GetFromIndex(move)));
			sb.Append(GetCapture(move) ? 'x' : '-');
			sb.Append(BitboardUtils.Index2Algebraic(Move.GetToIndex(move)));
			switch (Move.GetMoveType(move))
			{
				case TYPE_PROMOTION_QUEEN:
				{
					sb.Append("q");
					break;
				}

				case TYPE_PROMOTION_KNIGHT:
				{
					sb.Append("n");
					break;
				}

				case TYPE_PROMOTION_BISHOP:
				{
					sb.Append("b");
					break;
				}

				case TYPE_PROMOTION_ROOK:
				{
					sb.Append("r");
					break;
				}
			}
			return sb.ToString();
		}

		/// <summary>Des not append + or #</summary>
		/// <param name="board"></param>
		/// <param name="move"></param>
		/// <param name="legalMoves"></param>
		/// <param name="legalMoveCount"></param>
		/// <returns></returns>
		public static string ToSan(Board board, int move, int[] legalMoves, int legalMoveCount
			)
		{
			if (move == 0 || move == -1)
			{
				return "none";
			}
			if (Move.GetMoveType(move) == TYPE_KINGSIDE_CASTLING)
			{
				return "O-O";
			}
			else
			{
				if (Move.GetMoveType(move) == TYPE_QUEENSIDE_CASTLING)
				{
					return "O-O-O";
				}
			}
			StringBuilder sb = new StringBuilder();
			if (GetPieceMoved(move) != Move.PAWN)
			{
				sb.Append(" PNBRQK"[GetPieceMoved(move)]);
			}
			bool disambiguate = false;
			bool colEqual = false;
			bool rowEqual = false;
			for (int i = 0; i < legalMoveCount; i++)
			{
				int move2 = legalMoves[i];
				if (GetToIndex(move) == GetToIndex(move2) && GetFromIndex(move) != GetFromIndex(move2
					) && GetPieceMoved(move) == GetPieceMoved(move2))
				{
					disambiguate = true;
					if ((GetFromIndex(move) % 8) == (GetFromIndex(move2) % 8))
					{
						colEqual = true;
					}
					if ((GetFromIndex(move) / 8) == (GetFromIndex(move2) / 8))
					{
						rowEqual = true;
					}
				}
			}
			string fromSq = BitboardUtils.Index2Algebraic(Move.GetFromIndex(move));
			if (GetCapture(move) && GetPieceMoved(move) == Move.PAWN)
			{
				disambiguate = true;
			}
			if (disambiguate)
			{
				if (colEqual && rowEqual)
				{
					sb.Append(fromSq);
				}
				else
				{
					if (colEqual && !rowEqual)
					{
						sb.Append(fromSq[1]);
					}
					else
					{
						sb.Append(fromSq[0]);
					}
				}
			}
			if (GetCapture(move))
			{
				sb.Append("x");
			}
			sb.Append(BitboardUtils.Index2Algebraic(Move.GetToIndex(move)));
			switch (Move.GetMoveType(move))
			{
				case TYPE_PROMOTION_QUEEN:
				{
					sb.Append("Q");
					break;
				}

				case TYPE_PROMOTION_KNIGHT:
				{
					sb.Append("N");
					break;
				}

				case TYPE_PROMOTION_BISHOP:
				{
					sb.Append("B");
					break;
				}

				case TYPE_PROMOTION_ROOK:
				{
					sb.Append("R");
					break;
				}
			}
			return sb.ToString();
		}

		public static void PrintMoves(int[] moves, int from, int to)
		{
			for (int i = from; i < to; i++)
			{
				System.Console.Out.Write(Move.ToStringExt(moves[i]));
				System.Console.Out.Write(" ");
			}
			System.Console.Out.WriteLine();
		}
	}
}
