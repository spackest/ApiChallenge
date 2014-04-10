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
			for (BbcPlayerDay bbcPlayerDay : leagueSlot.getBbcPlayerDays()) {
				if (bbcPlayerDay.getPoints() >= maxPoints) {
					espnId = bbcPlayerDay.getEspnId();
					maxPoints = bbcPlayerDay.getPoints();
				}
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