import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.uci.UciEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Test a EPD file against engines executables with UCI interface
 */
public class EpdTest {
	private static final Logger logger = Logger.getLogger("EpdTest");

	int solved;
	int fails;
	int total;

	int[] avoidMoves;
	int[] bestMoves;
	boolean solutionFound;

	int bestMove;

	UciEngine engine;

	SearchParameters params;
	long lastTime;

	int processEpdFile(InputStream is, int time, int nodes) {
		solved = 0;
		total = 0;
		StringBuilder notSolved = new StringBuilder();
		// goes through all positions
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				logger.debug("Test = " + line);
				String avoidMovesString = null;
				int i0 = line.indexOf(" am ");
				if (i0 >= 0) {
					int i2 = line.indexOf(";", i0 + 4);
					avoidMovesString = line.substring(i0 + 4, i2);
				}
				String bestMovesString = null;
				int i1 = line.indexOf(" bm ");
				if (i1 >= 0) {
					int i2 = line.indexOf(";", i1 + 4);
					bestMovesString = line.substring(i1 + 4, i2);
				}

				total++;
				if (testPosition(line.substring(0, (i0 != -1 ? i0 : i1)), avoidMovesString, bestMovesString, time, nodes)) {
					solved++;
				} else {
					notSolved.append(line);
					notSolved.append("\n");
				}

				logger.debug("Status: " + solved + " positions solved of " + total);
				logger.debug("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		fails = total - solved;

		logger.debug("TEST    TIME");
		logger.debug("***** Positions not Solved:");
		logger.debug(notSolved.toString());
		logger.debug("***** Result:" + solved + " positions solved of " + total + " (" + fails + " fails)");

		return solved;
	}

	private int[] parseMoves(Board board, String movesString) {
		if (movesString == null) {
			return new int[0];
		}
		String[] movesStringArray = movesString.split(" ");
		int[] moves = new int[movesStringArray.length];
		for (int i = 0; i < moves.length; i++) {
			moves[i] = Move.getFromString(board, movesStringArray[i], true);
		}
		return moves;
	}

	private boolean testPosition(String fen, String avoidMovesString, String bestMovesString, int time, int nodes) {
		bestMove = 0;
		solutionFound = false;

		Board board = new Board();
		board.setFen(fen);
		System.out.println(board.toString());

		avoidMoves = parseMoves(board, avoidMovesString);
		if (avoidMovesString != null) {
			logger.debug("Lets see if " + avoidMovesString + (avoidMoves.length > 1 ? " are " : " is ") + "avoided");
		}
		bestMoves = parseMoves(board, bestMovesString);
		if (bestMovesString != null) {
			logger.debug("Lets see if " + bestMovesString + (bestMoves.length > 1 ? " are " : " is ") + "found");
		}

		engine.sendStop();
		engine.sendUciNewGame();
		engine.sendIsReady();
		engine.waitReadyOk();

		String bestMoveStr;
		if (nodes != 0) {
			bestMoveStr = engine.goNodes(board.getFen(), nodes);
		} else {
			bestMoveStr = engine.goMovetime(board.getFen(), time);
		}

		int bestMove = Move.getFromString(board, bestMoveStr, true);
		for (int move : bestMoves) {
			if (bestMove == move) {
				solutionFound = true;
				break;
			}
		}

		if (solutionFound) {
			logger.debug("Solution found " + Move.toStringExt(bestMove));
			return true;
		} else {
			logger.debug("Solution not found, instead played: " + bestMoveStr);
			return false;
		}
	}

	public void epdTest(String epdFile, String engineCommand, int time, int nodes) {
		engine = new UciEngine(engineCommand);
		engine.open(false);
		try {
			processEpdFile(new FileInputStream(new File(epdFile)), time, nodes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		engine.close();
	}

	public static void main(String[] args) {
		int time = 0;
		int nodes = 0;

		if (args.length != 4) {
			System.out.println("Test a EPD file against engines executables with UCI interface: ");
			System.out.println("  EPDTest EPD_file engine_executable -t time_in_ms");
			System.out.println("  EPDTest EPD_file engine_executable -n node_limit");
			System.exit(-1);
		}
		EpdTest epdUciTest = new EpdTest();
		if ("-t".equals(args[2])) {
			time = Integer.parseInt(args[3]);
		} else if ("-n".equals(args[2])) {
			nodes = Integer.parseInt(args[3]);
		}

		epdUciTest.epdTest(args[0], args[1], time, nodes);
	}
}