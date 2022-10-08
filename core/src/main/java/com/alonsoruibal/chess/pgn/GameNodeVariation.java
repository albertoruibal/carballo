package com.alonsoruibal.chess.pgn;

import java.util.ArrayList;

public class GameNodeVariation extends GameNode {

	public final ArrayList<GameNode> variation = new ArrayList<>();

	public void add(GameNode gameNode) {
		variation.add(gameNode);
	}

	public GameNode get(int index) {
		return variation.get(index);
	}

	public int size() {
		return variation.size();
	}

	@Override
	public String toString() {
		return "GameNodeVariation{" +
				"variation=" + variation +
				"}\n";
	}
}
