package com.apichallenge.common.model;

import com.apichallenge.common.bbc.*;

public class Player {
	private int id;
	private String name;
	private BbcTeam team;
	private BbcTeam opponent;
	private BbcTeam location;
	private int rank;
	private float average;
	private int points;

	public Player(int id, String name, BbcTeam team, String rawOpponentShortName, int rank, float average, int points) {
		this.id = id;
		this.name = name;
		this.rank = rank;
		this.average = average;
		this.points = points;

		this.team = team;
		opponent = BbcTeam.getTeamByShortName(rawOpponentShortName.replace("@", ""));
		location = (rawOpponentShortName.startsWith("@")) ? opponent : team;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BbcTeam getTeam() {
		return team;
	}

	public BbcTeam getOpponent() {
		return opponent;
	}

	public BbcTeam getLocation() {
		return location;
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
