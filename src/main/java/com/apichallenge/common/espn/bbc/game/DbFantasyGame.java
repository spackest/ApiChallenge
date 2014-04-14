package com.apichallenge.common.espn.bbc.game;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.repository.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.apache.commons.logging.*;
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
	BbcGameRepository bbcGameRepository;

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
	public BbcLeague getLeague(Date date) {
		long start = System.currentTimeMillis();

		System.out.println("getting league for " + date);

		BbcLeague bbcLeague = new BbcLeague();
		Map<SlotId, Map<BbcPlayerDay, List<BbcGame>>> bigMap = new HashMap<SlotId, Map<BbcPlayerDay, List<BbcGame>>>();

		for (BbcPoints bbcPoints : bbcPointsRepository.getPointsFromDate(date)) {
			SlotId slotId = bbcPoints.getSlotId();

			if (slotId == null || slotId.getId().equals(0)) {
				continue;
			}

			BbcTeam team = bbcTeamRepository.findOne(bbcPoints.getTeamId());
			BbcTeam opponent = bbcTeamRepository.findOne(bbcPoints.getOpponentId());

			BbcPlayerDay bbcPlayerDay = new BbcPlayerDay(bbcPoints.getEspnId(), null, false, team, opponent, bbcPoints.isHomeGame(), bbcPoints.getIncomingAveragePoints(), bbcPoints.getIncomingTotalPoints());

			Map<BbcPlayerDay, List<BbcGame>> slotMap;
			if (bigMap.containsKey(slotId)) {
				slotMap = bigMap.get(slotId);
			} else {
				slotMap = new HashMap<BbcPlayerDay, List<BbcGame>>();
				bigMap.put(slotId, slotMap);
			}

			long homeTeamId;
			long awayTeamId;

			if (bbcPoints.isHomeGame()) {
				homeTeamId = bbcPoints.getTeamId();
				awayTeamId = bbcPoints.getOpponentId();
			} else {
				homeTeamId = bbcPoints.getOpponentId();
				awayTeamId = bbcPoints.getTeamId();
			}

			slotMap.put(bbcPlayerDay, bbcGameRepository.getBbcGames(date, homeTeamId, awayTeamId));
		}

		for (Map.Entry<SlotId, Map<BbcPlayerDay, List<BbcGame>>> entry : bigMap.entrySet()) {
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
