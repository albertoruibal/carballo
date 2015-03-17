package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.Config;

public class SearchEngineThreaded extends SearchEngine {

	Thread thread;

	public SearchEngineThreaded(Config config) {
		super(config);
	}

	/**
	 * Threaded version
	 */
	public void go(SearchParameters searchParameteres) {
		if (!isInitialized()) return;
		if (!isSearching()) {
			this.searchParameters = searchParameteres;
			try {
				newRun();
				thread = new Thread(this);
				thread.start();
			} catch (SearchFinishedException ignored) {
				finishRun();
			}
		}
	}

	/**
	 * Stops thinking
	 */
	public void stop() {
		super.stop();
		while (isSearching()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}