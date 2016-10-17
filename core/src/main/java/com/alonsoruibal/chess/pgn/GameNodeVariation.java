package com.alonsoruibal.chess.pgn;

import java.util.ArrayList;

public class GameNodeVariation extends GameNode {

	public ArrayList<GameNode> variation = new ArrayList<>();

	@Override
	public String toString() {
		return "GameNodeVariation{" +
				"variation=" + variation +
				"}\n";
	}
}
