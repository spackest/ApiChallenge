package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.persistence.*;
import java.util.*;

@Service
public class BbcPointsService {
	@Autowired
	private BbcPointsRepository bbcPointsRepository;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	private static EntityManager entityManager;

	public int pointsLastNGames(Date date, EspnId espnId, int games) {
		int points = 0;

		List<Integer> pointses = bbcPointsLastNGames(date, espnId, games);

		for (int thesePoints : bbcPointsLastNGames(date, espnId, games)) {
			points += thesePoints;
		}

		return points;
	}

	public List<Integer> bbcPointsLastNGames(Date date, EspnId espnId, int games) {
		TypedQuery<Integer> query = getEntityManager().createQuery("SELECT points FROM BbcPoints p WHERE espnId = ?1 AND p.date < ?2 ORDER BY date DESC", Integer.class);
		query.setParameter(1, espnId.getId());
		query.setParameter(2, date);
		query.setFirstResult(0);
		query.setMaxResults(games);

		return query.getResultList() == null ? new ArrayList<Integer>() : query.getResultList();
	}

	public List<BbcPoints> bbcPointsLastNHomeAwayGames(Date date, EspnId espnId, boolean isHomeGame, int games) {
		TypedQuery<BbcPoints> query = getEntityManager().createQuery("SELECT p FROM BbcPoints p WHERE espnId = ?1 AND p.date < ?2 AND p.homeGame = ?3 ORDER BY p.date DESC", BbcPoints.class);
		query.setParameter(1, espnId.getId());
		query.setParameter(2, date);
		query.setParameter(3, isHomeGame);
		query.setFirstResult(0);
		query.setMaxResults(games);

		return query.getResultList() == null ? new ArrayList<BbcPoints>() : query.getResultList();
	}

	public List<BbcPoints> bbcPointsLastNOpponentGames(Date date, EspnId espnId, long opponentId, int games) {
		TypedQuery<BbcPoints> query = getEntityManager().createQuery("SELECT p FROM BbcPoints p WHERE espnId = ?1 AND p.date < ?2 AND p.opponentId = ?3 ORDER BY p.date DESC", BbcPoints.class);
		query.setParameter(1, espnId.getId());
		query.setParameter(2, date);
		query.setParameter(3, opponentId);
		query.setFirstResult(0);
		query.setMaxResults(games);

		return query.getResultList() == null ? new ArrayList<BbcPoints>() : query.getResultList();
	}

	public int pitchingStaffPointsAverage(Date date, BbcTeam team, int games) {
		TypedQuery<BbcPoints> query = getEntityManager().createQuery("SELECT p FROM BbcPoints p WHERE teamId = ?1 AND slotId = 10 AND p.date < ?2 ORDER BY date DESC", BbcPoints.class);
		query.setParameter(1, team.getId());
		query.setParameter(2, date);
		query.setFirstResult(0);
		query.setMaxResults(games);

		int points = 0;
		int actualGames = 0;

		for (BbcPoints bbcPoints : query.getResultList()) {
			points += bbcPoints.getPoints();
			actualGames++;
		}

		if (actualGames > 0) {
			points = (int) points / actualGames;
		}

		return points;
	}

	public float getAveragePointsToDate(int year, Date date, EspnId espnId) {
		Float averagePoints = bbcPointsRepository.getAveragePoints(year, date, espnId.getId());
		if (averagePoints == null) {
			averagePoints = 0f;
		}

		return averagePoints;
	}

	public int getTotalPointsToDate(int year, Date date, EspnId espnId) {
		Integer totalPoints = bbcPointsRepository.getTotalPoints(year, date, espnId.getId());
		if (totalPoints == null) {
			totalPoints = 0;
		}

		return totalPoints;
	}

	private EntityManager getEntityManager() {
		if (entityManager == null) {
			entityManager = entityManagerFactory.createEntityManager();
		}

		return entityManager;
	}
}
