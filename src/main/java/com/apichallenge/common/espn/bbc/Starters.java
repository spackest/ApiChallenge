package com.apichallenge.common.espn.bbc;

import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;

import java.util.*;

public class Starters {
	private Map<BbcPositionEnum, BbcPlayer> starters;

	public Starters() {
		this.starters = new HashMap<BbcPositionEnum, BbcPlayer>();
	}

	public void addStarter(BbcPositionEnum bbcPositionEnum, BbcPlayer bbcPlayer) {
		starters.put(bbcPositionEnum, bbcPlayer);
	}

	public Set<Map.Entry<BbcPositionEnum, BbcPlayer>> getEntrySet() {
		return starters.entrySet();
	}

	public BbcPlayer getStarter(BbcPositionEnum bbcPositionEnum) {
		return starters.get(bbcPositionEnum);
	}

	public boolean equals(Object object) {
		Starters startersObject = (Starters) object;
		if (startersObject == null) {
			return false;
		}

		Set<Map.Entry<BbcPositionEnum, BbcPlayer>> entrySetB = startersObject.getEntrySet();

		if (starters.entrySet().size() != entrySetB.size()) {
			return false;
		}

		for (Map.Entry<BbcPositionEnum, BbcPlayer> entryA : starters.entrySet()) {
			BbcPlayer bbcPlayerA = entryA.getValue();
			BbcPlayer bbcPlayerB = startersObject.getStarter(entryA.getKey());
			if (!bbcPlayerA.equals(bbcPlayerB)) {
				return false;
			}
		}

		return true;
	}
}
