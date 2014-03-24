package com.apichallenge.common.bbc;

import com.apichallenge.common.model.*;

import java.util.*;

public enum BbcPositionEnum implements PositionInterface {
	CATCHER(1, "C", "Catcher"),
	FIRST_BASE(2, "1B", "First Base"),
	SECOND_BASE(3, "2B", "Second Base"),
	THIRD_BASE(4, "3B", "Third Base"),
	SHORT_STOP(5, "SS", "Shortstop"),
	LEFT_FIELD(6, "LF", "Left Field"),
	CENTER_FIELD(7, "CF", "Center Field"),
	RIGHT_FIELD(8, "RF", "Right Field"),
	DESIGNATED_HITTER(9, "DH", "Designated Hitter"),
	PITCHING_STAFF(10, "PS", "Pitching Staff");

	private static Map<Integer, BbcPositionEnum> POSITION_BY_SLOT_ID = new HashMap<Integer, BbcPositionEnum>();
	private static Map<String, BbcPositionEnum> POSITION_BY_SHORT_NAME = new HashMap<String, BbcPositionEnum>();
	private Position position;

	static {
		for (BbcPositionEnum bbcPositionEnum : BbcPositionEnum.values()) {
			POSITION_BY_SLOT_ID.put(bbcPositionEnum.getSlotId(), bbcPositionEnum);
			POSITION_BY_SHORT_NAME.put(bbcPositionEnum.getShortName(), bbcPositionEnum);
		}
	}

	public static BbcPositionEnum getBbcPositionBySlotId(int slotId) {
		BbcPositionEnum bbcPositionEnum = POSITION_BY_SLOT_ID.get(slotId);

		if (bbcPositionEnum == null) {
			throw new IllegalArgumentException("no position for " + slotId);
		}

		return bbcPositionEnum;
	}

	public static BbcPositionEnum getBbcPositionBySlotShortName(String shortName) {
		BbcPositionEnum bbcPositionEnum = POSITION_BY_SHORT_NAME.get(shortName);

		if (bbcPositionEnum == null) {
			throw new IllegalArgumentException("no position for " + shortName);
		}

		return bbcPositionEnum;
	}

	private BbcPositionEnum(int slotId, String slot, String position) {
		this.position = new Position(slotId, slot, position);
	}

	public int getSlotId() {
		return position.getSlotId();
	}

	public String getShortName() {
		return position.getSlot();
	}
}
