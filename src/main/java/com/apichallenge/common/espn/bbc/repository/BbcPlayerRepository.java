package com.apichallenge.common.espn.bbc.repository;

import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface BbcPlayerRepository extends JpaRepository<BbcPlayer, Long> {
	@Query("SELECT COUNT(b) FROM BbcPlayer b")
	public Integer getPlayerCount();

	BbcPlayer getBbcPlayerByEspnId(int espnId);

	BbcPlayer getBbcPlayerByBbcId(int bbcId);

	@Query("SELECT b.slotId FROM BbcPlayer b WHERE b.espnId = ?1")
	Integer getSlotIdByEspnId(int espnId);
}
