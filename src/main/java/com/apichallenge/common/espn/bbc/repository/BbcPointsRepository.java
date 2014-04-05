package com.apichallenge.common.espn.bbc.repository;

import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Repository
public interface BbcPointsRepository extends JpaRepository<BbcPoints, Long> {
	@Modifying
	@Transactional
	@Query("DELETE FROM BbcPoints b WHERE b.espnGameId = ?1")
	public void clearOutEspnGameId(int espnGameId);
}
