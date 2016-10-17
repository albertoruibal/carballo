package com.alonsoruibal.chess.pgn;

public class NumericAnnotationGlyphs {

	public static String translate(String in) {
		switch (in) {
			case "$0":
				return "";
			case "$1":
				return "!";
			case "$2":
				return "?";
			case "$3":
				return "‼";
			case "$4":
				return "⁇";
			case "$5":
				return "⁉";
			case "$6":
				return "⁈";
			case "$7":
				return "□";
			case "$8":
				return "";

			case "$10":
				return "=";
			case "$13":
				return "∞";
			case "$14":
				return "⩲⩲+=";
			case "$15":
				return "=+";
			case "$16":
				return "±";
			case "$17":
				return "∓";
			case "$18":
				return "+-";
			case "$19":
				return "-+";

			case "$22":
				return "⨀";
			case "$23":
				return "⨀";

			case "$32":
				return "⟳";
			case "$33":
				return "⟳";

			case "$36":
				return "→";
			case "$37":
				return "→";
			case "$40":
				return "↑";
			case "$41":
				return "↑";

			case "$132":
				return "⇆";
			case "$133":
				return "⇆";
			case "$140":
				return "∆";
			case "$142":
				return "⌓";
			case "$145":
				return "RR";
			case "$146":
				return "N";

			case "$239":
				return "⇔";
			case "$240":
				return "⇗";
			case "$242":
				return "⟫";
			case "$243":
				return "⟪";
			case "$244":
				return "✕";
			case "$245":
				return "⊥";
		}
		return in;
	}

}
