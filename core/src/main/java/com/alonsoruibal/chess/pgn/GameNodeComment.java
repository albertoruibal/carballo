package com.alonsoruibal.chess.pgn;

public class GameNodeComment extends GameNode {

	public final String comment;

	public GameNodeComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "GameNodeComment{" +
				"comment='" + comment + '\'' +
				"}\n";
	}
}
