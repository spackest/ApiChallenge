package com.apichallenge.common.espn.bbc.entity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "BBC_BACKTEST_STARTER", uniqueConstraints = {@UniqueConstraint(columnNames = {"BACKTEST_ID", "DATE", "SLOT_ID"})})
public class BbcBacktestStarter extends ParentEntity<BbcBacktestStarter> {
	@Column(name = "BACKTEST_ID", nullable = false)
	private long backtestId;

	@Column(name = "DATE", nullable = false)
	private Date date;

	@Column(name = "SLOT_ID", nullable = false)
	private int slotId;

	@Column(name = "ESPN_ID", nullable = false)
	private int espnId;

	@Column(name = "POINTS", nullable = false)
	private int points;

	public BbcBacktestStarter(long backtestId, Date date, int slotId, int espnId, int points) {
		this.backtestId = backtestId;
		this.date = date;
		this.slotId = slotId;
		this.espnId = espnId;
		this.points = points;
	}
}