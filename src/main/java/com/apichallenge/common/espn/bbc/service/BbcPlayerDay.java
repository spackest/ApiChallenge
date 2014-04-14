package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.stereotype.*;

@Service
public class BbcPlayerDay {
	private EspnId espnId;
	private BbcId bbcId;
	private boolean injured;
	private BbcTeam team;
	private BbcTeam opponent;
	private boolean homeGame;
	private float average;
	private int points;
	private BbcPlayer teamStartingPitcher;
	private BbcPlayer opposingStartingPitcher;

	public BbcPlayerDay() {
	}

	public BbcPlayerDay(EspnId espnId, BbcId bbcId, boolean injured, BbcTeam team, BbcTeam opponent, boolean homeGame, float average, int points) {
		this.espnId = espnId;
		this.bbcId = bbcId;
		this.injured = injured;
		this.team = team;
		this.opponent = opponent;
		this.homeGame = homeGame;
		this.average = average;
		this.points = points;
	}

	public EspnId getEspnId() {
		return espnId;
	}

	public BbcId getBbcId() {
		return bbcId;
	}

	public int getPoints() {
		return points;
	}

	public float getAverage() {
		return average;
	}

	public boolean isHomeGame() {
		return homeGame;
	}

	public boolean isInjured() {
		return injured;
	}

	public BbcTeam getTeam() {
		return team;
	}

	public Long getTeamId() {
		return team == null ? null : team.getId();
	}

	public BbcTeam getOpponent() {
		return opponent;
	}

	public Long getOpponentId() {
		return opponent == null ? null : opponent.getId();
	}

	public BbcPlayer getTeamStartingPitcher() {
		return teamStartingPitcher;
	}

	public BbcPlayer getOpposingStartingPitcher() {
		return opposingStartingPitcher;
	}

	public void setTeamStartingPitcher(BbcPlayer teamStartingPitcher) {
		this.teamStartingPitcher = teamStartingPitcher;
	}

	public void setOpposingStartingPitcher(BbcPlayer opposingStartingPitcher) {
		this.opposingStartingPitcher = opposingStartingPitcher;
	}
}
