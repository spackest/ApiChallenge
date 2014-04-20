package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.persistence.*;
import java.util.*;

@Service
public class BbcPointService {
	@Autowired
	private BbcPointsRepository bbcPointsRepository;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	private static EntityManager entityManager;

	public int pointsLastNGames(Date date, EspnId espnId, int games) {
		TypedQuery<BbcPoints> query = getEntityManager().createQuery("SELECT p FROM BbcPoints p WHERE espnId = ?1 AND p.date < ?2 ORDER BY date DESC", BbcPoints.class);
		query.setParameter(1, espnId.getId());
		query.setParameter(2, date);
		query.setFirstResult(0);
		query.setMaxResults(games);

		int points = 0;


		for (BbcPoints bbcPoints : query.getResultList()) {
			points += bbcPoints.getPoints();
		}

		return points;
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


	private EntityManager getEntityManager() {
		if (entityManager == null) {
			entityManager = entityManagerFactory.createEntityManager();
		}

		return entityManager;
	}
}
