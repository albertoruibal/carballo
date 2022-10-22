package com.alonsoruibal.chess.tuning;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.evaluation.TunedEvaluator;
import com.alonsoruibal.chess.search.SearchEngine;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;

public class SigmoidGraph {
	static final int SIZE = 10000;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm:ss.SSS");
	private SearchEngine engine = new SearchEngine(new Config());

	private long[] positionCount = new long[SIZE];
	private double[] positionEvals = new double[SIZE];

	@Test
	@Tag("slow")
	public void sigmoid() {
		engine.getTT().clear();

		try {
			BufferedReader br = new BufferedReader(new FileReader(TuningConstants.POSITIONS_FILE));
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.contains(";")) {
					continue;
				}
				String[] split = line.split(";");
				engine.getBoard().setFen(split[0]);
				engine.setInitialPly();

				int score = (engine.getBoard().getTurn() ? 1 : -1) * engine.quiescentSearch(0, -TunedEvaluator.KNOWN_WIN, TunedEvaluator.KNOWN_WIN);

				score += SIZE / 2;
				if (score < 0 || score >= SIZE) {
					continue;
				}
				positionCount[score] = positionCount[score] + 1;
				positionEvals[score] = positionEvals[score] + Double.parseDouble(split[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuilder x = new StringBuilder();
		StringBuilder y = new StringBuilder();
		for (int i = 0; i < positionCount.length; i++) {
			if (positionCount[i] > 0) {
				x.append(i - SIZE / 2).append(" ");
				y.append(String.format("%.4f ", positionEvals[i] / positionCount[i]));
			}
		}
		System.out.println(x.toString());
		System.out.println(y.toString());
	}
}