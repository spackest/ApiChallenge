package com.apichallenge.common.espn.bbc.game;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.repository.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.apache.commons.logging.*;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.persistence.*;
import java.io.*;
import java.util.*;

@Service
public class DbFantasyGame extends FantasyGame {
	private static final Log LOG = LogFactory.getLog(DbFantasyGame.class);

	@Autowired
	BbcTeamRepository bbcTeamRepository;

	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	@Autowired
	BbcPointsRepository bbcPointsRepository;

	@Autowired
	EntityManagerFactory entityManagerFactory;

	@Override
	public Starters tradeForStarters(Starters starters) throws IOException {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public BbcLeague getLeague(Date date, boolean getFullLeague) {
		long start = System.currentTimeMillis();

		System.out.println("getting league for " + date);
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		BbcLeague bbcLeague = new BbcLeague();
		Map<Integer, List<BbcPlayerDay>> map = new HashMap<Integer, List<BbcPlayerDay>>();

		SQLQuery sqlQuery = entityManager.unwrap(Session.class).createSQLQuery("select slot_id, bbc_player.espn_id, bbc_player.bbc_id, bbc_points.team_id, bbc_points.opponent_id, bbc_points.home_game, bbc_points.incoming_total_points, bbc_points.incoming_average_points from bbc_player, bbc_game, bbc_points where bbc_points.date = :date and (bbc_game.away_team_id = bbc_player.team_id or bbc_game.home_team_id = bbc_player.team_id) and bbc_player.espn_id = bbc_points.espn_id and bbc_game.espn_game_id is not null AND bbc_game.espn_game_id = bbc_points.espn_game_id AND bbc_id IS NOT NULL AND slot_id IS NOT NULL order by slot_id");
		sqlQuery.setDate("date", date);
		for (Object rawObject : sqlQuery.list()) {
			Object[] objects = (Object[]) rawObject;
			Integer slotId = Integer.valueOf(objects[0].toString());
			EspnId espnId = new EspnId(Integer.valueOf(objects[1].toString()));
			BbcId bbcId = new BbcId(Integer.valueOf(objects[2].toString()));
			long teamId = Long.valueOf(objects[3].toString());
			Long opponentId = Long.valueOf(objects[4].toString());
			boolean homeGame = Boolean.valueOf(objects[5].toString());
			int points = Integer.valueOf(objects[6].toString());
			float average = Float.valueOf(objects[7].toString());

			List<BbcGame> bbcGames = new ArrayList<BbcGame>();

			BbcPlayerDay bbcPlayerDay = new BbcPlayerDay(espnId, bbcId, teamId, opponentId, homeGame, average, points);

			List<BbcPlayerDay> myList = map.containsKey(slotId) ? map.get(slotId) : new ArrayList<BbcPlayerDay>();
			myList.add(bbcPlayerDay);

			// yip, keep putting it back
			map.put(slotId, myList);
		}

		entityManager.clear();
		entityManager.close();

		for (Map.Entry<Integer, List<BbcPlayerDay>> entry : map.entrySet()) {
			LeagueSlot leagueSlot = new LeagueSlot(BbcPositionEnum.getBbcPositionBySlotId(entry.getKey()), entry.getValue());
			bbcLeague.addLeagueSlot(leagueSlot);
		}

		System.out.println(" -> got league (" + (System.currentTimeMillis() - start) + " ms)");

		if (bbcLeague.getLeague().size() != 10) {
			throw new IllegalStateException("league is the wrong size");
		}
		return bbcLeague;
	}

}
