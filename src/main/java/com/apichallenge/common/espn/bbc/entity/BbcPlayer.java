package com.apichallenge.common.espn.bbc.entity;

import com.apichallenge.common.espn.bbc.*;

import javax.persistence.*;

@Entity
@Table(name = "BBC_PLAYER", uniqueConstraints = {@UniqueConstraint(columnNames = {"ESPN_ID"})})
public class BbcPlayer extends ParentEntity<BbcPlayer> {
	@Column(name = "BBC_ID", nullable = true)
	private Integer bbcId;

	@Column(name = "ESPN_ID", nullable = false)
	private Integer espnId;

	@Column(name = "SLOT_ID", nullable = true)
	private Integer slotId;

	@Column(name = "TEAM_ID", nullable = false)
	private long teamId;

	@Column(name = "NAME", nullable = false)
	private String name;

	public BbcPlayer() {
	}

	public BbcPlayer(BbcId bbcId, EspnId espnId, Integer slotId, long teamId, String name) {
		this.bbcId = (bbcId == null) ? null : bbcId.getId();
		this.espnId = espnId.getId();
		this.slotId = slotId;
		this.teamId = teamId;
		this.name = name;
	}

	public void setBbcId(BbcId bbcId) {
		this.bbcId = bbcId.getId();
	}

	public BbcId getBbcId() {
		return new BbcId(bbcId);
	}

	public EspnId getEspnId() {
		return new EspnId(espnId);
	}

	public void setSlotId(Integer slotId) {
		this.slotId = slotId;
	}

	public Integer getSlotId() {
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
