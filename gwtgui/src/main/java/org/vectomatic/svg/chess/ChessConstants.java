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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface ChessConstants extends Constants {
	ChessConstants INSTANCE =  GWT.create(ChessConstants.class);
	String whitesVsBlacks();
	String whitesVsComputer();
	String blacksVsComputer();

	String computerVsComputer();

	String white();
	String black();
	
	String infoTab();
	String settingsTab();
	String aboutTab();

	String actions();
	String status();
	String mode();
	String reflectionTime();
	String player();
	String history();
	String fen();
	String advanced();
	
	String undo();
	String redo();
	String restart();
	String setFen();

	String thinking();
	String userMove();
	String whitesWin();
	String blacksWin();
	String draw();
	String confirmRestart();
	String confirmYes();
	String confirmNo();
	String waitMessage();

	String mt1s();
	String mt3s();
	String mt5s();
	String mt7s();
	String mt10s();
	String mt15s();
	String mt30s();

	String standard();

	String chess960();

	String about();
}
