package com.alonsoruibal.chess.pgn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Predicate;

public class PgnFile {

	public static final String UTF8_BOM = "\uFEFF";

	public static String getGameNumber(InputStream is, int gameNumber) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		int counter = 0;
		try {
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}

				if (line.startsWith(UTF8_BOM)) {
					line = line.substring(1);
				}

				if (line.startsWith("[Event ")) {
					if (counter == gameNumber) {
						StringBuilder pgnSb = new StringBuilder();
						try {
							do {
								pgnSb.append(line);
								pgnSb.append("\n");
								line = br.readLine();
							} while (line != null && !line.startsWith("[Event "));
						} catch (IOException ignored) {
						}
						return pgnSb.toString();
					}
					counter++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String readGames(InputStream is, Predicate<String> f) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			StringBuilder pgnSb = new StringBuilder();
			while (true) {
				line = br.readLine();
				if (line != null && line.startsWith(UTF8_BOM)) {
					line = line.substring(1);
				}
				if (line == null || line.startsWith("[Event ")) {
					if (pgnSb.length() > 0) {
						f.test(pgnSb.toString());
						pgnSb.delete(0, pgnSb.length());
					}

					if (line == null) {
						break;
					}
				}

				pgnSb.append(line);
				pgnSb.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}