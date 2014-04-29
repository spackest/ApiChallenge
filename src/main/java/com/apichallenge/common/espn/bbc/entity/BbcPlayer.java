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

	public BbcPlayer(BbcId bbcId, EspnId espnId, SlotId slotId, long teamId, String name) {
		this.bbcId = (bbcId == null) ? null : bbcId.getId();
		this.espnId = espnId.getId();
		this.slotId = slotId.getId();
		this.teamId = teamId;
		this.name = name;
	}

	public void setBbcId(BbcId bbcId) {
		this.bbcId = bbcId.getId();
	}

	public BbcId getBbcId() {
		return bbcId == null ? null : new BbcId(bbcId);
	}

	public EspnId getEspnId() {
		return new EspnId(espnId);
	}

	public void setSlotId(SlotId slotId) {
		this.slotId = slotId == null ? null : slotId.getId();
	}

	public SlotId getSlotId() {
		return slotId == null ? null : new SlotId(slotId);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BbcPlayer bbcPlayer = (BbcPlayer) o;

		if (teamId != bbcPlayer.teamId) return false;
		if (bbcId != null ? !bbcId.equals(bbcPlayer.bbcId) : bbcPlayer.bbcId != null) return false;
		if (!espnId.equals(bbcPlayer.espnId)) return false;
		if (!name.equals(bbcPlayer.name)) return false;
		if (slotId != null && !slotId.equals(bbcPlayer.slotId)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = getId() == null ? 1 : super.hashCode();
		result = 31 * result + (bbcId == null ? 0 : bbcId.hashCode());
		result = 31 * result + espnId.hashCode();
		result = 31 * result + (slotId == null ? 0 : slotId.hashCode());
		result = 31 * result + (int) (teamId ^ (teamId >>> 32));
		result = 31 * result + name.hashCode();
		return result;
	}
}
