package com.apichallenge.common;

import java.util.*;

public final class Constants {
	public static final int YEAR;

	static {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		YEAR = calendar.get(Calendar.YEAR);
	}
}
