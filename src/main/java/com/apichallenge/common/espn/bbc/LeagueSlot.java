package com.apichallenge.common.espn.bbc;

import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.service.*;

import java.util.*;

public class LeagueSlot {
	private BbcPositionEnum position;
	private Map<BbcPlayerDay, List<BbcGame>> bbcPlayerGames;

	public LeagueSlot(BbcPositionEnum position, Map<BbcPlayerDay, List<BbcGame>> bbcPlayerGames) {
		this.position = position;
		this.bbcPlayerGames = bbcPlayerGames;
	}

	public BbcPositionEnum getPosition() {
		return position;
	}

	public Map<BbcPlayerDay, List<BbcGame>> getBbcPlayerGames() {
		return bbcPlayerGames;
	}
}
