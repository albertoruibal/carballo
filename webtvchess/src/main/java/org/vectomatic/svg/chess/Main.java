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
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.book.JSONBook;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

/**
 * Main class. Instantiates the UI and runs the game loop
 */
public class Main implements EntryPoint, SearchObserver, KeyDownHandler, MoveListener {
	interface MainBinder extends UiBinder<FocusPanel, Main> {}
	private static MainBinder mainBinder = GWT.create(MainBinder.class);

	@UiField(provided=true)
	ChessConstants constants = ChessConstants.INSTANCE;
	@UiField(provided=true)
	ChessCss style = Resources.INSTANCE.getCss();

	@UiField
	FocusPanel focusPanel;

	@UiField
	HTML boardContainer;
	@UiField
	Button restartButton;
//	@UiField
//	Button fenButton;
	@UiField
	Button undoButton;
	@UiField
	Button redoButton;

//	@UiField
//	DisclosurePanel advancedPanel;
	
	@UiField
	Label status;	
	@UiField
	Label modeLabel;
	@UiField
	Label reflectionLabel;
	@UiField
//	Label fenLabel;
//	@UiField
	Label currentPlayerLabel;
	@UiField
	Label historyLabel;
	
	@UiField
	ListBox modeListBox;
	@UiField
	ListBox timeListBox;
//	@UiField
//	TextArea fenArea;
	@UiField
	TextArea historyArea;
	@UiField
	Label currentPlayerValueLabel;
	@UiField
	HTML about;
	
	@UiField
	ScrollPanel historyScrollPanel;

	/**
	 * The Carballo engine
	 */
	SearchEngine engine;
	/**
	 * The Carballo board
	 */
	Board board;
	/**
	 * The SVG chess board
	 */
	ChessBoard chessboard;
	/**
	 * A &lt;div&gt; element to contain the SVG root element
	 */
	private Element boardDiv;
	/**
	 * The root SVG element
	 */
	private OMSVGSVGElement boardElt;
	
	public int getHeight() {
		return (Window.getClientHeight() - 150);
	}

	/**
	 * GWT entry point
	 */
	public void onModuleLoad() {
		final DecoratedPopupPanel initBox = new DecoratedPopupPanel();
		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.add(new Label(ChessConstants.INSTANCE.waitMessage()));
		initBox.add(hpanel);
		initBox.center();
		initBox.show();

		// Inject CSS in the document headers
		StyleInjector.inject(Resources.INSTANCE.getCss().getText());
		
		// Instantiate UI
		FocusPanel binderPanel = mainBinder.createAndBindUi(Main.this);
		modeListBox.addItem(ChessMode.whitesVsComputer.getDescription(), ChessMode.whitesVsComputer.name());
		modeListBox.addItem(ChessMode.blacksVsComputer.getDescription(), ChessMode.blacksVsComputer.name());
		modeListBox.addItem(ChessMode.whitesVsBlacks.getDescription(), ChessMode.whitesVsBlacks.name());
//		modeListBox.addItem(ChessMode.computerVsComputer.getDescription(), ChessMode.computerVsComputer.name());
		modeListBox.setSelectedIndex(0);
		
		timeListBox.addItem(ChessConstants.INSTANCE.mt1s(), "1");
		timeListBox.addItem(ChessConstants.INSTANCE.mt3s(), "3");
		timeListBox.addItem(ChessConstants.INSTANCE.mt7s(), "7");
		timeListBox.addItem(ChessConstants.INSTANCE.mt10s(), "10");
		timeListBox.addItem(ChessConstants.INSTANCE.mt15s(), "15");
		timeListBox.addItem(ChessConstants.INSTANCE.mt30s(), "30");
		timeListBox.setSelectedIndex(1);

		about.setHTML(ChessConstants.INSTANCE.about());
		RootLayoutPanel.get().add(binderPanel);
		
        // Initialize engine
		new Timer()
        {
            public void run() {
                // Create a Carballo chess engine
                BitboardAttacks.USE_MAGIC = false;
				Config config = new Config();
				config.setTranspositionTableSize(2);
				config.setBook(new JSONBook());
				engine = new SearchEngine(config);
				engine.setObserver(Main.this);
				board = engine.getBoard();
		
				//advancedPanel.getHeaderTextAccessor().setText(constants.advanced());

				// Parse the SVG chessboard and insert it in the HTML UI
				// Note that the elements must be imported in the UI since they come from another XML document
				boardDiv = boardContainer.getElement();
				boardElt = OMSVGParser.parse(Resources.INSTANCE.getBoard().getText());
				boardDiv.appendChild(boardElt.getElement());
		
				// Create the SVG chessboard. Use a temporary chessboard
				// until the engine has been initialized
				chessboard = new ChessBoard(boardElt);
				chessboard.setMoveListener(Main.this);
		
				restart();
				initBox.hide();
				
				focusPanel.addKeyDownHandler(Main.this);
				focusPanel.setFocus(true);
			}
        }.schedule(500); // To let the interface be drawn
	}

	/**
	 * Refresh the non SVG elements of the UI (list of moves, current player, FEN) 
	 */
	private void updateUI() {
		StringBuffer buffer = new StringBuffer();
		int line = 0;
		for (int i = 0; i < board.getMoveNumber(); i++) {
			String move = board.getSanMove(i);
			if (board.getMoveTurn(i)) {// If turn is white
				if (buffer.length() != 0) {
					buffer.append("\n");
				}
				buffer.append(++line + ". " + move);
			} else {
				if (buffer.length() == 0) {
					buffer.append(++line + ". ...");
				}
				buffer.append(" " + move);
			}
		}
		historyArea.setVisibleLines(line);
		historyArea.setText(buffer.toString());
		historyScrollPanel.setVerticalScrollPosition(historyScrollPanel.getMaximumVerticalScrollPosition());
		
		//fenArea.setText(board.getFen());
		
		currentPlayerValueLabel.setText(board.getTurn() ? ChessConstants.INSTANCE.white() : ChessConstants.INSTANCE.black());
		int moveNumber = board.getMoveNumber();
		
		int lastPossibleMove = board.getLastMove();
		switch(getMode()) {
			case whitesVsBlacks:
				break;
			case whitesVsComputer:
				if (lastPossibleMove % 2 == 1) {
					lastPossibleMove--;
				}
				break;
			case blacksVsComputer:
				if (lastPossibleMove % 2 == 0) {
					lastPossibleMove--;
				}
				break;
//			case computerVsComputer:
//				lastPossibleMove = Integer.MIN_VALUE;
//				break;
		}
		undoButton.setEnabled(moveNumber > 0);
		redoButton.setEnabled(moveNumber < lastPossibleMove);
	}
	
	ChessMode getMode() {
		return ChessMode.valueOf(modeListBox.getValue(modeListBox.getSelectedIndex()));
	}
	
	/**
	 * Invoked to make the game advance to the next move
	 */
	public void nextMove() {
		updateUI();
		switch (board.isEndGame()) {
			case 1 :
				status.setText(ChessConstants.INSTANCE.whitesWin());
				break;
			case -1:
				status.setText(ChessConstants.INSTANCE.blacksWin());
				break;
			case 99:
				status.setText(ChessConstants.INSTANCE.draw());
				break;
			default:
				switch(getMode()) {
					case whitesVsBlacks:
						status.setText(ChessConstants.INSTANCE.userMove());
						break;
					case whitesVsComputer:
						if (!board.getTurn()) {
							computerMove();
						} else {
							status.setText(ChessConstants.INSTANCE.userMove());
						}
						break;
					case blacksVsComputer:
						if (board.getTurn()) {
							computerMove();
						} else {
							status.setText(ChessConstants.INSTANCE.userMove());
						}
						break;
//					case computerVsComputer:
//						computerMove();
//						break;
				}
				break;
		}
	}
	
	/**
	 * Invoked to make the computer play the next move
	 */
	private void computerMove() {
		status.setText(ChessConstants.INSTANCE.thinking());
		new Timer()
        {
            public void run() {
				engine.go(SearchParameters.get(1000 * Integer.valueOf(timeListBox.getValue(timeListBox.getSelectedIndex()))));
			}
			
		}.schedule(100);
	}

	/**
	 * Invoked by the carballo engine when the search is done
	 */
	public void bestMove(int bestMove, int ponder) {
		log("bestMove(" + Move.toStringExt(bestMove) + ", " + Move.toStringExt(ponder) + ")");
		doMove(bestMove);
	}

	/**
	 * Unused carballo chess engine event handler
	 */
	public void info(SearchStatusInfo info) {
		log(info.toString());
	}

	
	/**
	 * Start a new game
	 */
	public void restart() {
		//board.startPosition();
		board.setFen("rq2r1k1/5pp1/p7/4bNP1/1p2P2P/5Q2/PP4K1/5R1R w - -");
		chessboard.update(board.getFen(), 0, 0, true, true);
		nextMove();
	}
	
//	@UiHandler("fenButton")
//	public void updateFen(ClickEvent event) {
//		log("Main.updateFen(" + fenArea.getText() + ")");
//		board.setFen(fenArea.getText());
//		chessboard.update(board.getFen(), board.getLastMove(), 0, true, true);
//		nextMove();
//	}

	@UiHandler("modeListBox")
	public void modeChange(ChangeEvent event) {
		log("Main.modeChange(" + modeListBox.getSelectedIndex() + ")");
		nextMove();
	}
	
	@UiHandler("restartButton")
	public void restart(ClickEvent event) {
        restart();
    }

	@UiHandler("undoButton")
	public void undo(ClickEvent event) {
		undo();
	}
	
	private void undo() {
		int moveNumber = board.getMoveNumber();
		switch(getMode()) {
			case whitesVsBlacks:
				setMove(moveNumber - 1);
				break;
			case whitesVsComputer:
			case blacksVsComputer:
				setMove(moveNumber - 2);
				break;
//			case computerVsComputer:
//				break;
		}
		nextMove();
	}

	@UiHandler("redoButton")
	public void redo(ClickEvent event) {
		redo();
    }

	private void redo() {
		int moveNumber = board.getMoveNumber();
		switch(getMode()) {
			case whitesVsBlacks:
				setMove(moveNumber + 1);
				break;
			case whitesVsComputer:
			case blacksVsComputer:
				setMove(moveNumber + 2);
				break;
//			case computerVsComputer:
//				break;
		}
		nextMove();
	}
	
	private void setMove(int moveNumber) {
		board.undoMove(moveNumber);
		chessboard.update(board.getFen(), board.getLastMove(), 0, true, true);
	}
	
	@Override
	public void onKeyDown(KeyDownEvent event) {
		//status.setText("keycode " + event.getNativeKeyCode());
		switch (event.getNativeKeyCode()) {
		case KeyCodes.KEY_LEFT:
			chessboard.left();
			break;
		case KeyCodes.KEY_UP:
			chessboard.up();
			break;
		case KeyCodes.KEY_RIGHT:
			chessboard.right();
			break;
		case KeyCodes.KEY_DOWN:
			chessboard.down();
			break;
		case KeyCodes.KEY_ENTER:
			chessboard.enter();
			break;
		case 113: // MODE
			int i = modeListBox.getSelectedIndex() + 1;
			if (i >= modeListBox.getItemCount()) {
				i = 0;
			}
			modeListBox.setSelectedIndex(i);
			break;
		case 117: // BLUE
			int j = timeListBox.getSelectedIndex() + 1;
			if (j >= timeListBox.getItemCount()) {
				j = 0;
			}
			timeListBox.setSelectedIndex(j);
			break;
		case 46: // STOP
			restart();
			break;
		case 188: // Backward
			undo();
			break;
		case 190: // forward
			redo();
			break;
		case 27:
//			Main.closeBrowser();
			break;
		}
	}

//    /** 
//     * Close browser window. 
//     */ 
//    public static native void closeBrowser() 
//    /*-{ 
//   		//$wnd.close();
//		//HED.event.send('blusens_webapp', {action: "menushow-250ms"});
//		//try { HED.helpers.quit(); } catch (e) { }
//    }-*/; 
	
	@Override
	public void doMove(int move) {
		log("doMove(" + board.getMoveNumber() + ")");
		board.doMove(move);
		chessboard.update(board.getFen(), board.getLastMove(), 0, true, true);
		nextMove();
	}

    public static native void log(String message)
	/*-{
		console.debug(message);
	}-*/;
}