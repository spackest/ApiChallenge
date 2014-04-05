package com.apichallenge.common.espn.bbc.entity;


import javax.persistence.*;

@Entity
@Table(name = "BBC_TEAM", uniqueConstraints = {@UniqueConstraint(columnNames = {"NAME"})})
public class BbcTeam extends ParentEntity<BbcTeam> {
	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "SHORT_NAME", nullable = false)
	private String shortName;

	@Column(name = "SCHEDULE_NAME", nullable = false)
	private String scheduleName;

	public BbcTeam() {
	}

	public BbcTeam(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
		this.scheduleName = shortName;
	}

	public BbcTeam(String name, String shortName, String scheduleName) {
		this.name = name;
		this.shortName = shortName;
		this.scheduleName = scheduleName;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	@Override
	public String toString() {
		return "BbcTeam{" +
			"name='" + name + '\'' +
			", shortName='" + shortName + '\'' +
			", scheduleName='" + scheduleName + '\'' +
			'}';
	}
}
