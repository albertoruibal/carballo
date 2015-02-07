package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;
import com.alonsoruibal.chess.log.Logger;

import org.junit.Before;
import org.junit.Test;


public class CompleteEvaluatorTest {
	private static final Logger logger = Logger.getLogger("CompleteEvaluatorTest");

	CompleteEvaluator evaluator;

	@Before
	public void setUp() throws Exception {
		evaluator = new CompleteEvaluator(new Config());
		evaluator.debug = true;
	}

	@Test
	public void printAllPcsq() {
		ExperimentalEvaluator experimentalEvaluator = new ExperimentalEvaluator(new Config());
		logger.debug("***PAWN");
		printPcsq(experimentalEvaluator.pawnIndexValue);
		logger.debug("***KNIGHT");
		printPcsq(experimentalEvaluator.knightIndexValue);
		logger.debug("***BISHOP");
		printPcsq(experimentalEvaluator.bishopIndexValue);
		logger.debug("***ROOK");
		printPcsq(experimentalEvaluator.rookIndexValue);
		logger.debug("***QUEEN");
		printPcsq(experimentalEvaluator.queenIndexValue);
		logger.debug("***KING");
		printPcsq(experimentalEvaluator.kingIndexValue);
		logger.debug("PCSQ tables generated");
	}

	private static void printPcsq(int pcsq[]) {
		StringBuffer sb = new StringBuffer();
		for (int k = 0; k < 2; k++) {
			if (k == 0) {
				sb.append("Opening:\n");
			} else {
				sb.append("Endgame:\n");
			}
			for (int i = 0; i < 64; i++) {
				String aux = "     " + (k == 0 ? Evaluator.o(pcsq[i]) : Evaluator.e(pcsq[i]));
				aux = aux.substring(aux.length() - 5);
				sb.append(aux);
				if (i % 8 != 7) {
					sb.append(",");
				} else {
					sb.append("\n");
				}
			}
		}
		logger.debug(sb.toString());
	}
}
