package com.alonsoruibal.chess.pgn;

import com.alonsoruibal.chess.log.Logger;

import java.util.LinkedList;

/**
 * Pgn parser wit variations support
 *
 * @author rui
 */

public class PgnParser {
	private static final Logger logger = Logger.getLogger("Pgn");

	public static Game parsePgn(String pgn) {
		return parsePgn(pgn, true);
	}

	/**
	 * Parses a 1-game pgn
	 */
	public static Game parsePgn(String pgn, boolean parseBody) {
		if (pgn == null) {
			return null;
		}
		Game game = new Game();
		LinkedList<GameNodeVariation> variations = new LinkedList<>();
		GameNodeVariation principalVariation = new GameNodeVariation();
		GameNodeVariation currentVariation = principalVariation;
		variations.add(principalVariation);

		String lastMoveNumber = null;
		GameNodeMove lastMove = null;
		StringBuilder sb = new StringBuilder();
		boolean parsingHeaders = true;
		boolean parsingComment = false;

		try {
			String[] lines = pgn.split("\\r?\\n");

			for (String line : lines) {
				if (!"".equals(line.trim())) {
					if (parsingHeaders && line.indexOf("[") == 0) {
						// It is a header. Guard against malformed header lines without
						// quotes (e.g. "[Event]" or "[Event foo]"): the previous code called
						// line.substring(1, line.indexOf("\"")) which threw
						// StringIndexOutOfBoundsException when indexOf returned -1, dropping
						// the whole game.
						int firstQuote = line.indexOf("\"");
						if (firstQuote <= 0) {
							continue;
						}
						int lastQuote = line.lastIndexOf("\"");
						if (lastQuote <= firstQuote) {
							continue;
						}
						String headerName = line.substring(1, firstQuote).trim().toLowerCase();
						String headerValue = line.substring(firstQuote + 1, lastQuote);

						if (!"".equals(headerValue) && !"?".equals(headerValue) && !"-".equals(headerValue)) {
							switch (headerName) {
								case "event":
									game.setEvent(headerValue);
									break;
								case "round":
									game.setRound(headerValue);
									break;
								case "site":
									game.setSite(headerValue);
									break;
								case "eventdate":
									game.setEventDate(headerValue);
									break;
								case "date":
									game.setDate(headerValue);
									break;
								case "white":
									game.setWhite(headerValue);
									break;
								case "black":
									game.setBlack(headerValue);
									break;
							case "whiteelo":
								game.setWhiteElo(parseInteger(headerValue));
								break;
							case "blackelo":
								game.setBlackElo(parseInteger(headerValue));
								break;
							case "whitefideid":
								game.setWhiteFideId(parseInteger(headerValue));
								break;
							case "blackfideid":
								game.setBlackFideId(parseInteger(headerValue));
								break;
								case "result":
									game.setResult(headerValue);
									break;
								case "fen":
									game.setFenStartPosition(headerValue);
									break;
							}
						}
					} else {
						parsingHeaders = false;
						if (!parseBody) {
							break;
						}

						for (int i = 0; i < line.length(); i++) {
							char c = line.charAt(i);

							if (parsingComment) {
								if (c == '}') {
									currentVariation.variation.add(new GameNodeComment(sb.toString()));
									sb.setLength(0);
									parsingComment = false;
								} else {
									sb.append(c);
								}
							} else {
								boolean finishLastEntity = false;

								if (c == '{') {
									parsingComment = true;
									finishLastEntity = true;
								} else if (c == ' ') {
									if (sb.length() != 0) {
										finishLastEntity = true;
									}
								} else if (c == '(' || c == ')') {
									finishLastEntity = true;
								} else {
									sb.append(c);
									if (i == line.length() - 1) {
										finishLastEntity = true;
									}
								}

								if (finishLastEntity && sb.length() > 0) {
									String s = sb.toString();
									sb.setLength(0);

									if ("1-0".equals(s)
											|| "0-1".equals(s)
											|| "1/2-1/2".equals(s)
											|| "½-½".equals(s)
											|| "*".equals(s)) {
										currentVariation.variation.add(new GameNodeResult(s));

									} else if (s.length() > 1 && isAlphaNumeric(s.charAt(0))) {

										// Strip the move number (with one or more dots) from the beginning of the string
										if (s.charAt(0) >= '1' && s.charAt(0) <= '9') {
											int lastDotIndex = s.lastIndexOf(".");
											lastMoveNumber = s.substring(0, lastDotIndex + 1);
											s = s.substring(lastDotIndex + 1);
										}

										// Search annotations at the end of the move
										String annotation = null;
										int lastIndex;
										for (lastIndex = s.length() - 1; lastIndex >= 0; lastIndex--) {
											if (isAlphaNumeric(s.charAt(lastIndex))) {
												break;
											}
										}
										if (lastIndex < s.length() - 1) {
											annotation = s.substring(lastIndex + 1);
											s = s.substring(0, lastIndex + 1);
										}

										if (s.length() > 0) {
											lastMove = new GameNodeMove(lastMoveNumber, s, annotation);
											currentVariation.variation.add(lastMove);
											lastMoveNumber = null;
										}

									} else {
										// glyph
										if (lastMove != null) {
											if (lastMove.annotation == null) {
												lastMove.annotation = NumericAnnotationGlyphs.translate(s);
											} else {
												lastMove.annotation += " " + NumericAnnotationGlyphs.translate(s);
											}
										}
									}
								}

								// Variations management
								if (c == '(') {
									currentVariation = new GameNodeVariation();
									variations.add(currentVariation);
								} else if (c == ')') {
									GameNodeVariation lastVariation = variations.removeLast();
									currentVariation = variations.getLast();
									currentVariation.variation.add(lastVariation);
								}
							}
						}
						if (parsingComment) {
							sb.append(" ");
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR parsing pgn: " + pgn);
			e.printStackTrace();
		}

		game.pv = principalVariation;
		return game;
	}

	private static boolean isAlphaNumeric(char c) {
		// Include '0' so castling written as "0-0"/"0-0-0" (PGN spec allows both "O-O"
		// and "0-0") is treated as a move token and not routed to the glyph branch,
		// where it would be silently dropped.
		return (c >= 'A' && c <= 'Z')
				|| (c >= 'a' && c <= 'z')
				|| (c >= '0' && c <= '9');
	}

	/**
	 * Parses a PGN integer header value, returning null on a non-numeric value
	 * instead of throwing NumberFormatException and dropping the whole game.
	 */
	private static Integer parseInteger(String value) {
		if (value == null || value.isEmpty() || "?".equals(value)) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}