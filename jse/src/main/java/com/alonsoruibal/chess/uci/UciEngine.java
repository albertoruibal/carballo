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

	// These flags are written by the reader thread and read by the waiting
	// thread; they must be volatile to guarantee visibility.
	private volatile boolean uciOk = false;
	private volatile boolean readyOk = false;
	private volatile boolean died = false;
	private volatile String bestMove = null;

	public UciEngine(String command) {
		this.command = command;
		System.out.println(command);
	}

	public void open(boolean ownBook) {
		close(); // cleanup any previous process and streams before opening a new one

		try {
			process = Runtime.getRuntime().exec(command);
			pWriter = new PrintWriter(process.getOutputStream());
			InputStreamReader reader = new InputStreamReader(process.getInputStream());
			scanner = new Scanner(reader);

			thread = new Thread(this, "UciEngine-reader");
			thread.setDaemon(true);
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
		if (scanner != null) {
			scanner.close();
			scanner = null;
		}
		if (pWriter != null) {
			pWriter.close();
			pWriter = null;
		}
		if (process != null) {
			process.destroy();
			// Close the child streams explicitly to release file descriptors.
			try { process.getInputStream().close(); } catch (Exception ignored) {}
			try { process.getOutputStream().close(); } catch (Exception ignored) {}
			try { process.getErrorStream().close(); } catch (Exception ignored) {}
			process = null;
		}
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
		died = true;
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
					// Guard against a malformed "bestmove" line with no move token.
					bestMove = tokens.length > 1 ? tokens[1] : "";
				}
			}
		} catch (Exception e) {
			// Scanner closed or process died: unblock any waiter so it does not
			// hang forever waiting for a bestmove that will never come.
			died = true;
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
		// Bound the wait so a dead subprocess does not hang the caller forever.
		long deadline = System.currentTimeMillis() + 10_000;
		while (!uciOk && !died && System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
	}

	public void waitReadyOk() {
		long deadline = System.currentTimeMillis() + 10_000;
		while (!readyOk && !died && System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
	}

	public String waitBestMove() {
		// A generous default so tournament callers do not block indefinitely if
		// the engine dies without emitting bestmove; callers that need a hard
		// bound can wrap this.
		long deadline = System.currentTimeMillis() + 60_000;
		while (bestMove == null && !died && System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		return bestMove;
	}
}