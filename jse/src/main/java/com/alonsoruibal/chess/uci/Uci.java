package com.alonsoruibal.chess.uci;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchEngineThreaded;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * UCI Interface
 */
public class Uci implements SearchObserver {
	static final String NAME = "Carballo Chess Engine v1.9";
	static final String AUTHOR = "Alberto Alonso Ruibal";

	Config config;
	Board board;
	SearchEngineThreaded engine;
	SearchParameters searchParameters;

	boolean needsReload = true;

	public Uci() {
		Logger.noLog = true; // Disable logging
		config = new Config();
		config.setBook(new FileBook("/book_small.bin"));
		board = new Board();
	}

	void loop() {
		System.out.println(NAME + " by " + AUTHOR);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				String in = reader.readLine();
				if (in == null) {
					break; // EOF: stop the engine
				}
				String[] tokens = in.split(" ");
				int index = 0;
				String command = tokens[index++].toLowerCase();

				try {
					switch (command) {
						case "uci":
							System.out.println("id name " + NAME);
							System.out.println("id author " + AUTHOR);
							System.out.println("option name Hash type spin default " + Config.DEFAULT_TRANSPOSITION_TABLE_SIZE + " min 16 max 1024");
							System.out.println("option name Ponder type check default " + Config.DEFAULT_PONDER);
							System.out.println("option name OwnBook type check default " + Config.DEFAULT_USE_BOOK);
							System.out.println("option name UCI_Chess960 type check default " + Config.DEFAULT_UCI_CHESS960);
							System.out.println("option name UCI_LimitStrength type check default " + Config.DEFAULT_LIMIT_STRENGTH);
							System.out.println("option name UCI_Elo type spin default " + Config.DEFAULT_ELO + " min 500 max " + Config.DEFAULT_ELO);
							System.out.println("option name Evaluator type combo default " + Config.DEFAULT_EVALUATOR + " var simplified var complete var experimental");
							System.out.println("option name Contempt Factor type spin default " + Config.DEFAULT_CONTEMPT_FACTOR + " min -200 max 200");
							System.out.println("uciok");

							break;
						case "setoption":
							index++; // Skip name

							// Get the option name preserving spaces (e.g. "Contempt Factor").
							// The previous code appended the tokens without any separator, so
							// "Contempt Factor" became "ContemptFactor" and only matched by
							// accident; any advertised option with an internal space would
							// silently fail to be set.
							StringBuilder nameSB = new StringBuilder();
							while (index < tokens.length && !"value".equals(tokens[index])) {
								if (nameSB.length() > 0) {
									nameSB.append(' ');
								}
								nameSB.append(tokens[index++]);
							}
							String name = nameSB.toString();
							// Consume "value"
							if (index < tokens.length && "value".equals(tokens[index])) {
								index++;
							}
							String value = index < tokens.length ? tokens[index] : "";

							switch (name) {
								case "Hash":
									config.setTranspositionTableSize(parseIntSafe(value, Config.DEFAULT_TRANSPOSITION_TABLE_SIZE));
									break;
								case "Ponder":
									config.setPonder(Boolean.parseBoolean(value));
									break;
								case "OwnBook":
									config.setUseBook(Boolean.parseBoolean(value));
									break;
								case "UCI_Chess960":
									config.setUciChess960(Boolean.parseBoolean(value));
									break;
								case "UCI_LimitStrength":
									config.setLimitStrength(Boolean.parseBoolean(value));
									break;
								case "UCI_Elo":
									config.setElo(parseIntSafe(value, Config.DEFAULT_ELO));
									break;
								case "Evaluator":
									config.setEvaluator(value);
									break;
								case "Contempt Factor":
									config.setContemptFactor(parseIntSafe(value, Config.DEFAULT_CONTEMPT_FACTOR));
									break;
							}
							needsReload = true;

							break;
						case "isready":
							if (needsReload) {
								engine = new SearchEngineThreaded(config);
								engine.setObserver(this);
								needsReload = false;
								System.gc();
							} else if (engine != null) {
								// Wait for the engine to finish searching
								while (engine.isSearching()) {
									try {
										Thread.sleep(10);
									} catch (Exception e) {
									}
								}
							}
							System.out.println("readyok");

							break;
						case "quit":
							System.exit(0);

						case "go":
							if (engine == null) {
								System.out.println("info string The engine is not initialized: the isready command must be sent before any search");
								continue;
							}

							searchParameters = new SearchParameters();
							while (index < tokens.length) {
								String arg1 = tokens[index++];
								if ("searchmoves".equals(arg1)) {
									// While valid moves are found, add to the searchMoves
									while (index < tokens.length) {
										int move = Move.getFromString(board, tokens[index++], true);
										if (move != Move.NONE) {
											searchParameters.addSearchMove(move);
										} else {
											index--;
											break;
										}
									}
								} else if ("ponder".equals(arg1)) {
									searchParameters.setPonder(true);
								} else if ("wtime".equals(arg1)) {
									searchParameters.setWtime(parseIntSafe(nextToken(tokens, index++), 0));
								} else if ("btime".equals(arg1)) {
									searchParameters.setBtime(parseIntSafe(nextToken(tokens, index++), 0));
								} else if ("winc".equals(arg1)) {
									searchParameters.setWinc(parseIntSafe(nextToken(tokens, index++), 0));
								} else if ("binc".equals(arg1)) {
									searchParameters.setBinc(parseIntSafe(nextToken(tokens, index++), 0));
								} else if ("movestogo".equals(arg1)) {
									searchParameters.setMovesToGo(parseIntSafe(nextToken(tokens, index++), 0));
								} else if ("depth".equals(arg1)) {
									searchParameters.setDepth(parseIntSafe(nextToken(tokens, index++), Integer.MAX_VALUE));
								} else if ("nodes".equals(arg1)) {
									searchParameters.setNodes(parseIntSafe(nextToken(tokens, index++), Integer.MAX_VALUE));
								} else if ("mate".equals(arg1)) {
									searchParameters.setMate(parseIntSafe(nextToken(tokens, index++), 0));
								} else if ("movetime".equals(arg1)) {
									searchParameters.setMoveTime(parseIntSafe(nextToken(tokens, index++), Integer.MAX_VALUE));
								} else if ("infinite".equals(arg1)) {
									searchParameters.setInfinite(true);
								}
							}
							engine.getBoard().setFen(board.getInitialFen());
							engine.getBoard().doMoves(board.getMoves());
							engine.go(searchParameters);

							break;
						case "stop":
							if (engine != null) {
								engine.stop();
							}

							break;
						case "ucinewgame":
							board.startPosition();
							if (engine != null) {
								engine.clear();
							}

							break;
						case "position":
							if (index < tokens.length) {
								String arg1 = tokens[index++];
								if ("startpos".equals(arg1)) {
									board.startPosition();
								} else if ("fen".equals(arg1)) {
									// FEN string may have spaces
									StringBuilder fenSb = new StringBuilder();
									while (index < tokens.length) {
										if ("moves".equals(tokens[index])) {
											break;
										}
										if (fenSb.length() > 0) {
											fenSb.append(" ");
										}
										fenSb.append(tokens[index++]);
									}
									board.setFen(fenSb.toString());
								} else if ("moves".equals(arg1)) {
									// "position moves ..." without startpos/fen: treat as startpos
									board.startPosition();
									index--;
								}
							}
							if (index < tokens.length) {
								String arg1 = tokens[index++];
								if ("moves".equals(arg1)) {
									while (index < tokens.length) {
										int move = Move.getFromString(board, tokens[index++], true);
										// Validate the move: an illegal/unparseable move would
										// otherwise leave the board unadvanced and desynchronize
										// every subsequent move.
										if (move == Move.NONE || !board.doMove(move)) {
											System.out.println("info string illegal move in position: " + tokens[index - 1]);
											break;
										}
									}
								}
							}

							break;
						case "debug":
							break;
						case "ponderhit":
							if (searchParameters != null && engine != null) {
								searchParameters.setPonder(false);
								engine.updateSearchParameters(searchParameters);
							}

							break;
						case "register":
							// not used
							break;
						default:
							System.out.println("info string Wrong UCI command");
							break;
					}
				} catch (RuntimeException e) {
					// A malformed UCI command (missing tokens, bad numbers, etc.) must not
					// crash the engine loop: report it on stderr and keep processing.
					System.err.println("info string error processing '" + command + "': " + e.getMessage());
				}

			}

		} catch (IOException e) {
			System.out.println("info string Wrong UCI syntax");
		}
	}

	private static String nextToken(String[] tokens, int index) {
		return index >= 0 && index < tokens.length ? tokens[index] : "";
	}

	private static int parseIntSafe(String value, int defaultValue) {
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public void bestMove(int bestMove, int ponder) {
		StringBuilder sb = new StringBuilder();
		sb.append("bestmove ");
		sb.append(Move.toString(bestMove));
		if (config.getPonder() && ponder != Move.NONE) {
			sb.append(" ponder ");
			sb.append(Move.toString(ponder));
		}
		System.out.println(sb.toString());
		System.out.flush();
	}

	public void info(SearchStatusInfo info) {
		System.out.print("info ");
		System.out.println(info.toString());
		System.out.flush();
	}

	public static void main(String[] args) {
		Uci uci = new Uci();
		uci.loop();
	}
}