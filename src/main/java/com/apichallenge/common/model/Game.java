package com.apichallenge.common.model;

import java.util.*;

public class Game {
	Team homeTeam;
	Team awayTeam;
	Date date;
	Integer espnId;

	public Game(Team homeTeam, Team awayTeam, Date date, Integer espnId) {
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.date = date;
		this.espnId = espnId;
	}

	public Team getHomeTeam() {
		return homeTeam;
	}

	public Team getAwayTeam() {
		return awayTeam;
	}

	public Date getDate() {
		return date;
	}

	public Integer getEspnId() {
		return espnId;
	}

	@Override
	public String toString() {
		return "Game{" +
			homeTeam.getName() +
			" @ " + awayTeam.getName() +
			", date=" + date +
			", espnId=" + espnId +
			'}';
	}
}
