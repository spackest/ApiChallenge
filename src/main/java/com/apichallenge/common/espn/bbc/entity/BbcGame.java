package com.apichallenge.common.espn.bbc.entity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "BBC_GAME", uniqueConstraints = {@UniqueConstraint(columnNames = {"DATE", "HOME_TEAM_ID", "AWAY_TEAM_ID", "GAME_NUMBER"})})
public class BbcGame extends ParentEntity<BbcGame> {
	@Column(name = "ESPN_GAME_ID", nullable = true, unique = true)
	private Integer espnGameId;

	@Column(name = "DATE", nullable = false)
	private Date date;

	@Column(name = "YEAR", nullable = false)
	private int year;

	@Column(name = "HOME_TEAM_ID", nullable = false)
	private long homeTeamId;

	@Column(name = "HOME_STARTING_PITCHER_ESPN_ID", nullable = true)
	private Integer homeStartingPitcherEspnId;

	@Column(name = "AWAY_TEAM_ID", nullable = false)
	private long awayTeamId;

	@Column(name = "AWAY_STARTING_PITCHER_ESPN_ID", nullable = true)
	private Integer awayStartingPitcherEspnId;

	@Column(name = "GAME_NUMBER", nullable = false)
	private int gameNumber;

	public BbcGame() {
	}

	public BbcGame(Date date, BbcTeam homeTeam, BbcTeam awayTeam, int gameNumber) {
		espnGameId = null;
		this.date = date;
		this.homeTeamId = homeTeam.getId();
		this.awayTeamId = awayTeam.getId();
		this.gameNumber = gameNumber;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		year = calendar.get(Calendar.YEAR);
	}

	public void setEspnGameId(Integer espnGameId) {
		this.espnGameId = espnGameId;
	}

	public Integer getEspnGameId() {
		return espnGameId;
	}

	public Date getDate() {
		return date;
	}

	public long getHomeTeamId() {


		return homeTeamId;
	}

	public long getAwayTeamId() {
		return awayTeamId;
	}

	public void setHomeStartingPitcherEspnId(Integer homeStartingPitcherEspnId) {
		this.homeStartingPitcherEspnId = homeStartingPitcherEspnId;
	}

	public void setAwayStartingPitcherEspnId(Integer awayStartingPitcherEspnId) {
		this.awayStartingPitcherEspnId = awayStartingPitcherEspnId;
	}
}
