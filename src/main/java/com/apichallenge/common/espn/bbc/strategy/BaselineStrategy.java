package com.apichallenge.common.espn.bbc.strategy;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class BaselineStrategy implements Strategy {
	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	public Starters pickStarters(Date date, BbcLeague bbcLeague) {
		Starters starters = new Starters();

		for (LeagueSlot leagueSlot : bbcLeague.getLeague()) {
			EspnId espnId = null;

			int maxPoints = 0;
			for (Map.Entry<BbcPlayerDay, List<BbcGame>> entry : leagueSlot.getBbcPlayerGames().entrySet()) {
				List<BbcGame> bbcGames = entry.getValue();

				BbcPlayerDay bbcPlayerDay = entry.getKey();

				if (bbcPlayerDay.getEspnId() == null) {
					bbcPlayerDay.toString();
				}

				int points = entry.getKey().getPoints() * bbcGames.size();

				if (points >= maxPoints) {
					espnId = bbcPlayerDay.getEspnId();
					maxPoints = points;
				}
			}

			if (espnId == null) {
				throw new IllegalStateException("no starter for " + leagueSlot.getPosition().getShortName());
			}

			BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId.getId());

			starters.addStarter(leagueSlot.getPosition(), bbcPlayer);
		}

		return starters;
	}

	public String getName() {
		return "BaselineStrategy";
	}
}
