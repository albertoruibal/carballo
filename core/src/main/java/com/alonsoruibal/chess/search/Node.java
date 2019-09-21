package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.Evaluator;

/**
 * Stores the elements to be kept in each node of the search tree
 * <p>
 * Other nodes may access this elements
 */
public class Node {
	public final int distanceToInitialPly;

	// Current move
	public int move;
	// Transposition table move
	public int ttMove;

	// Two killer move slots
	public int killerMove1;
	public int killerMove2;

	// The static node eval
	public int staticEval;

	// The Move iterator
	public AttacksInfo attacksInfo;
	public MoveIterator moveIterator;

	public Node(SearchEngine searchEngine, int distanceToInitialPly) {
		this.distanceToInitialPly = distanceToInitialPly;

		attacksInfo = new AttacksInfo();
		moveIterator = new MoveIterator(searchEngine, attacksInfo, distanceToInitialPly);

		clear();
	}

	public void clear() {
		ttMove = Move.NONE;

		killerMove1 = Move.NONE;
		killerMove2 = Move.NONE;

		staticEval = Evaluator.NO_VALUE;
	}

	public void destroy() {
		moveIterator.destroy();
		moveIterator = null;
		attacksInfo = null;
	}

}