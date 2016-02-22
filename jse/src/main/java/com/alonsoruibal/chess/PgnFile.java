package com.alonsoruibal.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * TODO Parse comments
 * <p/>
 * 1. Event: the name of the tournament or match event. 2. Site: the location of
 * the event. This is in "City, Region COUNTRY" format, where COUNTRY is the
 * 3-letter International Olympic Committee code for the country. An example is
 * "New York City, NY USA". 3. Date: the starting date of the game, in
 * YYYY.MM.DD form. "??" are used for unknown values. 4. Round: the playing
 * round ordinal of the game within the event. 5. White: the player of the white
 * pieces, in "last name, first name" format. 6. Black: the player of the black
 * pieces, same format as White. 7. Result: the result of the game. This can
 * only have four possible values: "1-0" (White won), "0-1" (Black won),
 * "1/2-1/2" (Draw), or "*" (other, e.g., the game is ongoing).
 *
 * @author rui
 */

public class PgnFile extends Pgn {

	public static final String UTF8_BOM = "\uFEFF";

	public String getGameNumber(InputStream is, int gameNumber) {
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