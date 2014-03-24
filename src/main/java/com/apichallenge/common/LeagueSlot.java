package com.apichallenge.common;

import com.apichallenge.common.bbc.*;
import com.apichallenge.common.model.*;

import java.util.*;

public class LeagueSlot {
	private BbcPositionEnum position;
	private List<Player> players;

	public LeagueSlot(BbcPositionEnum position, List<Player> players) {
		this.position = position;
		this.players = players;
	}

	public BbcPositionEnum getPosition() {
		return position;
	}

	public List<Player> getPlayers() {
		return players;
	}
}
