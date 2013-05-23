/*
 * Code converted with Sharpen
 * 
 */using System;
using System.Collections.Generic;
using System.Text;
using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Bitboard;
using Com.Alonsoruibal.Chess.Hash;
using Com.Alonsoruibal.Chess.Log;
using Com.Alonsoruibal.Chess.Movegen;
using Sharpen;

namespace Com.Alonsoruibal.Chess
{
	/// <summary>
	/// Stores the position and the move history
	/// TODO FRC
	/// </summary>
	/// <author>Alberto Alonso Ruibal</author>
	public class Board
	{
		private static readonly Logger logger = Logger.GetLogger("Board");

		public const int MAX_MOVES = 1024;

		public static readonly string FEN_START_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

		internal LegalMoveGenerator legalMoveGenerator = new LegalMoveGenerator();

		internal int[] legalMoves = new int[256];

		internal int legalMoveCount = -1;

		internal long[] legalMovesKey = new long[] { 0, 0 };

		public Dictionary<int, string> sanMoves;

		public long whites = 0;

		public long blacks = 0;

		public long pawns = 0;

		public long rooks = 0;

		public long queens = 0;

		public long bishops = 0;

		public long knights = 0;

		public long kings = 0;

		public long flags = 0;

		public int fiftyMovesRule = 0;

		public int initialMoveNumber = 0;

		public int moveNumber = 0;

		public int outBookMove = 0;

		public long[] key = new long[] { 0, 0 };

		public string initialFen;

		public long[][] keyHistory;

		public int[] moveHistory;

		public long[] whitesHistory;

		public long[] blacksHistory;

		public long[] pawnsHistory;

		public long[] rooksHistory;

		public long[] queensHistory;

		public long[] bishopsHistory;

		public long[] knightsHistory;

		public long[] kingsHistory;

		public long[] flagsHistory;

		public int[] fiftyMovesRuleHistory;

		public char[] capturedPieces;

		public int[] seeGain;

		private const long FLAG_TURN = unchecked((long)(0x0001L));

		private const long FLAG_WHITE_DISABLE_KINGSIDE_CASTLING = unchecked((long)(0x0002L
			));

		private const long FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING = unchecked((long)(0x0004L
			));

		private const long FLAG_BLACK_DISABLE_KINGSIDE_CASTLING = unchecked((long)(0x0008L
			));

		private const long FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING = unchecked((long)(0x0010L
			));

		private const long FLAG_CHECK = unchecked((long)(0x0020L));

		private const long FLAGS_PASSANT = unchecked((long)(0x0000ff0000ff0000L));

		private static readonly int[] SEE_PIECE_VALUES = new int[] { 0, 100, 325, 330, 500
			, 900, 9999 };

		internal BitboardAttacks bbAttacks;

		public Board()
		{
			// if -1 then legal moves not generated
			// Bitboard arrays
			// History array indexed by moveNumber
			// to detect draw by treefold
			// Flags: must be changed only when Moving!!!
			// Position on boarch in which is captured
			// Thos the SEE SWAP algorithm
			whitesHistory = new long[MAX_MOVES];
			blacksHistory = new long[MAX_MOVES];
			pawnsHistory = new long[MAX_MOVES];
			knightsHistory = new long[MAX_MOVES];
			bishopsHistory = new long[MAX_MOVES];
			rooksHistory = new long[MAX_MOVES];
			queensHistory = new long[MAX_MOVES];
			kingsHistory = new long[MAX_MOVES];
			flagsHistory = new long[MAX_MOVES];
			keyHistory = new long[][] { new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new 
				long[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long
				[2], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2
				], new long[2], new long[2], new long[2], new long[2], new long[2], new long[2], 
				new long[2] };
			fiftyMovesRuleHistory = new int[MAX_MOVES];
			capturedPieces = new char[MAX_MOVES];
			seeGain = new int[32];
			moveHistory = new int[MAX_MOVES];
			sanMoves = new Dictionary<int, string>();
			bbAttacks = BitboardAttacks.GetInstance();
		}

		/// <summary>Also computes zobrish key</summary>
		public virtual void StartPosition()
		{
			SetFen(FEN_START_POSITION);
		}

		public virtual long GetKey()
		{
			return key[0] ^ key[1];
		}

		public virtual long GetExclusionKey()
		{
			return key[0] ^ key[1] ^ ZobristKey.exclusionKey;
		}

		/// <summary>An alternative key to avoid collisions on tt</summary>
		/// <returns></returns>
		public virtual long GetKey2()
		{
			return key[0] ^ ~key[1];
		}

		public virtual int GetMoveNumber()
		{
			return moveNumber;
		}

		/// <returns>true if white moves</returns>
		public bool GetTurn()
		{
			return (flags & FLAG_TURN) == 0;
		}

		public virtual bool GetWhiteKingsideCastling()
		{
			return (flags & FLAG_WHITE_DISABLE_KINGSIDE_CASTLING) == 0;
		}

		public virtual bool GetWhiteQueensideCastling()
		{
			return (flags & FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING) == 0;
		}

		public virtual bool GetBlackKingsideCastling()
		{
			return (flags & FLAG_BLACK_DISABLE_KINGSIDE_CASTLING) == 0;
		}

		public virtual bool GetBlackQueensideCastling()
		{
			return (flags & FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING) == 0;
		}

		public virtual long GetPassantSquare()
		{
			return flags & FLAGS_PASSANT;
		}

		public virtual bool GetCheck()
		{
			return (flags & FLAG_CHECK) != 0;
		}

		public virtual long GetAll()
		{
			return whites | blacks;
		}

		public virtual long GetMines()
		{
			return (flags & FLAG_TURN) == 0 ? whites : blacks;
		}

		public virtual long GetOthers()
		{
			return (flags & FLAG_TURN) == 0 ? blacks : whites;
		}

		public virtual char GetPieceAt(long square)
		{
			char p = ((pawns & square) != 0 ? 'p' : ((queens & square) != 0 ? 'q' : ((rooks &
				 square) != 0 ? 'r' : ((bishops & square) != 0 ? 'b' : ((knights & square) != 0 ? 
				'n' : ((kings & square) != 0 ? 'k' : '.'))))));
			//
			//
			//
			//
			//
			return ((whites & square) != 0 ? System.Char.ToUpper(p) : p);
		}

		public virtual void SetPieceAt(long square, char piece)
		{
			pawns &= ~square;
			queens &= ~square;
			rooks &= ~square;
			bishops &= ~square;
			knights &= ~square;
			kings &= ~square;
			if (piece == ' ' || piece == '.')
			{
				whites &= ~square;
				blacks &= ~square;
				return;
			}
			else
			{
				if (piece == System.Char.ToLower(piece))
				{
					whites &= ~square;
					blacks |= square;
				}
				else
				{
					whites |= square;
					blacks &= ~square;
				}
			}
			switch (System.Char.ToLower(piece))
			{
				case 'p':
				{
					pawns |= square;
					break;
				}

				case 'q':
				{
					queens |= square;
					break;
				}

				case 'r':
				{
					rooks |= square;
					break;
				}

				case 'b':
				{
					bishops |= square;
					break;
				}

				case 'n':
				{
					knights |= square;
					break;
				}

				case 'k':
				{
					kings |= square;
					break;
				}
			}
		}

		/// <summary>Converts board to its fen notation</summary>
		public virtual string GetFen()
		{
			StringBuilder sb = new StringBuilder();
			long i = BitboardUtils.A8;
			int j = 0;
			while (i != 0)
			{
				char p = GetPieceAt(i);
				if (p == '.')
				{
					j++;
				}
				if ((j != 0) && (p != '.' || ((i & BitboardUtils.b_r) != 0)))
				{
					sb.Append(j);
					j = 0;
				}
				if (p != '.')
				{
					sb.Append(p);
				}
				if ((i != 1) && (i & BitboardUtils.b_r) != 0)
				{
					sb.Append("/");
				}
				i = (long)(((ulong)i) >> 1);
			}
			sb.Append(" ");
			sb.Append((GetTurn() ? "w" : "b"));
			sb.Append(" ");
			if (GetWhiteQueensideCastling())
			{
				sb.Append("Q");
			}
			if (GetWhiteKingsideCastling())
			{
				sb.Append("K");
			}
			if (GetBlackQueensideCastling())
			{
				sb.Append("q");
			}
			if (GetBlackKingsideCastling())
			{
				sb.Append("k");
			}
			if (!GetWhiteQueensideCastling() && !GetWhiteKingsideCastling() && !GetBlackQueensideCastling
				() && !GetBlackKingsideCastling())
			{
				sb.Append("-");
			}
			sb.Append(" ");
			sb.Append((GetPassantSquare() != 0 ? BitboardUtils.Square2Algebraic(GetPassantSquare
				()) : "-"));
			sb.Append(" ");
			sb.Append(fiftyMovesRule);
			sb.Append(" ");
			sb.Append((moveNumber >> 1) + 1);
			// 0,1->1.. 2,3->2
			return sb.ToString();
		}

		/// <summary>Loads board from a fen notation</summary>
		/// <param name="fen"></param>
		public virtual void SetFen(string fen)
		{
			SetFenMove(fen, null);
		}

		/// <summary>Sets fen without destroying move history.</summary>
		/// <remarks>
		/// Sets fen without destroying move history. If lastMove = null destroy the
		/// move history
		/// </remarks>
		public virtual void SetFenMove(string fen, string lastMove)
		{
			long tmpWhites = 0;
			long tmpBlacks = 0;
			long tmpPawns = 0;
			long tmpRooks = 0;
			long tmpQueens = 0;
			long tmpBishops = 0;
			long tmpKnights = 0;
			long tmpKings = 0;
			long tmpFlags = 0;
			int tmpFiftyMovesRule = 0;
			int fenMoveNumber = 0;
			int i = 0;
			long j = BitboardUtils.A8;
			string[] tokens = fen.Split("[ \\t\\n\\x0B\\f\\r]+");
			string board = tokens[0];
			while ((i < board.Length) && (j != 0))
			{
				char p = board[i++];
				if (p != '/')
				{
					int number = 0;
					try
					{
						number = System.Convert.ToInt32(p.ToString());
					}
					catch (Exception)
					{
					}
					for (int k = 0; k < (number == 0 ? 1 : number); k++)
					{
						tmpWhites = (tmpWhites & ~j) | ((number == 0) && (p == System.Char.ToUpper(p)) ? 
							j : 0);
						tmpBlacks = (tmpBlacks & ~j) | ((number == 0) && (p == System.Char.ToLower(p)) ? 
							j : 0);
						tmpPawns = (tmpPawns & ~j) | (System.Char.ToUpper(p) == 'P' ? j : 0);
						tmpRooks = (tmpRooks & ~j) | (System.Char.ToUpper(p) == 'R' ? j : 0);
						tmpQueens = (tmpQueens & ~j) | (System.Char.ToUpper(p) == 'Q' ? j : 0);
						tmpBishops = (tmpBishops & ~j) | (System.Char.ToUpper(p) == 'B' ? j : 0);
						tmpKnights = (tmpKnights & ~j) | (System.Char.ToUpper(p) == 'N' ? j : 0);
						tmpKings = (tmpKings & ~j) | (System.Char.ToUpper(p) == 'K' ? j : 0);
						j = (long)(((ulong)j) >> 1);
						if (j == 0)
						{
							break;
						}
					}
				}
			}
			// security
			// Now the rest ...
			string turn = tokens[1];
			tmpFlags = FLAG_WHITE_DISABLE_KINGSIDE_CASTLING | FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING
				 | FLAG_BLACK_DISABLE_KINGSIDE_CASTLING | FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
			if ("b".Equals(turn))
			{
				tmpFlags |= FLAG_TURN;
			}
			if (tokens.Length > 2)
			{
				string promotions = tokens[2];
				if (promotions.IndexOf("K") >= 0)
				{
					tmpFlags &= ~FLAG_WHITE_DISABLE_KINGSIDE_CASTLING;
				}
				if (promotions.IndexOf("Q") >= 0)
				{
					tmpFlags &= ~FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING;
				}
				if (promotions.IndexOf("k") >= 0)
				{
					tmpFlags &= ~FLAG_BLACK_DISABLE_KINGSIDE_CASTLING;
				}
				if (promotions.IndexOf("q") >= 0)
				{
					tmpFlags &= ~FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
				}
				if (tokens.Length > 3)
				{
					string passant = tokens[3];
					tmpFlags |= FLAGS_PASSANT & BitboardUtils.Algebraic2Square(passant);
					if (tokens.Length > 4)
					{
						try
						{
							tmpFiftyMovesRule = System.Convert.ToInt32(tokens[4]);
						}
						catch (Exception)
						{
							tmpFiftyMovesRule = 0;
						}
						if (tokens.Length > 5)
						{
							string moveNumberString = tokens[5];
							int aux = System.Convert.ToInt32(moveNumberString);
							fenMoveNumber = ((aux > 0 ? aux - 1 : aux) << 1) + ((tmpFlags & FLAG_TURN) == 0 ? 
								0 : 1);
							if (fenMoveNumber < 0)
							{
								fenMoveNumber = 0;
							}
						}
					}
				}
			}
			// try to apply the last move to see if we are advancing or undo moves
			if ((moveNumber + 1) == fenMoveNumber && lastMove != null)
			{
				DoMove(Move.GetFromString(this, lastMove, false));
				logger.Debug(ToString());
				logger.Debug(GetFen());
			}
			else
			{
				if (fenMoveNumber < moveNumber)
				{
					for (int k = moveNumber; k > fenMoveNumber; k--)
					{
						UndoMove();
					}
				}
			}
			// Check if board changed or if we can keep the history
			if (whites != tmpWhites || blacks != tmpBlacks || pawns != tmpPawns || rooks != tmpRooks
				 || queens != tmpQueens || bishops != tmpBishops || knights != tmpKnights || kings
				 != tmpKings)
			{
				//
				//
				//
				//
				//
				//
				//
				// board reset
				sanMoves.Clear();
				initialFen = fen;
				initialMoveNumber = fenMoveNumber;
				moveNumber = fenMoveNumber;
				outBookMove = 9999;
				whites = tmpWhites;
				blacks = tmpBlacks;
				pawns = tmpPawns;
				rooks = tmpRooks;
				queens = tmpQueens;
				bishops = tmpBishops;
				knights = tmpKnights;
				kings = tmpKings;
				fiftyMovesRule = tmpFiftyMovesRule;
				// Flags are nort completed till verify, so skip checking
				flags = tmpFlags;
				Verify();
				// Finally set zobrish key and check flags
				key = ZobristKey.GetKey(this);
				SetCheckFlags(GetTurn());
				// and save history
				ResetHistory();
				SaveHistory(0, false);
			}
		}

		/// <summary>Does some board verification</summary>
		private void Verify()
		{
			// TODO Verify only one king per side
			// Verify castling
			if (GetWhiteKingsideCastling() && ((whites & kings & unchecked((int)(0x08))) == 0
				 || (whites & rooks & unchecked((int)(0x01))) == 0))
			{
				flags |= FLAG_WHITE_DISABLE_KINGSIDE_CASTLING;
			}
			if (GetWhiteQueensideCastling() && ((whites & kings & unchecked((int)(0x08))) == 
				0 || (whites & rooks & unchecked((int)(0x80))) == 0))
			{
				flags |= FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING;
			}
			if (GetBlackKingsideCastling() && ((blacks & kings & unchecked((long)(0x0800000000000000L
				))) == 0 || (blacks & rooks & unchecked((long)(0x0100000000000000L))) == 0))
			{
				flags |= FLAG_BLACK_DISABLE_KINGSIDE_CASTLING;
			}
			if (GetBlackQueensideCastling() && ((blacks & kings & unchecked((long)(0x0800000000000000L
				))) == 0 || (blacks & rooks & unchecked((long)(0x8000000000000000L))) == 0))
			{
				flags |= FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
			}
		}

		/// <summary>Prints board in one string</summary>
		public override string ToString()
		{
			StringBuilder sb = new StringBuilder();
			int j = 8;
			long i = BitboardUtils.A8;
			while (i != 0)
			{
				sb.Append(GetPieceAt(i));
				sb.Append(" ");
				if ((i & BitboardUtils.b_r) != 0)
				{
					sb.Append(j--);
					sb.Append("\n");
				}
				i = (long)(((ulong)i) >> 1);
			}
			sb.Append("a b c d e f g h  ");
			sb.Append((GetTurn() ? "white move\n" : "blacks move\n"));
			// sb.append(" "
			// +getWhiteKingsideCastling()+getWhiteQueensideCastling()+getBlackKingsideCastling()+getBlackQueensideCastling());
			return sb.ToString();
		}

		/// <summary>TODO is it necessary??</summary>
		private void ResetHistory()
		{
			Arrays.Fill(whitesHistory, 0);
			Arrays.Fill(blacksHistory, 0);
			Arrays.Fill(pawnsHistory, 0);
			Arrays.Fill(knightsHistory, 0);
			Arrays.Fill(bishopsHistory, 0);
			Arrays.Fill(rooksHistory, 0);
			Arrays.Fill(queensHistory, 0);
			Arrays.Fill(kingsHistory, 0);
			Arrays.Fill(flagsHistory, 0);
			for (int i = 0; i < MAX_MOVES; i++)
			{
				Arrays.Fill(keyHistory[i], 0);
			}
			Arrays.Fill(fiftyMovesRuleHistory, 0);
			Arrays.Fill(capturedPieces, '.');
			Arrays.Fill(moveHistory, 0);
			sanMoves.Clear();
		}

		private void SaveHistory(int move, bool fillInfo)
		{
			// logger.debug("saving History " + moveNumber + " " +
			// Move.toStringExt(move) + " fillinfo=" + fillInfo);
			if (fillInfo)
			{
				GenerateLegalMoves();
				sanMoves.Put(moveNumber, Move.ToSan(this, move, legalMoves, legalMoveCount));
			}
			moveHistory[moveNumber] = move;
			whitesHistory[moveNumber] = whites;
			blacksHistory[moveNumber] = blacks;
			pawnsHistory[moveNumber] = pawns;
			knightsHistory[moveNumber] = knights;
			bishopsHistory[moveNumber] = bishops;
			rooksHistory[moveNumber] = rooks;
			queensHistory[moveNumber] = queens;
			kingsHistory[moveNumber] = kings;
			flagsHistory[moveNumber] = flags;
			keyHistory[moveNumber][0] = key[0];
			keyHistory[moveNumber][1] = key[1];
			fiftyMovesRuleHistory[moveNumber] = fiftyMovesRule;
		}

		public virtual int GetLastMove()
		{
			if (moveNumber == 0)
			{
				return 0;
			}
			return moveHistory[moveNumber - 1];
		}

		/// <summary>
		/// Recapture for extensions: only if the value of the captured piece is
		/// similar and we recapture in the same square
		/// </summary>
		/// <returns></returns>
		public virtual bool GetLastMoveIsRecapture()
		{
			if (moveNumber > 1 && Move.GetToIndex(moveHistory[moveNumber - 1]) == Move.GetToIndex
				(moveHistory[moveNumber - 2]) && capturedPieces[moveNumber - 1] != '.' && capturedPieces
				[moveNumber - 2] != '.')
			{
				char captured1 = System.Char.ToLower(capturedPieces[moveNumber - 1]);
				char captured2 = System.Char.ToLower(capturedPieces[moveNumber - 2]);
				if (captured1 == 'n')
				{
					captured1 = 'b';
				}
				// Converts knights in bishops
				if (captured2 == 'n')
				{
					captured2 = 'b';
				}
				if (captured1 == captured2)
				{
					return true;
				}
			}
			return false;
		}

		public virtual char GetLastCapturedPiece()
		{
			if (moveNumber > 1)
			{
				return System.Char.ToLower(capturedPieces[moveNumber - 1]);
			}
			return '.';
		}

		public virtual bool DoMove(int move)
		{
			return DoMove(move, true);
		}

		/// <summary>
		/// Moves and also updates the board's zobrish key verify legality, if not
		/// legal undo move and return false 0 is the null move
		/// </summary>
		/// <param name="move"></param>
		/// <returns></returns>
		public virtual bool DoMove(int move, bool fillInfo)
		{
			// logger.debug("Before move: \n" + toString() + "\n " +
			// Move.toStringExt(move));
			if (move == -1)
			{
				return false;
			}
			// Save history
			SaveHistory(move, fillInfo);
			long from = Move.GetFromSquare(move);
			long to = Move.GetToSquare(move);
			int fromIndex = Move.GetFromIndex(move);
			int toIndex = Move.GetToIndex(move);
			int moveType = Move.GetMoveType(move);
			int pieceMoved = Move.GetPieceMoved(move);
			bool capture = Move.GetCapture(move);
			bool turn = GetTurn();
			int color = (turn ? 0 : 1);
			char capturedPiece = GetPieceAt(to);
			capturedPieces[moveNumber] = capturedPiece;
			// Count consecutive moves without capture or without pawn move
			fiftyMovesRule++;
			moveNumber++;
			// Count Ply moves
			// Remove passant flags: from the zobrist key
			if ((flags & FLAGS_PASSANT) != 0)
			{
				key[1 - color] ^= ZobristKey.passantColumn[BitboardUtils.GetColumn(flags & FLAGS_PASSANT
					)];
			}
			// and from the flags
			flags &= ~FLAGS_PASSANT;
			if (move != 0)
			{
				if ((from & GetMines()) == 0)
				{
					logger.Error("Origin square not valid");
					logger.Debug("\n" + ToString());
					logger.Debug("Move = " + Move.ToStringExt(move));
					Move.PrintMoves(moveHistory, 0, moveNumber);
					try
					{
						throw new Exception();
					}
					catch (Exception e)
					{
						Sharpen.Runtime.PrintStackTrace(e);
					}
					return false;
				}
				long moveMask = from | to;
				// Move is as easy as xor with this mask
				// (exceptions are in captures,
				// promotions and passant captures)
				// Is it is a capture, remove pieces in destination square
				if (capture)
				{
					fiftyMovesRule = 0;
					// Passant Pawn captures remove captured pawn, put the pawn in
					// to
					int toIndexCapture = toIndex;
					if (moveType == Move.TYPE_PASSANT)
					{
						to = (GetTurn() ? ((long)(((ulong)to) >> 8)) : (to << 8));
						toIndexCapture += (GetTurn() ? -8 : 8);
					}
					key[1 - color] ^= ZobristKey.GetKeyPieceIndex(toIndexCapture, GetPieceAt(to));
					whites &= ~to;
					blacks &= ~to;
					pawns &= ~to;
					queens &= ~to;
					rooks &= ~to;
					bishops &= ~to;
					knights &= ~to;
				}
				switch (pieceMoved)
				{
					case Move.PAWN:
					{
						// Pawn movements
						fiftyMovesRule = 0;
						// Set new passant flags if pawn is advancing two squares (marks
						// the destination square where the pawn can be captured)
						// Set only passant flags when the other side can capture
						if (((from << 16) & to) != 0 && (bbAttacks.pawnUpwards[toIndex - 8] & pawns & GetOthers
							()) != 0)
						{
							// white
							flags |= (from << 8);
						}
						if ((((long)(((ulong)from) >> 16)) & to) != 0 && (bbAttacks.pawnDownwards[toIndex
							 + 8] & pawns & GetOthers()) != 0)
						{
							// blask
							flags |= ((long)(((ulong)from) >> 8));
						}
						if ((flags & FLAGS_PASSANT) != 0)
						{
							key[color] ^= ZobristKey.passantColumn[BitboardUtils.GetColumn(flags & FLAGS_PASSANT
								)];
						}
						if (moveType == Move.TYPE_PROMOTION_QUEEN || moveType == Move.TYPE_PROMOTION_KNIGHT
							 || moveType == Move.TYPE_PROMOTION_BISHOP || moveType == Move.TYPE_PROMOTION_ROOK)
						{
							// Promotions:
							// change
							// the piece
							pawns &= ~from;
							key[color] ^= ZobristKey.pawn[color][fromIndex];
							switch (moveType)
							{
								case Move.TYPE_PROMOTION_QUEEN:
								{
									queens |= to;
									key[color] ^= ZobristKey.queen[color][toIndex];
									break;
								}

								case Move.TYPE_PROMOTION_KNIGHT:
								{
									knights |= to;
									key[color] ^= ZobristKey.knight[color][toIndex];
									break;
								}

								case Move.TYPE_PROMOTION_BISHOP:
								{
									bishops |= to;
									key[color] ^= ZobristKey.bishop[color][toIndex];
									break;
								}

								case Move.TYPE_PROMOTION_ROOK:
								{
									rooks |= to;
									key[color] ^= ZobristKey.rook[color][toIndex];
									break;
								}
							}
						}
						else
						{
							pawns ^= moveMask;
							key[color] ^= ZobristKey.pawn[color][fromIndex] ^ ZobristKey.pawn[color][toIndex];
						}
						break;
					}

					case Move.ROOK:
					{
						rooks ^= moveMask;
						key[color] ^= ZobristKey.rook[color][fromIndex] ^ ZobristKey.rook[color][toIndex];
						break;
					}

					case Move.BISHOP:
					{
						bishops ^= moveMask;
						key[color] ^= ZobristKey.bishop[color][fromIndex] ^ ZobristKey.bishop[color][toIndex
							];
						break;
					}

					case Move.KNIGHT:
					{
						knights ^= moveMask;
						key[color] ^= ZobristKey.knight[color][fromIndex] ^ ZobristKey.knight[color][toIndex
							];
						break;
					}

					case Move.QUEEN:
					{
						queens ^= moveMask;
						key[color] ^= ZobristKey.queen[color][fromIndex] ^ ZobristKey.queen[color][toIndex
							];
						break;
					}

					case Move.KING:
					{
						// if castling, moves rooks too
						long rookMask = 0;
						switch (moveType)
						{
							case Move.TYPE_KINGSIDE_CASTLING:
							{
								rookMask = (GetTurn() ? unchecked((long)(0x05L)) : unchecked((long)(0x0500000000000000L
									)));
								key[color] ^= ZobristKey.rook[color][toIndex - 1] ^ ZobristKey.rook[color][toIndex
									 + 1];
								break;
							}

							case Move.TYPE_QUEENSIDE_CASTLING:
							{
								rookMask = (GetTurn() ? unchecked((long)(0x90L)) : unchecked((long)(0x9000000000000000L
									)));
								key[color] ^= ZobristKey.rook[color][toIndex - 1] ^ ZobristKey.rook[color][toIndex
									 + 2];
								break;
							}
						}
						if (rookMask != 0)
						{
							if (GetTurn())
							{
								whites ^= rookMask;
							}
							else
							{
								blacks ^= rookMask;
							}
							rooks ^= rookMask;
						}
						kings ^= moveMask;
						key[color] ^= ZobristKey.king[color][fromIndex] ^ ZobristKey.king[color][toIndex];
						break;
					}
				}
				// Move pieces in colour fields
				if (GetTurn())
				{
					whites ^= moveMask;
				}
				else
				{
					blacks ^= moveMask;
				}
				// Tests to disable castling
				if ((moveMask & unchecked((long)(0x0000000000000009L))) != 0 && (flags & FLAG_WHITE_DISABLE_KINGSIDE_CASTLING
					) == 0)
				{
					flags |= FLAG_WHITE_DISABLE_KINGSIDE_CASTLING;
					key[0] ^= ZobristKey.whiteKingSideCastling;
				}
				if ((moveMask & unchecked((long)(0x0000000000000088L))) != 0 && (flags & FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING
					) == 0)
				{
					flags |= FLAG_WHITE_DISABLE_QUEENSIDE_CASTLING;
					key[0] ^= ZobristKey.whiteQueenSideCastling;
				}
				if ((moveMask & unchecked((long)(0x0900000000000000L))) != 0 && (flags & FLAG_BLACK_DISABLE_KINGSIDE_CASTLING
					) == 0)
				{
					flags |= FLAG_BLACK_DISABLE_KINGSIDE_CASTLING;
					key[1] ^= ZobristKey.blackKingSideCastling;
				}
				if ((moveMask & unchecked((long)(0x8800000000000000L))) != 0 && (flags & FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING
					) == 0)
				{
					flags |= FLAG_BLACK_DISABLE_QUEENSIDE_CASTLING;
					key[1] ^= ZobristKey.blackQueenSideCastling;
				}
			}
			// Change turn
			flags ^= FLAG_TURN;
			key[0] ^= ZobristKey.whiteMove;
			// // TODO remove
			// long aux[] = ZobristKey.getKey(this);
			// if (key[0] != aux[0] || key[1] != aux[1]) {
			// System.out.println("Zobrist key Error");
			// logger.debug("\n" + toString());
			// logger.debug("Move = " + Move.toStringExt(move));
			// Move.printMoves(moveHistory, 0, moveNumber);
			// logger.debug("afterc: " + aux[0] + " " + aux[1]);
			// logger.debug("after:  " + key[0] + " " + key[1]);
			// System.exit(-1);
			// key = aux;
			// }
			if (IsValid(!turn))
			{
				SetCheckFlags(!turn);
				if (fillInfo)
				{
					GenerateLegalMoves();
					if (IsMate())
					{
						// Append # when mate
						sanMoves.Put(moveNumber - 1, sanMoves.Get(moveNumber - 1) + "#");
					}
					else
					{
						if (GetCheck())
						{
							// Append + when check
							sanMoves.Put(moveNumber - 1, sanMoves.Get(moveNumber - 1) + "+");
						}
					}
				}
				return true;
			}
			else
			{
				UndoMove();
				return false;
			}
		}

		/// <summary>Checks is a state is valid Basically, not entering own king in check</summary>
		private bool IsValid(bool turn)
		{
			return (!bbAttacks.IsSquareAttacked(this, kings & GetOthers(), !turn));
		}

		private void SetCheckFlags(bool turn)
		{
			// Set check flags
			if (bbAttacks.IsSquareAttacked(this, kings & GetMines(), turn))
			{
				flags |= FLAG_CHECK;
			}
			else
			{
				flags &= ~FLAG_CHECK;
			}
		}

		public virtual void UndoMove()
		{
			UndoMove(moveNumber - 1);
		}

		public virtual void UndoMove(int moveNumber)
		{
			if (moveNumber < 0 || moveNumber < initialMoveNumber)
			{
				return;
			}
			this.moveNumber = moveNumber;
			whites = whitesHistory[moveNumber];
			blacks = blacksHistory[moveNumber];
			pawns = pawnsHistory[moveNumber];
			knights = knightsHistory[moveNumber];
			bishops = bishopsHistory[moveNumber];
			rooks = rooksHistory[moveNumber];
			queens = queensHistory[moveNumber];
			kings = kingsHistory[moveNumber];
			flags = flagsHistory[moveNumber];
			key[0] = keyHistory[moveNumber][0];
			key[1] = keyHistory[moveNumber][1];
			fiftyMovesRule = fiftyMovesRuleHistory[moveNumber];
		}

		/// <summary>0 no, 1 whites won, -1 blacks won, 99 draw</summary>
		public virtual int IsEndGame()
		{
			int endGame = 0;
			GenerateLegalMoves();
			if (legalMoveCount == 0)
			{
				if (GetCheck())
				{
					endGame = (GetTurn() ? -1 : 1);
				}
				else
				{
					endGame = 99;
				}
			}
			else
			{
				if (IsDraw())
				{
					endGame = 99;
				}
			}
			return endGame;
		}

		public virtual bool IsMate()
		{
			int endgameState = IsEndGame();
			return endgameState == 1 || endgameState == -1;
		}

		/// <summary>checks draw by fiftymoves rule and threefold repetition</summary>
		public virtual bool IsDraw()
		{
			if (fiftyMovesRule >= 100)
			{
				return true;
			}
			int repetitions = 0;
			// logger.debug("My keys key0=" + key[0] + " " + " key1=" + key[1]);
			for (int i = 0; i < (moveNumber - 1); i++)
			{
				if (keyHistory[i][0] == key[0] && keyHistory[i][1] == key[1])
				{
					repetitions++;
				}
				// logger.debug("movenumber="+i+" key0=" + keyHistory[i][0] + " " +
				// " key1=" + keyHistory[i][1] + " Repetitions="+repetitions);
				if (repetitions >= 2)
				{
					// with the las one they are 3
					return true;
				}
			}
			// Draw by no material to mate
			return pawns == 0 && rooks == 0 && queens == 0 && bishops == 0 && knights == 0;
		}

		/// <summary>The SWAP algorithm</summary>
		public virtual int See(int move)
		{
			int pieceCaptured = 0;
			long to = Move.GetToSquare(move);
			if ((to & knights) != 0)
			{
				pieceCaptured = Move.KNIGHT;
			}
			else
			{
				if ((to & bishops) != 0)
				{
					pieceCaptured = Move.BISHOP;
				}
				else
				{
					if ((to & rooks) != 0)
					{
						pieceCaptured = Move.ROOK;
					}
					else
					{
						if ((to & queens) != 0)
						{
							pieceCaptured = Move.QUEEN;
						}
						else
						{
							if (Move.IsCapture(move))
							{
								pieceCaptured = Move.PAWN;
							}
						}
					}
				}
			}
			return See(Move.GetFromIndex(move), Move.GetToIndex(move), Move.GetPieceMoved(move
				), pieceCaptured);
		}

		public virtual int See(int fromIndex, int toIndex, int pieceMoved, int targetPiece
			)
		{
			int d = 0;
			long mayXray = pawns | bishops | rooks | queens;
			// not kings nor
			// knights
			long fromSquare = 1 << fromIndex;
			long all = GetAll();
			long attacks = bbAttacks.GetIndexAttacks(this, toIndex);
			long fromCandidates = 0;
			seeGain[d] = SEE_PIECE_VALUES[targetPiece];
			do
			{
				long side = (d & 1) == 0 ? GetOthers() : GetMines();
				d++;
				// next depth and side
				// speculative store, if defended
				seeGain[d] = SEE_PIECE_VALUES[pieceMoved] - seeGain[d - 1];
				attacks ^= fromSquare;
				// reset bit in set to traverse
				all ^= fromSquare;
				// reset bit in temporary occupancy (for x-Rays)
				if ((fromSquare & mayXray) != 0)
				{
					attacks |= bbAttacks.GetXrayAttacks(this, toIndex, all);
				}
				// Gets the next attacker
				fromSquare = 0;
				fromCandidates = 0;
				if ((fromCandidates = attacks & pawns & side) != 0)
				{
					pieceMoved = Move.PAWN;
				}
				else
				{
					if ((fromCandidates = attacks & knights & side) != 0)
					{
						pieceMoved = Move.KNIGHT;
					}
					else
					{
						if ((fromCandidates = attacks & bishops & side) != 0)
						{
							pieceMoved = Move.BISHOP;
						}
						else
						{
							if ((fromCandidates = attacks & rooks & side) != 0)
							{
								pieceMoved = Move.ROOK;
							}
							else
							{
								if ((fromCandidates = attacks & queens & side) != 0)
								{
									pieceMoved = Move.QUEEN;
								}
								else
								{
									if ((fromCandidates = attacks & kings & side) != 0)
									{
										pieceMoved = Move.KING;
									}
								}
							}
						}
					}
				}
				fromSquare = BitboardUtils.Lsb(fromCandidates);
			}
			while (fromSquare != 0);
			while (--d != 0)
			{
				seeGain[d - 1] = -Math.Max(-seeGain[d - 1], seeGain[d]);
			}
			return seeGain[0];
		}

		public virtual int GetOutBookMove()
		{
			return outBookMove;
		}

		public virtual void SetOutBookMove(int outBookMove)
		{
			this.outBookMove = outBookMove;
		}

		/// <summary>Check if a passed pawn is in the square, useful to trigger extensions</summary>
		/// <param name="index"></param>
		/// <returns></returns>
		public virtual bool IsPassedPawn(int index)
		{
			int rank = index >> 3;
			int column = 7 - index & 7;
			long square = unchecked((long)(0x1L)) << index;
			if ((whites & square) != 0)
			{
				return ((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) 
					& BitboardUtils.RANKS_UPWARDS[rank] & pawns & blacks) == 0;
			}
			else
			{
				if ((blacks & square) != 0)
				{
					return ((BitboardUtils.COLUMN[column] | BitboardUtils.COLUMNS_ADJACENTS[column]) 
						& BitboardUtils.RANKS_DOWNWARDS[rank] & pawns & whites) == 0;
				}
			}
			return false;
		}

		/// <summary>Returns true if move is legal</summary>
		/// <param name="move"></param>
		/// <returns></returns>
		public virtual bool IsMoveLegal(int move)
		{
			bool moveOk = false;
			GenerateLegalMoves();
			for (int i = 0; i < legalMoveCount; i++)
			{
				// logger.debug(Move.toStringExt(legalMoves[i]));
				if (move == legalMoves[i])
				{
					moveOk = true;
				}
			}
			return moveOk;
		}

		/// <summary>Generates legal moves for the position when not already generated</summary>
		private void GenerateLegalMoves()
		{
			if ((key[0] != legalMovesKey[0]) || (key[1] != legalMovesKey[1]))
			{
				legalMoveCount = legalMoveGenerator.GenerateMoves(this, legalMoves, 0);
				legalMovesKey[0] = key[0];
				legalMovesKey[1] = key[1];
			}
		}

		// logger.debug("Generated " + legalMoveCount + " legal moves....");
		public virtual int GetLegalMoves(int[] moves)
		{
			GenerateLegalMoves();
			for (int i = 0; i < legalMoveCount; i++)
			{
				moves[i] = legalMoves[i];
			}
			return legalMoveCount;
		}

		public virtual string GetSanMove(int moveNumber)
		{
			return sanMoves.Get(moveNumber);
		}

		public virtual bool GetMoveTurn(int moveNumber)
		{
			return (flagsHistory[moveNumber] & FLAG_TURN) == 0;
		}
	}
}
