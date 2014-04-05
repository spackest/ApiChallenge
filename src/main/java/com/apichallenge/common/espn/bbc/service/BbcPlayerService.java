package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class BbcPlayerService {
	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	private static final Set<Integer> EXTANT_PLAYERS = new HashSet<Integer>();

	public void perhapsInsertPlayer(int espnId, int slotId, BbcTeam bbcTeam, String name) {
		if (!EXTANT_PLAYERS.contains(espnId)) {
			BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId);
			if (bbcPlayer == null && slotId != 0) {
				bbcPlayer = new BbcPlayer(espnId, slotId, bbcTeam.getId(), name);
				bbcPlayer = bbcPlayerRepository.save(bbcPlayer);
				if (bbcPlayer != null) {
					EXTANT_PLAYERS.add(espnId);
				}
			}
		}
	}
}
