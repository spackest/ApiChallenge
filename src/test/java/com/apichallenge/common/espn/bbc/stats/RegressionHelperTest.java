package com.apichallenge.common.espn.bbc.stats;

import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import org.junit.*;

public class RegressionHelperTest {
	@Test
	public void lifeCycleTest() {
		RegressionHelper regressionHelper = new RegressionHelper(null);

		BbcPlayer bbcPlayerOne = new BbcPlayer(new BbcId(1), new EspnId(1), new SlotId(1), 1, "Adam Abraham");
		BbcPlayer bbcPlayerTwo = new BbcPlayer(new BbcId(2), new EspnId(2), new SlotId(1), 1, "Bubba Bubbingham");
		BbcPlayer bbcPlayerThree = new BbcPlayer(new BbcId(3), new EspnId(3), new SlotId(1), 1, "Chopper Choppingthar");
		BbcPlayer bbcPlayerFour = new BbcPlayer(new BbcId(4), new EspnId(4), new SlotId(1), 1, "Farrelly Sparse");

		//RealMatrix coefficients = new Array2DRowRealMatrix(new double[][]{{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}}, false);

		DataColumn dataColumnOne = new DataColumn("column 1");
		dataColumnOne.addStat(bbcPlayerOne, 2, 2);
		dataColumnOne.addStat(bbcPlayerTwo, 3, 3);
		dataColumnOne.addStat(bbcPlayerThree, -2, -2);
		regressionHelper.addDataColumn(dataColumnOne);

		DataColumn todayColumnTwo = new DataColumn("column 2");
		todayColumnTwo.addStat(bbcPlayerOne, -1, -1);
		todayColumnTwo.addStat(bbcPlayerTwo, 7, 7);
		todayColumnTwo.addStat(bbcPlayerThree, 6, 6);
		//todayColumnTwo.addStat(bbcPlayerFour, 6);
		regressionHelper.addDataColumn(todayColumnTwo);

		DataColumn todayColumnThree = new DataColumn("column 3");
		todayColumnThree.addStat(bbcPlayerOne, 4, 4);
		todayColumnThree.addStat(bbcPlayerTwo, -3, -3);
		todayColumnThree.addStat(bbcPlayerThree, -5, -5);
		regressionHelper.addDataColumn(todayColumnThree);

		regressionHelper.setTodayPoint(bbcPlayerOne, 1);
		regressionHelper.setTodayPoint(bbcPlayerTwo, -2);
		regressionHelper.setTodayPoint(bbcPlayerThree, 1);


		PredictedPoints predictedPoints = regressionHelper.getPredictedPoints();
	}
}
