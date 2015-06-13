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
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface Resources extends ClientBundle {
	Resources INSTANCE =  GWT.create(Resources.class);
	@Source("chess.svg")
	TextResource getBoard();
	@Source("chess.css")
	ChessCss getCss();

}
