package com.apichallenge.common.espn.bbc;

import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.service.*;

import java.util.*;

public class LeagueSlot {
	private BbcPositionEnum position;
	private List<BbcPlayerDay> bbcPlayerDays;

	public LeagueSlot(BbcPositionEnum position, List<BbcPlayerDay> bbcPlayerDays) {
		this.position = position;
		this.bbcPlayerDays = bbcPlayerDays;
	}

	public BbcPositionEnum getPosition() {
		return position;
	}

	public List<BbcPlayerDay> getBbcPlayerDays() {
		return bbcPlayerDays;
	}
}
