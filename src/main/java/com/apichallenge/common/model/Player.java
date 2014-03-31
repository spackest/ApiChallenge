package com.apichallenge.common.model;

import com.apichallenge.common.bbc.*;

public class Player {
	private int id;
	private String name;
	private Team team;
	private Team opponent;
	private Team homeTeam;
	private int rank;
	private float average;
	private int points;

	public Player(int id, String name, Team team, String rawOpponentShortName, int rank, float average, int points) {
		this.id = id;
		this.name = name;
		this.rank = rank;
		this.average = average;
		this.points = points;

		this.team = team;
		opponent = BbcTeam.getTeam(rawOpponentShortName.replace("@", ""));
		homeTeam = (rawOpponentShortName.startsWith("@")) ? opponent : team;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Team getTeam() {
		return team;
	}

	public Team getOpponent() {
		return opponent;
	}

	public Team getHomeTeam() {
		return homeTeam;
	}

	public int getRank() {
		return rank;
	}

	public float getAverage() {
		return average;
	}

	public int getPoints() {
		return points;
	}

	public boolean equals(Object object) {
		Player playerB = (Player) object;
		if (object == null || playerB.getId() == 0) {
			return false;
		}

		return id == playerB.getId();
	}
}
