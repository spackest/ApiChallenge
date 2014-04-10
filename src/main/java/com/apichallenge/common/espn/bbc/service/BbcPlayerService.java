package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class BbcPlayerService {
	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	private static final Set<EspnId> EXTANT_PLAYERS = new HashSet<EspnId>();

	public void perhapsInsertPlayer(BbcId bbcId, EspnId espnId, Integer slotId, BbcTeam bbcTeam, String name) {
		if (!EXTANT_PLAYERS.contains(espnId)) {
			BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId.getId());
			if (bbcPlayer == null) {
				if (slotId == null) {
					slotId = bbcPlayerRepository.getSlotIdByEspnId(espnId.getId());
				}

				bbcPlayer = new BbcPlayer(bbcId, espnId, slotId, bbcTeam.getId(), name);
				bbcPlayer = bbcPlayerRepository.save(bbcPlayer);
				if (bbcPlayer != null) {
					EXTANT_PLAYERS.add(espnId);
				}
			} else {
				boolean changed = false;

				if (slotId != null && bbcPlayer.getSlotId() == null) {
					bbcPlayer.setSlotId(slotId);
					changed = true;
				}

				if (bbcId != null && bbcPlayer.getBbcId() == null) {
					bbcPlayer.setBbcId(bbcId);
					changed = true;
				}

				if (changed) {
					bbcPlayerRepository.save(bbcPlayer);
				}
			}
		}
	}
}