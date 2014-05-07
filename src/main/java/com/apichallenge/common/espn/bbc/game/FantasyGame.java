package com.apichallenge.common.espn.bbc.game;

import com.apichallenge.common.espn.bbc.*;
import org.apache.commons.logging.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.*;

@Service
public abstract class FantasyGame {
	private static final Log LOG = LogFactory.getLog(FantasyGame.class);

	protected FantasyTeam fantasyTeam;

	public FantasyGame() {
	}

	public void tradeForTomorrow() throws Exception {
		tradeForDate(DateUtil.getGameTomorrow());
	}

	public void tradeForDate(Date date) throws Exception {
		BbcLeague bbcLeague = getLeague(date);

		Starters expectedStarters = fantasyTeam.getStrategy().pickStarters(date, bbcLeague);
		Starters actualStarters = tradeForStarters(expectedStarters);

		boolean startersSet = actualStarters.equals(expectedStarters);
		System.out.println("starters look good - " + startersSet);
	}

	public abstract BbcLeague getLeague(Date date) throws IOException;

	public abstract Starters tradeForStarters(Starters starters) throws IOException;
}
