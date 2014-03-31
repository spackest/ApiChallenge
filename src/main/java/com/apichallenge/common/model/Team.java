package com.apichallenge.common.model;

public class Team {
	private String name;
	private String shortName;
	private String scheduleName;

	public Team(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
		this.scheduleName = shortName;
	}

	public Team(String name, String shortName, String scheduleName) {
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
}
