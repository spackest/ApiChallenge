package com.apichallenge.common.espn.bbc;

import java.util.*;

public class BbcLeague {
	private List<LeagueSlot> league = new ArrayList<LeagueSlot>();

	public void addLeagueSlot(LeagueSlot leagueSlot) {
		league.add(leagueSlot);
	}

	public List<LeagueSlot> getLeague() {
		return league;
	}
}
