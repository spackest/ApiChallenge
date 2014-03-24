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

	public static MyConnection login(String username, String password) throws Exception {
		MyConnection myConnection = EspnUtil.login(username, password, BBC_FRONT_PAGE, "espn_bbc");
		return myConnection;
	}

	public static Starters getStarters(MyConnection myConnection, String entryName) throws IOException {
		Starters starters = new Starters();

		EspnEntry espnEntry = EspnUtil.getEspnEntry(myConnection, entryName);

		String url = BBC_FRONT_PAGE + "entry?entryID=" + espnEntry.getId();

		Connection.Response response = myConnection.get(url, null);

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
			form.put("playerInSlot_" + entry.getKey().getSlotId(), String.valueOf(entry.getValue().getId()));
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

		BbcTeam team = null;

		String teamShortName = element.select("[class=player_team]").text();
		String opponentShortName = element.select("[class=player_opp]").text();

		if (teamShortName == null || teamShortName.length() == 0) {
			team = BbcTeam.getTeamByName(name);
		} else {
			team = BbcTeam.getTeamByShortName(teamShortName);
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
}
