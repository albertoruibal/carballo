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
	public void go(SearchParameters searchParameters) {
		if (initialized && !searching) {
			searching = true;
			setSearchParameters(searchParameters);
			thread = new Thread(this);
			thread.start();
		}
	}

	/**
	 * Stops thinking
	 */
	public void stop() {
		super.stop();
		while (searching) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}