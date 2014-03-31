package com.apichallenge.common.bbc;

import com.apichallenge.common.model.*;

import java.util.*;

public enum BbcTeam {
	ARIZONA_DIAMONDBACKS("Arizona Diamondbacks", "ari"),
	ATLANTA_BRAVES("Atlanta Braves", "atl"),
	BALTIMORE_ORIOLES("Baltimore Orioles", "bal"),
	BOSTON_REDSOX("Boston Red Sox", "bos"),
	CHICAGO_CUBS("Chicago Cubs", "chc"),
	CHICAGO_WHITE_SOX("Chicago White Sox", "chw"),
	CINCINNATI_REDS("Cincinnati Reds", "cin"),
	CLEVELAND_INDIANS("Cleveland Indians", "cle"),
	COLORADO_ROCKIES("Colorado Rockies", "col"),
	DETROIT_TIGERS("Detroit Tigers", "det"),
	HOUSTON_ASTROS("Houston Astros", "hou"),
	KANSAS_CITY_ROYALS("Kansas City Royals", "kc", "kan"),
	LOS_ANGELES_ANGELS("Los Angeles Angels", "laa"),
	LOS_ANGELES_DODGERS("Los Angeles Dodgers", "lad"),
	MIAMI_MARLINS("Miami Marlins", "mia"),
	MILWAUKEE_BREWERS("Milwaukee Brewers", "mil"),
	MINNESOTA_TWINS("Minnesota Twins", "min"),
	NEW_YORK_METS("New York Mets", "nym"),
	NEW_YORK_YANKEES("New York Yankees", "nyy"),
	OAKLAND_ATHLETICS("Oakland Athletics", "oak"),
	PHILADELPHIA_PHILLIES("Philadelphia Phillies", "phi"),
	PITTSBURG_PIRATES("Pittsburgh Pirates", "pit"),
	SAN_DIEGO_PADRES("San Diego Padres", "sd", "sdg"),
	SEATTLE_MARINERS("Seattle Mariners", "sea"),
	SAN_FRANCISCO_GIANTS("San Francisco Giants", "sf", "sfo"),
	ST_LOUIS_CARDINALS("St. Louis Cardinals", "stl"),
	TAMPA_BAY_RAYS("Tampa Bay Rays", "tb", "tam"),
	TEXAS_RANGERS("Texas Rangers", "tex"),
	TORONTO_BLUE_JAYS("Toronto Blue Jays", "tor"),
	WASHINGTON_NATIONALS("Washington Nationals", "wsh", "was");

	private static Map<String, BbcTeam> TEAM_BY_SHORT_NAME = new HashMap<String, BbcTeam>();
	private static Map<String, BbcTeam> TEAM_BY_SCHEDULE_NAME = new HashMap<String, BbcTeam>();
	private static Map<String, BbcTeam> TEAM_BY_NAME = new HashMap<String, BbcTeam>();

	private Team team;

	private BbcTeam(String name, String shortName) {
		team = new Team(name, shortName);
	}

	private BbcTeam(String name, String shortName, String scheduleName) {
		team = new Team(name, shortName, scheduleName);
	}

	public Team getTeam() {
		return team;
	}

	static {
		for (BbcTeam bbcTeam : BbcTeam.values()) {
			TEAM_BY_SHORT_NAME.put(bbcTeam.getTeam().getShortName(), bbcTeam);
			TEAM_BY_NAME.put(bbcTeam.getTeam().getName(), bbcTeam);
			TEAM_BY_SCHEDULE_NAME.put(bbcTeam.getTeam().getScheduleName(), bbcTeam);
		}
	}

	public static Team getTeam(String name) {
		BbcTeam bbcTeam = null;

		if (TEAM_BY_SHORT_NAME.containsKey(name.toLowerCase())) {
			bbcTeam = TEAM_BY_SHORT_NAME.get(name.toLowerCase());
		} else if (TEAM_BY_SCHEDULE_NAME.containsKey(name.toLowerCase())) {
			bbcTeam = TEAM_BY_SCHEDULE_NAME.get(name.toLowerCase());
		} else if (TEAM_BY_NAME.containsKey(name)) {
			bbcTeam = TEAM_BY_NAME.get(name);
		}

		if (bbcTeam == null) {
			throw new IllegalArgumentException("no team for " + name);
		}

		return bbcTeam.getTeam();
	}
}
