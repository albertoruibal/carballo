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
	static final String NAME = "Carballo Chess Engine v1.2";
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
					System.out.println("option name Null Move type check default " + Config.DEFAULT_NULL_MOVE);
					System.out.println("option name Null Move Margin type spin default " + Config.DEFAULT_NULL_MOVE_MARGIN + " min 0 max 1000");
					System.out.println("option name Static Null Move type check default " + Config.DEFAULT_STATIC_NULL_MOVE);
					System.out.println("option name LMR type check default " + Config.DEFAULT_LMR);
					System.out.println("option name IID type check default " + Config.DEFAULT_IID);
					System.out.println("option name IID Margin type spin default " + Config.DEFAULT_IID_MARGIN + " min 1 max 1000");
					System.out.println("option name Extensions Check type spin default " + Config.DEFAULT_EXTENSIONS_CHECK + " min 0 max 2");
					System.out.println("option name Extensions Pawn Push type spin default " + Config.DEFAULT_EXTENSIONS_PAWN_PUSH + " min 0 max 2");
					System.out.println("option name Extensions Passed Pawn type spin default " + Config.DEFAULT_EXTENSIONS_PASSED_PAWN + " min 0 max 2");
					System.out.println("option name Extensions Mate Threat type spin default " + Config.DEFAULT_EXTENSIONS_MATE_THREAT + " min 0 max 2");
					System.out.println("option name Extensions Singular type spin default " + Config.DEFAULT_EXTENSIONS_SINGULAR + " min 0 max 2");
					System.out
							.println("option name Singular Extension Margin type spin default " + Config.DEFAULT_SINGULAR_EXTENSION_MARGIN + " min 0 max 300");
					System.out.println("option name Evaluator type combo default " + Config.DEFAULT_EVALUATOR
							+ " var simplified var complete var experimental");
					System.out.println("option name Aspiration Window type check default " + Config.DEFAULT_ASPIRATION_WINDOW);
					System.out.println("option name Aspiration Window Sizes type string default " + Config.DEFAULT_ASPIRATION_WINDOW_SIZES);
					System.out.println("option name Futility type check default " + Config.DEFAULT_FUTILITY);
					System.out.println("option name Futility Margin type spin default " + Config.DEFAULT_FUTILITY_MARGIN + " min 1 max 1000");
					System.out.println("option name Aggressive Futility type check default " + Config.DEFAULT_AGGRESSIVE_FUTILITY);
					System.out.println("option name Aggressive Futility Margin type spin default " + Config.DEFAULT_AGGRESSIVE_FUTILITY_MARGIN
							+ " min 1 max 1000");
					System.out.println("option name Futility Margin QS type spin default " + Config.DEFAULT_FUTILITY_MARGIN_QS + " min 1 max 1000");
					System.out.println("option name Razoring type check default " + Config.DEFAULT_RAZORING);
					System.out.println("option name Razoring Margin type spin default " + Config.DEFAULT_RAZORING_MARGIN + " min 1 max 1000");
					System.out.println("option name Contempt Factor type spin default " + Config.DEFAULT_CONTEMPT_FACTOR + " min -200 max 200");
					System.out.println("option name Eval Center type spin default " + Config.DEFAULT_EVAL_CENTER + " min 0 max 200");
					System.out.println("option name Eval Positional type spin default " + Config.DEFAULT_EVAL_POSITIONAL + " min 0 max 200");
					System.out.println("option name Eval Attacks type spin default " + Config.DEFAULT_EVAL_ATTACKS + " min 0 max 200");
					System.out.println("option name Eval Mobility type spin default " + Config.DEFAULT_EVAL_MOBILITY + " min 0 max 200");
					System.out.println("option name Eval Pawn Structure type spin default " + Config.DEFAULT_EVAL_PAWN_STRUCTURE + " min 0 max 200");
					System.out.println("option name Eval Passed Pawns type spin default " + Config.DEFAULT_EVAL_PASSED_PAWNS + " min 0 max 200");
					System.out.println("option name Eval King Safety type spin default " + Config.DEFAULT_EVAL_KING_SAFETY + " min 0 max 200");
					System.out.println("option name Rand type spin default " + Config.DEFAULT_RAND + " min 0 max 100");
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
					String value = tokens[index++];

					if ("Hash".equals(name)) {
						config.setTranspositionTableSize(Integer.parseInt(value));
					} else if ("Ponder".equals(name)) {
						config.setPonder(Boolean.parseBoolean(value));
					} else if ("OwnBook".equals(name)) {
						config.setUseBook(Boolean.parseBoolean(value));
					} else if ("NullMove".equals(name)) {
						config.setNullMove(Boolean.parseBoolean(value));
					} else if ("NullMoveMargin".equals(name)) {
						config.setNullMoveMargin(Integer.parseInt(value));
					} else if ("StaticNullMove".equals(name)) {
						config.setStaticNullMove(Boolean.parseBoolean(value));
					} else if ("IID".equals(name)) {
						config.getIid(Boolean.parseBoolean(value));
					} else if ("IIDMargin".equals(name)) {
						config.setIidMargin(Integer.parseInt(value));
					} else if ("ExtensionsCheck".equals(name)) {
						config.setExtensionsCheck(Integer.parseInt(value));
					} else if ("ExtensionsPawnPush".equals(name)) {
						config.setExtensionsPawnPush(Integer.parseInt(value));
					} else if ("ExtensionsPassedPawn".equals(name)) {
						config.setExtensionsPassedPawn(Integer.parseInt(value));
					} else if ("ExtensionsMateThreat".equals(name)) {
						config.setExtensionsMateThreat(Integer.parseInt(value));
					} else if ("ExtensionsSingular".equals(name)) {
						config.setExtensionsSingular(Integer.parseInt(value));
					} else if ("SingularExtensionMargin".equals(name)) {
						config.setSingularExtensionMargin(Integer.parseInt(value));
					} else if ("Evaluator".equals(name)) {
						config.setEvaluator(value);
					} else if ("AspirationWindow".equals(name)) {
						config.setAspirationWindow(Boolean.parseBoolean(value));
					} else if ("AspirationWindowSizes".equals(name)) {
						config.setAspirationWindowSizes(value);
					} else if ("Futility".equals(name)) {
						config.setFutility(Boolean.parseBoolean(value));
					} else if ("FutilityMargin".equals(name)) {
						config.setFutilityMargin(Integer.parseInt(value));
					} else if ("AggressiveFutility".equals(name)) {
						config.setAggressiveFutility(Boolean.parseBoolean(value));
					} else if ("AggressiveFutilityMargin".equals(name)) {
						config.setAggressiveFutilityMargin(Integer.parseInt(value));
					} else if ("FutilityMarginQS".equals(name)) {
						config.setFutilityMarginQS(Integer.parseInt(value));
					} else if ("Razoring".equals(name)) {
						config.setRazoring(Boolean.parseBoolean(value));
					} else if ("RazoringMargin".equals(name)) {
						config.setRazoringMargin(Integer.parseInt(value));
					} else if ("ContemptFactor".equals(name)) {
						config.setContemptFactor(Integer.parseInt(value));
					} else if ("EvalCenter".equals(name)) {
						config.setEvalCenter(Integer.parseInt(value));
					} else if ("EvalPositional".equals(name)) {
						config.setEvalPositional(Integer.parseInt(value));
					} else if ("EvalAttacks".equals(name)) {
						config.setEvalAttacks(Integer.parseInt(value));
					} else if ("EvalMobility".equals(name)) {
						config.setEvalMobility(Integer.parseInt(value));
					} else if ("EvalPawnStructure".equals(name)) {
						config.setEvalPawnStructure(Integer.parseInt(value));
					} else if ("EvalPassedPawns".equals(name)) {
						config.setEvalPassedPawns(Integer.parseInt(value));
					} else if ("EvalKingSafety".equals(name)) {
						config.setEvalKingSafety(Integer.parseInt(value));
					} else if ("Rand".equals(name)) {
						config.setRand(Integer.parseInt(value));
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
								Thread.sleep(100);
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
						engine.setSearchLimits(searchParameters);
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
	}

	public void info(SearchStatusInfo info) {
		System.out.print("info ");
		System.out.println(info.toString());
	}

	public static void main(String args[]) {
		Uci uci = new Uci();
		uci.loop();
	}
}