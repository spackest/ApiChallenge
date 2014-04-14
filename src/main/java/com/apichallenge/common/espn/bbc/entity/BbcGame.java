package com.apichallenge.common.espn.bbc.entity;

import com.apichallenge.common.espn.bbc.*;

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

	public BbcGame(BbcTeam homeTeam, BbcTeam awayTeam, Integer homeStartingPitcherEspnId, Integer awayStartingPitcherEspnId, int gameNumber) {
		this.homeTeamId = homeTeam.getId();
		this.awayTeamId = awayTeam.getId();
		this.homeStartingPitcherEspnId = homeStartingPitcherEspnId;
		this.awayStartingPitcherEspnId = awayStartingPitcherEspnId;
		this.gameNumber = gameNumber;
	}

	public BbcGame(Date date, EspnGameId espnGameId, BbcTeam homeTeam, BbcTeam awayTeam, EspnId homeStartingPitcherEspnId, EspnId awayStartingPitcherEspnId, int gameNumber) {
		year = DateUtil.getYear(date);
		this.date = date;
		this.espnGameId = espnGameId == null ? null : espnGameId.getId();
		this.homeTeamId = homeTeam.getId();
		this.awayTeamId = awayTeam.getId();
		this.homeStartingPitcherEspnId = homeStartingPitcherEspnId == null ? null : homeStartingPitcherEspnId.getId();
		this.awayStartingPitcherEspnId = awayStartingPitcherEspnId == null ? null : awayStartingPitcherEspnId.getId();
		this.gameNumber = gameNumber;
	}

	public EspnGameId getEspnGameId() {
		return espnGameId == null ? null : new EspnGameId(espnGameId);
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

	public int getGameNumber() {
		return gameNumber;
	}

	public void setEspnGameId(EspnGameId espnGameId) {
		this.espnGameId = espnGameId == null ? null : espnGameId.getId();
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void setHomeTeamId(long homeTeamId) {
		this.homeTeamId = homeTeamId;
	}

	public void setHomeStartingPitcherEspnId(Integer homeStartingPitcherEspnId) {
		this.homeStartingPitcherEspnId = homeStartingPitcherEspnId;
	}

	public void setAwayTeamId(long awayTeamId) {
		this.awayTeamId = awayTeamId;
	}

	public void setAwayStartingPitcherEspnId(Integer awayStartingPitcherEspnId) {
		this.awayStartingPitcherEspnId = awayStartingPitcherEspnId;
	}

	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public Integer getHomeStartingPitcherEspnId() {
		return homeStartingPitcherEspnId;
	}

	public Integer getAwayStartingPitcherEspnId() {
		return awayStartingPitcherEspnId;
	}
}
