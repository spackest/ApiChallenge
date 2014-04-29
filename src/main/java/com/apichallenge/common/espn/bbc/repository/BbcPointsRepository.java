package com.apichallenge.common.espn.bbc.repository;

import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Repository
public interface BbcPointsRepository extends JpaRepository<BbcPoints, Long> {
	@Modifying
	@Transactional
	@Query("DELETE FROM BbcPoints b WHERE b.espnGameId = ?1")
	public void clearOutEspnGameId(int espnGameId);

	@Modifying
	@Transactional
	@Query("UPDATE BbcPoints b SET slotId = ?3 WHERE b.year = ?1 AND b.espnId = ?2")
	public void updateSlotId(int year, int espnId, int slotId);

	@Query("SELECT SUM(points) FROM BbcPoints b WHERE b.year = ?1 AND b.espnId = ?2")
	public Integer getTotalPoints(int year, int espnId);

	@Query("SELECT AVG(points) FROM BbcPoints b WHERE b.year = ?1 AND b.espnId = ?2")
	public Float getAveragePoints(int year, int espnId);

	@Query("SELECT SUM(points) FROM BbcPoints b WHERE b.date = ?1 AND b.espnId = ?2")
	public Integer getPointsFromDateEspnId(Date date, int espnId);

	@Query("SELECT b FROM BbcPoints b WHERE b.date = ?1")
	public List<BbcPoints> getPointsFromDate(Date date);

	@Query("SELECT b FROM BbcPoints b WHERE b.year = ?1 ORDER BY espnId, date")
	public List<BbcPoints> getSeason(int year);

	@Query("SELECT COUNT(DISTINCT espnGameId) FROM BbcPoints b WHERE b.year = ?1 AND incomingAveragePoints IS NOT NULL AND incomingTotalPoints IS NOT NULL")
	public int getCompletePointsGameCount(int year);

	@Query("SELECT espnId FROM BbcPoints b WHERE b.date = ?1 AND espnId IN ?2 ORDER BY points DESC")
	public List<Integer> getBestForDay(Date date, List<Integer> espnIds);

	@Query("SELECT SUM(points) FROM BbcPoints b WHERE b.espnGameId = ?1")
	public Integer getTotalPointsForEspnGameId(int espnGameId);

	public Page<BbcPoints> findByEspnIdAndDateLessThan(int espnId, Date date, Pageable pageRequest);

	@Query("SELECT MAX(p.date) FROM BbcPoints p WHERE p.date <= ?1 AND p.espnId = ?2")
	public Date getMostRecentGameDate(Date date, int espnId);

	@Query("SELECT SUM(points) FROM BbcPoints p WHERE p.date = ?1 AND p.teamId = ?2 AND p.slotId = ?3")
	public Integer getPitchingStaffPointsFromDateTeamId(Date date, long teamId, int slotId);
}
