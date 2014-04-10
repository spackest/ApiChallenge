package com.apichallenge.common.espn.bbc.entity;

import com.apichallenge.common.espn.bbc.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "BBC_BACKTEST", uniqueConstraints = {@UniqueConstraint(columnNames = {"STRATEGY", "YEAR", "START_DATE"})})
public class BbcBacktest extends ParentEntity<BbcBacktest> {
	@Column(name = "STRATEGY", nullable = false)
	private String strategy;

	@Column(name = "YEAR", nullable = false)
	private int year;

	@Column(name = "START_DATE", nullable = false)
	private Date startDate;

	@Column(name = "FINISH_DATE", nullable = true)
	private Date finishDate;

	@Column(name = "POINTS", nullable = true)
	private Integer points;

	public BbcBacktest() {
	}

	public BbcBacktest(int year, Strategy strategy) {
		this.strategy = strategy.getName();
		this.year = year;
		startDate = DateUtil.getNow();
		finishDate = null;
		points = null;
	}

	public int getYear() {
		return year;
	}

	public void setPoints(int points) {
		this.points = points;
		finishDate = DateUtil.getNow();
	}
}
