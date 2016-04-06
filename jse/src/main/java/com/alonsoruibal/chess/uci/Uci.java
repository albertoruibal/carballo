package com.alonsoruibal.chess.uci;

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
	static final String NAME = "Carballo Chess Engine v1.4";
	static final String AUTHOR = "Alberto Alonso Ruibal";

	Config config;
	SearchEngineThreaded engine;
	SearchParameters searchParameters;

	boolean needsReload = true;

	public Uci() {
		Logger.noLog = true; // Disable logging
		config = new Config();
		config.setBook(new FileBook("/book_small.bin"));
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

				if ("uci".equals(command)) {
					System.out.println("id name " + NAME);
					System.out.println("id author " + AUTHOR);
					System.out.println("option name Hash type spin default " + Config.DEFAULT_TRANSPOSITION_TABLE_SIZE + " min 16 max 256");
					System.out.println("option name Ponder type check default " + Config.DEFAULT_PONDER);
					System.out.println("option name OwnBook type check default " + Config.DEFAULT_USE_BOOK);
					System.out.println("option name UCI_Chess960 type check default false");
					System.out.println("option name Evaluator type combo default " + Config.DEFAULT_EVALUATOR + " var simplified var complete var experimental");
					System.out.println("option name Elo type spin default " + Config.DEFAULT_ELO + " min 500 max " + Config.DEFAULT_ELO);
					System.out.println("uciok");

				} else if ("setoption".equals(command)) {
					index++; // Skip name
					// get the option name without spaces
					StringBuilder nameSB = new StringBuilder();
					String tok;
					while (!"value".equals(tok = tokens[index++])) {
						nameSB.append(tok);
					}
					String name = nameSB.toString();
					String value = tokens[index];

					if ("Hash".equals(name)) {
						config.setTranspositionTableSize(Integer.parseInt(value));
					} else if ("Ponder".equals(name)) {
						config.setPonder(Boolean.parseBoolean(value));
					} else if ("OwnBook".equals(name)) {
						config.setUseBook(Boolean.parseBoolean(value));
					} else if ("UCI_Chess960".equals(name)) {
						config.setUciChess960(Boolean.parseBoolean(value));
					} else if ("Evaluator".equals(name)) {
						config.setEvaluator(value);
					} else if ("Elo".equals(name)) {
						config.setElo(Integer.parseInt(value));
					}
					needsReload = true;

				} else if ("isready".equals(command)) {
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

				} else if ("quit".equals(command)) {
					System.exit(0);

				} else if ("go".equals(command)) {
					searchParameters = new SearchParameters();
					while (index < tokens.length) {
						String arg1 = tokens[index++];
						if ("searchmoves".equals(arg1)) {
							// TODO
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
					engine.go(searchParameters);

				} else if ("stop".equals(command)) {
					engine.stop();

				} else if ("ucinewgame".equals(command)) {
					engine.getBoard().startPosition();
					engine.clear();

				} else if ("position".equals(command)) {
					if (index < tokens.length) {
						String arg1 = tokens[index++];
						if ("startpos".equals(arg1)) {
							engine.getBoard().startPosition();
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
							engine.getBoard().setFen(fenSb.toString());
						}
					}
					if (index < tokens.length) {
						String arg1 = tokens[index++];
						if ("moves".equals(arg1)) {
							while (index < tokens.length) {
								int move = Move.getFromString(engine.getBoard(), tokens[index++], true);
								engine.getBoard().doMove(move);
							}
						}
					}

				} else if ("debug".equals(command)) {
				} else if ("ponderhit".equals(command)) {
					if (searchParameters != null) {
						searchParameters.setPonder(false);
						engine.setSearchLimits(searchParameters, false);
					}

				} else if ("register".equals(command)) {
					// not used
				} else {
					// System.out.println("Command not recognized: " + in);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
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

	public static void main(String args[]) {
		Uci uci = new Uci();
		uci.loop();
	}
}