package com.alonsoruibal.chess.log;

public class Logger {
	// Read by all loggers and written by the UCI setup; volatile so the change is
	// visible across threads (the UCI thread sets noLog = true at startup).
	public static volatile boolean noLog = false;
	private final String prefix;

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
		System.out.println(in == null ? "null" : in.toString());
	}

	public void debug(Object in) {
		if (noLog) return;
		System.out.print("DEBUG ");
		System.out.print(prefix);
		System.out.print(" - ");
		System.out.println(in == null ? "null" : in.toString());
	}

	public void error(Object in) {
		// Errors are always emitted, even in UCI mode (noLog = true): suppressing them
		// makes debugging impossible, and routing them to stderr keeps them off the
		// UCI stdout protocol stream. INFO/DEBUG stay on stdout when not suppressed.
		System.err.print("ERROR ");
		System.err.print(prefix);
		System.err.print(" - ");
		System.err.println(in == null ? "null" : in.toString());
	}

}
