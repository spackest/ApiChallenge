package com.apichallenge.common.espn.bbc.entity;


import javax.persistence.*;

@Entity
@Table(name = "BBC_POINTS", uniqueConstraints = {@UniqueConstraint(columnNames = {"ESPN_GAME_ID", "ESPN_ID", "POINTS"})})
public class BbcPoints extends ParentEntity<BbcPoints> {
	@Column(name = "ESPN_GAME_ID", nullable = false)
	private Integer espnGameId;

	@Column(name = "ESPN_ID", nullable = false)
	private Integer espnId;

	@Column(name = "POINTS", nullable = false)
	private Integer points;

	public BbcPoints() {
	}

	public BbcPoints(Integer espnGameId, Integer espnId, Integer points) {
		this.espnGameId = espnGameId;
		this.espnId = espnId;
		this.points = points;
	}

	@Override
	public String toString() {
		return "BbcPoints{" +
			"espnGameId=" + espnGameId +
			", espnId=" + espnId +
			", points=" + points +
			'}';
	}
}
