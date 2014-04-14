package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Service
public class BbcPlayerService {
	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	public void perhapsInsertPlayer(BbcId bbcId, EspnId espnId, SlotId slotId, BbcTeam bbcTeam, String name) {
		BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId.getId());
		if (bbcPlayer == null) {
			if (slotId == null) {
				slotId = new SlotId(bbcPlayerRepository.getSlotIdByEspnId(espnId.getId()));
			}

			bbcPlayer = new BbcPlayer(bbcId, espnId, slotId, bbcTeam.getId(), name);
			bbcPlayer = bbcPlayerRepository.save(bbcPlayer);
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