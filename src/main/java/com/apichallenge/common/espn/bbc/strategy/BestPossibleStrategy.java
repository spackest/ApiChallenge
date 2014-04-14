package com.apichallenge.common.espn.bbc.strategy;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class BestPossibleStrategy implements Strategy {
	@Autowired
	BbcPointsRepository bbcPointsRepository;

	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	@Override
	public Starters pickStarters(Date date, BbcLeague bbcLeague) {
		Starters starters = new Starters();

		for (LeagueSlot leagueSlot : bbcLeague.getLeague()) {
			List<Integer> espnIds = new ArrayList<Integer>();

			for (Map.Entry<BbcPlayerDay, List<BbcGame>> entry : leagueSlot.getBbcPlayerGames().entrySet()) {
				espnIds.add(entry.getKey().getEspnId().getId());
			}

			// TODO limit 1
			List<Integer> theseEspnIds = bbcPointsRepository.getBestForDay(date, espnIds);

			BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(theseEspnIds.get(0));

			starters.addStarter(leagueSlot.getPosition(), bbcPlayer);
		}

		return starters;
	}

	@Override
	public String getName() {
		return "BestPossibleStrategy";
	}
}
