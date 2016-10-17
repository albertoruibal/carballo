package com.alonsoruibal.chess.pgn;

public class GameNodeMove extends GameNode {

	public String number;
	public String move;
	public String annotation; // Glyphs

	public GameNodeMove(String number, String move, String annotation) {
		this.number = number;
		this.move = move;
		this.annotation = annotation;
	}

	@Override
	public String toString() {
		return "GameNodeMove{" +
				"number='" + number + '\'' +
				", move='" + move + '\'' +
				", annotation='" + annotation + '\'' +
				"}\n";
	}
}
