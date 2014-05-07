package com.apichallenge.common.espn.bbc;

import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.service.*;

import java.util.*;

public class LeagueSlot {
	private BbcPositionEnum position;
	private Map<BbcPlayerDay, List<BbcGame>> bbcPlayerGames;
	private Map<EspnId, Integer> gameCount = null;

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

	public int getGameCount(BbcPlayer bbcPlayer) {

		if (gameCount == null) {
			gameCount = new HashMap<EspnId, Integer>();

			for (Map.Entry<BbcPlayerDay, List<BbcGame>> entry : bbcPlayerGames.entrySet()) {
				gameCount.put(entry.getKey().getEspnId(), entry.getValue().size());
			}
		}

		if (!gameCount.containsKey(bbcPlayer.getEspnId())) {
			throw new IllegalStateException("player missing from gameCount: " + gameCount);
		}

		return gameCount.get(bbcPlayer.getEspnId());
	}
}
