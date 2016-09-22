package com.alonsoruibal.chess.uci;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class UciEngine {
	String command;
	Process process;
	PrintWriter pWriter;
	Scanner scanner;

	public UciEngine(String command) {
		this.command = command;
		System.out.println(command);
	}

	public void init() {
		if (process != null) {
			process.destroy();
		}

		try {
			process = Runtime.getRuntime().exec(command);
			pWriter = new PrintWriter(process.getOutputStream());
			InputStreamReader reader = new InputStreamReader(process.getInputStream());
			scanner = new Scanner(reader);

			pWriter.println("uci");
			pWriter.println("isready");
			pWriter.flush();

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				System.out.println(line);
				if (line.startsWith("readyok")) {
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		pWriter.println("stop");
	}

	public void ucinewgame() {
		pWriter.println("ucinewgame");
	}

	public String goMovetime(String fen, int movetime) {
		pWriter.println("position fen " + fen);
		pWriter.println("go movetime " + movetime);
		pWriter.flush();
		return waitBestMove();
	}

	public String goNodes(String fen, int nodes) {
		pWriter.println("position fen " + fen);
		pWriter.println("go nodes " + nodes);
		pWriter.flush();
		return waitBestMove();
	}

	public String go(String fen, int wtime, int btime) {
		pWriter.println("position fen " + fen);
		pWriter.println("go wtime " + wtime + " btime " + btime);
		pWriter.flush();
		return waitBestMove();
	}

	private String waitBestMove() {
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			System.out.println(line);
			if (line.startsWith("bestmove")) {
				String tokens[] = line.split(" ");
				return tokens[1];
			}
		}
		return null;
	}
}
