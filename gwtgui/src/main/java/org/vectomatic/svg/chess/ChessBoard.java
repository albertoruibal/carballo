/**********************************************
 * Copyright (C) 2009 Lukas Laag
 * This file is part of lib-gwt-svg-chess.
 * 
 * libgwtsvg-chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * libgwtsvg-chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with libgwtsvg-chess.  If not, see http://www.gnu.org/licenses/
 **********************************************/
package org.vectomatic.svg.chess;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;

import org.vectomatic.dom.svg.OMSVGAnimatedString;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGMatrix;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGUseElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to update the SVG chess board Representations used for the chessboard:
 * <dl>
 * <dt>index</dt>
 * <dd>int 0 ... 63</dd>
 * <dt>algebraic</dt>
 * <dd>string: a1 ... h8</dd>
 * <dt>square</dt>
 * <dd>long: (1 bit per square)</dd>
 * <dt>coords</dt>
 * <dd>0 &lt;= x &lt;= 7 ; 0 &lt;= y &lt= 7</dd>
 * </dl>
 * 
 * @author Lukas Laag (laaglu@gmail.com)
 */
public class ChessBoard implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {

	private ChessCss css;
	private OMSVGSVGElement svgElt;
	private OMSVGGElement boardElt;
	private OMSVGDocument boardDoc;
	private OMSVGUseElement slidingPiece;
	private int sqWidth;
	private int sqHeight;

	/**
	 * Coordinates of the mousedown origin point
	 */
	private OMSVGPoint mouseDownCoords;
	/**
	 * Maps algebraic coordinates to board squares
	 */
	private Map<String, OMSVGRectElement> algebraicToRects;
	/**
	 * Maps algebraic coordinates to chess pieces
	 */
	private Map<String, OMSVGUseElement> algebraicToPieces;

	Board board = new Board();

	List<Integer> possibleDestinySquares;

	static int legalMoves[] = new int[256];
	static int legalMoveCount;

	private int keySquareIndex = 36;
	private boolean showSquareKey = false;

	MoveListener moveListener;

	int lastMove = -1;
	int moveOriginIndex = -1;

	int mouseIndex = -1;

	public ChessBoard(OMSVGSVGElement svgElt) {
		possibleDestinySquares = new ArrayList<Integer>();

		this.svgElt = svgElt;
		this.boardDoc = (OMSVGDocument) svgElt.getOwnerDocument();
		this.boardElt = boardDoc.getElementById("board");
		this.css = Resources.INSTANCE.getCss();
		moveOriginIndex = -1;
		this.algebraicToRects = new HashMap<String, OMSVGRectElement>();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int index = j + 8 * i;

				String squareId = BitboardUtils.index2Algebraic(index);
				OMSVGRectElement squareElt = boardDoc.getElementById(squareId);
				algebraicToRects.put(squareId, squareElt);
			}
		}
		OMSVGRectElement sqElement = algebraicToRects.get("a1");
		this.sqWidth = (int) sqElement.getWidth().getBaseVal().getValue();
		this.sqHeight = (int) sqElement.getHeight().getBaseVal().getValue();
		this.algebraicToPieces = new HashMap<String, OMSVGUseElement>();

		update();

		// Wire events
		boardElt.addMouseMoveHandler(this);
		boardElt.addMouseUpHandler(this);
	}

	/**
	 * Adds a new piece to the chessboard. Pieces are represented by svg
	 * &lt;use&gt; elements
	 * 
	 * @param piece
	 *            The piece to add
	 * @param algebraic
	 *            The position
	 */
	private OMSVGUseElement addPiece(char piece, String algebraic) {
		// GWT.log("addPiece("+piece+", "+algebraic+")");
		if (piece != '.') {
			OMSVGRectElement squareElt = boardDoc.getElementById(algebraic);
			OMSVGUseElement useElt = boardDoc.createSVGUseElement();
			useElt.getX().getBaseVal().setValue(squareElt.getX().getBaseVal().getValue());
			useElt.getY().getBaseVal().setValue(squareElt.getY().getBaseVal().getValue());
			useElt.getWidth().getBaseVal().setValue(sqWidth);
			useElt.getHeight().getBaseVal().setValue(sqHeight);
			useElt.getHref().setBaseVal("#" + Character.toString(piece));
			useElt.getStyle().setCursor(Cursor.MOVE);
			useElt.addMouseDownHandler(this);
			useElt.addMouseUpHandler(this);
			boardElt.appendChild(useElt);
			algebraicToPieces.put(algebraic, useElt);
			return useElt;
		}
		return null;
	}

	/**
	 * Removes a piece from the chessboard at the specified position
	 * 
	 * @param algebraic
	 *            The position
	 */
	public void removePiece(String algebraic) {
		OMSVGUseElement useElt = algebraicToPieces.remove(algebraic);
		if (useElt != null) {
			boardElt.removeChild(useElt);
		}
	}

	/**
	 * Returns the piece at the specified position
	 * 
	 * @param algebraic
	 *            The position
	 * @return
	 */
	public char getPiece(String algebraic) {
		OMSVGUseElement useElt = algebraicToPieces.get(algebraic);
		if (useElt != null) {
			OMSVGAnimatedString href = useElt.getHref();
			if (href != null) {
				String baseVal = href.getBaseVal();
				if (baseVal != null) {
					return baseVal.charAt(1);
				}
			}
		}
		return '.';
	}

	public synchronized void update(String fen, int lastMove, int hintMove, boolean userToMove, boolean acceptInput) {
		this.lastMove = lastMove;

		board.setFen(fen);

		legalMoveCount = board.getLegalMoves(legalMoves);
		possibleDestinySquares.clear();

		update();
	}

	/**
	 * Update the chessboard
	 */
	private void updateSquare(int i, long square, boolean checkPieces) {
		String squareId = BitboardUtils.index2Algebraic(i);
		OMSVGRectElement squareElt = algebraicToRects.get(squareId);

		// Change the colors of the squares to highlight possible moves
		String className;
		
		if (showSquareKey && i == keySquareIndex) {
			className = css.blueSquare();
		} else if (possibleDestinySquares.contains(i) || i == moveOriginIndex) {
			className = css.yellowSquare();
		} else if (lastMove != 0 && (i == Move.getFromIndex(lastMove) || i == Move.getToIndex(lastMove))) {
			className = css.greenSquare();
		} else {
			className = (((i / 8) + i) % 2) == 0 ? css.whiteSquare() : css.blackSquare();
		}
		
		if (!className.equals(squareElt.getClassName().getBaseVal())) {
			squareElt.setClassNameBaseVal(className);
			// GWT.log("Setting: " + className, null);
		}

		if (checkPieces) {
			// Update the piece on this square, if any
			char piece = board.getPieceAt(square);
			if (getPiece(squareId) != piece) {
				//GWT.log("update() remove and add piece!");
				removePiece(squareId);
				addPiece(piece, squareId);
			}
		}
	}

	private void update() {
//		long T1 = System.currentTimeMillis();
//		GWT.log("update() Starting at " + T1);
		long square = 1;
		for (int i = 0; i < 64; i++) {
			updateSquare(i, square, true);
			square <<= 1;
		}
//		long T2 = System.currentTimeMillis();
//		GWT.log("update() ending at " + T2 + " total = " + (T2 - T1) + "ms");
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		// GWT.log("onMouseDown(" + toString(event) + "))", null);
		onMouseDown_(event);
		event.stopPropagation();
		event.preventDefault();
	}

	private void onMouseDown_(MouseEvent<?> event) {
		//GWT.log("onMouseDown_(" + event + "))", null);
		showSquareKey = false;
		String algebraic = getAlgebraic(event);
		mouseIndex = BitboardUtils.algebraic2Index(algebraic);
		if (setPossibleDestinySquares(mouseIndex)) {
			// Remove and add piece to be on top
			char piece = board.getPieceAt(BitboardUtils.index2Square(mouseIndex));
			removePiece(algebraic);
			slidingPiece = addPiece(piece, algebraic);

			mouseDownCoords = getLocalCoordinates(event);
			moveOriginIndex = mouseIndex;
			update();
		}
		event.stopPropagation();
		event.preventDefault();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		// GWT.log("onMouseUp(" + toString(event) + "))", null);

		if (slidingPiece != null) {
			if (possibleDestinySquares.contains(mouseIndex)) {
				doMove(moveOriginIndex, mouseIndex);
			} else {
				slidingPiece.getX().getBaseVal().setValue(getX(moveOriginIndex));
				slidingPiece.getY().getBaseVal().setValue(getY(moveOriginIndex));
			}
			slidingPiece = null;
		}
		event.stopPropagation();
		event.preventDefault();
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		String algebraic = getAlgebraic(event);
		// GWT.log("onMouseMove(" + algebraic + "))", null);
		mouseIndex = algebraic != null ? BitboardUtils.algebraic2Index(algebraic) : -1;
		if (slidingPiece != null) {
			// Compute the delta from the mousedown point.
			OMSVGPoint p = getLocalCoordinates(event);
			slidingPiece.getX().getBaseVal().setValue(getX(moveOriginIndex) + p.getX() - mouseDownCoords.getX());
			slidingPiece.getY().getBaseVal().setValue(getY(moveOriginIndex) + p.getY() - mouseDownCoords.getY());
			update();
		}
		event.stopPropagation();
		event.preventDefault();
	}

	public OMSVGPoint getLocalCoordinates(MouseEvent<?> e) {
		OMSVGPoint p = svgElt.createSVGPoint(e.getClientX(), e.getClientY());
		OMSVGMatrix m = boardElt.getScreenCTM().inverse();
		return p.matrixTransform(m);
	}

	public int getX(int index) {
		return sqWidth * (7 - (index % 8));
	}

	public int getY(int index) {
		return sqHeight * (7 - (index / 8));
	}

	/**
	 * Returns the algebraic corresponding to a mouse event, or null if there is
	 * no square
	 * 
	 * @param event
	 *            The mouse event
	 * @return The algebraic corresponding to a mouse event
	 */
	public String getAlgebraic(MouseEvent<?> event) {

		OMSVGPoint p = getLocalCoordinates(event);
		int x = (int) (p.getX() / sqWidth);
		int y = (int) (p.getY() / sqHeight);
		if (x >= 0 && x <= 7 && y >= 0 && y <= 7) {
			char algebraic[] = new char[2];
			algebraic[0] = (char) ('a' + x);
			algebraic[1] = (char) ('8' - y);
			return new String(algebraic);
		}
		return null;
	}

	private boolean pieceOfMovingColor(int fromIndex) {
		long square = BitboardUtils.index2Square(fromIndex);
		return (board.getTurn() ? (board.whites & square) != 0 : (board.blacks & square) != 0);
	}

	// public String toString(MouseEvent e) {
	// String width = svgElt.getStyle().getWidth();
	// float r = (Integer.parseInt(width.substring(0, width.length() - 2 /* 2 ==
	// "px".length() */))) / svgElt.getBBox().getWidth();
	// StringBuffer buffer = new StringBuffer();
	// buffer.append(" e=");
	// buffer.append(e.getRelativeElement());
	// buffer.append(" t=");
	// buffer.append(e.getNativeEvent().getEventTarget());
	// buffer.append(" cet=");
	// buffer.append(e.getNativeEvent().getCurrentEventTarget());
	// buffer.append(" cx=");
	// buffer.append(e.getClientX());
	// buffer.append(" cy=");
	// buffer.append(e.getClientY());
	// buffer.append(" scx=");
	// buffer.append((int)((e.getClientX())/ r));
	// buffer.append(" scy=");
	// buffer.append((int)((e.getClientY()) / r));
	// buffer.append(" rx=");
	// buffer.append(e.getRelativeX(svgElt.getElement()));
	// buffer.append(" ry=");
	// buffer.append(e.getRelativeY(svgElt.getElement()));
	// buffer.append(" x=");
	// buffer.append(e.getX());
	// buffer.append(" y=");
	// buffer.append(e.getY());
	// buffer.append(" sx=");
	// buffer.append(e.getScreenX());
	// buffer.append(" sy=");
	// buffer.append(e.getScreenY());
	// return buffer.toString();
	// }
	// private void log(String s) {
	// Text t = (Text) DOM.getElementById("title").getFirstChild();
	// t.setData(s);
	// }

	public void up() {
		showSquareKey = true;
		if (keySquareIndex + 8 <= 63) {
			int oldKeySquareIndex = keySquareIndex;
			keySquareIndex += 8;
			updateSquare(oldKeySquareIndex, BitboardUtils.index2Square(oldKeySquareIndex), false);
			updateSquare(keySquareIndex, BitboardUtils.index2Square(keySquareIndex), false);
		}
	}

	public void down() {
		showSquareKey = true;
		if (keySquareIndex - 8 >= 0) {
			int oldKeySquareIndex = keySquareIndex;
			keySquareIndex -= 8;
			updateSquare(oldKeySquareIndex, BitboardUtils.index2Square(oldKeySquareIndex), false);
			updateSquare(keySquareIndex, BitboardUtils.index2Square(keySquareIndex), false);
		}
	}

	public void left() {
		showSquareKey = true;
		if (keySquareIndex % 8 < 7) {
			int oldKeySquareIndex = keySquareIndex;
			keySquareIndex++;
			updateSquare(oldKeySquareIndex, BitboardUtils.index2Square(oldKeySquareIndex), false);
			updateSquare(keySquareIndex, BitboardUtils.index2Square(keySquareIndex), false);
		}
	}

	public void right() {
		showSquareKey = true;
		if (keySquareIndex % 8 > 0) {
			int oldKeySquareIndex = keySquareIndex;
			keySquareIndex--;
			updateSquare(oldKeySquareIndex, BitboardUtils.index2Square(oldKeySquareIndex), false);
			updateSquare(keySquareIndex, BitboardUtils.index2Square(keySquareIndex), false);
		}
	}

	public void enter() {
		showSquareKey = true;
		if (moveOriginIndex == -1) {
			if (setPossibleDestinySquares(keySquareIndex)) {
				moveOriginIndex = keySquareIndex;
			} else {
				moveOriginIndex = -1;
			}
			update();
		} else if (possibleDestinySquares.contains(keySquareIndex)) {
			doMove(moveOriginIndex, keySquareIndex);
		} else {
			moveOriginIndex = -1;
			setPossibleDestinySquares(-1);
			update();
		}
	}

	private void doMove(int fromIndex, int toIndex) {
		GWT.log("ChessBoard.doMove(" + fromIndex + ", " + toIndex + ")");
		final int move = Move.getFromString(board, BitboardUtils.index2Algebraic(fromIndex) + BitboardUtils.index2Algebraic(toIndex), true);

		showSquareKey = false;
		possibleDestinySquares.clear();
		moveOriginIndex = -1;

		if (moveListener != null) {
			moveListener.doMove(move);
		}
	}

	private boolean setPossibleDestinySquares(int fromIndex) {
		possibleDestinySquares.clear();
		for (int j = 0; j < legalMoveCount; j++) {
			if (fromIndex == Move.getFromIndex(legalMoves[j])) {
				// GWT.log("Adding to possible moves " + legalMoves[j]);
				possibleDestinySquares.add(Move.getToIndex(legalMoves[j]));
			}
		}
		// return possibleDestinySquares.size() > 0; before, it let select the
		// piece if it had legal moves
		// Lets pick the piece if there is a piece of our color
		return pieceOfMovingColor(fromIndex);
	}

	public void setMoveListener(MoveListener moveListener) {
		this.moveListener = moveListener;
	}
}