package com.apichallenge.common.espn.bbc;

import com.apichallenge.common.espn.*;

import java.util.*;

public class BbcLeague {
	private EspnEntry espnEntry;
	private List<LeagueSlot> league = new ArrayList<LeagueSlot>();

	public BbcLeague(EspnEntry espnEntry) {
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
