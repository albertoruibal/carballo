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
	static final String NAME = "Carballo Chess Engine v1.8";
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
				String[] tokens = in.split(" ");
				int index = 0;
				String command = tokens[index++].toLowerCase();

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
						System.out.println("option name Evaluator type combo default " + Config.DEFAULT_EVALUATOR + " var tuned var simplified var complete var experimental");
						System.out.println("option name Contempt Factor type spin default " + Config.DEFAULT_CONTEMPT_FACTOR + " min -200 max 200");
						System.out.println("uciok");

						break;
					case "setoption":
						index++; // Skip name

						// get the option name without spaces
						StringBuilder nameSB = new StringBuilder();
						String tok;
						while (!"value".equals(tok = tokens[index++])) {
							nameSB.append(tok);
						}
						String name = nameSB.toString();
						String value = tokens[index];

						switch (name) {
							case "Hash":
								config.setTranspositionTableSize(Integer.parseInt(value));
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
								config.setElo(Integer.parseInt(value));
								break;
							case "Evaluator":
								config.setEvaluator(value);
								break;
							case "ContemptFactor":
								config.setContemptFactor(Integer.parseInt(value));
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
						} else {
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
								searchParameters.setWtime(Integer.parseInt(tokens[index++]));
							} else if ("btime".equals(arg1)) {
								searchParameters.setBtime(Integer.parseInt(tokens[index++]));
							} else if ("winc".equals(arg1)) {
								searchParameters.setWinc(Integer.parseInt(tokens[index++]));
							} else if ("binc".equals(arg1)) {
								searchParameters.setBinc(Integer.parseInt(tokens[index++]));
							} else if ("movestogo".equals(arg1)) {
								searchParameters.setMovesToGo(Integer.parseInt(tokens[index++]));
							} else if ("depth".equals(arg1)) {
								searchParameters.setDepth(Integer.parseInt(tokens[index++]));
							} else if ("nodes".equals(arg1)) {
								searchParameters.setNodes(Integer.parseInt(tokens[index++]));
							} else if ("mate".equals(arg1)) {
								searchParameters.setMate(Integer.parseInt(tokens[index++]));
							} else if ("movetime".equals(arg1)) {
								searchParameters.setMoveTime(Integer.parseInt(tokens[index++]));
							} else if ("infinite".equals(arg1)) {
								searchParameters.setInfinite(true);
							}
						}
						engine.getBoard().setFen(board.getInitialFen());
						engine.getBoard().doMoves(board.getMoves());
						engine.go(searchParameters);

						break;
					case "stop":
						engine.stop();

						break;
					case "ucinewgame":
						board.startPosition();
						engine.clear();

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
									fenSb.append(tokens[index++]);
									if (index < tokens.length) {
										fenSb.append(" ");
									}
								}
								board.setFen(fenSb.toString());
							}
						}
						if (index < tokens.length) {
							String arg1 = tokens[index++];
							if ("moves".equals(arg1)) {
								while (index < tokens.length) {
									int move = Move.getFromString(board, tokens[index++], true);
									board.doMove(move);
								}
							}
						}

						break;
					case "debug":
						break;
					case "ponderhit":
						if (searchParameters != null) {
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
			}

		} catch (IOException e) {
			System.out.println("info string Wrong UCI syntax");
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