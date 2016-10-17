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

	/**
	 * Parses a 1-game pgn
	 */
	public static Game parsePgn(String pgn) {

		// logger.debug("Loading PGN " + pgn);

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
			String lines[] = pgn.split("\\r?\\n");

			for (String line : lines) {
				if (!"".equals(line.trim())) {
					if (parsingHeaders && line.indexOf("[") == 0) {
						// It is a header
						String headerName = line.substring(1, line.indexOf("\"")).trim().toLowerCase();
						String headerValue = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));

						if ("event".equals(headerName)) {
							game.setEvent(headerValue);
						} else if ("round".equals(headerName)) {
							game.setRound(headerValue);
						} else if ("site".equals(headerName)) {
							game.setSite(headerValue);
						} else if ("eventdate".equals(headerName)) {
							game.setEventDate(headerValue);
						} else if ("date".equals(headerName)) {
							game.setDate(headerValue);
						} else if ("white".equals(headerName)) {
							game.setWhite(headerValue);
						} else if ("black".equals(headerName)) {
							game.setBlack(headerValue);
						} else if ("whiteelo".equals(headerName)) {
							game.setWhiteElo(Integer.valueOf(headerValue));
						} else if ("blackelo".equals(headerName)) {
							game.setBlackElo(Integer.valueOf(headerValue));
						} else if ("whitefideid".equals(headerName)) {
							game.setWhiteFideId(Integer.valueOf(headerValue));
						} else if ("blackfideid".equals(headerName)) {
							game.setBlackFideId(Integer.valueOf(headerValue));
						} else if ("result".equals(headerName)) {
							game.setResult(headerValue);
						} else if ("fen".equals(headerName)) {
							game.setFenStartPosition(headerValue);
						}
					} else {
						parsingHeaders = false;

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
											|| "0-1".equals(s)
											|| "1/2-1/2".equals(s)
											|| "*".equals(s)) {
										currentVariation.variation.add(new GameNodeResult(s));

									} else if (s.charAt(0) >= '1' && s.charAt(0) <= '9' && s.charAt(s.length() - 1) == '.') {
										// Move number
										lastMoveNumber = s;

									} else if ((s.charAt(0) >= 'A' && s.charAt(0) <= 'Z')
											|| (s.charAt(0) >= 'a' && s.charAt(0) <= 'z')
											|| s.charAt(0) == '0') {
										lastMove = new GameNodeMove(lastMoveNumber, s, null);
										currentVariation.variation.add(lastMove);
										lastMoveNumber = null;

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
}