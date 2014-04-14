package com.apichallenge.common.espn.bbc;

public class SlotId {
	private Integer slotId;

	public SlotId(Integer slotId) {
		this.slotId = slotId;
	}

	public Integer getId() {
		return slotId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SlotId slotId1 = (SlotId) o;

		if (slotId != null ? !slotId.equals(slotId1.slotId) : slotId1.slotId != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return slotId != null ? slotId.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "SlotId{" +
			"slotId=" + slotId +
			'}';
	}
}
