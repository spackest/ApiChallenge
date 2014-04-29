package com.apichallenge.common.espn.bbc.stats;

import com.apichallenge.common.espn.bbc.entity.*;

import java.util.*;

public class PredictedPoints {
	private Map<BbcPlayer, Double> predictedPointsMap = new HashMap<BbcPlayer, Double>();

	public void addPredictedPoints(BbcPlayer bbcPlayer, Double predictedPoints) {
		if (predictedPointsMap.containsKey(bbcPlayer)) {
			throw new IllegalArgumentException("predicted points already exist for " + bbcPlayer);
		} else {
			predictedPointsMap.put(bbcPlayer, predictedPoints);
		}
	}

	public BbcPlayer getBestPredictedPlayer() {
		BbcPlayer bbcPlayer = null;
		Double maxPoints = null;

		for (Map.Entry<BbcPlayer, Double> entry : predictedPointsMap.entrySet()) {
			BbcPlayer thisBbcPlayer = entry.getKey();
			double thesePoints = entry.getValue();

			if (bbcPlayer == null || thesePoints > maxPoints) {
				bbcPlayer = thisBbcPlayer;
				maxPoints = thesePoints;
			}
		}

		return bbcPlayer;
	}
}
