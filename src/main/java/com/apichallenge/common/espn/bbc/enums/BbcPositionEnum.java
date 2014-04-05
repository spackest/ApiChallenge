package com.apichallenge.common.espn.bbc.enums;

import com.apichallenge.common.espn.bbc.entity.*;

import java.util.*;

public enum BbcPositionEnum {
	CATCHER(1, "C", "Catcher"),
	FIRST_BASE(2, "1B", "First Base"),
	SECOND_BASE(3, "2B", "Second Base"),
	THIRD_BASE(4, "3B", "Third Base"),
	SHORT_STOP(5, "SS", "Shortstop"),
	LEFT_FIELD(6, "LF", "Left Field"),
	CENTER_FIELD(7, "CF", "Center Field"),
	RIGHT_FIELD(8, "RF", "Right Field"),
	DESIGNATED_HITTER(9, "DH", "Designated Hitter"),
	PITCHER(10, "P", "Pitcher"),
	PITCHING_STAFF(10, "PS", "Pitching Staff"),
	PINCH_HITTER(0, "PH", "Pinch Hitter"),
	PINCH_RUNNER(0, "PR", "Pinch Runner");

	private static Map<Integer, BbcPositionEnum> POSITION_BY_SLOT_ID = new HashMap<Integer, BbcPositionEnum>();
	private static Map<String, BbcPositionEnum> POSITION_BY_SHORT_NAME = new HashMap<String, BbcPositionEnum>();
	private BbcPosition bbcPosition;

	static {
		List<BbcPosition> bbcPositions = new ArrayList<BbcPosition>();

		for (BbcPositionEnum bbcPositionEnum : BbcPositionEnum.values()) {
			POSITION_BY_SLOT_ID.put(bbcPositionEnum.getSlotId(), bbcPositionEnum);
			POSITION_BY_SHORT_NAME.put(bbcPositionEnum.getShortName(), bbcPositionEnum);

			bbcPositions.add(bbcPositionEnum.bbcPosition);
		}
	}

	public static BbcPositionEnum getBbcPositionBySlotId(int slotId) {
		BbcPositionEnum bbcPositionEnum = POSITION_BY_SLOT_ID.get(slotId);

		if (bbcPositionEnum == null) {
			throw new IllegalArgumentException("no bbcPosition for " + slotId);
		}

		return bbcPositionEnum;
	}

	public static BbcPositionEnum getBbcPositionBySlotShortName(String shortName) {
		BbcPositionEnum bbcPositionEnum = POSITION_BY_SHORT_NAME.get(shortName);

		if (bbcPositionEnum == null) {
			throw new IllegalArgumentException("no bbcPosition for " + shortName);
		}

		return bbcPositionEnum;
	}

	private BbcPositionEnum(int slotId, String slot, String bbcPosition) {
		this.bbcPosition = new BbcPosition(slotId, slot, bbcPosition);
	}

	public int getSlotId() {
		return bbcPosition.getSlotId();
	}

	public String getShortName() {
		return bbcPosition.getSlot();
	}
}
