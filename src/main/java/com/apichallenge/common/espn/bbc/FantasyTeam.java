package com.apichallenge.common.espn.bbc;

public class FantasyTeam {
	private String entryName;
	private Strategy strategy;

	public FantasyTeam(String entryName, Strategy strategy) {
		this.entryName = entryName;
		this.strategy = strategy;
	}

	public String getEntryName() {
		return entryName;
	}

	public Strategy getStrategy() {
		return strategy;
	}
}
