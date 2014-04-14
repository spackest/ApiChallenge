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

	public Collection<BbcPlayer> getStarters() {
		return starters.values();
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

		int matches = 0;

		for (Map.Entry<BbcPositionEnum, BbcPlayer> entryA : starters.entrySet()) {
			BbcPositionEnum bbcPositionEnum = entryA.getKey();
			BbcPlayer bbcPlayerA = entryA.getValue();
			BbcPlayer bbcPlayerB = startersObject.getStarter(entryA.getKey());

			if (bbcPositionEnum.getSlotId() == BbcPositionEnum.PITCHING_STAFF.getSlotId()) {
				if (bbcPlayerA.getTeamId() == bbcPlayerB.getTeamId()) {
					matches++;
				}
			} else {
				if (bbcPlayerA.getEspnId().getId().equals(bbcPlayerB.getEspnId().getId())) {
					matches++;
				} else {
					return false;
				}
			}
		}

		return matches == starters.entrySet().size();
	}

	@Override
	public String toString() {
		String toString = "";

		for (int slotId = 1; slotId <= 10; slotId++) {
			BbcPositionEnum bbcPositionEnum = BbcPositionEnum.getBbcPositionBySlotId(new SlotId(slotId));
			toString += slotId + ":" + bbcPositionEnum.getName() + " -> " + starters.get(bbcPositionEnum).getName() + "; ";
		}

		toString = toString.replaceAll("; $", "");
		return toString;
	}
}
