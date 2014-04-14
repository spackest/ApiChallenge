package com.apichallenge.common.espn.bbc;

import java.util.*;

public class DateUtil {
	public static Date getNow() {
		GregorianCalendar calendar = new GregorianCalendar();
		return calendar.getTime();
	}

	public static Date getGameTomorrow() {
		GregorianCalendar calendar = new GregorianCalendar();

		Date tomorrow;

		if (calendar.get(Calendar.HOUR_OF_DAY) != 0) {
			calendar.add(calendar.DAY_OF_MONTH, 1);
		}

		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		tomorrow = calendar.getTime();

		return tomorrow;
	}

	public static int getCurrentHour() {
		GregorianCalendar calendar = new GregorianCalendar();
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public static int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}
}
