package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import org.springframework.stereotype.*;

@Service
public class BbcPlayerDay {
	private EspnId espnId;
	private BbcId bbcId;
	private long teamId;
	private Long opponentId;
	private boolean homeGame;
	private float average;
	private int points;
	private BbcPlayerDay startingPitcher;

	public BbcPlayerDay() {
	}

	public BbcPlayerDay(EspnId espnId, BbcId bbcId, long teamId, Long opponentId, boolean homeGame, float average, int points) {
		this.espnId = espnId;
		this.bbcId = bbcId;
		this.teamId = teamId;
		this.opponentId = opponentId;
		this.homeGame = homeGame;
		this.average = average;
		this.points = points;
	}

	public BbcPlayerDay getStartingPitcher() {
		return startingPitcher;
	}

	public void setStartingPitcher(BbcPlayerDay startingPitcher) {
		this.startingPitcher = startingPitcher;
	}

	public EspnId getEspnId() {
		return espnId;
	}

	public BbcId getBbcId() {
		return bbcId;
	}

	public long getTeamId() {
		return teamId;
	}

	public long getOpponentId() {
		return opponentId;
	}

	public boolean isHomeGame() {
		return homeGame;
	}

	public float getAverage() {
		return average;
	}

	public int getPoints() {
		return points;
	}
}
