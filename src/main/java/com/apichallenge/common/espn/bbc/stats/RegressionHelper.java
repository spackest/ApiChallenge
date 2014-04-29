package com.apichallenge.common.espn.bbc.stats;

import Jama.*;
import com.apichallenge.common.espn.bbc.entity.*;

import java.util.*;

public class RegressionHelper {
	private List<DataColumn> dataColumns = new ArrayList<DataColumn>();
	private Map<BbcPlayer, Double> todayPoints = new HashMap<BbcPlayer, Double>();
	private Matrix todayMatrix = null;
	private Matrix tomorrowMatrix = null;

	public RegressionHelper() {
	}

	public void addDataColumn(DataColumn dataColumn) {
		dataColumns.add(dataColumn);
	}

	public void addDataColumns(Collection<DataColumn> dataColumns) {
		this.dataColumns.addAll(dataColumns);
	}

	public void setTodayPoint(BbcPlayer bbcPlayer, double points) {
		if (todayPoints.containsKey(bbcPlayer)) {
			throw new IllegalStateException("points already exist for " + bbcPlayer);
		}

		todayPoints.put(bbcPlayer, points);
	}

	public PredictedPoints getPredictedPoints() {
		if (todayPoints == null) {
			throw new IllegalStateException("todayPointsMatrix is null");
		}

		if (dataColumns.size() == 0) {
			throw new IllegalStateException("dataColumns.size() == 0");
		}

		List<BbcPlayer> playerList = formPlayersList();
		PredictedPoints predictedPoints = new PredictedPoints();
		try {
			formMatrices(playerList);

			Matrix todayPointsMatrix = formTodayPoints(playerList);

			Matrix coefficients = todayMatrix.solve(todayPointsMatrix);

			Matrix predictedPointsMatrix = tomorrowMatrix.times(coefficients);

			for (int i = 0; i < playerList.size(); i++) {
				BbcPlayer bbcPlayer = playerList.get(i);
				double points = predictedPointsMatrix.get(i, 0);
				predictedPoints.addPredictedPoints(bbcPlayer, points);
			}
		} catch (Exception e) {
			System.out.println("lame predicted points because of " + e.getMessage());

			for (BbcPlayer bbcPlayer : playerList) {
				predictedPoints.addPredictedPoints(bbcPlayer, todayPoints.get(bbcPlayer));
			}
		}

		return predictedPoints;
	}

	private double getTodayStat(DataColumn dataColumn, BbcPlayer bbcPlayer) {
		return (dataColumn.todayContains(bbcPlayer)) ? dataColumn.getTodayStat(bbcPlayer) : dataColumn.getTodayAverage();
	}

	private double getTomorrowStat(DataColumn dataColumn, BbcPlayer bbcPlayer) {
		return (dataColumn.tomorrowContains(bbcPlayer)) ? dataColumn.getTomorrowStat(bbcPlayer) : dataColumn.getTomorrowAverage();
	}

	private void formMatrices(List<BbcPlayer> playerList) {
		double[][] rawTodayMatrix = new double[playerList.size()][dataColumns.size()];
		double[][] rawTomorrowMatrix = new double[playerList.size()][dataColumns.size()];

		for (int i = 0; i < playerList.size(); i++) {
			BbcPlayer bbcPlayer = playerList.get(i);

			for (int j = 0; j < dataColumns.size(); j++) {
				rawTodayMatrix[i][j] = getTodayStat(dataColumns.get(j), bbcPlayer);
				rawTomorrowMatrix[i][j] = getTomorrowStat(dataColumns.get(j), bbcPlayer);
			}
		}

		todayMatrix = new Matrix(rawTodayMatrix);
		tomorrowMatrix = new Matrix(rawTomorrowMatrix);
	}

	private Matrix formTodayPoints(List<BbcPlayer> playerList) {
		double[][] rawTodayPoints = new double[playerList.size()][1];

		for (int i = 0; i < playerList.size(); i++) {
			rawTodayPoints[i][0] = todayPoints.containsKey(playerList.get(i)) ? todayPoints.get(playerList.get(i)) : 0;
		}

		return new Matrix(rawTodayPoints);
	}


	private List<BbcPlayer> formPlayersList() {
		List<BbcPlayer> playerList = new ArrayList<BbcPlayer>();

		for (DataColumn dataColumn : dataColumns) {
			for (BbcPlayer bbcPlayer : dataColumn.getBbcPlayers()) {
				if (!playerList.contains(bbcPlayer)) {
					playerList.add(bbcPlayer);
				}
			}
		}

		return playerList;
	}
}