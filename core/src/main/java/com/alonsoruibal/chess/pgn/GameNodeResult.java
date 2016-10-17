package com.alonsoruibal.chess.pgn;

public class GameNodeResult extends GameNode {

	public String result;

	public GameNodeResult(String result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "GameNodeResult{" +
				"result='" + result + '\'' +
				"}\n";
	}
}
