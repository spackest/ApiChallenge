package com.apichallenge.common.espn.bbc.repository;

import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
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

	@Query("SELECT b FROM BbcPlayer b WHERE b.teamId = ?1 AND bbcId IS NOT NULL AND slotId = ?2")
	BbcPlayer getPitchingStaff(long teamId, int slotId);
}
