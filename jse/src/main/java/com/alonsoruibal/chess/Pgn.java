package com.alonsoruibal.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.alonsoruibal.chess.log.Logger;

/**
 * TODO Parse comments
 * 
 * 1. Event: the name of the tournament or match event. 2. Site: the location of
 * the event. This is in "City, Region COUNTRY" format, where COUNTRY is the
 * 3-letter International Olympic Committee code for the country. An example is
 * "New York City, NY USA". 3. Date: the starting date of the game, in
 * YYYY.MM.DD form. "??" are used for unknown values. 4. Round: the playing
 * round ordinal of the game within the event. 5. White: the player of the white
 * pieces, in "last name, first name" format. 6. Black: the player of the black
 * pieces, same format as White. 7. Result: the result of the game. This can
 * only have four possible values: "1-0" (White won), "0-1" (Black won),
 * "1/2-1/2" (Draw), or "*" (other, e.g., the game is ongoing).
 * 
 * @author rui
 * 
 */

public class Pgn {
	private static final Logger logger = Logger.getLogger("Pgn");

	static SimpleDateFormat formatDate = new SimpleDateFormat("yyyy.MM.dd");

	String pgnCurrentGame;

	String fenStartPosition;

	String event;
	String site;
	String date;
	String round;
	String white;
	String black;
	String whiteElo;
	String blackElo;
	String result;
	String eventType;
	String eventDate;
	String annotator;

	ArrayList<String> moves = new ArrayList<String>();;

	public String getPgn(Board b, String whiteName, String blackName) {
		return getPgn(b, whiteName, blackName, null);
	}

	/**
	 * 
	 * @return
	 */
	public String getPgn(Board b, String whiteName, String blackName, String result) {
		// logger.debug("PGN start");

		StringBuffer sb = new StringBuffer();

		if (whiteName == null || "".equals(whiteName))
			whiteName = "?";
		if (blackName == null || "".equals(blackName))
			blackName = "?";

		Date d = new Date();

		sb.append("[Event \"Chess Game\"]\n");
		sb.append("[Site \"Carballo Chess Engine\"]\n");
		sb.append("[Date \"" + formatDate.format(d) + "\"]\n");
		sb.append("[Round \"?\"]\n");
		sb.append("[White \"" + whiteName + "\"]\n");
		sb.append("[Black \"" + blackName + "\"]\n");
		if (result == null) {
			result = "*";
			switch (b.isEndGame()) {
			case 1:
				result = "1-0";
				break;
			case -1:
				result = "0-1";
				break;
			case 99:
				result = "1/2-1/2";
				break;
			}
		}
		sb.append("[Result \"" + result + "\"]\n");
		if (!Board.FEN_START_POSITION.equals(b.initialFen)) {
			sb.append("[FEN \"" + b.initialFen + "\"]\n");
		}
		sb.append("[PlyCount \"" + (b.moveNumber - b.initialMoveNumber) + "\"]\n");
		sb.append("\n");

		StringBuffer line = new StringBuffer();

		for (int i = b.initialMoveNumber; i < b.moveNumber; i++) {
			line.append(" ");
			if ((i & 1) == 0) {
				line.append((i >>> 1) + 1);
				line.append(".");
			}
			line.append(b.getSanMove(i));
		}

		if (!"*".equals(result)) {
			line.append(" ");
			line.append(result);
		}
		// Cut line in a limit of 80 characters
		String[] tokens = line.toString().split("[ \\t\\n\\x0B\\f\\r]+");

		int length = 0;
		for (int i = 0; i < tokens.length; i++) {
			String next = tokens[i];
			if (length + next.length() + 1 > 80) {
				sb.append("\n");
				length = 0;
			} else if (length > 0) {
				sb.append(" ");
				length++;
			}
			length += next.length();
			sb.append(next);
		}
		// logger.debug("PGN end");

		// logger.debug("PGN:\n" + sb.toString());
		return sb.toString();
	}

	/**
	 * Sets a board from a only 1-game pgn
	 * 
	 */
	public void parsePgn(String pgn) {
		event = "";
		round = "";
		site = "";
		date = "";
		white = "";
		black = "";
		whiteElo = "";
		blackElo = "";
		result = "";
		eventType = "";
		eventDate = "";
		annotator = "";
		moves.clear();

		fenStartPosition = Board.FEN_START_POSITION;

		// logger.debug("Loading PGN " + pgn);

		if (pgn == null)
			return;

		StringBuffer movesSb = new StringBuffer();

		try {
			String lines[] = pgn.split("\\r?\\n");

			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];

				if (line.indexOf("[") == 0) {
					// Is a header
					String headerName = line.substring(1, line.indexOf("\"")).trim().toLowerCase();
					String headerValue = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));

					if ("event".equals(headerName)) {
						event = headerValue;
					} else if ("round".equals(headerName)) {
						round = headerValue;
					} else if ("site".equals(headerName)) {
						site = headerValue;
					} else if ("date".equals(headerName)) {
						date = headerValue;
					} else if ("white".equals(headerName)) {
						white = headerValue;
					} else if ("black".equals(headerName)) {
						black = headerValue;
					} else if ("whiteelo".equals(headerName)) {
						whiteElo = headerValue;
					} else if ("blackelo".equals(headerName)) {
						blackElo = headerValue;
					} else if ("result".equals(headerName)) {
						result = headerValue;
					} else if ("fen".equals(headerName)) {
						fenStartPosition = headerValue;
					}
				} else {
					movesSb.append(line);
					movesSb.append(" ");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Remove all comments
		int comment1 = 0;
		int comment2 = 0;

		// logger.debug("Moves = " + movesSb.toString());

		String[] tokens = movesSb.toString().split("[ \\t\\n\\x0B\\f\\r]+");
		for (int i = 0; i < tokens.length; i++) {
			String el = tokens[i].trim();

			boolean addMove = true;

			if (el.indexOf("(") >= 0) {
				addMove = false;
				comment1++;
			}
			if (el.indexOf(")") >= 0) {
				addMove = false;
				comment1--;
			}
			if (el.indexOf("{") >= 0) {
				addMove = false;
				comment2++;
			}
			if (el.indexOf("}") >= 0) {
				addMove = false;
				comment2--;
			}

			if (addMove) {
				if ("1/2-1/2".equals(el)) {
				} else if ("1-0".equals(el)) {
				} else if ("0-1".equals(el)) {
				} else if (comment1 == 0 && comment2 == 0) {
					// Move 1.
					if (el.indexOf(".") >= 0) {
						el = el.substring(el.lastIndexOf(".") + 1);
					}

					if (el.length() > 0 && comment1 == 0 && comment2 == 0 && el.indexOf("$") < 0) {
						moves.add(el);
					}
				}
			}
		}
	}

	// parses a PGN and does all moves
	public void setBoard(Board b, String pgn) {
		parsePgn(pgn);
		b.setFen(fenStartPosition);

		for (String moveString : moves) {
			if ("*".equals(moveString))
				break;
			int move = Move.getFromString(b, moveString, true);
			if (move == 0 || move == -1) {
				logger.error("Move not Parsed: " + moveString);
				break;
			}

			if (!b.doMove(move)) {
				logger.error("Doing move=" + moveString + " " + Move.toStringExt(move) + " " + b.getTurn());
				break;
			}
		}
	}

	public String getGameNumber(InputStream is, int gameNumber) {
		// logger.debug("Loading GameNumber " + gameNumber);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		int counter = 0;
		try {
			while (true) {
				line = br.readLine();
				if (line == null)
					break;

				if (line.indexOf("[Event ") == 0) {
					if (counter == gameNumber) {
						StringBuffer pgnSb = new StringBuffer();
						try {
							while (true) {
								pgnSb.append(line);
								pgnSb.append("\n");
								line = br.readLine();
								if (line == null || line.indexOf("[Event ") == 0)
									break;
							}
						} catch (IOException e) {
						}
						return pgnSb.toString();
					}
					counter++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getPgnCurrentGame() {
		return pgnCurrentGame;
	}

	public String getEvent() {
		return event;
	}

	public String getRound() {
		return round;
	}

	public String getSite() {
		return site;
	}

	public String getDate() {
		return date;
	}

	public String getWhite() {
		return white;
	}

	public String getBlack() {
		return black;
	}

	public String getWhiteElo() {
		return whiteElo;
	}

	public String getBlackElo() {
		return blackElo;
	}

	public String getResult() {
		return result;
	}

	public String getEventType() {
		return eventType;
	}

	public String getEventDate() {
		return eventDate;
	}

	public String getAnnotator() {
		return annotator;
	}

	public String getFenStartPosition() {
		return fenStartPosition;
	}

	public ArrayList<String> getMoves() {
		return moves;
	}

}