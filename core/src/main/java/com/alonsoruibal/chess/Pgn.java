package com.alonsoruibal.chess;

import com.alonsoruibal.chess.log.Logger;

import java.util.ArrayList;
import java.util.Date;

/**
 * TODO Parse comments
 * <p/>
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
 */

public class Pgn {
	private static final Logger logger = Logger.getLogger("Pgn");

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
	String whiteFideId;
	String blackFideId;
	String result;
	String eventType;
	String eventDate;
	String annotator;

	ArrayList<String> moves = new ArrayList<String>();

	public String getPgn(Board b, String whiteName, String blackName) {
		return getPgn(b, whiteName, blackName, null, null, null);
	}

	@SuppressWarnings("deprecation")
	public String getPgn(Board b, String whiteName, String blackName, String event, String site, String result) {
		// logger.debug("PGN start");

		StringBuilder sb = new StringBuilder();

		if (whiteName == null || "".equals(whiteName))
			whiteName = "?";
		if (blackName == null || "".equals(blackName))
			blackName = "?";

		if (event == null) {
			event = "Chess Game";
		}
		if (site == null) {
			site = "-";
		}

		sb.append("[Event \"").append(event).append("\"]\n");
		sb.append("[Site \"").append(site).append("\"]\n");

		Date d = new Date();
		// For GWT we use deprecated methods
		sb.append("[Date \"").append(d.getYear() + 1900).append(".").append(d.getMonth() + 1).append(".").append(d.getDate()).append("\"]\n");
		sb.append("[Round \"?\"]\n");
		sb.append("[White \"").append(whiteName).append("\"]\n");
		sb.append("[Black \"").append(blackName).append("\"]\n");
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
		sb.append("[Result \"").append(result).append("\"]\n");
		if (!Board.FEN_START_POSITION.equals(b.initialFen)) {
			sb.append("[FEN \"").append(b.initialFen).append("\"]\n");
		}
		sb.append("[PlyCount \"").append(b.moveNumber - b.initialMoveNumber).append("\"]\n");
		sb.append("\n");

		StringBuilder line = new StringBuilder();

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
		for (String token : tokens) {
			if (length + token.length() + 1 > 80) {
				sb.append("\n");
				length = 0;
			} else if (length > 0) {
				sb.append(" ");
				length++;
			}
			length += token.length();
			sb.append(token);
		}
		// logger.debug("PGN end");

		// logger.debug("PGN:\n" + sb.toString());
		return sb.toString();
	}

	/**
	 * Sets a board from a only 1-game pgn
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
		whiteFideId = "";
		blackFideId = "";
		result = "";
		eventType = "";
		eventDate = "";
		annotator = "";
		moves.clear();

		fenStartPosition = Board.FEN_START_POSITION;

		// logger.debug("Loading PGN " + pgn);

		if (pgn == null)
			return;

		StringBuilder movesSb = new StringBuilder();

		try {
			String lines[] = pgn.split("\\r?\\n");

			for (String line : lines) {
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
					} else if ("eventdate".equals(headerName)) {
						eventDate = headerValue;
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
					} else if ("whitefideid".equals(headerName)) {
						whiteFideId = headerValue;
					} else if ("blackfideid".equals(headerName)) {
						blackFideId = headerValue;
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
		for (String token : tokens) {
			String el = token.trim();

			boolean addMove = true;

			if (el.contains("(")) {
				addMove = false;
				comment1++;
			}
			if (el.contains(")")) {
				addMove = false;
				comment1--;
			}
			if (el.contains("{")) {
				addMove = false;
				comment2++;
			}
			if (el.contains("}")) {
				addMove = false;
				comment2--;
			}

			if (addMove) {
				if (comment1 == 0 && comment2 == 0) {
					if ("*".equals(el) ||
							"1/2-1/2".equals(el) ||
							"1-0".equals(el) ||
							"0-1".equals(el)) {
						break;
					} else {
						// Move 1.
						if (el.contains(".")) {
							el = el.substring(el.lastIndexOf(".") + 1);
						}
						if (el.length() > 0 && !el.contains("$")) {
							moves.add(el);
						}
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

	public String getWhiteFideId() {
		return whiteFideId;
	}

	public String getBlackFideId() {
		return blackFideId;
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

	public String getGameNumber(String pgnFileContent, int gameNumber) {
		int lineNumber = 0;
		String lines[] = pgnFileContent.split("\\r?\\n");
		String line;
		int counter = 0;

		try {
			while (true) {
				line = lines[lineNumber++];
				if (line == null) {
					break;
				}

				if (line.indexOf("[Event ") == 0) {
					if (counter == gameNumber) {
						StringBuilder pgnSb = new StringBuilder();
						while (true) {
							pgnSb.append(line);
							pgnSb.append("\n");
							line = lines[lineNumber++];
							if (line == null || line.indexOf("[Event ") == 0)
								break;
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

}