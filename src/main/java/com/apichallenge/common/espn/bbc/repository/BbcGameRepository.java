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

	@Query("SELECT g FROM BbcGame g WHERE date = ?1 AND homeTeamId = ?2 AND awayTeamId = ?3")
	List<BbcGame> getBbcGames(Date date, long homeTeamId, long awayTeamId);

	@Query("SELECT DISTINCT(date) FROM BbcGame g WHERE year = ?1 AND espnGameId IS NOT NULL")
	List<Date> getDistinctCompletedDates(int year);

	@Query("SELECT espnGameId FROM BbcGame WHERE date = ?1")
	List<Integer> getEspnGameIdsByDate(Date date);

	@Query("SELECT b FROM BbcGame b WHERE date = ?1")
	List<BbcGame> getBbcGamesByDate(Date date);

	@Query("SELECT COUNT(id) FROM BbcGame WHERE year = ?1")
	int getGameCountForYear(int year);

	@Query("SELECT COUNT(b.id) FROM BbcGame b WHERE date = ?1 AND espnGameId != ?2 AND homeTeamId = ?3 AND awayTeamId = ?4 ")
	Integer getSimilarGameCount(Date date, int espnGameId, long homeTeamId, long awayTeamId);

	@Query("SELECT COUNT(g.id) FROM BbcGame g WHERE year = ?1 AND date < ?2 AND espnGameId IS NULL")
	Integer getUncompletedGameCount(int year, Date date);
}
