package com.apichallenge.common.espn.bbc.strategy;

import com.apichallenge.common.*;
import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.game.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.apache.commons.logging.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

import java.util.*;

public final class BestPossibleBacktest {
	private static final Log LOG = LogFactory.getLog(BestPossibleBacktest.class);
	private static final int BACKTEST_YEAR = Constants.YEAR;

	public static void main(String[] args) throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"});
		BbcBacktestRunner bbcBacktestRunner = applicationContext.getBean(BbcBacktestRunner.class);
		DbFantasyGame dbFantasyGame = applicationContext.getBean(DbFantasyGame.class);
		BestPossibleStrategy bestPossibleStrategy = applicationContext.getBean(BestPossibleStrategy.class);

		List<FantasyTeam> fantasyTeams = Arrays.asList(new FantasyTeam("back test guy", bestPossibleStrategy));

		for (FantasyTeam fantasyTeam : fantasyTeams) {
			LOG.info(fantasyTeam.getEntryName());

			bbcBacktestRunner.init(BACKTEST_YEAR, fantasyTeam, dbFantasyGame);
			bbcBacktestRunner.backtest();
		}
	}
}
