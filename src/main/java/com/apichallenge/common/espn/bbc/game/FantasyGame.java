package com.apichallenge.common.espn.bbc.game;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.*;

@Service
public abstract class FantasyGame {
	private static final Log LOG = LogFactory.getLog(FantasyGame.class);

	protected FantasyTeam fantasyTeam;

	@Autowired
	private BbcPointsRepository bbcPointsRepository;

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

	public int getPoints(Date date, Starters starters) {
		int points = 0;

		for (BbcPlayer bbcPlayer : starters.getStarters()) {

			try {
				Integer thesePoints = null;
				if (bbcPlayer.getSlotId().getId() == BbcPositionEnum.PITCHER.getSlotId().getId()) {
					thesePoints = bbcPointsRepository.getPitchingStaffPointsFromDateTeamId(date, bbcPlayer.getTeamId(), BbcPositionEnum.PITCHER.getSlotId().getId());
				} else {
					thesePoints = bbcPointsRepository.getPointsFromDateEspnId(date, bbcPlayer.getEspnId().getId());
				}
				if (thesePoints != null) {
					points += thesePoints;
				} else {
					System.out.println("null thesePoints");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return points;
	}

	public abstract BbcLeague getLeague(Date date) throws IOException;

	public abstract Starters tradeForStarters(Starters starters) throws IOException;
}
