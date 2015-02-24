package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
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
	public void printPcsq() {
		CompleteEvaluator evaluator = new CompleteEvaluator(new Config());
		logger.debug("***PAWN");
		printPcsq(evaluator.pawnIndexValue);
		logger.debug("***KNIGHT");
		printPcsq(evaluator.knightIndexValue);
		logger.debug("***BISHOP");
		printPcsq(evaluator.bishopIndexValue);
		logger.debug("***ROOK");
		printPcsq(evaluator.rookIndexValue);
		logger.debug("***QUEEN");
		printPcsq(evaluator.queenIndexValue);
		logger.debug("***KING");
		printPcsq(evaluator.kingIndexValue);
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
