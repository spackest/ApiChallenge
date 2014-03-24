package com.apichallenge.common.bbc;

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
	KANSAS_CITY_ROYALS("Kansas City Royals", "kc"),
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
	SAN_DIEGO_PADRES("San Diego Padres", "sd"),
	SEATTLE_MARINERS("Seattle Mariners", "sea"),
	SAN_FRANCISCO_GIANTS("San Francisco Giants", "sf"),
	ST_LOUIS_CARDINALS("St. Louis Cardinals", "stl"),
	TAMPA_BAY_RAYS("Tampa Bay Rays", "tb"),
	TEXAS_RANGERS("Texas Rangers", "tex"),
	TORONTO_BLUE_JAYS("Toronto Blue Jays", "tor"),
	WASHINGTON_NATIONALS("Washington Nationals", "wsh");

	private static Map<String, BbcTeam> TEAM_BY_SHORT_NAME = new HashMap<String, BbcTeam>();
	private static Map<String, BbcTeam> TEAM_BY_NAME = new HashMap<String, BbcTeam>();

	private String name;
	private String shortName;

	private BbcTeam(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}

	static {
		for (BbcTeam bbcTeam : BbcTeam.values()) {
			TEAM_BY_SHORT_NAME.put(bbcTeam.getShortName(), bbcTeam);
			TEAM_BY_NAME.put(bbcTeam.getName(), bbcTeam);
		}
	}

	public static BbcTeam getTeamByShortName(String shortName) {
		BbcTeam bbcTeam = TEAM_BY_SHORT_NAME.get(shortName.toLowerCase());

		if (bbcTeam == null) {
			throw new IllegalArgumentException("no team for " + shortName);
		}

		return bbcTeam;
	}

	public static BbcTeam getTeamByName(String name) {
		BbcTeam bbcTeam = TEAM_BY_NAME.get(name);

		if (bbcTeam == null) {
			throw new IllegalArgumentException("no team for " + name);
		}

		return bbcTeam;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}
}
