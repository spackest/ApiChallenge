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
	BbcBacktestDayRepository bbcBacktestDayRepository;

	@Autowired
	BbcSlurp bbcSlurp;

	private BbcBacktest bbcBacktest;
	private FantasyTeam fantasyTeam;
	private DbFantasyGame dbFantasyGame;

	public void init(int year, FantasyTeam fantasyTeam, DbFantasyGame dbFantasyGame) {
		Integer playerCount = bbcPlayerRepository.getPlayerCount();

		if (playerCount == null || playerCount == 0) {
			throw new IllegalStateException("gotta populate some players first. Run your BbcMain and trade for your team first :)");
		}

		int lastYear = year - 1;
		int gamesForLastYear = bbcGameRepository.getGameCountForYear(lastYear);
		if (gamesForLastYear < 2300) {
			bbcSlurp.slurpSchedule(lastYear);
		}

		int gamesForYear = bbcGameRepository.getGameCountForYear(year);

		if (gamesForYear < 2300) {
			bbcSlurp.slurpSchedule(year);
		}

		BbcBacktest thisBbcBacktest = new BbcBacktest(year, fantasyTeam.getStrategy());
		this.bbcBacktest = bbcBacktestRepository.save(thisBbcBacktest);
		this.fantasyTeam = fantasyTeam;
		this.dbFantasyGame = dbFantasyGame;
		this.dbFantasyGame.setBacktestId(bbcBacktest.getId());
	}

	public void backtest() {
		int total_points = 0;
		for (Date date : bbcGameRepository.getDistinctCompletedDates(bbcBacktest.getYear())) {
			long dateStart = System.currentTimeMillis();

			System.out.println("start for " + date);

			long start = System.currentTimeMillis();

			Integer points = null;

			try {
				BbcLeague bbcLeague = dbFantasyGame.getLeague(date);
				System.out.println(" -> got league in " + (System.currentTimeMillis() - start) + " ms");

				Strategy strategy = fantasyTeam.getStrategy();

				start = System.currentTimeMillis();
				Starters starters = strategy.pickStarters(date, bbcLeague);
				System.out.println(" -> picked starters in " + (System.currentTimeMillis() - start) + " ms");

				start = System.currentTimeMillis();
				dbFantasyGame.tradeForStarters(starters);
				System.out.println(" -> traded starters in " + (System.currentTimeMillis() - start) + " ms");

				start = System.currentTimeMillis();
				points = dbFantasyGame.getPoints(date, starters);
				System.out.println(" -> got points in " + (System.currentTimeMillis() - start) + " ms");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			if (points == null) {
				points = 0;
			}

			BbcBacktestDay bbcBacktestDay = new BbcBacktestDay(bbcBacktest.getId(), date, points);
			bbcBacktestDayRepository.save(bbcBacktestDay);

			total_points += points;
			System.out.println(" -> " + points + " points took " + (System.currentTimeMillis() - dateStart) + " ms\n");
		}

		bbcBacktest.setPoints(total_points);
		bbcBacktestRepository.save(bbcBacktest);
	}

	public void analyze() {
	}
}
