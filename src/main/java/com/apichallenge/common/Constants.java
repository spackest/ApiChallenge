package com.apichallenge.common;

import java.util.*;

public final class Constants {
	public static final int YEAR;
	public static final Date SEASON_MID_POINT;

	static {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		YEAR = calendar.get(Calendar.YEAR);

		calendar.set(YEAR, 6, 15);
		SEASON_MID_POINT = calendar.getTime();
	}
}
