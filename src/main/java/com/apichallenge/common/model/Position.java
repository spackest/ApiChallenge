package com.apichallenge.common.model;

public class Position {
	private int slotId;
	private String slot;
	private String position;

	public Position(int slotId, String slot, String position) {
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
