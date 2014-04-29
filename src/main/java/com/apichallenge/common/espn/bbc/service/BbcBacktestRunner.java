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

		int lastYear = year - 1;
		int gamesForLastYear = bbcGameRepository.getGameCountForYear(lastYear);
		if (gamesForLastYear < 2300) {
			bbcSlurp.slurpSchedule(lastYear);
			gamesForLastYear = bbcGameRepository.getGameCountForYear(lastYear);
		}

		int completePointsCountLastYear = bbcPointsRepository.getCompletePointsGameCount(lastYear);
		if (completePointsCountLastYear != gamesForLastYear) {
			bbcSlurp.handleIncomingPoints(lastYear);
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
			long dateStart = System.currentTimeMillis();

			System.out.println("start for " + date);

			long start = System.currentTimeMillis();
			BbcLeague bbcLeague = fantasyGame.getLeague(date);
			System.out.println(" -> got league in " + (System.currentTimeMillis() - start) + " ms");

			Strategy strategy = fantasyTeam.getStrategy();

			start = System.currentTimeMillis();
			Starters starters = strategy.pickStarters(date, bbcLeague);
			System.out.println(" -> picked starters in " + (System.currentTimeMillis() - start) + " ms");

			start = System.currentTimeMillis();
			fantasyGame.tradeForStarters(starters);
			System.out.println(" -> traded starters in " + (System.currentTimeMillis() - start) + " ms");

			start = System.currentTimeMillis();
			int points = fantasyGame.getPoints(date, starters);
			System.out.println(" -> got points in " + (System.currentTimeMillis() - start) + " ms");

			total_points += points;
			System.out.println(" -> " + points + " points took " + (System.currentTimeMillis() - dateStart) + " ms\n");
		}

		bbcBacktest.setPoints(total_points);
		bbcBacktestRepository.save(bbcBacktest);
	}

	public void analyze() {
	}
}
