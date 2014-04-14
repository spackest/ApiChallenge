package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class BbcGameService {
	@Autowired
	BbcGameRepository bbcGameRepository;

	@Autowired
	BbcPointsRepository bbcPointsRepository;

	public boolean gameComplete(EspnGameId espnGameId) {
		if (espnGameId == null) {
			return false;
		}

		Integer points = bbcPointsRepository.getTotalPointsForEspnGameId(espnGameId.getId());
		return points != null;
	}

	public void sync(BbcGame bbcGame) {
		BbcGame dbBbcGame = bbcGameRepository.getBbcGame(bbcGame.getDate(), bbcGame.getHomeTeamId(), bbcGame.getAwayTeamId(), bbcGame.getGameNumber());
		BbcGame origBbcGame = bbcGameRepository.getBbcGame(bbcGame.getDate(), bbcGame.getHomeTeamId(), bbcGame.getAwayTeamId(), bbcGame.getGameNumber());

		if (dbBbcGame == null) {
			bbcGameRepository.save(bbcGame);
		} else {
			int differences = 0;

			if (bbcGame.getEspnGameId() != null && dbBbcGame.getEspnGameId() == null && !bbcGame.getEspnGameId().equals(dbBbcGame.getEspnGameId())) {
				dbBbcGame.setEspnGameId(bbcGame.getEspnGameId());
				differences++;
			}

			if (bbcGame.getHomeStartingPitcherEspnId() != null && !bbcGame.getHomeStartingPitcherEspnId().equals(dbBbcGame.getHomeStartingPitcherEspnId())) {
				dbBbcGame.setHomeStartingPitcherEspnId(bbcGame.getHomeStartingPitcherEspnId());
				differences++;
			}

			if (bbcGame.getAwayStartingPitcherEspnId() != null && !bbcGame.getAwayStartingPitcherEspnId().equals(dbBbcGame.getAwayStartingPitcherEspnId())) {
				dbBbcGame.setAwayStartingPitcherEspnId(bbcGame.getAwayStartingPitcherEspnId());
				differences++;
			}

			if (differences > 0) {
				try {
					bbcGameRepository.save(dbBbcGame);
				} catch (Exception e) {
					e.toString();
				}
			}
		}
	}

	public int getGameNumber(Date date, EspnGameId espnGameId, BbcTeam homeTeam, BbcTeam awayTeam) {
		if (espnGameId == null || homeTeam == null || awayTeam == null) {
			return 1;
		}

		Integer count = bbcGameRepository.getSimilarGameCount(date, espnGameId.getId(), homeTeam.getId(), awayTeam.getId());

		return (count == null || count.equals(0)) ? 1 : 2;
	}
}
