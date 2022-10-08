package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.log.Logger;

public class SearchStats {
	private static final Logger logger = Logger.getLogger(SearchStats.class.getName());
	public static final boolean DEBUG = false;

	// Aspiration window
	public static long pvCutNodes;
	public static long pvAllNodes;
	public static long nullCutNodes;
	public static long nullAllNodes;
	public static long aspirationWindowProbe = 0;
	public static long aspirationWindowHit = 0;
	// Futility pruning
	public static long futilityHit = 0;
	// Razoring
	public static long razoringProbe = 0;
	public static long razoringHit = 0;
	// Singular Extension
	public static long singularExtensionProbe = 0;
	public static long singularExtensionHit = 0;
	// Null Move
	public static long nullMoveProbe = 0;
	public static long nullMoveHit = 0;
	// Transposition Table
	public static long ttProbe = 0;
	public static long ttPvHit = 0;
	public static long ttLBHit = 0;
	public static long ttUBHit = 0;
	public static long ttEvalHit = 0;
	public static long ttEvalProbe = 0;

	public static void print(long nodeCount) {
		logger.debug("Positions         = " + nodeCount);
		logger.debug("PV Cut            = " + SearchStats.pvCutNodes + " " + (100 * SearchStats.pvCutNodes / (SearchStats.pvCutNodes + SearchStats.pvAllNodes + 1)) + "%");
		logger.debug("PV All            = " + SearchStats.pvAllNodes);
		logger.debug("Null Cut          = " + SearchStats.nullCutNodes + " " + (100 * SearchStats.nullCutNodes / (SearchStats.nullCutNodes + SearchStats.nullAllNodes + 1)) + "%");
		logger.debug("Null All          = " + SearchStats.nullAllNodes);
		if (SearchStats.aspirationWindowProbe > 0) {
			logger.debug("Asp Win      Hits = " + (100 * SearchStats.aspirationWindowHit / SearchStats.aspirationWindowProbe) + "%");
		}
		if (SearchStats.ttEvalProbe > 0) {
			logger.debug("TT Eval      Hits = " + SearchStats.ttEvalHit + " " + (100 * SearchStats.ttEvalHit / SearchStats.ttEvalProbe) + "%");
		}
		if (SearchStats.ttProbe > 0) {
			logger.debug("TT PV        Hits = " + SearchStats.ttPvHit + " " + (1000000 * SearchStats.ttPvHit / SearchStats.ttProbe) + " per 10^6");
			logger.debug("TT LB        Hits = " + SearchStats.ttProbe + " " + (100 * SearchStats.ttLBHit / SearchStats.ttProbe) + "%");
			logger.debug("TT UB        Hits = " + SearchStats.ttUBHit + " " + (100 * SearchStats.ttUBHit / SearchStats.ttProbe) + "%");
		}
		logger.debug("Futility     Hits = " + SearchStats.futilityHit);
		if (SearchStats.nullMoveProbe > 0) {
			logger.debug("Null Move    Hits = " + SearchStats.nullMoveHit + " " + (100 * SearchStats.nullMoveHit / SearchStats.nullMoveProbe) + "%");
		}
		if (SearchStats.razoringProbe > 0) {
			logger.debug("Razoring     Hits = " + SearchStats.razoringHit + " " + (100 * SearchStats.razoringHit / SearchStats.razoringProbe) + "%");
		}
		if (SearchStats.singularExtensionProbe > 0) {
			logger.debug("S.Extensions Hits = " + SearchStats.singularExtensionHit + " " + (100 * SearchStats.singularExtensionHit / SearchStats.singularExtensionProbe) + "%");
		}
	}
}
