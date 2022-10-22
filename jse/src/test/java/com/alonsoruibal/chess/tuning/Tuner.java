package com.alonsoruibal.chess.tuning;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.TunedEvaluator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

abstract class Tuner {
	static final int MAXIMUM_TIMES_NOT_IMPROVED = 10;
	String POSITIONS_FILE = TuningConstants.POSITIONS_FILE;
	int INITIAL_DIRECTION = 1;
	double K = 1;
	int DELTA = 1;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm:ss.SSS");
	Board board = new Board();
	AttacksInfo attacksInfo = new AttacksInfo();

	private long positionCount;

	private TunedEvaluator[] evaluators;
	private double[] sigmoidSum;
	private double[] errors;

	private int[] directions;
	private int[] timesNotImproved;

	abstract int[] getParameters(TunedEvaluator evaluator);

	abstract void paramsToEval(TunedEvaluator evaluator);

	private void resetSigmoid() {
		Arrays.fill(sigmoidSum, 0);
		positionCount = 0;
	}

	private void addSigmoid(int i, double result, int eval) {
		sigmoidSum[i] += Math.pow(result - 1.0 / (1.0 + Math.pow(10.0, -K * eval / 400.0)), 2);
	}

	private boolean shouldSkipParameter(int i) {
		return timesNotImproved[i] >= MAXIMUM_TIMES_NOT_IMPROVED;
	}

	void calculateErrors() {
		resetSigmoid();
		try {
			BufferedReader br = new BufferedReader(new FileReader(POSITIONS_FILE));
			String line;
			int positions = 0;
			while ((line = br.readLine()) != null) {
				if (positions++ % 50000 == 0) {
					System.out.print(".");
					System.out.flush();
				}
				if (!line.contains(";")) {
					continue;
				}
				String[] split = line.split(";");
				board.setFen(split[0]);

				for (int i = 0; i < evaluators.length; i++) {
					if (i > 0 && shouldSkipParameter(i - 1)) {
						continue;
					}
					int evalScore = evaluators[i].evaluate(board, attacksInfo);
					addSigmoid(i, Double.parseDouble(split[1]), evalScore);
				}
				positionCount++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" Done");
		for (int i = 0; i < evaluators.length; i++) {
			errors[i] = sigmoidSum[i] / positionCount;
		}
	}

	void doTuning() {
		doTuning(null);
	}

	void doTuning(Consumer<int[]> afterEachIteration) {
		System.out.println("Tuning...");

		int iteration = 0;

		TunedEvaluator baseEval = new TunedEvaluator();
		int[] parameters = getParameters(baseEval);

		if (afterEachIteration != null) {
			afterEachIteration.accept(parameters);
		}

		evaluators = new TunedEvaluator[parameters.length + 1];
		evaluators[0] = baseEval;
		for (int i = 0; i < parameters.length; i++) {
			evaluators[i + 1] = new TunedEvaluator();
		}

		sigmoidSum = new double[parameters.length + 1];
		errors = new double[parameters.length + 1];

		directions = new int[parameters.length];
		Arrays.fill(directions, INITIAL_DIRECTION);
		timesNotImproved = new int[parameters.length];
		Arrays.fill(timesNotImproved, 0);

		int parametersNotImproved = 0;

		while (iteration < 100) {
			iteration++;

			System.out.println(dateFormat.format(new Date()) + " Iteration " + String.format("%03d", iteration) + " START values = " + Arrays.toString(parameters));

			for (int i = 0; i < parameters.length; i++) {
				parameters[i] += DELTA * directions[i];
				paramsToEval(evaluators[i + 1]);
				parameters[i] -= DELTA * directions[i];
			}

			calculateErrors();

			int parametersImprovedCount = 0;
			for (int i = 0; i < parameters.length; i++) {
				if (shouldSkipParameter(i) || (errors[i + 1] >= errors[0])) {
					directions[i] = -directions[i];
					parametersNotImproved++;
					timesNotImproved[i]++;
					//System.out.println("Parameter " + i + " not improving, changed direction to " + directions[i]);
				} else {
					//double diff = -(errors[i + 1] - errors[0]) * 1000000000;
					parametersNotImproved = 0;
					parametersImprovedCount++;
					parameters[i] += DELTA * directions[i];
					//System.out.println("Parameter " + i + " improving, diff = " + diff);
				}
			}
			paramsToEval(evaluators[0]);

			System.out.println("Parameters improved: " + parametersImprovedCount + " / " + parameters.length);
			System.out.println(dateFormat.format(new Date()) + " Iteration " + String.format("%03d", iteration) + " END values = " + Arrays.toString(parameters));
			System.out.println(dateFormat.format(new Date()) + " Errors       " + Arrays.toString(errors));
			if (afterEachIteration != null) {
				afterEachIteration.accept(parameters);
			}

			if (parametersNotImproved >= 2 * parameters.length) {
				System.out.println("Error minimized for parameters");
				break;
			}
		}
	}

}