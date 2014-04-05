package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.stereotype.*;

@Service
public class BbcPlayerDay {
	private BbcPlayer bbcPlayer;
	private BbcTeam team;
	private BbcTeam opponent;
	private boolean homeGame;
	private double average;
	private int points;

	public BbcPlayerDay() {
	}

	public BbcPlayerDay(BbcPlayer bbcPlayer, BbcTeam team, BbcTeam opponent, boolean homeGame, double average, int points) {
		this.bbcPlayer = bbcPlayer;
		this.team = team;
		this.opponent = opponent;
		this.homeGame = homeGame;
		this.average = average;
		this.points = points;
	}

	public BbcPlayer getBbcPlayer() {
		return bbcPlayer;
	}

	public BbcTeam getTeam() {
		return team;
	}

	public BbcTeam getOpponent() {
		return opponent;
	}

	public boolean isHomeGame() {
		return homeGame;
	}

	public double getAverage() {
		return average;
	}

	public int getPoints() {
		return points;
	}

	@Override
	public String toString() {
		return bbcPlayer.getName() +
			", team=" + team.getShortName() +
			", opponent=" + (opponent == null ? null : opponent.getShortName()) +
			", homeGame=" + homeGame +
			", average=" + average +
			", points=" + points;
	}
}
