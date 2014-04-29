package com.apichallenge.common.espn.bbc.stats;

import com.apichallenge.common.espn.bbc.entity.*;

import java.util.*;

public class DataColumn {
	private String name;
	private Map<BbcPlayer, Double> todayDataColumn;
	private Map<BbcPlayer, Double> tomorrowDataColumn;
	private Double todayAverage = null;
	private Double tomorrowAverage = null;

	public DataColumn(String name) {
		this.name = name;
		todayDataColumn = new HashMap<BbcPlayer, Double>();
		tomorrowDataColumn = new HashMap<BbcPlayer, Double>();
	}

	public void addStat(BbcPlayer bbcPlayer, double todayNumber, double tomorrowNumber) {
		if (todayDataColumn.containsKey(bbcPlayer)) {
			throw new IllegalArgumentException(bbcPlayer + " is already in todayDataColumn " + todayDataColumn);
		} else {
			todayDataColumn.put(bbcPlayer, todayNumber);
		}

		if (tomorrowDataColumn.containsKey(bbcPlayer)) {
			throw new IllegalArgumentException(bbcPlayer + " is already in todayDataColumn " + tomorrowDataColumn);
		} else {
			tomorrowDataColumn.put(bbcPlayer, tomorrowNumber);
		}
	}

	public double getTodayStat(BbcPlayer bbcPlayer) {
		return todayDataColumn.get(bbcPlayer);
	}

	public double getTomorrowStat(BbcPlayer bbcPlayer) {
		return tomorrowDataColumn.get(bbcPlayer);
	}

	public boolean todayContains(BbcPlayer bbcPlayer) {
		return todayDataColumn.containsKey(bbcPlayer);
	}

	public boolean tomorrowContains(BbcPlayer bbcPlayer) {
		return tomorrowDataColumn.containsKey(bbcPlayer);
	}

	public Set<BbcPlayer> getBbcPlayers() {
		return todayDataColumn.keySet();
	}

	public double getTodayAverage() {
		return getAverage(todayDataColumn, todayAverage);
	}

	public double getTomorrowAverage() {
		return getAverage(tomorrowDataColumn, tomorrowAverage);
	}

	private double getAverage(Map<BbcPlayer, Double> dataMap, Double average) {
		if (average == null) {
			if (dataMap.size() == 0) {
				throw new IllegalStateException("dataMap.size() == 0");
			}

			double total = 0;
			for (double stat : dataMap.values()) {
				total += stat;
			}

			average = total / dataMap.size();
		}

		return average;
	}
}