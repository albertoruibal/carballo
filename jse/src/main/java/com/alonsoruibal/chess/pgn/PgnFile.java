package com.alonsoruibal.chess.pgn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
							while (true) {
								pgnSb.append(line);
								pgnSb.append("\n");
								line = br.readLine();
								if (line == null || line.startsWith("[Event ")) {
									break;
								}
							}
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

}