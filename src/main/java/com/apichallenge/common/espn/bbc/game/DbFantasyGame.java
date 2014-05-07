package com.apichallenge.common.espn.bbc.game;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.repository.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.*;

@Service
public class DbFantasyGame extends FantasyGame {
	private static final Log LOG = LogFactory.getLog(DbFantasyGame.class);

	private long backtestId;

	@Autowired
	BbcTeamRepository bbcTeamRepository;

	@Autowired
	BbcGameRepository bbcGameRepository;

	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	@Autowired
	BbcPointsRepository bbcPointsRepository;

	@Autowired
	BbcBacktestStarterRepository bbcBacktestStarterRepository;

	@Autowired
	BbcPointsService bbcPointsService;

	public void setBacktestId(long backtestId) {
		this.backtestId = backtestId;
	}

	@Override
	public Starters tradeForStarters(Starters starters) throws IOException {
		return null;
	}

	@Override
	public BbcLeague getLeague(Date date) {
		BbcLeague bbcLeague = new BbcLeague();
		Map<SlotId, Map<BbcPlayerDay, List<BbcGame>>> bigMap = new HashMap<SlotId, Map<BbcPlayerDay, List<BbcGame>>>();

		Set<EspnId> inLeague = new HashSet<EspnId>();

		for (BbcPoints bbcPoints : bbcPointsRepository.getPointsFromDate(date)) {
			SlotId slotId = bbcPoints.getSlotId();

			EspnId espnId = bbcPoints.getEspnId();

			if (slotId == null || slotId.getId().equals(0) || inLeague.contains(espnId)) {
				continue;
			}

			BbcTeam team = bbcTeamRepository.findOne(bbcPoints.getTeamId());
			BbcTeam opponent = bbcTeamRepository.findOne(bbcPoints.getOpponentId());

			int year = DateUtil.getYear(date);

			BbcPlayerDay bbcPlayerDay = new BbcPlayerDay(bbcPoints.getEspnId(), null, false, team, opponent, bbcPoints.isHomeGame(), bbcPointsService.getAveragePointsToDate(year, date, espnId), bbcPointsService.getTotalPointsToDate(year, date, espnId));

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

			inLeague.add(bbcPlayerDay.getEspnId());
			slotMap.put(bbcPlayerDay, bbcGameRepository.getBbcGames(date, homeTeamId, awayTeamId));
		}

		for (Map.Entry<SlotId, Map<BbcPlayerDay, List<BbcGame>>> entry : bigMap.entrySet()) {
			LeagueSlot leagueSlot = new LeagueSlot(BbcPositionEnum.getBbcPositionBySlotId(entry.getKey()), entry.getValue());
			bbcLeague.addLeagueSlot(leagueSlot);
		}

		if (bbcLeague.getLeague().size() != 10) {
			throw new IllegalStateException("league is the wrong size");
		}

		return bbcLeague;
	}

	public int getPoints(Date date, Starters starters) {
		int points = 0;

		List<BbcBacktestStarter> bbcBacktestStarters = new ArrayList<BbcBacktestStarter>();

		for (BbcPlayer bbcPlayer : starters.getStarters()) {

			try {
				Integer thesePoints = null;
				SlotId slotId = bbcPlayer.getSlotId();

				if (slotId.getId() == BbcPositionEnum.PITCHER.getSlotId().getId()) {
					thesePoints = bbcPointsRepository.getPitchingStaffPointsFromDateTeamId(date, bbcPlayer.getTeamId(), BbcPositionEnum.PITCHER.getSlotId().getId());
				} else {
					thesePoints = bbcPointsRepository.getPointsFromDateEspnId(date, bbcPlayer.getEspnId().getId());
				}

				if (thesePoints == null) {
					System.out.println("null thesePoints");
					thesePoints = 0;
				}

				BbcBacktestStarter bbcBacktestStarter = new BbcBacktestStarter(backtestId, date, slotId.getId(), bbcPlayer.getEspnId().getId(), thesePoints);
				bbcBacktestStarters.add(bbcBacktestStarter);
				points += thesePoints;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		bbcBacktestStarterRepository.save(bbcBacktestStarters);

		return points;
	}
}
