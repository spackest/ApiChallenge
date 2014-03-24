package com.apichallenge.common;

import java.util.*;

public class League {
	private EspnEntry espnEntry;
	private List<LeagueSlot> league = new ArrayList<LeagueSlot>();

	public League(EspnEntry espnEntry) {
		this.espnEntry = espnEntry;
	}

	public void addLeagueSlot(LeagueSlot leagueSlot) {
		league.add(leagueSlot);
	}

	public EspnEntry getEspnEntry() {
		return espnEntry;
	}

	public List<LeagueSlot> getLeague() {
		return league;
	}
}
