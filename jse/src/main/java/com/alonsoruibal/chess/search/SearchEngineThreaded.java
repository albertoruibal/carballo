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
	@Override
	public void go(SearchParameters searchParameters) {
		synchronized (startStopSearchLock) {
			if (!initialized || searching) {
				return;
			}
			searching = true;
			setInitialSearchParameters(searchParameters);
		}

		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Stops thinking
	 */
	@Override
	public void stop() {
		synchronized (startStopSearchLock) {
			while (searching) {
				super.stop();
				sleep(10);
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