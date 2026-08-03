package com.alonsoruibal.chess.pgn;

public class Game {
	private long id;
	private String event;
	private String eventDate;
	private String site;
	private String date;
	private String round;
	private String white;
	private String black;
	private Integer whiteElo;
	private Integer whiteFideId;
	private Integer blackElo;
	private Integer blackFideId;
	private String fenStartPosition;
	private String result;
	private String eco;
	GameNodeVariation pv;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEvent() {
		return event;
	}

	/**
	 * Strips the PGN "unknown" marker only when the whole value is that marker, so
	 * legitimate data containing "?" (e.g. "Who? Open" or a date "2020.??.??") is not
	 * mangled. The previous substring replace removed every "?" from the value.
	 */
	private static String cleanUnknown(String value) {
		if (value == null) {
			return null;
		}
		if ("?".equals(value)) {
			return null;
		}
		return value;
	}

	public void setEvent(String event) {
		this.event = cleanUnknown(event);
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = cleanUnknown(site);
	}

	public String getRound() {
		return round;
	}

	public void setRound(String round) {
		this.round = cleanUnknown(round);
	}

	public String getWhite() {
		return white;
	}

	public void setWhite(String white) {
		this.white = white;
	}

	public String getBlack() {
		return black;
	}

	public void setBlack(String black) {
		this.black = black;
	}

	public Integer getWhiteElo() {
		return whiteElo;
	}

	public void setWhiteElo(Integer whiteElo) {
		this.whiteElo = whiteElo;
	}

	public Integer getBlackElo() {
		return blackElo;
	}

	public void setBlackElo(Integer blackElo) {
		this.blackElo = blackElo;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		if ("1".equals(result)) {
			this.result = "1-0";
		} else if ("0".equals(result)) {
			this.result = "0-1";
		} else if ("1/2-1/2".equals(result) || "½-½".equals(result)) {
			this.result = "½-½";
		} else {
			// "*" and any other literal are kept as-is. Do not treat "=" as a result: it
			// is a NAG shorthand for "equal chances", not a game result.
			this.result = result;
		}
	}

	public String getEco() {
		return eco;
	}

	public void setEco(String eco) {
		this.eco = eco;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		// Only clear the value when it is the PGN "unknown" date "????.??.??", not when a
		// partial date like "2020.??.??" legitimately contains "?" (the previous substring
		// replace turned "2020.??.??" into "2020." with a dangling dot).
		this.date = "????.??.??".equals(date) ? null : date;
	}

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = "????.??.??".equals(eventDate) ? null : eventDate;
	}

	public Integer getWhiteFideId() {
		return whiteFideId;
	}

	public void setWhiteFideId(Integer whiteFideId) {
		this.whiteFideId = whiteFideId;
	}

	public Integer getBlackFideId() {
		return blackFideId;
	}

	public void setBlackFideId(Integer blackFideId) {
		this.blackFideId = blackFideId;
	}

	public String getFenStartPosition() {
		return fenStartPosition;
	}

	public void setFenStartPosition(String fenStartPosition) {
		this.fenStartPosition = fenStartPosition;
	}

	public GameNodeVariation getPv() {
		return pv;
	}

	public void setPv(GameNodeVariation pv) {
		this.pv = pv;
	}
}
