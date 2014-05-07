package com.apichallenge.common.espn.bbc.entity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "BBC_BACKTEST_DAY", uniqueConstraints = {@UniqueConstraint(columnNames = {"BACKTEST_ID", "GAME_DATE"})})
public class BbcBacktestDay extends ParentEntity<BbcBacktestDay> {
	@Column(name = "BACKTEST_ID", nullable = false)
	private long backtestId;

	@Column(name = "GAME_DATE", nullable = false)
	private Date gameDate;

	@Column(name = "POINTS", nullable = false)
	private int points;

	public BbcBacktestDay(long backtestId, Date gameDate, int points) {
		this.backtestId = backtestId;
		this.gameDate = gameDate;
		this.points = points;
	}
}