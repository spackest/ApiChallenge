package com.apichallenge.common;

import com.apichallenge.common.bbc.*;
import com.apichallenge.common.model.*;

import java.util.*;

public class Starters {
	private Map<BbcPositionEnum, Player> starters;

	public Starters() {
		this.starters = new HashMap<BbcPositionEnum, Player>();
	}

	public void addStarter(BbcPositionEnum bbcPositionEnum, Player player) {
		starters.put(bbcPositionEnum, player);
	}

	public Set<Map.Entry<BbcPositionEnum, Player>> getEntrySet() {
		return starters.entrySet();
	}

	public Player getStarter(BbcPositionEnum bbcPositionEnum) {
		return starters.get(bbcPositionEnum);
	}

	public boolean equals(Object object) {
		Starters startersObject = (Starters) object;
		if (startersObject == null) {
			return false;
		}

		Set<Map.Entry<BbcPositionEnum, Player>> entrySetB = startersObject.getEntrySet();

		if (starters.entrySet().size() != entrySetB.size()) {
			return false;
		}

		for (Map.Entry<BbcPositionEnum, Player> entryA : starters.entrySet()) {
			Player playerA = entryA.getValue();
			Player playerB = startersObject.getStarter(entryA.getKey());
			if (!playerA.equals(playerB)) {
				return false;
			}
		}

		return true;
	}
}
