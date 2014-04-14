package com.apichallenge.common.espn.bbc.entity;


import com.apichallenge.common.espn.bbc.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity
@Table(name = "BBC_POINTS", uniqueConstraints = {@UniqueConstraint(columnNames = {"ESPN_GAME_ID", "ESPN_ID"})})
public class BbcPoints extends ParentEntity<BbcPoints> {
	@Index(name = "BBC_POINTS_ESPN_GAME_ID_DATE", columnNames = {"ESPN_GAME_ID", "YEAR", "DATE"})

	@Column(name = "YEAR", nullable = false)
	private int year;

	@Column(name = "DATE", nullable = false)
	private Date date;

	@Column(name = "ESPN_GAME_ID", nullable = false)
	private Integer espnGameId;

	@Column(name = "TEAM_ID", nullable = false)
	private long teamId;

	@Column(name = "OPPONENT_ID", nullable = false)
	private long opponentId;

	@Column(name = "HOME_GAME", nullable = false)
	private boolean homeGame;

	@Column(name = "ESPN_ID", nullable = false)
	private int espnId;

	@Column(name = "SLOT_ID", nullable = true)
	private Integer slotId;

	@Column(name = "INCOMING_GAMES", nullable = true)
	private Integer incomingGames;

	@Column(name = "INCOMING_TOTAL_POINTS", nullable = true)
	private Integer incomingTotalPoints;

	@Column(name = "INCOMING_AVERAGE_POINTS", nullable = true)
	private Float incomingAveragePoints;

	@Column(name = "POINTS", nullable = true)
	private Integer points;

	public BbcPoints() {
	}

	public BbcPoints(Date date, long teamId, long opponentId, boolean homeGame, EspnId espnId, Integer slotId) {
		this.teamId = teamId;
		this.opponentId = opponentId;
		this.homeGame = homeGame;
		this.espnId = espnId.getId();
		this.slotId = slotId;
	}


	public BbcPoints(Date date, EspnGameId espnGameId, long teamId, long opponentId, boolean homeGame, EspnId espnId, SlotId slotId, Integer points) {
		this.date = date;
		year = DateUtil.getYear(date);
		this.espnGameId = espnGameId.getId();
		this.teamId = teamId;
		this.opponentId = opponentId;
		this.homeGame = homeGame;
		this.espnId = espnId.getId();
		this.slotId = slotId.getId();
		this.points = points;
		incomingTotalPoints = null;
		incomingAveragePoints = null;
	}

	public void setIncomingGames(Integer incomingGames) {
		this.incomingGames = incomingGames;
	}

	public void setIncomingTotalPoints(Integer incomingTotalPoints) {
		this.incomingTotalPoints = incomingTotalPoints;
	}

	public void setIncomingAveragePoints(float incomingAveragePoints) {
		this.incomingAveragePoints = incomingAveragePoints;
	}

	public Integer getPoints() {
		return points;
	}

	public EspnId getEspnId() {
		return new EspnId(espnId);
	}

	public Integer getIncomingTotalPoints() {
		return incomingTotalPoints;
	}

	public Float getIncomingAveragePoints() {
		return incomingAveragePoints;
	}

	public long getTeamId() {
		return teamId;
	}

	public long getOpponentId() {
		return opponentId;
	}

	public int getYear() {
		return year;
	}

	public Date getDate() {
		return date;
	}

	public EspnGameId getEspnGameId() {
		return espnGameId == null ? null : new EspnGameId(espnGameId);
	}

	public boolean isHomeGame() {
		return homeGame;
	}

	public SlotId getSlotId() {
		return slotId == null ? null : new SlotId(slotId);
	}

	public Integer getIncomingGames() {
		return incomingGames;
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
