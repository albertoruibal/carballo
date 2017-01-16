package com.alonsoruibal.chess.pgn;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;

import java.util.Date;

public class PgnImportExport {

	/**
	 * Parses a PGN and does all the moves in a board
	 */
	public static void setBoard(Board b, String pgnString) {
		Game game = PgnParser.parsePgn(pgnString);
		if (game.getFenStartPosition() != null) {
			b.setFen(game.getFenStartPosition());
		} else {
			b.startPosition();
		}

		for (GameNode gameNode : game.getPv().variation) {
			if (gameNode instanceof GameNodeMove) {
				int move = Move.getFromString(b, ((GameNodeMove) gameNode).move, true);
				b.doMove(move);
			}
		}
	}

	public static String getPgn(Board b, String whiteName, String blackName) {
		return getPgn(b, whiteName, blackName, null, null, null);
	}

	public static String getPgn(Board b, String whiteName, String blackName, String event, String site, String result) {

		StringBuilder sb = new StringBuilder();

		if (whiteName == null || "".equals(whiteName)) {
			whiteName = "?";
		}
		if (blackName == null || "".equals(blackName)) {
			blackName = "?";
		}

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
				line.append(". ");
			}
			line.append(b.getSanMove(i));
		}

		line.append(" ");
		line.append(result);
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

		return sb.toString();
	}
}