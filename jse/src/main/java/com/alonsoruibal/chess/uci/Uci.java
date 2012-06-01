package com.alonsoruibal.chess.uci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchEngineThreaded;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;

/**
 * UCI Interface TODO ponder
 * 
 * @author rui
 */
public class Uci implements SearchObserver {

	SearchEngineThreaded engine;

	boolean needsReload = true;

	public Uci() {
		Logger.noLog = true; // Disable logging
		engine = new SearchEngineThreaded(new Config());
		engine.getConfig().setBook(new FileBook("/book_small.bin"));
	}

	void loop() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				String in = reader.readLine();
				String[] tokens = in.split(" ");
				int index = 0;
				String command = tokens[index++].toLowerCase();

				if ("uci".equals(command)) {
					System.out.println("id name Carballo Chess Engine v0.5");
					System.out.println("id author Alberto Alonso Ruibal");
					System.out.println("option name Hash type spin default " + Config.DEFAULT_TRANSPOSITION_TABLE_SIZE + " min 16 max 256");
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
					System.out.println("option name Extensions Recapture type spin default " + Config.DEFAULT_EXTENSIONS_RECAPTURE + " min 0 max 2");
					System.out.println("option name Extensions Singular type spin default " + Config.DEFAULT_EXTENSIONS_SINGULAR + " min 0 max 2");
					System.out
							.println("option name Singular Extension Margin type spin default " + Config.DEFAULT_SINGULAR_EXTENSION_MARGIN + " min 0 max 300");
					System.out.println("option name Evaluator type combo default " + Config.DEFAULT_EVALUATOR
							+ " var simplified var complete var experimental var experimentalold");
					System.out.println("option name Aspiration Window type check default " + Config.DEFAULT_ASPIRATION_WINDOW);
					System.out.println("option name Aspiration Window Sizes type string default " + Config.DEFAULT_ASPIRATION_WINDOW_SIZES);
					System.out.println("option name Futility type check default " + Config.DEFAULT_FUTILITY);
					System.out.println("option name Futility Margin type spin default " + Config.DEFAULT_FUTILITY_MARGIN + " min 1 max 1000");
					System.out.println("option name Aggressive Futility type check default " + Config.DEFAULT_AGGRESIVE_FUTILITY);
					System.out.println("option name Aggressive Futility Margin type spin default " + Config.DEFAULT_AGGRESIVE_FUTILITY_MARGIN
							+ " min 1 max 1000");
					System.out.println("option name Futility Margin QS spin default " + Config.DEFAULT_FUTILITY_MARGIN_QS + " min 1 max 1000");
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

					// get the option name without spaces
					StringBuffer nameSB = new StringBuffer();
					String tok;
					while (!"value".equals(tok = tokens[index++])) {
						nameSB.append(tok);
					}
					String name = nameSB.toString();
					String value = tokens[index++];

					if ("Hash".equals(name)) {
						engine.getConfig().setTranspositionTableSize(Integer.parseInt(value));
					} else if ("OwnBook".equals(name)) {
						engine.getConfig().setUseBook(Boolean.parseBoolean(value));
					} else if ("NullMove".equals(name)) {
						engine.getConfig().setNullMove(Boolean.parseBoolean(value));
					} else if ("NullMoveMargin".equals(name)) {
						engine.getConfig().setNullMoveMargin(Integer.parseInt(value));
					} else if ("StaticNullMove".equals(name)) {
						engine.getConfig().setStaticNullMove(Boolean.parseBoolean(value));
					} else if ("IID".equals(name)) {
						engine.getConfig().getIid(Boolean.parseBoolean(value));
					} else if ("IIDMargin".equals(name)) {
						engine.getConfig().setIidMargin(Integer.parseInt(value));
					} else if ("ExtensionsCheck".equals(name)) {
						engine.getConfig().setExtensionsCheck(Integer.parseInt(value));
					} else if ("ExtensionsPawnPush".equals(name)) {
						engine.getConfig().setExtensionsPawnPush(Integer.parseInt(value));
					} else if ("ExtensionsPassedPawn".equals(name)) {
						engine.getConfig().setExtensionsPassedPawn(Integer.parseInt(value));
					} else if ("ExtensionsMateThreat".equals(name)) {
						engine.getConfig().setExtensionsMateThreat(Integer.parseInt(value));
					} else if ("ExtensionsRecapture".equals(name)) {
						engine.getConfig().setExtensionsRecapture(Integer.parseInt(value));
					} else if ("ExtensionsSingular".equals(name)) {
						engine.getConfig().setExtensionsSingular(Integer.parseInt(value));
					} else if ("SingularExtensionMargin".equals(name)) {
						engine.getConfig().setSingularExtensionMargin(Integer.parseInt(value));
					} else if ("Evaluator".equals(name)) {
						engine.getConfig().setEvaluator(value);
					} else if ("AspirationWindow".equals(name)) {
						engine.getConfig().setAspirationWindow(Boolean.parseBoolean(value));
					} else if ("AspirationWindowSizes".equals(name)) {
						engine.getConfig().setAspirationWindowSizes(value);
					} else if ("Futility".equals(name)) {
						engine.getConfig().setFutility(Boolean.parseBoolean(value));
					} else if ("FutilityMargin".equals(name)) {
						engine.getConfig().setFutilityMargin(Integer.parseInt(value));
					} else if ("AggressiveFutility".equals(name)) {
						engine.getConfig().setAggressiveFutility(Boolean.parseBoolean(value));
					} else if ("AggressiveFutilityMargin".equals(name)) {
						engine.getConfig().setAggressiveFutilityMargin(Integer.parseInt(value));
					} else if ("FutilityMarginQS".equals(name)) {
						engine.getConfig().setFutilityMarginQS(Integer.parseInt(value));
					} else if ("Razoring".equals(name)) {
						engine.getConfig().setRazoring(Boolean.parseBoolean(value));
					} else if ("RazoringMargin".equals(name)) {
						engine.getConfig().setRazoringMargin(Integer.parseInt(value));
					} else if ("ContemptFactor".equals(name)) {
						engine.getConfig().setContemptFactor(Integer.parseInt(value));
					} else if ("EvalCenter".equals(name)) {
						engine.getConfig().setEvalCenter(Integer.parseInt(value));
					} else if ("EvalPositional".equals(name)) {
						engine.getConfig().setEvalPositional(Integer.parseInt(value));
					} else if ("EvalAttacks".equals(name)) {
						engine.getConfig().setEvalAttacks(Integer.parseInt(value));
					} else if ("EvalMobility".equals(name)) {
						engine.getConfig().setEvalMobility(Integer.parseInt(value));
					} else if ("EvalPawnStructure".equals(name)) {
						engine.getConfig().setEvalPawnStructure(Integer.parseInt(value));
					} else if ("EvalPassedPawns".equals(name)) {
						engine.getConfig().setEvalPassedPawns(Integer.parseInt(value));
					} else if ("EvalKingSafety".equals(name)) {
						engine.getConfig().setEvalKingSafety(Integer.parseInt(value));
					} else if ("Rand".equals(name)) {
						engine.getConfig().setRand(Integer.parseInt(value));
					}
					needsReload = true;

				} else if ("isready".equals(command)) {
					if (needsReload) {
						engine.init();
						engine.setObserver(this);
						System.gc();
						needsReload = false;
					}
					System.out.println("readyok");
				} else if ("quit".equals(command)) {
					System.exit(0);
				} else if ("go".equals(command)) {
					SearchParameters searchParameters = new SearchParameters();
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
				} else if ("position".equals(command)) {
					if (index < tokens.length) {
						String arg1 = tokens[index++];
						if ("startpos".equals(arg1)) {
							engine.getBoard().startPosition();
						} else if ("fen".equals(arg1)) {
							// FEN string may have spaces
							StringBuffer fenSb = new StringBuffer();
							while (index < tokens.length) {
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
					// TODO ponder not supported
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
		StringBuffer sb = new StringBuffer();
		sb.append("bestmove ");
		sb.append(Move.toString(bestMove));
		if (ponder != 0 && ponder != -1) {
			sb.append(" ponder ");
			sb.append(Move.toString(ponder));
		}
		System.out.println(sb.toString());
		// TODO reset engine, a weird problem originates strange moves if engine
		// is not reset
		// engine.getTT().clear();
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