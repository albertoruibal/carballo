/*
 * Code converted with Sharpen
 * 
 */using System;
using System.Text;
using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Log;
using Sharpen;

namespace Com.Alonsoruibal.Chess
{
	/// <summary>
	/// TODO Parse comments
	/// 1.
	/// </summary>
	/// <remarks>
	/// TODO Parse comments
	/// 1. Event: the name of the tournament or match event. 2. Site: the location of
	/// the event. This is in "City, Region COUNTRY" format, where COUNTRY is the
	/// 3-letter International Olympic Committee code for the country. An example is
	/// "New York City, NY USA". 3. Date: the starting date of the game, in
	/// YYYY.MM.DD form. "??" are used for unknown values. 4. Round: the playing
	/// round ordinal of the game within the event. 5. White: the player of the white
	/// pieces, in "last name, first name" format. 6. Black: the player of the black
	/// pieces, same format as White. 7. Result: the result of the game. This can
	/// only have four possible values: "1-0" (White won), "0-1" (Black won),
	/// "1/2-1/2" (Draw), or "*" (other, e.g., the game is ongoing).
	/// </remarks>
	/// <author>rui</author>
	public class Pgn
	{
		private static readonly Logger logger = Logger.GetLogger("Pgn");

		internal string pgnCurrentGame;

		internal string fenStartPosition;

		internal string @event;

		internal string site;

		internal string date;

		internal string round;

		internal string white;

		internal string black;

		internal string whiteElo;

		internal string blackElo;

		internal string result;

		internal string eventType;

		internal string eventDate;

		internal string annotator;

		internal AList<string> moves = new AList<string>();

		public virtual string GetPgn(Board b, string whiteName, string blackName)
		{
			return GetPgn(b, whiteName, blackName, null, null, null);
		}

		public virtual string GetPgn(Board b, string whiteName, string blackName, string 
			@event, string site, string result)
		{
			// logger.debug("PGN start");
			StringBuilder sb = new StringBuilder();
			if (whiteName == null || string.Empty.Equals(whiteName))
			{
				whiteName = "?";
			}
			if (blackName == null || string.Empty.Equals(blackName))
			{
				blackName = "?";
			}
			if (@event == null)
			{
				@event = "Chess Game";
			}
			if (site == null)
			{
				site = "-";
			}
			sb.Append("[Event \"" + @event + "\"]\n");
			sb.Append("[Site \"" + site + "\"]\n");
			//DateTime d = new DateTime();
			// For GWT we use deprecated methods
			//sb.Append("[Date \"" + d.GetYear() + "." + d.GetMonth() + "." + d.GetDay() + "\"]\n"
			//	);
			sb.Append("[Round \"?\"]\n");
			sb.Append("[White \"" + whiteName + "\"]\n");
			sb.Append("[Black \"" + blackName + "\"]\n");
			if (result == null)
			{
				result = "*";
				switch (b.IsEndGame())
				{
					case 1:
					{
						result = "1-0";
						break;
					}

					case -1:
					{
						result = "0-1";
						break;
					}

					case 99:
					{
						result = "1/2-1/2";
						break;
					}
				}
			}
			sb.Append("[Result \"" + result + "\"]\n");
			if (!Board.FEN_START_POSITION.Equals(b.initialFen))
			{
				sb.Append("[FEN \"" + b.initialFen + "\"]\n");
			}
			sb.Append("[PlyCount \"" + (b.moveNumber - b.initialMoveNumber) + "\"]\n");
			sb.Append("\n");
			StringBuilder line = new StringBuilder();
			for (int i = b.initialMoveNumber; i < b.moveNumber; i++)
			{
				line.Append(" ");
				if ((i & 1) == 0)
				{
					line.Append(((int)(((uint)i) >> 1)) + 1);
					line.Append(".");
				}
				line.Append(b.GetSanMove(i));
			}
			if (!"*".Equals(result))
			{
				line.Append(" ");
				line.Append(result);
			}
			// Cut line in a limit of 80 characters
			string[] tokens = line.ToString().Split("[ \\t\\n\\x0B\\f\\r]+");
			int length = 0;
			for (int i_1 = 0; i_1 < tokens.Length; i_1++)
			{
				string next = tokens[i_1];
				if (length + next.Length + 1 > 80)
				{
					sb.Append("\n");
					length = 0;
				}
				else
				{
					if (length > 0)
					{
						sb.Append(" ");
						length++;
					}
				}
				length += next.Length;
				sb.Append(next);
			}
			// logger.debug("PGN end");
			// logger.debug("PGN:\n" + sb.toString());
			return sb.ToString();
		}

		/// <summary>Sets a board from a only 1-game pgn</summary>
		public virtual void ParsePgn(string pgn)
		{
			@event = string.Empty;
			round = string.Empty;
			site = string.Empty;
			date = string.Empty;
			white = string.Empty;
			black = string.Empty;
			whiteElo = string.Empty;
			blackElo = string.Empty;
			result = string.Empty;
			eventType = string.Empty;
			eventDate = string.Empty;
			annotator = string.Empty;
			moves.Clear();
			fenStartPosition = Board.FEN_START_POSITION;
			// logger.debug("Loading PGN " + pgn);
			if (pgn == null)
			{
				return;
			}
			StringBuilder movesSb = new StringBuilder();
			try
			{
				string[] lines = pgn.Split("\\r?\\n");
				for (int i = 0; i < lines.Length; i++)
				{
					string line = lines[i];
					if (line.IndexOf("[") == 0)
					{
						// Is a header
						string headerName = Sharpen.Runtime.Substring(line, 1, line.IndexOf("\"")).Trim()
							.ToLower();
						string headerValue = Sharpen.Runtime.Substring(line, line.IndexOf("\"") + 1, line
							.LastIndexOf("\""));
						if ("event".Equals(headerName))
						{
							@event = headerValue;
						}
						else
						{
							if ("round".Equals(headerName))
							{
								round = headerValue;
							}
							else
							{
								if ("site".Equals(headerName))
								{
									site = headerValue;
								}
								else
								{
									if ("date".Equals(headerName))
									{
										date = headerValue;
									}
									else
									{
										if ("white".Equals(headerName))
										{
											white = headerValue;
										}
										else
										{
											if ("black".Equals(headerName))
											{
												black = headerValue;
											}
											else
											{
												if ("whiteelo".Equals(headerName))
												{
													whiteElo = headerValue;
												}
												else
												{
													if ("blackelo".Equals(headerName))
													{
														blackElo = headerValue;
													}
													else
													{
														if ("result".Equals(headerName))
														{
															result = headerValue;
														}
														else
														{
															if ("fen".Equals(headerName))
															{
																fenStartPosition = headerValue;
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
					else
					{
						movesSb.Append(line);
						movesSb.Append(" ");
					}
				}
			}
			catch (Exception e)
			{
				Sharpen.Runtime.PrintStackTrace(e);
			}
			// Remove all comments
			int comment1 = 0;
			int comment2 = 0;
			// logger.debug("Moves = " + movesSb.toString());
			string[] tokens = movesSb.ToString().Split("[ \\t\\n\\x0B\\f\\r]+");
			for (int i_1 = 0; i_1 < tokens.Length; i_1++)
			{
				string el = tokens[i_1].Trim();
				bool addMove = true;
				if (el.IndexOf("(") >= 0)
				{
					addMove = false;
					comment1++;
				}
				if (el.IndexOf(")") >= 0)
				{
					addMove = false;
					comment1--;
				}
				if (el.IndexOf("{") >= 0)
				{
					addMove = false;
					comment2++;
				}
				if (el.IndexOf("}") >= 0)
				{
					addMove = false;
					comment2--;
				}
				if (addMove)
				{
					if ("1/2-1/2".Equals(el))
					{
					}
					else
					{
						if ("1-0".Equals(el))
						{
						}
						else
						{
							if ("0-1".Equals(el))
							{
							}
							else
							{
								if (comment1 == 0 && comment2 == 0)
								{
									// Move 1.
									if (el.IndexOf(".") >= 0)
									{
										el = Sharpen.Runtime.Substring(el, el.LastIndexOf(".") + 1);
									}
									if (el.Length > 0 && comment1 == 0 && comment2 == 0 && el.IndexOf("$") < 0)
									{
										moves.AddItem(el);
									}
								}
							}
						}
					}
				}
			}
		}

		// parses a PGN and does all moves
		public virtual void SetBoard(Board b, string pgn)
		{
			ParsePgn(pgn);
			b.SetFen(fenStartPosition);
			foreach (string moveString in moves)
			{
				if ("*".Equals(moveString))
				{
					break;
				}
				int move = Move.GetFromString(b, moveString, true);
				if (move == 0 || move == -1)
				{
					logger.Error("Move not Parsed: " + moveString);
					break;
				}
				if (!b.DoMove(move))
				{
					logger.Error("Doing move=" + moveString + " " + Move.ToStringExt(move) + " " + b.
						GetTurn());
					break;
				}
			}
		}

		public virtual string GetPgnCurrentGame()
		{
			return pgnCurrentGame;
		}

		public virtual string GetEvent()
		{
			return @event;
		}

		public virtual string GetRound()
		{
			return round;
		}

		public virtual string GetSite()
		{
			return site;
		}

		public virtual string GetDate()
		{
			return date;
		}

		public virtual string GetWhite()
		{
			return white;
		}

		public virtual string GetBlack()
		{
			return black;
		}

		public virtual string GetWhiteElo()
		{
			return whiteElo;
		}

		public virtual string GetBlackElo()
		{
			return blackElo;
		}

		public virtual string GetResult()
		{
			return result;
		}

		public virtual string GetEventType()
		{
			return eventType;
		}

		public virtual string GetEventDate()
		{
			return eventDate;
		}

		public virtual string GetAnnotator()
		{
			return annotator;
		}

		public virtual string GetFenStartPosition()
		{
			return fenStartPosition;
		}

		public virtual AList<string> GetMoves()
		{
			return moves;
		}

		public virtual string GetGameNumber(string pgnFileContent, int gameNumber)
		{
			int lineNumber = 0;
			string[] lines = pgnFileContent.Split("\\r?\\n");
			string line;
			int counter = 0;
			try
			{
				while (true)
				{
					line = lines[lineNumber++];
					if (line == null)
					{
						break;
					}
					if (line.IndexOf("[Event ") == 0)
					{
						if (counter == gameNumber)
						{
							StringBuilder pgnSb = new StringBuilder();
							while (true)
							{
								pgnSb.Append(line);
								pgnSb.Append("\n");
								line = lines[lineNumber++];
								if (line == null || line.IndexOf("[Event ") == 0)
								{
									break;
								}
							}
							return pgnSb.ToString();
						}
						counter++;
					}
				}
			}
			catch (Exception e)
			{
				Sharpen.Runtime.PrintStackTrace(e);
			}
			return null;
		}
	}
}
