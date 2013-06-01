package com.alonsoruibal.chess.log;

public class Logger {
	public static boolean noLog = false;
	String prefix;

	private Logger(String prefix) {
		this.prefix = prefix;
	}

	public static Logger getLogger(String prefix) {
		return new Logger(prefix);
	}

	public void info(Object in) {
		if (noLog) return;
		System.out.print("INFO ");
		System.out.print(prefix);
		System.out.print(" - ");
		System.out.println(in.toString());
	}

	public void debug(Object in) {
		if (noLog) return;
		System.out.print("DEBUG ");
		System.out.print(prefix);
		System.out.print(" - ");
		System.out.println(in.toString());
	}

	public void error(Object in) {
		if (noLog) return;
		System.out.print("ERROR ");
		System.out.print(prefix);
		System.out.print(" - ");
		System.out.println(in.toString());
	}

}
