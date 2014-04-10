package com.apichallenge.common.espn.bbc.repository;

import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface BbcGameRepository extends JpaRepository<BbcGame, Long> {
	BbcGame getBbcGameByEspnGameId(int espnGameId);

	@Query("SELECT g FROM BbcGame g WHERE date = ?1 AND homeTeamId = ?2 AND awayTeamId = ?3 AND gameNumber = ?4")
	BbcGame getBbcGame(Date date, long homeTeamId, long awayTeamId, int gameNumber);

	@Query("SELECT DISTINCT(date) FROM BbcGame g WHERE year = ?1 AND espnGameId IS NOT NULL")
	List<Date> getDistinctCompletedDates(int year);

	@Query("SELECT espnGameId FROM BbcGame WHERE date = ?1")
	List<Integer> getEspnGameIdsByDate(Date date);

	@Query("SELECT COUNT(id) FROM BbcGame WHERE year = ?1")
	int getGameCountForYear(int year);
}
