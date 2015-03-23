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
	public static final ChessConstants INSTANCE =  GWT.create(ChessConstants.class);
	public String whitesVsBlacks();
	public String whitesVsComputer();
	public String blacksVsComputer();

	public String computerVsComputer();

	public String white();
	public String black();
	
	public String infoTab();
	public String settingsTab();
	public String aboutTab();

	public String actions();
	public String status();
	public String mode();
	public String reflectionTime();
	public String player();
	public String history();
	public String fen();
	public String advanced();
	
	public String undo();
	public String redo();
	public String restart();
	public String setFen();

	public String thinking();
	public String userMove();
	public String whitesWin();
	public String blacksWin();
	public String draw();
	public String confirmRestart();
	public String confirmYes();
	public String confirmNo();
	public String waitMessage();

	public String mt1s();
	public String mt3s();
	public String mt5s();
	public String mt7s();
	public String mt10s();
	public String mt15s();
	public String mt30s();

	public String standard();

	public String chess960();

	public String about();
}
