package com.alonsoruibal.chess.uci;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class UciEngine implements Runnable {
	private String command;
	private Process process;
	private PrintWriter pWriter;
	private Scanner scanner;

	private Thread thread;

	private boolean uciOk = false;
	private boolean readyOk = false;
	private String bestMove = null;

	public UciEngine(String command) {
		this.command = command;
		System.out.println(command);
	}

	public void open(boolean ownBook) {
		if (process != null) {
			process.destroy();
		}

		try {
			process = Runtime.getRuntime().exec(command);
			pWriter = new PrintWriter(process.getOutputStream());
			InputStreamReader reader = new InputStreamReader(process.getInputStream());
			scanner = new Scanner(reader);

			thread = new Thread(this);
			thread.start();

			sendCommand("uci");
			waitUciOk();
			sendCommand("setoption name OwnBook value " + ownBook);
			sendIsReady();

			waitReadyOk();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		process.destroy();
	}

	public void run() {
		try {
			while (true) {
				String line = scanner.nextLine();
				System.out.println("UCI <- " + line);
				if (line.startsWith("uciok")) {
					uciOk = true;
				} else if (line.startsWith("readyok")) {
					readyOk = true;
				} else if (line.startsWith("bestmove")) {
					String[] tokens = line.split(" ");
					bestMove = tokens[1];
				}
			}
		} catch (Exception e) {

		}
	}

	private void sendCommand(String command) {
		System.out.println("UCI -> " + command);
		pWriter.println(command);
		pWriter.flush();
	}

	public void sendStop() {
		sendCommand("stop");
	}

	public void sendUciNewGame() {
		sendCommand("ucinewgame");
	}

	public void sendIsReady() {
		readyOk = false;
		sendCommand("isready");
	}

	public String goMovetime(String fen, int movetime) {
		bestMove = null;
		sendCommand("position fen " + fen);
		sendCommand("go movetime " + movetime);
		return waitBestMove();
	}

	public String goNodes(String fen, int nodes) {
		bestMove = null;
		sendCommand("position fen " + fen);
		sendCommand("go nodes " + nodes);
		return waitBestMove();
	}

	public String goDepth(String fen, String moves, int depth) {
		bestMove = null;
		sendCommand("position fen " + fen + " moves " + moves);
		sendCommand("go depth " + depth);
		return waitBestMove();
	}

	public String go(String fen, int wtime, int btime) {
		bestMove = null;
		sendCommand("position fen " + fen);
		sendCommand("go wtime " + wtime + " btime " + btime);
		return waitBestMove();
	}

	public void waitUciOk() {
		while (!uciOk) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
	}

	public void waitReadyOk() {
		while (!readyOk) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
	}

	public String waitBestMove() {
		while (bestMove == null) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		return bestMove;
	}
}