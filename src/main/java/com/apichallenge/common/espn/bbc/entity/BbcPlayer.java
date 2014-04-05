package com.apichallenge.common.espn.bbc.entity;

import javax.persistence.*;

@Entity
@Table(name = "BBC_PLAYER", uniqueConstraints = {@UniqueConstraint(columnNames = {"ESPN_ID"})})
public class BbcPlayer extends ParentEntity<BbcPlayer> {
	@Column(name = "ESPN_ID", nullable = false)
	private int espnId;

	@Column(name = "SLOT_ID", nullable = false)
	private int slotId;

	@Column(name = "TEAM_ID", nullable = false)
	private long teamId;

	@Column(name = "NAME", nullable = false)
	private String name;

	public BbcPlayer() {
	}

	public BbcPlayer(int espnId, int slotId, long teamId, String name) {
		this.espnId = espnId;
		this.slotId = slotId;
		this.teamId = teamId;
		this.name = name;
	}

	public int getEspnId() {
		return espnId;
	}

	public int getSlotId() {
		return slotId;
	}

	public long getTeamId() {
		return teamId;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + ", slotId=" + slotId;
	}
}
