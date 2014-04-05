package com.apichallenge.common.espn.bbc.entity;

import javax.persistence.*;

@Entity
@Table(name = "BBC_POSITION", uniqueConstraints = {@UniqueConstraint(columnNames = {"SLOT"})})
public class BbcPosition extends ParentEntity<BbcPosition> {
	@Column(name = "SLOT_ID", nullable = false)
	private int slotId;

	@Column(name = "SLOT", nullable = false)
	private String slot;

	@Column(name = "POSITION", nullable = false)
	private String position;

	public BbcPosition() {
	}

	public BbcPosition(int slotId, String slot, String position) {
		this.slotId = slotId;
		this.slot = slot;
		this.position = position;
	}

	public int getSlotId() {
		return slotId;
	}

	public String getSlot() {
		return slot;
	}

	public String getPosition() {
		return position;
	}
}
