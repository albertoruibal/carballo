package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.Config;

public class SearchEngineThreaded extends SearchEngine {

	private Thread thread;

	public SearchEngineThreaded(Config config) {
		super(config);
	}

	/**
	 * Threaded version
	 */
	@Override
	public void go(SearchParameters searchParameters) {
		synchronized (startStopSearchLock) {
			if (!initialized || searching) {
				return;
			}
			searching = true;
			setInitialSearchParameters(searchParameters);
		}

		thread = new Thread(this, "Carballo-Search");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Stops thinking. Does not hold startStopSearchLock while sleeping, so the search
	 * thread can proceed with its own synchronization (e.g. searchLock) and the UCI
	 * thread remains responsive. Polls the volatile searching flag and joins the worker
	 * to guarantee that bestMove has been emitted before returning.
	 */
	@Override
	public void stop() {
		Thread t;
		synchronized (startStopSearchLock) {
			t = thread;
			super.stop();
		}
		// Wait outside the lock so we don't block concurrent go()/isready() handlers.
		while (searching) {
			sleep(10);
		}
		if (t != null) {
			try {
				t.join(2000);
			} catch (InterruptedException ignored) {
			}
		}
	}

	@Override
	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}
}