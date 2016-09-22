package com.alonsoruibal.chess.uci;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class UciEngine implements Runnable {
	String command;
	Process process;
	PrintWriter pWriter;
	Scanner scanner;

	Thread thread;

	boolean ready = false;
	String bestMove = null;

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
			sendCommand("setoption name OwnBook value " + ownBook);
			sendCommand("isready");

			while (!ready) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
				}
			}
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
				System.out.println(line);
				if (line.startsWith("readyok")) {
					ready = true;
				} else if (line.startsWith("bestmove")) {
					String tokens[] = line.split(" ");
					bestMove = tokens[1];
				}
			}
		} catch (Exception e) {

		}
	}

	private void sendCommand(String command) {
		System.out.println("> " + command);
		pWriter.println(command);
		pWriter.flush();
	}

	public void stop() {
		sendCommand("stop");
	}

	public void ucinewgame() {
		sendCommand("ucinewgame");
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

	public String go(String fen, int wtime, int btime) {
		bestMove = null;
		sendCommand("position fen " + fen);
		sendCommand("go wtime " + wtime + " btime " + btime);
		return waitBestMove();
	}

	private String waitBestMove() {
		while (bestMove == null) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		return bestMove;
	}
}