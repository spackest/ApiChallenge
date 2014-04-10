package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.game.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class BbcBacktestRunner {
	@Autowired
	BbcGameRepository bbcGameRepository;

	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	@Autowired
	BbcPointsRepository bbcPointsRepository;

	@Autowired
	BbcBacktestRepository bbcBacktestRepository;

	@Autowired
	BbcSlurp bbcSlurp;

	private BbcBacktest bbcBacktest;
	private FantasyTeam fantasyTeam;
	private FantasyGame fantasyGame;

	public void init(int year, FantasyTeam fantasyTeam, FantasyGame fantasyGame) {
		Integer playerCount = bbcPlayerRepository.getPlayerCount();

		if (playerCount == null || playerCount == 0) {
			throw new IllegalStateException("gotta populate some players first. Run your BbcMain and trade for your team first :)");
		}

		int gamesForYear = bbcGameRepository.getGameCountForYear(year);

		if (gamesForYear < 2300) {
			bbcSlurp.slurpSchedule(year);
			gamesForYear = bbcGameRepository.getGameCountForYear(year);
		}

		int completePointsCount = bbcPointsRepository.getCompletePointsGameCount(year);
		if (completePointsCount != gamesForYear) {
			bbcSlurp.handleIncomingPoints(year);
		}

		BbcBacktest thisBbcBacktest = new BbcBacktest(year, fantasyTeam.getStrategy());
		this.bbcBacktest = bbcBacktestRepository.save(thisBbcBacktest);
		this.fantasyTeam = fantasyTeam;
		this.fantasyGame = fantasyGame;
	}

	public void backtest() throws Exception {

		int total_points = 0;
		for (Date date : bbcGameRepository.getDistinctCompletedDates(bbcBacktest.getYear())) {
			System.out.println("start for " + date);
			BbcLeague bbcLeague = fantasyGame.getLeague(date, true);
			Strategy strategy = fantasyTeam.getStrategy();
			Starters starters = strategy.pickStarters(date, bbcLeague);
			System.out.println("got starters for " + date);
			fantasyGame.tradeForStarters(starters);
			System.out.println("traded starters for " + date);
			int points = fantasyGame.getPoints(date, starters);
			System.out.println("got points for " + date);
			total_points += points;
			System.out.println(date + " - " + points);
		}

		bbcBacktest.setPoints(total_points);
		bbcBacktestRepository.save(bbcBacktest);
	}

	public void analyze() {
	}
}
