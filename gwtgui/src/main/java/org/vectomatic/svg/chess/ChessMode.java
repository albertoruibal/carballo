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

/**
 * Enum to represent possible game modes
 */
public enum ChessMode {
	whitesVsBlacks {
		public String getDescription() {
			return ChessConstants.INSTANCE.whitesVsBlacks();
		}
	},
	whitesVsComputer {
		public String getDescription() {
			return ChessConstants.INSTANCE.whitesVsComputer();
		}
	},
	blacksVsComputer {
		public String getDescription() {
			return ChessConstants.INSTANCE.blacksVsComputer();
		}
	},
	computerVsComputer {
		public String getDescription() {
			return ChessConstants.INSTANCE.computerVsComputer();
		}
	};

	public abstract String getDescription();
}
