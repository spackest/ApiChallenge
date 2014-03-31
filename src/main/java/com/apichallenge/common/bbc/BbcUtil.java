package com.apichallenge.common.bbc;

import com.apichallenge.common.*;
import com.apichallenge.common.model.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public final class BbcUtil {
	private static final String BBC_FRONT_PAGE = String.format("http://games.espn.go.com/baseball-challenge/%d/en/", Constants.YEAR);
	private static final String SET_ROSTER_URL = BBC_FRONT_PAGE + "util/api/setRoster";

	private static final Pattern SLOT_ID_PATTERN = Pattern.compile("slotID=(\\d+)");
	private static final Pattern SPID_ID_PATTERN = Pattern.compile("spid=(\\d+)");
	private static final Pattern SCHEDULE_NAME_PATTERN = Pattern.compile("team=(\\w+)");
	private static final Pattern MONTH_DAY_PATTERN = Pattern.compile("[A-Z]\\w+,\\s*([A-Z]\\w+)\\s+(\\d+)");
	private static final Pattern SCHEDULE_NAME_PATTERN_TWO = Pattern.compile("/team/_/name/(\\w+)/");
	private static final Pattern ESPN_GAME_ID_PATTERN = Pattern.compile("recap.id=(\\d+)");
	private static final Pattern POSITION_PLAYER_BOX_PATTERN = Pattern.compile("/id/(\\d+)/[^>]+>([^<]+)</a>\\s*(\\w+)[^<]*(?:</div>)?</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*");
	private static final Pattern PITCHER_BOX_PATTERN = Pattern.compile("/id/(\\d+)/[^>]+>([^<]+)</a>.*?\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*");
	private static final Pattern EXTRA_BOX_PATTERN = Pattern.compile("<strong>(2B|3B|HR|SB):</strong>(.*?)<", Pattern.DOTALL);
	private static final Pattern CHUNK_BOX_PATTERN = Pattern.compile("([^(]+)\\s+\\((\\d+)");

	private static final Map<String, Integer> MONTHS = new HashMap<String, Integer>();

	static {
		MONTHS.put("Jan", 0);
		MONTHS.put("Feb", 1);
		MONTHS.put("Mar", 2);
		MONTHS.put("Apr", 3);
		MONTHS.put("May", 4);
		MONTHS.put("Jun", 5);
		MONTHS.put("Jul", 6);
		MONTHS.put("Aug", 7);
		MONTHS.put("Sep", 8);
		MONTHS.put("Oct", 9);
		MONTHS.put("Nov", 10);
		MONTHS.put("Dec", 11);
	}

	public static MyConnection login(String username, String password) throws Exception {
		MyConnection myConnection = EspnUtil.login(username, password, BBC_FRONT_PAGE, "espn_bbc");
		return myConnection;
	}

	public static Starters getStarters(MyConnection myConnection, String entryName) throws IOException {
		Starters starters = new Starters();

		EspnEntry espnEntry = EspnUtil.getEspnEntry(myConnection, entryName);

		String url = BBC_FRONT_PAGE + "entry?entryID=" + espnEntry.getId();
		Connection.Response response = myConnection.get(url, null);
		Document frontDocument = response.parse();

		String leagueUrl = null;

		int count = 0;

		for (Element element : frontDocument.select("[class$=available")) {
			for (Element thisElement : element.select("[class$=ajaxify")) {
				leagueUrl = thisElement.attr("abs:href");

				if (++count > 1) {
					leagueUrl.toString();
				}
			}
		}

		response = myConnection.get(leagueUrl, null);
		Document document = response.parse();

		Elements playerTable = document.select("[class*=player-table]");

		for (Element positionElement : playerTable.select("[id^=plyr]")) {
			Player player = getPlayingPlayerFromElement(positionElement);

			BbcPositionEnum bbcPositionEnum = null;

			for (Element slotElement : positionElement.select("[class*=games-left]")) {
				String slotShortName = slotElement.text();

				if (slotShortName == null || slotShortName.length() > 2) {
					continue;
				}

				bbcPositionEnum = BbcPositionEnum.getBbcPositionBySlotShortName(slotShortName);
			}

			starters.addStarter(bbcPositionEnum, player);
		}

		return starters;
	}

	public static League getLeague(MyConnection myConnection, String entryName) throws IOException {
		EspnEntry espnEntry = EspnUtil.getEspnEntry(myConnection, entryName);

		Connection.Response response = myConnection.get(espnEntry.getUrl(), null);

		Document frontDocument = response.parse();

		String leagueUrl = null;

		int count = 0;

		for (Element element : frontDocument.select("[class$=available")) {
			for (Element thisElement : element.select("[class$=ajaxify")) {
				leagueUrl = thisElement.attr("abs:href");

				if (++count > 1) {
					leagueUrl.toString();
				}
			}
		}

		response = myConnection.get(leagueUrl, null);

		Document leagueDocument = response.parse();

		int slots = 0;

		League league = new League(espnEntry);

		Integer spid = null;

		for (Element leagueElement : leagueDocument.select("[id^=edit_slot]")) {
			String url = leagueElement.attr("abs:href");

			response = myConnection.get(url, null);

			Document positionDocument = response.parse();

			Elements tableElements = positionDocument.select("[id^=playertablebody]");

			assert (tableElements.size() == 1);

			List<Player> players = new ArrayList<Player>();

			Integer slotId = null;

			Matcher matcher = SLOT_ID_PATTERN.matcher(url);
			if (matcher.find()) {
				slotId = Integer.valueOf(matcher.group(1));
			}

			if (slotId == null) {
				throw new IllegalArgumentException("no slotId for " + url);
			}

			if (spid == null) {
				matcher = SPID_ID_PATTERN.matcher(url);
				if (matcher.find()) {
					spid = Integer.valueOf(matcher.group(1));
				}

				if (spid == null) {
					throw new IllegalArgumentException("no spid for " + url);
				}
			}

			BbcPositionEnum position = BbcPositionEnum.getBbcPositionBySlotId(slotId);

			for (Element positionElement : tableElements.select("[id^=plyr]")) {
				Player player = getPlayingPlayerFromElement(positionElement);
				if (player == null) {
					continue;
				}

				players.add(player);
			}

			league.addLeagueSlot(new LeagueSlot(position, players));
			slots++;
		}

		if (spid == null) {
			throw new IllegalArgumentException("no spid, darn it");
		}

		myConnection.setSpid(spid);

		return league;
	}

	public static Starters tradeForStarters(MyConnection myConnection, EspnEntry espnEntry, Starters starters) throws IOException {
		Map<String, String> form = new HashMap<String, String>();
		for (Map.Entry<BbcPositionEnum, Player> entry : starters.getEntrySet()) {
			Player player = entry.getValue();

			System.out.println("starting " + player.getName());

			form.put("playerInSlot_" + entry.getKey().getSlotId(), String.valueOf(player.getId()));
		}
		form.put("entryID", String.valueOf(espnEntry.getId()));
		form.put("redirect", BBC_FRONT_PAGE + "/entry?entryID=" + espnEntry.getId());
		form.put("creationSource", String.valueOf(1));
		form.put("teams", String.valueOf(-1));
		form.put("spid", String.valueOf(myConnection.getSpid()));

		myConnection.post(SET_ROSTER_URL, form);

		return getStarters(myConnection, espnEntry.getName());
	}


	private static Player getPlayingPlayerFromElement(Element element) {
		int id = Integer.valueOf(element.attr("player_id").toString());
		String name = element.select("[id=pFN]").text() + " " + element.select("[id=pLN]").text();
		name = name.trim();
		int rank = Integer.valueOf(element.select("[class*=st-frnk]").text());
		float average = Float.valueOf(element.select("[class*=st-favg]").text());
		int points = Integer.valueOf(element.select("[class*=st-fpts]").text());

		Elements injuryStatuses = element.select("[class=injury-status]");

		// skip injured folks
		if (injuryStatuses.size() > 0) {
			return null;
		}

		Team team = null;

		String teamShortName = element.select("[class=player_team]").text();
		String opponentShortName = element.select("[class=player_opp]").text();

		if (teamShortName == null || teamShortName.length() == 0) {
			team = BbcTeam.getTeam(name);
		} else {
			team = BbcTeam.getTeam(teamShortName);
		}

		if (team == null) {
			throw new IllegalArgumentException("no team from '" + element.toString() + "'");
		}

		// skip folks that aren't playing
		if (opponentShortName == null || opponentShortName.length() == 0 || opponentShortName.equals("--")) {
			return null;
		}

		return new Player(id, name, team, opponentShortName, rank, average, points);
	}

	private static List<Team> getTeams() {
		String html = SimpleHttpCache.getHtmlFromUrl("http://espn.go.com/mlb/schedule");

		List<Team> teams = new ArrayList<Team>();

		Document document = Jsoup.parse(html);
		Elements options = document.select("select > option");
		for (Element option : options) {
			String value = option.attr("value");
			if (value == null || value.length() == 0 || value.equals("#")) {
				continue;
			}

			String scheduleName = null;

			Matcher matcher = SCHEDULE_NAME_PATTERN.matcher(value);
			if (matcher.find()) {
				scheduleName = matcher.group(1);
			}

			if (scheduleName == null) {
				throw new IllegalArgumentException("no schedule name for " + value);
			}

			Team bbcTeam = BbcTeam.getTeam(scheduleName);

			if (bbcTeam == null) {
				throw new IllegalArgumentException("no bbcTeam for " + scheduleName);
			}

			teams.add(bbcTeam);

		}

		return teams;
	}

	public static void slurpSchedule(int year) {
		List<Team> teams = getTeams();

		for (Team team : teams) {
			List<Game> games = new ArrayList<Game>();

			for (int half : Arrays.asList(1, 2)) {
				String url = String.format("http://espn.go.com/mlb/team/schedule/_/name/%s/year/%d/seasontype/2/half/%d/", team.getScheduleName(), year, half);
				String html = SimpleHttpCache.getHtmlFromUrl(url);

				Document document = Jsoup.parse(html);

				for (Element element : document.select("[class*=row team]")) {
					String row = element.toString();

					if (row.contains("POSTPONED")) {
						continue;
					}

					Date date = null;

					Matcher matcher = MONTH_DAY_PATTERN.matcher(row);
					if (matcher.find()) {
						int month = MONTHS.get(matcher.group(1));
						int day = Integer.valueOf(matcher.group(2));

						date = new Date(year, month, day);
					}

					String gameStatus = element.select("[class=game-status]").text();

					Team homeTeam;
					Team awayTeam;

					Team opponentTeam = null;

					matcher = SCHEDULE_NAME_PATTERN_TWO.matcher(row);
					if (matcher.find()) {
						String opponentName = matcher.group(1);
						opponentTeam = BbcTeam.getTeam(opponentName);
					}

					if (opponentTeam == null) {
						throw new IllegalArgumentException("no opponent team for '" + row + "'");
					}

					if (gameStatus.equals("vs")) {
						homeTeam = team;
						awayTeam = opponentTeam;
					} else if (gameStatus.equals("@")) {
						homeTeam = opponentTeam;
						awayTeam = team;
					} else {
						throw new IllegalArgumentException("no game status for '" + row + "'");
					}

					Integer espnGameId = null;

					matcher = ESPN_GAME_ID_PATTERN.matcher(element.select("[class=score]").toString());
					if (matcher.find()) {
						espnGameId = Integer.valueOf(matcher.group(1));
						handleEspnGameId(espnGameId);
					}

					Game game = new Game(homeTeam, awayTeam, date, espnGameId);
					games.add(game);
				}
			}

			System.out.println(team.getName() + " - " + games.size());
		}
	}

	private static void handleEspnGameId(int gameId) {
		String url = "http://espn.go.com/mlb/boxscore?gameId=" + gameId;

		String html = SimpleHttpCache.getHtmlFromUrl(url);

		Document document = Jsoup.parse(html);

		for (Element playerElement : document.select("[class*=player]")) {
			Matcher matcher = POSITION_PLAYER_BOX_PATTERN.matcher(playerElement.toString());

			if (matcher.find()) {
				System.out.println("position player");
				for (int i = 1; i <= matcher.groupCount(); i++) {
					String match = matcher.group(i);
					System.out.println(i + " - " + match);
				}
				continue;
			}

			matcher = PITCHER_BOX_PATTERN.matcher(playerElement.toString());
			if (matcher.find()) {
				System.out.println("pitcher");
				for (int i = 1; i <= matcher.groupCount(); i++) {
					String match = matcher.group(i);
					System.out.println(i + " - " + match);
				}

				continue;
			}

			playerElement.toString();
			System.out.println();
		}

		Matcher matcher = EXTRA_BOX_PATTERN.matcher(html);

		while (matcher.find()) {
			System.out.println("extra");

			String type = matcher.group(1).toUpperCase();
			String rest = matcher.group(2);

			for (String chunk : rest.split("\\s*;\\s*")) {
				Matcher chunkMatcher = CHUNK_BOX_PATTERN.matcher(chunk);

				while (chunkMatcher.find()) {
					System.out.println("chunk");
					for (int i = 1; i <= chunkMatcher.groupCount(); i++) {
						String match = chunkMatcher.group(i);
						System.out.println(i + " - " + match);
					}

					//if ($chunk = ~m @([^(]+)\s + (\d *)\s *\(@) {
					//	my($name, $count) = ($1, $2);
					//$count || = 1;
					//$players -> {$names -> {$name}} {points}+=$count * $multiplier -> {$type};
				}
			}
		}
	}
}
