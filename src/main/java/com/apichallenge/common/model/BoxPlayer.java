package com.apichallenge.common.model;

public class BoxPlayer {
	private int espnId;
	private String name;
	private int runs;
	private int hits;
	private int rbis;
	private int walks;
	private int doubles;
	private int triples;
	private int homeRuns;
	private int stolenBases;
	private int points;

	public BoxPlayer(int espnId, String name, int runs, int hits, int rbis, int walks, int doubles, int triples, int homeRuns, int stolenBases) {
		this.espnId = espnId;
		this.name = name;
		this.runs = runs;
		this.hits = hits;
		this.rbis = rbis;
		this.walks = walks;
		this.doubles = doubles;
		this.triples = triples;
		this.homeRuns = homeRuns;
		this.stolenBases = stolenBases;
		this.points = runs + hits + rbis + walks + doubles + triples * 2 + homeRuns * 3 + stolenBases;
	}

	public void setEspnId(int espnId) {
		this.espnId = espnId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRuns(int runs) {
		this.runs = runs;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public void setRbis(int rbis) {
		this.rbis = rbis;
	}

	public void setWalks(int walks) {
		this.walks = walks;
	}

	public void setDoubles(int doubles) {
		this.doubles = doubles;
	}

	public void setTriples(int triples) {
		this.triples = triples;
	}

	public void setHomeRuns(int homeRuns) {
		this.homeRuns = homeRuns;
	}

	public void setStolenBases(int stolenBases) {
		this.stolenBases = stolenBases;
	}

	public void setPoints(int points) {
		this.points = points;
	}
}
