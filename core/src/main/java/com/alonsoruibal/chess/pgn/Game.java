package com.alonsoruibal.chess.pgn;

public class Game {
	long id;
	String event;
	String eventDate;
	String site;
	String date;
	String round;
	String white;
	String black;
	Integer whiteElo;
	Integer whiteFideId;
	Integer blackElo;
	Integer blackFideId;
	String fenStartPosition;
	String result;
	String eco;
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

	public void setEvent(String event) {
		this.event = event == null ? null : event.replace("?", "");
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site == null ? null : site.replace("?", "");
	}

	public String getRound() {
		return round;
	}

	public void setRound(String round) {
		this.round = round == null ? null : round.replace("?", "");
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
		} else if ("=".equals(result) || "1/2-1/2".equals(result)) {
			this.result = "½-½";
		} else {
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
		this.date = date == null ? null : date.replace(".??.??", "").replace("????", "");
	}

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = eventDate == null ? null : eventDate.replace(".??.??", "").replace("????", "");
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
